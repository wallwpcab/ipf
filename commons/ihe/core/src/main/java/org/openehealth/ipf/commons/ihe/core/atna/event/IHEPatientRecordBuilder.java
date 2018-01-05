/*
 * Copyright 2017 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.openehealth.ipf.commons.ihe.core.atna.event;

import org.openehealth.ipf.commons.audit.AuditException;
import org.openehealth.ipf.commons.audit.codes.EventActionCode;
import org.openehealth.ipf.commons.audit.codes.ParticipantObjectIdTypeCode;
import org.openehealth.ipf.commons.audit.event.PatientRecordBuilder;
import org.openehealth.ipf.commons.audit.model.ParticipantObjectIdentificationType;
import org.openehealth.ipf.commons.audit.types.EventType;
import org.openehealth.ipf.commons.audit.types.PurposeOfUse;
import org.openehealth.ipf.commons.ihe.core.atna.AuditDataset;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Builder for building IHE-specific PatientRecord events. It automatically sets the AuditSource,
 * local and remote ActiveParticipant and a Human Requestor and provides methods for adding patient IDs.
 *
 * @author Christian Ohr
 */
public class IHEPatientRecordBuilder<T extends IHEPatientRecordBuilder<T>> extends IHEAuditMessageBuilder<T, PatientRecordBuilder> {

    private static final Pattern PATIENT_ID_PATTERN = Pattern.compile("^.+?\\^\\^\\^.*?&.+?&ISO(\\^.*){0,4}$");

    public IHEPatientRecordBuilder(AuditDataset auditDataset, EventActionCode action, EventType eventType) {
        this(auditDataset, action, eventType, Collections.emptyList());
    }

    public IHEPatientRecordBuilder(AuditDataset auditDataset, EventActionCode action, EventType eventType,
                                   List<PurposeOfUse> purposesOfUse) {
        super(new PatientRecordBuilder(
                auditDataset.getEventOutcomeIndicator(),
                action,
                eventType,
                purposesOfUse.toArray(new PurposeOfUse[purposesOfUse.size()])));

        setAuditSource(auditDataset);

        // First the source, then the destination
        if (auditDataset.isServerSide()) {
            setRemoteParticipant(auditDataset);
            addHumanRequestor(auditDataset);
            setLocalParticipant(auditDataset);
        } else {
            setLocalParticipant(auditDataset);
            addHumanRequestor(auditDataset);
            setRemoteParticipant(auditDataset);
        }
    }

    /**
     * Adds each patient ID as ParticipantObject in the context of a single request
     *
     * @param requestIdDesignator request or message ID designator, e.g. MSH-10 for HL7v2 and II for HL7v3
     * @param requestId           ID of the request or message
     * @param patientIds          IDs of the patient
     * @return this
     */
    public T addPatients(String requestIdDesignator, String requestId, String... patientIds) {
        if (patientIds != null)
            Arrays.stream(patientIds).forEach(patientId -> delegate.addPatient(patientId, null,
                    requestIdDesignator != null && requestId != null ?
                            Arrays.asList(getTypeValuePair(requestIdDesignator, requestId)) :
                            Collections.emptyList()));
        return self();
    }

    @Override
    public void validate() {
        super.validate();
        ParticipantObjectIdentificationType patient = getMessage().findParticipantObjectIdentifications(poi -> ParticipantObjectIdTypeCode.PatientNumber.equals(poi.getParticipantObjectIDTypeCode())).get(0);
        if (!PATIENT_ID_PATTERN.matcher(patient.getParticipantObjectID()).matches()) {
            throw new AuditException("Patient ID should be in CX format " + PATIENT_ID_PATTERN.pattern());
        }
    }
}