/*
 * Copyright 2009 the original author or authors.
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
package org.openehealth.ipf.platform.camel.core.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Processor;
import org.apache.camel.model.OutputType;
import org.apache.camel.model.ProcessorType;
import org.apache.camel.processor.Pipeline;
import org.apache.camel.spi.RouteContext;

/**
 * @author Martin Krasser
 */
public abstract class DelegateType extends OutputType<ProcessorType> {

    @Override
    public Processor createProcessor(RouteContext routeContext) throws Exception {
        Processor delegate = doCreateDelegate(routeContext);
        Processor next = routeContext.createProcessor(this);
        
        List<Processor> processors = new ArrayList<Processor>();
        processors.add(delegate);
        if (next != null) {
            processors.add(next);
        }
        return Pipeline.newInstance(processors);
    }
    
    protected abstract Processor doCreateDelegate(RouteContext routeContext);
    
}
