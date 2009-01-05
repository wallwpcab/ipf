/*
 * Copyright 2008 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openehealth.ipf.platform.camel.lbs.extend

import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.net.URI

import javax.activation.DataHandler

import org.apache.camel.Exchange

import org.openehealth.ipf.platform.camel.core.builder.RouteBuilderConfig

import org.openehealth.ipf.commons.lbs.attachment.AttachmentFactory

import org.openehealth.ipf.commons.lbs.attachment.AttachmentDataSource
import org.openehealth.ipf.platform.camel.lbs.process.AttachmentHandler

import org.apache.camel.builder.RouteBuilder


/**
 * @author Jens Riemschneider
 */
class LbsHttpRouteBuilderConfig implements RouteBuilderConfig {
    
    void apply(RouteBuilder builder) {
        
        builder.errorHandler(builder.deadLetterChannel().maximumRedeliveries(2).initialRedeliveryDelay(0));

        // --------------------------------------------------------------
        //  LBS routes
        // --------------------------------------------------------------
        AttachmentHandler handler = builder.bean(AttachmentHandler.class)

        builder.from('jetty:http://localhost:8080/lbstest_no_extract')
            .to('mock:mock')

        builder.from('jetty:http://localhost:8080/lbstest_extract')
            .store().with(handler)
            .to('mock:mock')

        builder.from('jetty:http://localhost:8080/lbstest_ping')
            .store().with(handler)
            .process { Exchange exchange ->
                def dataSource = exchange.in.getBody(AttachmentDataSource.class)
                exchange.out.setBody(dataSource)
            }
            .to('mock:mock');
            
        builder.from('jetty:http://localhost:8080/lbstest_extract_factory_via_bean')
            .store().with('httpExtractionHandler')
            .to('mock:mock')

        builder.from('jetty:http://localhost:8080/lbstest_extract_router')
            .store().with(handler)
            .setHeader('tag').constant('I was here')
            .fetch().with(handler)
            .to('http://localhost:8080/lbstest_receiver')

        builder.from('direct:lbstest_send_only')
            .fetch().with(handler)
            .to('http://localhost:8080/lbstest_receiver')
            
        builder.from('direct:lbstest_non_http')
            .store().with(handler)
            .to('mock:mock')
            
        builder.from('jetty:http://localhost:8080/lbstest_receiver')
            .store().with(handler)
            .to('mock:mock')
            
        
        // Example routes only tested with groovy
        builder.from('jetty:http://localhost:8080/lbstest_example1')
            .store().with(handler)
            .process { Exchange exchange ->
                def reader = new BufferedReader(new InputStreamReader(exchange.in.getBody(InputStream.class)))
                try {
                    def line = reader.readLine()
                    while (line != null && !line.contains('blu')) {
                        line = reader.readLine()
                    }
                    if (line != null) {
                        exchange.in.setHeader('tokenfound', 'yes')
                    }
                }
                finally {
                    reader.close()
                }
            }
            .to('mock:mock')
            
        builder.from('jetty:http://localhost:8080/lbstest_example2')
            .store().with(handler)
            .process { Exchange exchange ->
                exchange.in.attachments.each {
                    if (it.value.contentType.startsWith('text/plain')) {
                        exchange.in.setHeader('textfound', 'yes')
                    }
                }
            }
            .to('mock:mock')
            
        builder.from('jetty:http://localhost:8080/lbstest_example3')
            .store().with(handler)
            .process { Exchange exchange ->
                def attachmentFactory = builder.bean(AttachmentFactory.class, 'attachmentFactory') 
                def inputStream = new ByteArrayInputStream('hello world'.bytes)
                def attachment = attachmentFactory.createAttachment(exchange.unitOfWork.id, 'text/xml', null, null, inputStream)
                exchange.in.addAttachment('hello', new DataHandler(attachment))
            }
            .fetch().with(handler)
            .to('http://localhost:8080/lbstest_receiver')
            
        builder.from('mina:tcp://localhost:6125?sync=true&codec=mllpStoreCodec')
            .process { Exchange exchange ->
                def reader = new BufferedReader(new InputStreamReader(exchange.in.getBody(InputStream.class)))
                try {
                    def line = reader.readLine()
                    while (line != null && !line.contains('blu')) {
                        line = reader.readLine()
                    }
                    if (line != null) {
                        exchange.in.setHeader('tokenfound', 'yes')
                    }
                }
                finally {
                    reader.close()
                }
            }
    }    
}
