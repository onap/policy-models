/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy Models
 * ================================================================================
 * Copyright (C) 2019, 2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2021 Nordix Foundation.
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

package org.onap.policy.models.pap.concepts;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;
import lombok.ToString;
import org.onap.policy.common.parameters.BeanValidator;
import org.onap.policy.common.parameters.FieldValidator;
import org.onap.policy.common.parameters.ValidationResult;
import org.onap.policy.common.parameters.annotations.NotNull;
import org.onap.policy.common.parameters.annotations.Valid;
import org.onap.policy.models.tosca.authorative.concepts.ToscaConceptIdentifierOptVersion;

/**
 * Request deploy or update a set of policies using the <i>simple</i> PDP Group deployment
 * REST API. Only the "name" and "version" fields of a Policy are used, and only the
 * "name" field is actually required.
 */
@ToString
public class PdpDeployPolicies {
    @NotNull
    private List<@NotNull @Valid PapPolicyIdentifier> policies;

    /**
     * Get the identifiers of the policies on the list.
     *
     * @return The list of identifiers
     */
    public List<ToscaConceptIdentifierOptVersion> getPolicies() {
        return policies == null ? null
                        : policies.stream().map(PapPolicyIdentifier::getGenericIdentifier).collect(Collectors.toList());
    }

    /**
     * Set the identifiers of the policies on the list.
     *
     * @param policies The list of identifiers
     */
    public void setPolicies(final List<ToscaConceptIdentifierOptVersion> policies) {
        this.policies = policies == null ? null
                        : policies.stream().map(PapPolicyIdentifier::new).collect(Collectors.toList());
    }

    /**
     * Gets the list of policies.
     * @return the list of policies
     */
    public List<PapPolicyIdentifier> getPlainPolicies() {
        return policies;
    }

    /**
     * Validates that appropriate fields are populated for an incoming call to the PAP
     * REST API.
     *
     * @return the validation result
     */
    public ValidationResult validatePapRest() {
        /*
         * Note: we can't use a plain validator, as the above getPolicies() method does
         * not return a list of the correct type. We'll create a validator that uses
         * getPlainPolicies(), instead, to valid the list of policies.
         */
        BeanValidator validator = new BeanValidator() {
            @Override
            protected FieldValidator makeFieldValidator(Class<?> clazz, Field field) {
                if (clazz == PdpDeployPolicies.class && "policies".equals(field.getName())) {
                    return new FieldValidator(this, clazz, field) {
                        @Override
                        protected Method getAccessor(Class<?> clazz, String fieldName) {
                            return getMethod(clazz, "getPlainPolicies");
                        }
                    };

                } else {
                    return super.makeFieldValidator(clazz, field);
                }
            }
        };

        return validator.validateTop(PdpDeployPolicies.class.getSimpleName(), this);
    }
}
