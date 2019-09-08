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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import org.onap.policy.models.base.PfAuthorative;
import org.onap.policy.models.base.PfConcept;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfKey;
import org.onap.policy.models.base.PfReferenceKey;
import org.onap.policy.models.base.PfUtils;
import org.onap.policy.models.base.PfValidationMessage;
import org.onap.policy.models.base.PfValidationResult;
import org.onap.policy.models.base.PfValidationResult.ValidationResult;
import org.onap.policy.models.tosca.authorative.concepts.ToscaConstraint;
import org.onap.policy.models.tosca.authorative.concepts.ToscaProperty;
import org.onap.policy.models.tosca.authorative.concepts.ToscaProperty.Status;

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
public class JpaToscaProperty extends PfConcept implements PfAuthorative<ToscaProperty> {
    private static final long serialVersionUID = 1675770231921107988L;

    @EmbeddedId
    private PfReferenceKey key;

    @Column
    private PfConceptKey type;

    @Column
    private String description;

    @Column
    private boolean required = false;

    @Column(name = "default")
    private String defaultValue;

    @Column
    private Status status = Status.SUPPORTED;

    @ElementCollection
    private List<JpaToscaConstraint> constraints;

    @Column
    private JpaToscaEntrySchema entrySchema;

    @ElementCollection
    private Map<String, String> metadata;

    /**
     * The Default Constructor creates a {@link JpaToscaProperty} object with a null key.
     */
    public JpaToscaProperty() {
        this(new PfReferenceKey());
    }

    /**
     * The Key Constructor creates a {@link JpaToscaProperty} object with the given concept key.
     *
     * @param key the key
     */
    public JpaToscaProperty(@NonNull final PfReferenceKey key) {
        this(key, new PfConceptKey());
    }

    /**
     * The Key Constructor creates a {@link JpaToscaProperty} object with the given concept key.
     *
     * @param key the key
     * @param type the key of the property type
     */
    public JpaToscaProperty(@NonNull final PfReferenceKey key, @NonNull final PfConceptKey type) {
        this.key = key;
        this.type = type;
    }

    /**
     * Copy constructor.
     *
     * @param copyConcept the concept to copy from
     */
    public JpaToscaProperty(final JpaToscaProperty copyConcept) {
        super(copyConcept);
        this.key = new PfReferenceKey(copyConcept.key);
        this.type = new PfConceptKey(copyConcept.type);
        this.description = copyConcept.description;
        this.required = copyConcept.required;
        this.defaultValue = copyConcept.defaultValue;
        this.status = copyConcept.status;
        // Constraints are immutable
        this.constraints = (copyConcept.constraints != null ? new ArrayList<>(copyConcept.constraints) : null);
        this.entrySchema = (copyConcept.entrySchema != null ? new JpaToscaEntrySchema(copyConcept.entrySchema) : null);
        this.metadata = (copyConcept.metadata != null ? new LinkedHashMap<>(copyConcept.metadata) : null);
    }

    /**
     * Authorative constructor.
     *
     * @param authorativeConcept the authorative concept to copy from
     */
    public JpaToscaProperty(final ToscaProperty authorativeConcept) {
        this.fromAuthorative(authorativeConcept);
    }

    @Override
    public ToscaProperty toAuthorative() {
        ToscaProperty toscaProperty = new ToscaProperty();

        toscaProperty.setName(key.getLocalName());

        toscaProperty.setType(type.getName());
        toscaProperty.setTypeVersion(type.getVersion());

        toscaProperty.setDescription(description);
        toscaProperty.setRequired(required);
        toscaProperty.setDefaultValue(defaultValue);
        toscaProperty.setStatus(status);

        if (constraints != null) {
            List<ToscaConstraint> toscaConstraints = new ArrayList<>();

            for (JpaToscaConstraint constraint : constraints) {
                toscaConstraints.add(constraint.toAuthorative());
            }

            toscaProperty.setConstraints(toscaConstraints);
        }

        if (entrySchema != null) {
            toscaProperty.setEntrySchema(entrySchema.toAuthorative());
        }

        if (metadata != null) {
            toscaProperty.setMetadata(new LinkedHashMap<>(metadata));
        }

        return toscaProperty;
    }

    @Override
    public void fromAuthorative(ToscaProperty toscaProperty) {
        this.setKey(new PfReferenceKey());
        getKey().setLocalName(toscaProperty.getName());

        if (toscaProperty.getTypeVersion() != null) {
            type = new PfConceptKey(toscaProperty.getType(), toscaProperty.getTypeVersion());
        } else {
            type = new PfConceptKey(toscaProperty.getType(), PfKey.NULL_KEY_VERSION);
        }

        description = toscaProperty.getDescription();
        required = toscaProperty.isRequired();
        defaultValue = toscaProperty.getDefaultValue();
        status = toscaProperty.getStatus();

        if (toscaProperty.getConstraints() != null) {
            constraints = new ArrayList<>();

            for (ToscaConstraint toscaConstraint : toscaProperty.getConstraints()) {
                constraints.add(JpaToscaConstraint.newInstance(toscaConstraint));
            }
        }

        if (toscaProperty.getEntrySchema() != null) {
            entrySchema = new JpaToscaEntrySchema(toscaProperty.getEntrySchema());
        }

        // Add the property metadata if it doesn't exist already
        if (toscaProperty.getMetadata() != null) {
            metadata = new LinkedHashMap<>(toscaProperty.getMetadata());
        }

    }

    @Override
    public List<PfKey> getKeys() {
        final List<PfKey> keyList = getKey().getKeys();

        keyList.addAll(type.getKeys());

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

        if (entrySchema != null) {
            entrySchema.clean();
        }

        if (metadata != null) {
            for (Entry<String, String> metadataEntry : metadata.entrySet()) {
                metadataEntry.setValue(metadataEntry.getValue().trim());
            }
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
            for (JpaToscaConstraint constraint : constraints) {
                if (constraint == null) {
                    result.addValidationMessage(new PfValidationMessage(key, this.getClass(), ValidationResult.INVALID,
                            "property constraint may not be null "));
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
            return getClass().getName().compareTo(otherConcept.getClass().getName());
        }

        final JpaToscaProperty other = (JpaToscaProperty) otherConcept;
        if (!key.equals(other.key)) {
            return key.compareTo(other.key);
        }

        return compareFields(other);
    }

    /**
     * Compare the fields of this ToscaProperty object with the fields of the other ToscaProperty object.
     *
     * @param other the other ToscaProperty object
     */
    private int compareFields(final JpaToscaProperty other) {
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
}
