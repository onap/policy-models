/*
 * ============LICENSE_START=======================================================
 * ONAP Policy Models
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 */

package org.onap.policy.models.pdp.concepts;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import org.onap.policy.common.parameters.BeanValidationResult;
import org.onap.policy.common.parameters.ObjectValidationResult;
import org.onap.policy.common.parameters.ValidationResult;
import org.onap.policy.common.parameters.ValidationStatus;
import org.onap.policy.common.utils.coder.Coder;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.coder.StandardYamlCoder;

/**
 * Request deploy or update a set of groups via the PDP Group deployment REST API.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
public class PdpGroups {
    private static final String GROUPS_FIELD = "groups";

    private List<PdpGroup> groups;

    /**
     * Makes a structure from a URL-encoded JSON or YAML string. If the string begins with "{", then
     * it's assumed to be a JSON string, otherwise it's treated as YAML.
     *
     * @param jsonOrYaml JSON or YAML string, URL-encoded
     * @throws CoderException if the string cannot be decoded
     */
    public static PdpGroups fromString(@NonNull String jsonOrYaml) throws CoderException {
        try {
            String plain = URLDecoder.decode(jsonOrYaml, "UTF-8");
            Coder coder = (plain.trim().startsWith("{") ? new StandardCoder() : new StandardYamlCoder());

            return coder.decode(plain, PdpGroups.class);

        } catch (UnsupportedEncodingException e) {
            throw new CoderException(e);
        }
    }

    /**
     * Get the contents of this class as a list of PDP group maps.
     *
     * @return the PDP groups in a list of maps
     */
    public List<Map<String, PdpGroup>> toMapList() {
        final Map<String, PdpGroup> pdpGroupMap = new LinkedHashMap<>();
        for (PdpGroup pdpGroup : groups) {
            pdpGroupMap.put(pdpGroup.getName() + ':' + pdpGroup.getVersion(), pdpGroup);
        }

        return Collections.singletonList(pdpGroupMap);
    }

    /**
     * Validates that appropriate fields are populated for an incoming call to the PAP
     * REST API.
     *
     * @return the validation result
     */
    public ValidationResult validatePapRest() {
        BeanValidationResult result = new BeanValidationResult(GROUPS_FIELD, this);

        result.validateNotNullList(GROUPS_FIELD, groups, PdpGroup::validatePapRest);
        if (!result.isValid()) {
            return result;
        }

        // verify that the same group doesn't appear more than once
        List<String> names = groups.stream().map(PdpGroup::getName).collect(Collectors.toList());
        if (groups.size() == new HashSet<>(names).size()) {
            return result;
        }

        // different sizes implies duplicates
        return new ObjectValidationResult(GROUPS_FIELD, names, ValidationStatus.INVALID, "duplicate group names");
    }
}
