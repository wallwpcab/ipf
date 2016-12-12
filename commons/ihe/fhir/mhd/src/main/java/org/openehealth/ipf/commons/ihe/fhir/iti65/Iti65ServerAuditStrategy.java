/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openehealth.ipf.commons.ihe.fhir.iti65;

/**
 * Strategy for auditing ITI-65 transactions on the server side
 *
 * @author Christian Ohr
 * @since 3.2
 */
public class Iti65ServerAuditStrategy extends Iti65AuditStrategy {

    private static class LazyHolder {
        private static final Iti65ServerAuditStrategy INSTANCE = new Iti65ServerAuditStrategy();
    }

    public static Iti65ServerAuditStrategy getInstance() {
        return LazyHolder.INSTANCE;
    }

    public Iti65ServerAuditStrategy() {
        super(true);
    }

}