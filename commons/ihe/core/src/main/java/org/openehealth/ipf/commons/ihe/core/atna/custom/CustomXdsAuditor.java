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
package org.openehealth.ipf.commons.ihe.core.atna.custom;

import org.openhealthtools.ihe.atna.auditor.XDSAuditor;
import org.openhealthtools.ihe.atna.auditor.codes.dicom.DICOMEventIdCodes;
import org.openhealthtools.ihe.atna.auditor.codes.ihe.IHETransactionEventTypeCodes;
import org.openhealthtools.ihe.atna.auditor.codes.rfc3881.RFC3881EventCodes;
import org.openhealthtools.ihe.atna.auditor.context.AuditorModuleContext;
import org.openhealthtools.ihe.atna.auditor.events.ihe.GenericIHEAuditEventMessage;
import org.openhealthtools.ihe.atna.auditor.utils.EventUtils;

import static org.openehealth.ipf.commons.ihe.core.atna.custom.CustomAuditorUtils.configureEvent;

/**
 * Implementation of ATNA Auditors for the following XDS-based transactions:
 * <ul>
 *     <li>ITI-51 -- XDS.b Multi-Patient Stored Query</li>
 *     <li>ITI-61 -- XDS.b Register On-Demand Document Entry</li>
 *     <li>ITI-63 -- XCF Cross-Community Fetch</li>
 *     <li>RAD-69 -- XDS-I.b Retrieve Imaging Document Set</li>
 *     <li>RAD-75 -- XCA-I Cross-Gateway Retrieve Imaging Document Set</li>
 * </ul>
 *
 * @author Dmytro Rud
 */
public class CustomXdsAuditor extends XDSAuditor {

    public static CustomXdsAuditor getAuditor() {
        AuditorModuleContext ctx = AuditorModuleContext.getContext();
        return (CustomXdsAuditor) ctx.getAuditor(CustomXdsAuditor.class);
    }


    /**
     * Audits an ITI-61 Register On-Demand Document Entry event.
     *
     * @param serverSide
     *      <code>true</code> for the Document Registry actor,
     *      <code>false</code> for the On-Demand Document Source actor.
     * @param eventOutcome
     *      event outcome code.
     * @param userId
     *      user ID (contents of the WS-Addressing &lt;ReplyTo&gt; header).
     * @param userName
     *      user name on the Document Consumer side (for XUA).
     * @param serviceEndpointUri
     *      network endpoint URI of the Document Registry actor.
     * @param clientIpAddress
     *      IP address of the Document On-Demand Document Source actor.
     * @param submissionSetUniqueId
     *      unique ID of the XDS submission set.
     * @param patientId
     *      patient ID as an HL7 v2 CX string.
     */
    public void auditIti61(
            boolean serverSide,
            RFC3881EventCodes.RFC3881EventOutcomeCodes eventOutcome,
            String userId,
            String userName,
            String serviceEndpointUri,
            String clientIpAddress,
            String submissionSetUniqueId,
            String patientId)
    {
        if (! isAuditorEnabled()) {
            return;
        }

        GenericIHEAuditEventMessage event = new GenericIHEAuditEventMessage(
                true,
                eventOutcome,
                serverSide ? RFC3881EventCodes.RFC3881EventActionCodes.CREATE : RFC3881EventCodes.RFC3881EventActionCodes.READ,
                serverSide ? new DICOMEventIdCodes.Import() : new DICOMEventIdCodes.Export(),
                new CustomIHETransactionEventTypeCodes.RegisterOnDemandDocumentEntry());

        configureEvent(this, serverSide, event, userId, userName, serviceEndpointUri, serviceEndpointUri, clientIpAddress);
        if (!EventUtils.isEmptyOrNull(patientId)) {
            event.addPatientParticipantObject(patientId);
        }
        event.addSubmissionSetParticipantObject(submissionSetUniqueId);
        audit(event);
    }

}
