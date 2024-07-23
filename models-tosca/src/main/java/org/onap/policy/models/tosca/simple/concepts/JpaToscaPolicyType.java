/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy Model
 * ================================================================================
 * Copyright (C) 2019-2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019-2020, 2023-2024 Nordix Foundation.
 * Modifications Copyright (C) 2022 Bell Canada. All rights reserved.
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

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.io.Serial;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.onap.policy.common.parameters.BeanValidationResult;
import org.onap.policy.common.parameters.annotations.NotNull;
import org.onap.policy.common.parameters.annotations.Valid;
import org.onap.policy.models.base.PfConcept;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfKey;
import org.onap.policy.models.base.PfUtils;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyType;

/**
 * Class to represent the policy type in TOSCA definition.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 * @author Liam Fallon (liam.fallon@est.tech)
 */

@Entity
@Table(name = "ToscaPolicyType")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class JpaToscaPolicyType extends JpaToscaWithToscaProperties<ToscaPolicyType> {
    @Serial
    private static final long serialVersionUID = -563659852901842616L;

    @ElementCollection
    @CollectionTable(joinColumns = {
        @JoinColumn(name = "toscaPolicyTypeName",    referencedColumnName = "name"),
        @JoinColumn(name = "toscaPolicyTypeVersion",    referencedColumnName = "version")
    })
    private List<@NotNull @Valid PfConceptKey> targets;

    @ElementCollection
    @Lob
    private List<@NotNull @Valid JpaToscaTrigger> triggers;

    /**
     * The Key Constructor creates a {@link JpaToscaPolicyType} object with the given concept key.
     *
     * @param key the key
     */
    public JpaToscaPolicyType(@NonNull final PfConceptKey key) {
        super(key);
    }

    /**
     * Copy constructor.
     *
     * @param copyConcept the concept to copy from
     */
    public JpaToscaPolicyType(final JpaToscaPolicyType copyConcept) {
        super(copyConcept);
        this.targets = PfUtils.mapList(copyConcept.targets, PfConceptKey::new);
        this.triggers = PfUtils.mapList(copyConcept.triggers, JpaToscaTrigger::new);
    }

    /**
     * Authorative constructor.
     *
     * @param authorativeConcept the authorative concept to copy from
     */
    public JpaToscaPolicyType(final ToscaPolicyType authorativeConcept) {
        super(authorativeConcept);
    }

    @Override
    public ToscaPolicyType toAuthorative() {
        var toscaPolicyType = new ToscaPolicyType();
        super.setToscaEntity(toscaPolicyType);
        super.toAuthorative();

        return toscaPolicyType;
    }

    @Override
    public List<PfKey> getKeys() {
        final List<PfKey> keyList = super.getKeys();

        if (targets != null) {
            keyList.addAll(targets);
        }

        if (triggers != null) {
            for (JpaToscaTrigger trigger : triggers) {
                keyList.addAll(trigger.getKeys());
            }
        }

        return keyList;
    }

    @Override
    public void clean() {
        super.clean();

        if (targets != null) {
            for (PfConceptKey target : targets) {
                target.clean();
            }
        }

        if (triggers != null) {
            for (JpaToscaTrigger trigger : triggers) {
                trigger.clean();
            }
        }
    }

    @Override
    public BeanValidationResult validate(@NonNull String fieldName) {
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

        final JpaToscaPolicyType other = (JpaToscaPolicyType) otherConcept;

        result = PfUtils.compareCollections(targets, other.targets);
        if (result != 0) {
            return result;
        }

        return PfUtils.compareCollections(triggers, other.triggers);
    }
}
