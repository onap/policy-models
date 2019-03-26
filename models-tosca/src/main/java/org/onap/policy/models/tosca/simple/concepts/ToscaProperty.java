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

import com.google.gson.annotations.SerializedName;

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
 * Class to represent the property in TOSCA definition.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 * @author Liam Fallon (liam.fallon@est.tech)
 */
@Entity
@Table(name = "ToscaProperty")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Data
@EqualsAndHashCode(callSuper = false)
public class ToscaProperty extends PfConcept {
    private static final long serialVersionUID = 1675770231921107988L;

    public enum Status {
        SUPPORTED, UNSUPPORTED, EXPERIMENTAL, DEPRECATED
    }

    @EmbeddedId
    private PfReferenceKey key;

    @Column
    private PfConceptKey type;

    @Column
    private String description;

    @Column
    private boolean required = false;

    @Column(name = "default")
    @SerializedName("default")
    private String defaultValue;

    @Column
    @NonNull
    private Status status = Status.SUPPORTED;

    @ElementCollection
    private List<ToscaConstraint> constraints;

    @Column
    @SerializedName("entry_schema")
    private ToscaEntrySchema entrySchema;

    /**
     * The Default Constructor creates a {@link ToscaProperty} object with a null key.
     */
    public ToscaProperty() {
        this(new PfReferenceKey());
    }

    /**
     * The Key Constructor creates a {@link ToscaProperty} object with the given concept key.
     *
     * @param key the key
     */
    public ToscaProperty(@NonNull final PfReferenceKey key) {
        this(key, new PfConceptKey());
    }

    /**
     * The Key Constructor creates a {@link ToscaProperty} object with the given concept key.
     *
     * @param key the key
     * @param type the key of the property type
     */
    public ToscaProperty(@NonNull final PfReferenceKey key, @NonNull final PfConceptKey type) {
        this.key = key;
        this.type = type;
    }

    /**
     * Copy constructor.
     *
     * @param copyConcept the concept to copy from
     */
    public ToscaProperty(final ToscaProperty copyConcept) {
        super(copyConcept);
    }

    @Override
    public List<PfKey> getKeys() {
        final List<PfKey> keyList = getKey().getKeys();

        keyList.addAll(type.getKeys());

        if (constraints != null) {
            for (ToscaConstraint constraint : constraints) {
                keyList.addAll(constraint.getKeys());
            }
        }

        if (entrySchema != null) {
            keyList.addAll(entrySchema.getKeys());
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

        if (defaultValue != null) {
            defaultValue = defaultValue.trim();
        }

        if (constraints != null) {
            for (ToscaConstraint constraint : constraints) {
                constraint.clean();
            }
        }

        if (entrySchema != null) {
            entrySchema.clean();
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
                    "property type may not be null"));
        }

        return validateFields(result);
    }

    /**
     * Validate the property fields.
     *
     * @param resultIn the incoming validation results so far
     * @return the validation results including this validation
     */
    private PfValidationResult validateFields(final PfValidationResult resultIn) {
        PfValidationResult result = resultIn;

        if (description != null && description.trim().length() == 0) {
            result.addValidationMessage(new PfValidationMessage(key, this.getClass(), ValidationResult.INVALID,
                    "property description may not be blank"));
        }

        if (defaultValue != null && defaultValue.trim().length() == 0) {
            result.addValidationMessage(new PfValidationMessage(key, this.getClass(), ValidationResult.INVALID,
                    "property default value may not be null"));
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
        return (entrySchema != null ? entrySchema.validate(result) : result);
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

        final ToscaProperty other = (ToscaProperty) otherConcept;
        if (!key.equals(other.key)) {
            return key.compareTo(other.key);
        }

        return compareFields(other);
    }

    /**
     * Compare the fields of this ToscaProperty object with the fields of the other ToscaProperty
     * object.
     *
     * @param other the other ToscaProperty object
     */
    private int compareFields(final ToscaProperty other) {
        if (!type.equals(other.type)) {
            return type.compareTo(other.type);
        }

        int result = ObjectUtils.compare(description, other.description);
        if (result != 0) {
            return result;
        }

        result = ObjectUtils.compare(required, other.required);
        if (result != 0) {
            return result;
        }

        result = ObjectUtils.compare(defaultValue, other.defaultValue);
        if (result != 0) {
            return result;
        }

        result = ObjectUtils.compare(status, other.status);
        if (result != 0) {
            return result;
        }

        result = PfUtils.compareObjects(constraints, other.constraints);
        if (result != 0) {
            return result;
        }

        return entrySchema.compareTo(other.entrySchema);
    }

    @Override
    public PfConcept copyTo(@NonNull final PfConcept target) {
        Assertions.instanceOf(target, ToscaProperty.class);

        final ToscaProperty copy = ((ToscaProperty) target);
        copy.setKey(new PfReferenceKey(key));
        copy.setType(new PfConceptKey(type));
        copy.setDescription(description);
        copy.setRequired(required);
        copy.setDefaultValue(defaultValue);
        copy.setStatus(status);
        copy.constraints = constraints; // Constraints are immutable
        copy.setEntrySchema(entrySchema != null ? new ToscaEntrySchema(entrySchema) : null);

        return copy;
    }
}
