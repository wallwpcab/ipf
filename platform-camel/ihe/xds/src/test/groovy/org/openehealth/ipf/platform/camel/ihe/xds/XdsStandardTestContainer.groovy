/*
 * Copyright 2018 the original author or authors.
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

package org.openehealth.ipf.platform.camel.ihe.xds

import org.junit.Assert
import org.openehealth.ipf.commons.audit.codes.EventActionCode
import org.openehealth.ipf.commons.audit.codes.EventOutcomeIndicator
import org.openehealth.ipf.commons.audit.codes.ParticipantObjectTypeCode
import org.openehealth.ipf.commons.audit.codes.ParticipantObjectTypeCodeRole
import org.openehealth.ipf.commons.audit.model.ActiveParticipantType
import org.openehealth.ipf.commons.audit.model.AuditMessage
import org.openehealth.ipf.commons.audit.model.AuditSourceIdentificationType
import org.openehealth.ipf.commons.audit.model.EventIdentificationType
import org.openehealth.ipf.commons.audit.model.ParticipantObjectIdentificationType
import org.openehealth.ipf.commons.audit.model.TypeValuePairType
import org.openehealth.ipf.commons.audit.types.CodedValueType
import org.openehealth.ipf.platform.camel.ihe.ws.StandardTestContainer

import java.nio.charset.StandardCharsets

/**
 * @author Christian Ohr
 */
class XdsStandardTestContainer extends StandardTestContainer<AuditMessage> {

    List<AuditMessage> getAudit(EventActionCode actionCode, String addr) {
        auditSender.messages.findAll {
            it.eventIdentification.eventActionCode == actionCode
        }.findAll {
            it.activeParticipants.any { obj -> obj.userID == addr } ||
                    it.participantObjectIdentifications.any { obj -> obj.participantObjectID == addr }
        }
    }

    void checkCode(CodedValueType actual, String code, String scheme) {
        assert actual.code == code && actual.codeSystemName == scheme
    }

    void checkEvent(EventIdentificationType event, String code, String iti, EventActionCode actionCode, EventOutcomeIndicator outcome) {
        checkCode(event.eventID, code, 'DCM')
        checkCode(event.eventTypeCode[0], iti, 'IHE Transactions')
        assert event.eventActionCode == actionCode
        assert event.eventDateTime != null
        assert event.eventOutcomeIndicator == outcome
    }

    void checkSource(ActiveParticipantType source, String httpAddr, boolean requestor) {
        checkSource(source, requestor, true)
        assert source.userID == httpAddr
    }

    void checkSource(ActiveParticipantType source, boolean requestor, boolean userIdRequired = false) {
        // This should be something useful, but it isn't fully specified yet (see CP-402)
        if (userIdRequired) {
            assert source.userID != null && source.userID != ''
        }
        assert source.userIsRequestor == requestor
        assert source.networkAccessPointTypeCode
        assert source.networkAccessPointID
        // This will be required soon:
        // assert source.@AlternativeUserID != null && source.@AlternativeUserID != ''
        checkCode(source.roleIDCodes[0], '110153', 'DCM')
    }

    void checkDestination(ActiveParticipantType destination, String httpAddr, boolean requestor) {
        checkDestination(destination, requestor, true)
        assert destination.userID == httpAddr
    }

    void checkDestination(ActiveParticipantType destination, boolean requestor, boolean userIdRequired = true) {
        // This should be something useful, but it isn't fully specified yet (see CP-402)
        if (userIdRequired) {
            assert destination.userID != null && destination.userID != ''
        }
        assert destination.userIsRequestor == requestor
        assert destination.networkAccessPointTypeCode
        assert destination.networkAccessPointID
        // This will be required soon:
        // assert source.@AlternativeUserID != null && source.@AlternativeUserID != ''
        checkCode(destination.roleIDCodes[0], '110152', 'DCM')
    }

    void checkAuditSource(AuditSourceIdentificationType auditSource, String sourceId) {
        assert auditSource.auditSourceID == sourceId
    }

    void checkHumanRequestor(ActiveParticipantType human, String name, List<CodedValueType> roles = []) {
        assert human.userIsRequestor
        assert human.userID == name
        assert human.userName == name
        assert human.roleIDCodes.size() == roles.size()
        roles.eachWithIndex { CodedValueType cvt, int i ->
            assert human.roleIDCodes[i].code == cvt.code
            assert human.roleIDCodes[i].codeSystemName == cvt.codeSystemName
            assert human.roleIDCodes[i].originalText == cvt.originalText
        }
    }

    void checkPatient(ParticipantObjectIdentificationType patient, String... allowedIds = ['id3^^^&1.3&ISO']) {
        assert patient.participantObjectTypeCode == ParticipantObjectTypeCode.Person
        assert patient.participantObjectTypeCodeRole == ParticipantObjectTypeCodeRole.Patient
        checkCode(patient.participantObjectIDTypeCode, '2', 'RFC-3881')
        assert patient.participantObjectID in allowedIds
    }

    void checkQuery(ParticipantObjectIdentificationType query, String iti, String queryText, String queryUuid) {
        assert query.participantObjectTypeCode == ParticipantObjectTypeCode.System
        assert query.participantObjectTypeCodeRole == ParticipantObjectTypeCodeRole.Query
        checkCode(query.participantObjectIDTypeCode, iti, 'IHE Transactions')
        assert query.participantObjectID == queryUuid
        byte[] decodedBytes = Base64.getDecoder().decode(query.participantObjectQuery)
        String decoded = new String(decodedBytes, StandardCharsets.UTF_8)
        assert decoded.contains(queryText)
    }

    void checkUri(ParticipantObjectIdentificationType uri, String docUri, String docUniqueId) {
        assert uri.participantObjectTypeCode == ParticipantObjectTypeCode.System
        assert uri.participantObjectTypeCodeRole == ParticipantObjectTypeCodeRole.Report
        checkCode(uri.participantObjectIDTypeCode, '12', 'RFC-3881')
        assert uri.participantObjectID == docUri
        checkParticipantObjectDetail(uri.participantObjectDetail[0], "bla", docUniqueId)
    }

    void checkDocument(ParticipantObjectIdentificationType uri, String docUniqueId, String homeId, String repoId) {
        assert uri.participantObjectTypeCode == ParticipantObjectTypeCode.System
        assert uri.participantObjectTypeCodeRole == ParticipantObjectTypeCodeRole.Report
        checkCode(uri.participantObjectIDTypeCode, '9', 'RFC-3881')
        assert uri.participantObjectID == docUniqueId

        checkParticipantObjectDetail(uri.participantObjectDetail[0], 'Repository Unique Id', repoId)
        checkParticipantObjectDetail(uri.participantObjectDetail[1], 'ihe:homeCommunityID', homeId)
    }

    void checkImageDocument(ParticipantObjectIdentificationType uri, String docUniqueId, String homeId, String repoId, String studyId, String seriesId) {
        assert uri.participantObjectTypeCode == ParticipantObjectTypeCode.System
        assert uri.participantObjectTypeCodeRole == ParticipantObjectTypeCodeRole.Report
        checkCode(uri.participantObjectIDTypeCode, '9', 'RFC-3881')
        assert uri.participantObjectID == docUniqueId

        checkParticipantObjectDetail(uri.participantObjectDetail[0], 'Study Instance Unique Id', studyId)
        checkParticipantObjectDetail(uri.participantObjectDetail[1], 'Series Instance Unique Id', seriesId)
        checkParticipantObjectDetail(uri.participantObjectDetail[2], 'Repository Unique Id', repoId)
        checkParticipantObjectDetail(uri.participantObjectDetail[3], 'ihe:homeCommunityID', homeId)
    }

    void checkParticipantObjectDetail(TypeValuePairType detail, String expectedType, String expectedValue) {
        assert detail.type == expectedType
        byte[] decodedActualValue = Base64.getDecoder().decode(detail.value)
        String actualValue = new String(decodedActualValue, StandardCharsets.UTF_8)
        assert expectedValue == actualValue
    }

    void checkSubmissionSet(ParticipantObjectIdentificationType submissionSet) {
        assert submissionSet.participantObjectTypeCode == ParticipantObjectTypeCode.System
        assert submissionSet.participantObjectTypeCodeRole == ParticipantObjectTypeCodeRole.Job
        checkCode(submissionSet.participantObjectIDTypeCode, 'urn:uuid:a54d6aa5-d40d-43f9-88c5-b4633d873bdd', 'IHE XDS Metadata')
        assert submissionSet.participantObjectID == '123'
    }

    void checkRegistryObjectParticipantObjectDetail(ParticipantObjectIdentificationType detail, String typeCode, String registryObjectUuid) {
        assert detail.participantObjectTypeCode == ParticipantObjectTypeCode.System
        assert detail.participantObjectTypeCodeRole == ParticipantObjectTypeCodeRole.Report
        checkCode(detail.participantObjectIDTypeCode, typeCode, 'IHE XDS Metadata')
        assert detail.participantObjectID == registryObjectUuid
    }
}