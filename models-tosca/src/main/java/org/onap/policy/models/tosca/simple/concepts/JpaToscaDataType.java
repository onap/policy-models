/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy Model
 * ================================================================================
 * Copyright (C) 2019-2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019-2020, 2023 Nordix Foundation.
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

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.onap.policy.common.parameters.annotations.NotNull;
import org.onap.policy.common.parameters.annotations.Valid;
import org.onap.policy.models.base.PfConcept;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfUtils;
import org.onap.policy.models.tosca.authorative.concepts.ToscaDataType;

/**
 * Class to represent custom data type in TOSCA definition.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 * @author Liam Fallon (liam.fallon@est.tech)
 */
@Entity
@Table(name = "ToscaDataType")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class JpaToscaDataType extends JpaToscaWithToscaProperties<ToscaDataType> {
    @Serial
    private static final long serialVersionUID = -3922690413436539164L;

    @ElementCollection
    private List<@NotNull @Valid JpaToscaConstraint> constraints;

    /**
     * The Key Constructor creates a {@link JpaToscaDataType} object with the given concept key.
     *
     * @param key the key
     */
    public JpaToscaDataType(@NonNull final PfConceptKey key) {
        super(key);
    }

    /**
     * Copy constructor.
     *
     * @param copyConcept the concept to copy from
     */
    public JpaToscaDataType(final JpaToscaDataType copyConcept) {
        super(copyConcept);
        // Constraints are immutable
        this.constraints = (copyConcept.constraints != null ? new ArrayList<>(copyConcept.constraints) : null);
    }

    /**
     * Authorative constructor.
     *
     * @param authorativeConcept the authorative concept to copy from
     */
    public JpaToscaDataType(final ToscaDataType authorativeConcept) {
        super(authorativeConcept);
    }

    @Override
    public ToscaDataType toAuthorative() {
        var toscaDataType = new ToscaDataType();
        super.setToscaEntity(toscaDataType);
        super.toAuthorative();

        toscaDataType.setConstraints(PfUtils.mapList(constraints, JpaToscaConstraint::toAuthorative));

        return toscaDataType;
    }

    @Override
    public void fromAuthorative(final ToscaDataType toscaDataType) {
        super.fromAuthorative(toscaDataType);

        constraints = PfUtils.mapList(toscaDataType.getConstraints(), JpaToscaConstraint::newInstance);
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

        final JpaToscaDataType other = (JpaToscaDataType) otherConcept;

        return PfUtils.compareCollections(constraints, other.constraints);
    }
}
