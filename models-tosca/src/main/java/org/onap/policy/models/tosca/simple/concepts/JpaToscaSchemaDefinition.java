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

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.apache.commons.lang3.ObjectUtils;
import org.onap.policy.common.parameters.annotations.NotBlank;
import org.onap.policy.common.parameters.annotations.NotNull;
import org.onap.policy.common.parameters.annotations.Valid;
import org.onap.policy.common.utils.validation.Assertions;
import org.onap.policy.models.base.PfAuthorative;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfKey;
import org.onap.policy.models.base.PfUtils;
import org.onap.policy.models.base.Validated;
import org.onap.policy.models.base.validation.annotations.VerifyKey;
import org.onap.policy.models.tosca.authorative.concepts.ToscaConstraint;
import org.onap.policy.models.tosca.authorative.concepts.ToscaSchemaDefinition;

/**
 * Class to represent the EntrySchema of list/map property in TOSCA definition.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 * @author Liam Fallon (liam.fallon@est.tech)
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class JpaToscaSchemaDefinition extends Validated
        implements PfAuthorative<ToscaSchemaDefinition>, Serializable, Comparable<JpaToscaSchemaDefinition> {

    @Serial
    private static final long serialVersionUID = 3645882081163287058L;

    @Column
    @VerifyKey
    @NotNull
    private PfConceptKey type;

    @Column
    @NotBlank
    private String description;

    @ElementCollection
    private List<@NotNull @Valid JpaToscaConstraint> constraints;

    /**
     * The full constructor creates a {@link JpaToscaSchemaDefinition} object with mandatory fields.
     *
     * @param type the type of the entry schema
     */
    public JpaToscaSchemaDefinition(@NonNull final PfConceptKey type) {
        this.type = type;
    }

    /**
     * Copy constructor.
     *
     * @param copyConcept the concept to copy from
     */
    public JpaToscaSchemaDefinition(@NonNull final JpaToscaSchemaDefinition copyConcept) {
        copyConcept.copyTo(this);
    }

    /**
     * Authorative constructor.
     *
     * @param authorativeConcept the authorative concept to copy from
     */
    public JpaToscaSchemaDefinition(final ToscaSchemaDefinition authorativeConcept) {
        this.fromAuthorative(authorativeConcept);
    }

    @Override
    public ToscaSchemaDefinition toAuthorative() {
        var toscaEntrySchema = new ToscaSchemaDefinition();

        toscaEntrySchema.setType(type.getName());
        toscaEntrySchema.setTypeVersion(type.getVersion());

        toscaEntrySchema.setDescription(description);

        if (constraints != null) {
            List<ToscaConstraint> toscaConstraints = new ArrayList<>();

            for (JpaToscaConstraint constraint : constraints) {
                toscaConstraints.add(constraint.toAuthorative());
            }

            toscaEntrySchema.setConstraints(toscaConstraints);
        }

        return toscaEntrySchema;
    }

    @Override
    public void fromAuthorative(final ToscaSchemaDefinition toscaEntrySchema) {
        if (toscaEntrySchema.getTypeVersion() != null) {
            type = new PfConceptKey(toscaEntrySchema.getType(), toscaEntrySchema.getTypeVersion());
        } else {
            type = new PfConceptKey(toscaEntrySchema.getType(), PfKey.NULL_KEY_VERSION);
        }

        description = toscaEntrySchema.getDescription();

        if (toscaEntrySchema.getConstraints() != null) {
            constraints = new ArrayList<>();

            for (ToscaConstraint toscaConstraint : toscaEntrySchema.getConstraints()) {
                constraints.add(JpaToscaConstraint.newInstance(toscaConstraint));
            }
        }
    }

    public List<PfKey> getKeys() {
        return type.getKeys();
    }

    public void clean() {
        type.clean();
        description = (description != null ? description.trim() : null);
    }

    @Override
    public int compareTo(final JpaToscaSchemaDefinition other) {
        if (other == null) {
            return -1;
        }
        if (this == other) {
            return 0;
        }

        int result = ObjectUtils.compare(description, other.description);
        if (result != 0) {
            return result;
        }

        return PfUtils.compareCollections(constraints, other.constraints);
    }

    /**
     * Copy this entry schema to another.
     *
     * @param target the other schemaa
     * @return the copied concept
     */
    public JpaToscaSchemaDefinition copyTo(@NonNull final JpaToscaSchemaDefinition target) {
        Assertions.instanceOf(target, JpaToscaSchemaDefinition.class);

        final JpaToscaSchemaDefinition copy = (target);
        copy.setType(new PfConceptKey(type));
        copy.setDescription(description);

        if (constraints != null) {
            // Constraints are immutable
            final List<JpaToscaConstraint> newConstraints = new ArrayList<>(constraints);
            copy.setConstraints(newConstraints);
        }

        return copy;
    }
}
