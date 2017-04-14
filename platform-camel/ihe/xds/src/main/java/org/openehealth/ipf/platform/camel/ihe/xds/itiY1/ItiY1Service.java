/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openehealth.ipf.platform.camel.ihe.xds.itiY1;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.openehealth.ipf.commons.ihe.xds.core.ebxml.ebxml30.RemoveDocumentsRequestType;
import org.openehealth.ipf.commons.ihe.xds.core.responses.ErrorCode;
import org.openehealth.ipf.commons.ihe.xds.core.responses.Response;
import org.openehealth.ipf.commons.ihe.xds.core.stub.ebrs30.rs.RegistryResponseType;
import org.openehealth.ipf.commons.ihe.xds.itiY1.ItiY1PortType;
import org.openehealth.ipf.platform.camel.core.util.Exchanges;
import org.openehealth.ipf.platform.camel.ihe.ws.AbstractWebService;
import org.openehealth.ipf.platform.camel.ihe.xds.core.converters.EbXML30Converters;

/**
 * Service implementation for the IHE ITI-Y1 transaction (Remove Documents).
 *
 * @since 3.3
 */
@Slf4j
@AllArgsConstructor
public class ItiY1Service extends AbstractWebService implements ItiY1PortType {

    @Override
    public RegistryResponseType documentRepositoryRemoveDocuments(RemoveDocumentsRequestType body) {
        Exchange result = process(body);
        Exception exception = Exchanges.extractException(result);
        if (exception != null) {
            log.debug(getClass().getSimpleName() + " service failed", exception);
            Response errorResponse = new Response(
                    exception,
                    ErrorCode.REPOSITORY_METADATA_ERROR,
                    ErrorCode.REPOSITORY_ERROR,
                    null);
            return EbXML30Converters.convert(errorResponse);
        }
        return Exchanges.resultMessage(result).getBody(RegistryResponseType.class);
    }

}
