/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy Models
 * ================================================================================
 * Copyright (C) 2019, 2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2020-2021, 2025 OpenInfra Foundation Europe. All rights reserved.
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

import java.io.Serial;
import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.onap.policy.common.parameters.BeanValidationResult;
import org.onap.policy.common.parameters.ValidationResult;
import org.onap.policy.models.base.PfKey;

/**
 * Identifies a concept. Both the name and version must be non-null.
 */
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ToscaConceptIdentifier extends ToscaNameVersion
                implements Serializable, Comparable<ToscaConceptIdentifier> {

    @Serial
    private static final long serialVersionUID = 8010649773816325786L;


    public ToscaConceptIdentifier(@NonNull String name, @NonNull String version) {
        super(name, version);
    }

    public ToscaConceptIdentifier(@NonNull PfKey key) {
        super(key);
    }

    public ToscaConceptIdentifier(ToscaConceptIdentifier source) {
        super(source);
    }

    /**
     * Validates that appropriate fields are populated for an incoming call to the PAP REST API.
     *
     * @return the validation result
     */
    public ValidationResult validatePapRest() {
        var result = new BeanValidationResult("identifier", this);

        result.validateNotNull("name", getName());
        result.validateNotNull("version", getVersion());

        return result;
    }

    @Override
    public int compareTo(ToscaConceptIdentifier other) {
        return commonCompareTo(other);
    }
}
