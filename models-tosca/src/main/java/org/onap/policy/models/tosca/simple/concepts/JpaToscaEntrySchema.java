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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import org.apache.commons.lang3.ObjectUtils;
import org.onap.policy.common.utils.validation.Assertions;
import org.onap.policy.models.base.PfConcept;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfKey;
import org.onap.policy.models.base.PfReferenceKey;
import org.onap.policy.models.base.PfUtils;
import org.onap.policy.models.base.PfValidationMessage;
import org.onap.policy.models.base.PfValidationResult;
import org.onap.policy.models.base.PfValidationResult.ValidationResult;


/**
 * Class to represent the EntrySchema of list/map property in TOSCA definition.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 * @author Liam Fallon (liam.fallon@est.tech)
 */
@Entity
@Table(name = "ToscaEntrySchema")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Data
@EqualsAndHashCode(callSuper = false)
public class JpaToscaEntrySchema extends PfConcept {
    private static final long serialVersionUID = 3645882081163287058L;

    @EmbeddedId
    private PfReferenceKey key;

    @Column
    private PfConceptKey type;

    @Column
    private String description;

    @ElementCollection
    private List<JpaToscaConstraint> constraints;

    /**
     * The Default Constructor creates a {@link JpaToscaEntrySchema} object with a null key.
     */
    public JpaToscaEntrySchema() {
        this(new PfReferenceKey());
    }

    /**
     * The Key Constructor creates a {@link JpaToscaEntrySchema} object with the given concept key.
     *
     * @param key the key
     */
    public JpaToscaEntrySchema(@NonNull final PfReferenceKey key) {
        this(key, new PfConceptKey());
    }

    /**
     * The full constructor creates a {@link JpaToscaEntrySchema} object with mandatory fields.
     *
     * @param key the key
     * @param type the type of the entry schema
     */
    public JpaToscaEntrySchema(@NonNull final PfReferenceKey key, @NonNull final PfConceptKey type) {
        this.key = key;
        this.type = type;
    }

    /**
     * Copy constructor.
     *
     * @param copyConcept the concept to copy from
     */
    public JpaToscaEntrySchema(final JpaToscaEntrySchema copyConcept) {
        super(copyConcept);
    }

    @Override
    public List<PfKey> getKeys() {
        final List<PfKey> keyList = getKey().getKeys();

        keyList.addAll(type.getKeys());

        if (constraints != null) {
            for (JpaToscaConstraint constraint : constraints) {
                keyList.addAll(constraint.getKeys());
            }
        }

        return keyList;
    }

    @Override
    public void clean() {
        key.clean();

        type.clean();
        description = (description != null ? description.trim() : null);

        if (constraints != null) {
            for (JpaToscaConstraint constraint : constraints) {
                constraint.clean();
            }
        }
    }

    @Override
    public PfValidationResult validate(@NonNull final PfValidationResult resultIn) {
        PfValidationResult result = resultIn;

        if (key.isNullKey()) {
            result.addValidationMessage(
                    new PfValidationMessage(key, this.getClass(), ValidationResult.INVALID, "key is a null key"));
        }

        result = key.validate(result);

        if (type == null || type.isNullKey()) {
            result.addValidationMessage(new PfValidationMessage(key, this.getClass(), ValidationResult.INVALID,
                    "entry schema type may not be null"));
        }

        if (description != null && description.trim().length() == 0) {
            result.addValidationMessage(new PfValidationMessage(key, this.getClass(), ValidationResult.INVALID,
                    "entry schema description may not be blank"));
        }


        if (constraints != null) {
            for (JpaToscaConstraint constraint : constraints) {
                if (constraint == null) {
                    result.addValidationMessage(new PfValidationMessage(key, this.getClass(), ValidationResult.INVALID,
                            "property constraint may not be null "));
                } else {
                    result = constraint.validate(result);
                }
            }
        }

        return result;
    }

    @Override
    public int compareTo(final PfConcept otherConcept) {
        if (otherConcept == null) {
            return -1;
        }
        if (this == otherConcept) {
            return 0;
        }
        if (getClass() != otherConcept.getClass()) {
            return this.hashCode() - otherConcept.hashCode();
        }

        final JpaToscaEntrySchema other = (JpaToscaEntrySchema) otherConcept;
        if (!key.equals(other.key)) {
            return key.compareTo(other.key);
        }

        if (!type.equals(other.type)) {
            return type.compareTo(other.type);
        }

        int result = ObjectUtils.compare(description, other.description);
        if (result != 0) {
            return result;
        }

        return PfUtils.compareObjects(constraints, other.constraints);
    }

    @Override
    public PfConcept copyTo(@NonNull final PfConcept target) {
        Assertions.instanceOf(target, JpaToscaEntrySchema.class);

        final JpaToscaEntrySchema copy = ((JpaToscaEntrySchema) target);
        copy.setKey(new PfReferenceKey(key));
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
