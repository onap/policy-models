/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2020, 2023 Nordix Foundation.
 * Modifications Copyright (C) 2020-2021 AT&T Intellectual Property. All rights reserved.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.policy.models.tosca.simple.concepts;

import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import java.io.Serial;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.onap.policy.common.parameters.BeanValidationResult;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.tosca.authorative.concepts.ToscaCapabilityType;

/**
 * Class to represent the capability type in TOSCA definition.
 */

@Entity
@Table(name = "ToscaCapabilityType")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class JpaToscaCapabilityType extends JpaToscaWithToscaProperties<ToscaCapabilityType> {
    @Serial
    private static final long serialVersionUID = -563659852901842616L;

    /**
     * The Key Constructor creates a {@link JpaToscaCapabilityType} object with the given concept key.
     *
     * @param key the key
     */
    public JpaToscaCapabilityType(@NonNull final PfConceptKey key) {
        super(key);
    }

    /**
     * Copy constructor.
     *
     * @param copyConcept the concept to copy from
     */
    public JpaToscaCapabilityType(final JpaToscaCapabilityType copyConcept) {
        super(copyConcept);
    }

    /**
     * Authorative constructor.
     *
     * @param authorativeConcept the authorative concept to copy from
     */
    public JpaToscaCapabilityType(final ToscaCapabilityType authorativeConcept) {
        super(authorativeConcept);
    }

    @Override
    public ToscaCapabilityType toAuthorative() {
        super.setToscaEntity(new ToscaCapabilityType());
        return super.toAuthorative();
    }

    @Override
    public BeanValidationResult validate(@NonNull String fieldName) {
        return validateWithKey(fieldName);
    }
}
