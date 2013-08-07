/*
 * Copyright 2012 the original author or authors.
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
package org.openehealth.ipf.commons.ihe.xds.iti61;

import org.openehealth.ipf.commons.ihe.core.atna.AuditorManager;
import org.openehealth.ipf.commons.ihe.ws.cxf.audit.WsAuditDataset;
import org.openehealth.ipf.commons.ihe.xds.core.audit.XdsSubmitAuditDataset;
import org.openehealth.ipf.commons.ihe.xds.core.audit.XdsSubmitAuditStrategy30;

/**
 * Audit strategy for ITI-61.
 * @author Dmytro Rud
 */
public class Iti61AuditStrategy extends XdsSubmitAuditStrategy30 {

    private static final String[] NECESSARY_AUDIT_FIELDS = new String[] {
        "EventOutcomeCode",
        "ServiceEndpointUrl",
        "SubmissionSetUuid",
        "PatientId"};


    public Iti61AuditStrategy(boolean serverSide, boolean allowIncompleteAudit) {
        super(serverSide, allowIncompleteAudit);
    }


    @Override
    public void doAudit(WsAuditDataset auditDataset) {
        if (auditDataset instanceof XdsSubmitAuditDataset){
            XdsSubmitAuditDataset xdsAuditDataset = (XdsSubmitAuditDataset)auditDataset;
            AuditorManager.getCustomXdsAuditor().auditIti61(
                    isServerSide(),
                    xdsAuditDataset.getEventOutcomeCode(),
                    xdsAuditDataset.getUserId(),
                    xdsAuditDataset.getUserName(),
                    xdsAuditDataset.getServiceEndpointUrl(),
                    xdsAuditDataset.getClientIpAddress(),
                    xdsAuditDataset.getSubmissionSetUuid(),
                    xdsAuditDataset.getPatientId());
        }
    }


    @Override
    public String[] getNecessaryAuditFieldNames() {
        return NECESSARY_AUDIT_FIELDS;
    }
}
