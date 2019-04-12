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

package org.onap.policy.models.tosca.authorative.concepts;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.apache.commons.lang3.ObjectUtils;
import org.onap.policy.common.parameters.BeanValidationResult;
import org.onap.policy.common.parameters.ValidationResult;

/**
 * Identifies a policy. Both the name and version must be non-null.
 */
@Data
@NoArgsConstructor
public class ToscaPolicyIdentifier implements Comparable<ToscaPolicyIdentifier> {

    @NonNull
    private String name;

    @NonNull
    private String version;


    public ToscaPolicyIdentifier(@NonNull String name, @NonNull String version) {
        this.name = name;
        this.version = version;
    }

    public ToscaPolicyIdentifier(ToscaPolicyIdentifier source) {
        this.name = source.name;
        this.version = source.version;
    }

    /**
     * Validates that appropriate fields are populated for an incoming call to the PAP
     * REST API.
     *
     * @return the validation result
     */
    public ValidationResult validatePapRest() {
        BeanValidationResult result = new BeanValidationResult("group", this);

        result.validateNotNull("name", name);
        result.validateNotNull("version", version);

        return result;
    }

    @Override
    public int compareTo(ToscaPolicyIdentifier other) {
        if (this == other) {
            return 0;
        }

        if (other == null) {
            return 1;
        }

        int result = ObjectUtils.compare(getName(), other.getName());
        if (result != 0) {
            return result;
        }

        return ObjectUtils.compare(getVersion(), other.getVersion());
    }
}
