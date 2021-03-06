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

package org.openehealth.ipf.commons.ihe.fhir.translation;

import org.openehealth.ipf.commons.map.MappingService;

import java.util.Optional;

/**
 * Default URI Mapper implementation that requires a {@link MappingService}
 * for URI-to-OID translation
 *
 * @author Christian Ohr
 * @since 3.1
 */
public class DefaultUriMapper extends AbstractUriMapper {

    private final MappingService mappingService;
    private String uriToOidMappingKey;
    private String uriToNamespaceMappingKey;

    public DefaultUriMapper(MappingService mappingService) {
        this.mappingService = mappingService;
    }

    public DefaultUriMapper(MappingService mappingService, String uriToOidMappingKey, String uriToNamespaceMappingKey) {
        this.mappingService = mappingService;
        this.uriToOidMappingKey = uriToOidMappingKey;
        this.uriToNamespaceMappingKey = uriToNamespaceMappingKey;
    }

    @Override
    protected Optional<String> mapUriToOid(String uri) {
        return Optional.ofNullable((String) mappingService.get(uriToOidMappingKey, uri));
    }

    @Override
    protected Optional<String> mapOidToUri(String oid) {
        return Optional.ofNullable((String) mappingService.getKey(uriToOidMappingKey, oid));
    }

    @Override
    protected Optional<String> mapUriToNamespace(String uri) {
        return Optional.ofNullable((String) mappingService.get(uriToNamespaceMappingKey, uri));
    }

    @Override
    protected Optional<String> mapNamespaceToUri(String namespace) {
        return Optional.ofNullable((String) mappingService.getKey(uriToNamespaceMappingKey, namespace));
    }
}
