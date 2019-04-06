/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy Model
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019 Nordix Foundation.
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.ElementCollection;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import org.apache.commons.lang3.ObjectUtils;
import org.onap.policy.common.utils.validation.Assertions;
import org.onap.policy.models.base.PfAuthorative;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfKey;
import org.onap.policy.models.base.PfUtils;
import org.onap.policy.models.base.PfValidationMessage;
import org.onap.policy.models.base.PfValidationResult;
import org.onap.policy.models.base.PfValidationResult.ValidationResult;
import org.onap.policy.models.tosca.authorative.concepts.ToscaConstraint;
import org.onap.policy.models.tosca.authorative.concepts.ToscaEntrySchema;


/**
 * Class to represent the EntrySchema of list/map property in TOSCA definition.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 * @author Liam Fallon (liam.fallon@est.tech)
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class JpaToscaEntrySchema
        implements PfAuthorative<ToscaEntrySchema>, Serializable, Comparable<JpaToscaEntrySchema> {

    private static final long serialVersionUID = 3645882081163287058L;

    // Recurring string constants
    private static final String ENTRY_SCHEMA = "EntrySchema";

    @Column
    private PfConceptKey type;

    @Column
    private String description;

    @ElementCollection
    private List<JpaToscaConstraint> constraints;

    /**
     * The full constructor creates a {@link JpaToscaEntrySchema} object with mandatory fields.
     *
     * @param type the type of the entry schema
     */
    public JpaToscaEntrySchema(@NonNull final PfConceptKey type) {
        this.type = type;
    }

    /**
     * Copy constructor.
     *
     * @param copyConcept the concept to copy from
     */
    public JpaToscaEntrySchema(@NonNull final JpaToscaEntrySchema copyConcept) {
        copyConcept.copyTo(this);
    }

    /**
     * Authorative constructor.
     *
     * @param authorativeConcept the authorative concept to copy from
     */
    public JpaToscaEntrySchema(final ToscaEntrySchema authorativeConcept) {
        this.fromAuthorative(authorativeConcept);
    }

    @Override
    public ToscaEntrySchema toAuthorative() {
        ToscaEntrySchema toscaEntrySchema = new ToscaEntrySchema();

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
    public void fromAuthorative(final ToscaEntrySchema toscaEntrySchema) {
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

    /**
     * Validate the entry schema.
     *
     * @param resultIn the incoming result
     * @return the ooutput result witht he result of this validation
     */
    public PfValidationResult validate(@NonNull final PfValidationResult resultIn) {
        PfValidationResult result = resultIn;

        if (type == null || type.isNullKey()) {
            result.addValidationMessage(new PfValidationMessage(new PfConceptKey(ENTRY_SCHEMA, PfKey.NULL_KEY_VERSION),
                    this.getClass(), ValidationResult.INVALID, "entry schema type may not be null"));
        }

        if (description != null && description.trim().length() == 0) {
            result.addValidationMessage(new PfValidationMessage(new PfConceptKey(ENTRY_SCHEMA, PfKey.NULL_KEY_VERSION),
                    this.getClass(), ValidationResult.INVALID, "entry schema description may not be blank"));
        }


        if (constraints != null) {
            for (JpaToscaConstraint constraint : constraints) {
                if (constraint == null) {
                    result.addValidationMessage(
                            new PfValidationMessage(new PfConceptKey(ENTRY_SCHEMA, PfKey.NULL_KEY_VERSION),
                                    this.getClass(), ValidationResult.INVALID, "property constraint may not be null "));
                }
            }
        }

        return result;
    }

    @Override
    public int compareTo(final JpaToscaEntrySchema other) {
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

        return PfUtils.compareObjects(constraints, other.constraints);
    }

    /**
     * Copy this entry schema to another.
     *
     * @param target the other schemaa
     * @return the copied concept
     */
    public JpaToscaEntrySchema copyTo(@NonNull final JpaToscaEntrySchema target) {
        Assertions.instanceOf(target, JpaToscaEntrySchema.class);

        final JpaToscaEntrySchema copy = (target);
        copy.setType(new PfConceptKey(type));
        copy.setDescription(description);

        if (constraints != null) {
            final List<JpaToscaConstraint> newConstraints = new ArrayList<>();
            for (final JpaToscaConstraint constraint : constraints) {
                newConstraints.add(constraint); // Constraints are immutable
            }
            copy.setConstraints(newConstraints);
        }

        return copy;
    }
}
