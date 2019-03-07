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

package org.onap.policy.models.tosca;

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
public class ToscaEntrySchema extends PfConcept {
    private static final long serialVersionUID = 3645882081163287058L;

    @EmbeddedId
    private PfReferenceKey key;

    @Column
    private PfConceptKey type;

    @Column
    private String description;

    @ElementCollection
    private List<ToscaConstraint> constraints;

    /**
     * The Default Constructor creates a {@link ToscaEntrySchema} object with a null key.
     */
    public ToscaEntrySchema() {
        this(new PfReferenceKey());
    }

    /**
     * The Key Constructor creates a {@link ToscaEntrySchema} object with the given concept key.
     *
     * @param key the key
     */
    public ToscaEntrySchema(@NonNull final PfReferenceKey key) {
        this.key = key;
    }

    /**
     * Copy constructor.
     *
     * @param copyConcept the concept to copy from
     */
    public ToscaEntrySchema(final ToscaEntrySchema copyConcept) {
        super(copyConcept);
    }

    @Override
    public List<PfKey> getKeys() {
        final List<PfKey> keyList = getKey().getKeys();

        keyList.addAll(type.getKeys());

        for (ToscaConstraint constraint : constraints) {
            keyList.addAll(constraint.getKeys());
        }

        return keyList;
    }

    @Override
    public void clean() {
        key.clean();

        type.clean();

        if (description != null) {
            description = description.trim();
        }

        for (ToscaConstraint constraint : constraints) {
            constraint.clean();
        }
    }

    @Override
    public PfValidationResult validate(final PfValidationResult resultIn) {
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
            for (ToscaConstraint constraint : constraints) {
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

        final ToscaEntrySchema other = (ToscaEntrySchema) otherConcept;
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

        if (!constraints.equals(other.constraints)) {
            return (constraints.hashCode() - other.constraints.hashCode());
        }


        return 0;
    }

    @Override
    public PfConcept copyTo(@NonNull final PfConcept target) {
        final Object copyObject = target;
        Assertions.instanceOf(copyObject, ToscaTimeInterval.class);

        final ToscaEntrySchema copy = ((ToscaEntrySchema) copyObject);
        copy.setKey(new PfReferenceKey(key));
        copy.setType(new PfConceptKey(type));
        copy.setDescription(description);

        final List<ToscaConstraint> newConstraints = new ArrayList<>();
        for (final ToscaConstraint constraint : constraints) {
            newConstraints.add(constraint); // Constraints are immutable
        }
        copy.setConstraints(newConstraints);

        return copy;
    }
}