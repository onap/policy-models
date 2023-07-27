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

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.io.Serial;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.apache.commons.lang3.ObjectUtils;
import org.onap.policy.common.parameters.BeanValidationResult;
import org.onap.policy.common.parameters.annotations.Valid;
import org.onap.policy.models.base.PfConcept;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfKey;
import org.onap.policy.models.tosca.authorative.concepts.ToscaNodeType;

/**
 * Class to represent the node type in TOSCA definition.
 */

@Entity
@Table(name = "ToscaNodeType")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class JpaToscaNodeType extends JpaToscaWithToscaProperties<ToscaNodeType> {
    @Serial
    private static final long serialVersionUID = -563659852901842616L;


    // formatter:off
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "requirementsName", referencedColumnName = "name")
    @JoinColumn(name = "requirementsVersion", referencedColumnName = "version")
    // @formatter:on
    @Valid
    private JpaToscaRequirements requirements;

    /**
     * The Key Constructor creates a {@link JpaToscaNodeType} object with the given concept key.
     *
     * @param key the key
     */
    public JpaToscaNodeType(@NonNull final PfConceptKey key) {
        super(key);
    }

    /**
     * Copy constructor.
     *
     * @param copyConcept the concept to copy from
     */
    public JpaToscaNodeType(final JpaToscaNodeType copyConcept) {
        super(copyConcept);
        this.requirements =
                (copyConcept.requirements != null ? new JpaToscaRequirements(copyConcept.requirements) : null);
    }

    /**
     * Authorative constructor.
     *
     * @param authorativeConcept the authorative concept to copy from
     */
    public JpaToscaNodeType(final ToscaNodeType authorativeConcept) {
        super(authorativeConcept);
    }

    @Override
    public ToscaNodeType toAuthorative() {
        var toscaNodeType = new ToscaNodeType();
        super.setToscaEntity(toscaNodeType);
        super.toAuthorative();

        if (requirements != null) {
            toscaNodeType.setRequirements(requirements.toAuthorative());
        }

        return toscaNodeType;
    }

    @Override
    public void fromAuthorative(final ToscaNodeType toscaNodeType) {
        super.fromAuthorative(toscaNodeType);

        if (toscaNodeType.getRequirements() != null) {
            requirements = new JpaToscaRequirements();
            requirements.fromAuthorative(toscaNodeType.getRequirements());
        }
    }

    @Override
    public List<PfKey> getKeys() {
        final List<PfKey> keyList = super.getKeys();

        if (requirements != null) {
            keyList.addAll(requirements.getKeys());
        }

        return keyList;
    }

    @Override
    public void clean() {
        super.clean();

        if (requirements != null) {
            requirements.clean();
        }
    }

    @Override
    public BeanValidationResult validate(String fieldName) {
        return validateWithKey(fieldName);
    }

    @Override
    public int compareTo(final PfConcept otherConcept) {
        if (this == otherConcept) {
            return 0;
        }

        int result = super.compareTo(otherConcept);
        if (result != 0) {
            return result;
        }

        final JpaToscaNodeType other = (JpaToscaNodeType) otherConcept;

        return ObjectUtils.compare(requirements, other.requirements);
    }
}
