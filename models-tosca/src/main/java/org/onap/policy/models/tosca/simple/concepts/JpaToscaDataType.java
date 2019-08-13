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

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import org.onap.policy.common.utils.validation.Assertions;
import org.onap.policy.models.base.PfAuthorative;
import org.onap.policy.models.base.PfConcept;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfKey;
import org.onap.policy.models.base.PfReferenceKey;
import org.onap.policy.models.base.PfUtils;
import org.onap.policy.models.base.PfValidationMessage;
import org.onap.policy.models.base.PfValidationResult;
import org.onap.policy.models.base.PfValidationResult.ValidationResult;
import org.onap.policy.models.base.utils.BeanCopyUtils;
import org.onap.policy.models.tosca.authorative.concepts.ToscaConstraint;
import org.onap.policy.models.tosca.authorative.concepts.ToscaDataType;
import org.onap.policy.models.tosca.authorative.concepts.ToscaProperty;

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
public class JpaToscaDataType extends JpaToscaEntityType<ToscaDataType> implements PfAuthorative<ToscaDataType> {
    private static final long serialVersionUID = -3922690413436539164L;

    @ElementCollection
    private List<JpaToscaConstraint> constraints;

    @ElementCollection
    private Map<String, JpaToscaProperty> properties;

    /**
     * The Default Constructor creates a {@link JpaToscaDataType} object with a null key.
     */
    public JpaToscaDataType() {
        this(new PfConceptKey());
    }

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
    }

    /**
     * Authorative constructor.
     *
     * @param authorativeConcept the authorative concept to copy from
     */
    public JpaToscaDataType(final ToscaDataType authorativeConcept) {
        this.fromAuthorative(authorativeConcept);
    }

    @Override
    public ToscaDataType toAuthorative() {
        ToscaDataType toscaDataType = new ToscaDataType();
        super.setToscaEntity(toscaDataType);
        super.toAuthorative();

        if (constraints != null) {
            List<ToscaConstraint> toscaConstraints = new ArrayList<>();

            for (JpaToscaConstraint constraint : constraints) {
                toscaConstraints.add(constraint.toAuthorative());
            }

            toscaDataType.setConstraints(toscaConstraints);
        }

        if (properties != null) {
            Map<String, ToscaProperty> propertyMap = new LinkedHashMap<>();

            for (Entry<String, JpaToscaProperty> entry : properties.entrySet()) {
                propertyMap.put(entry.getKey(), entry.getValue().toAuthorative());
            }

            toscaDataType.setProperties(propertyMap);
        }

        return toscaDataType;
    }

    @Override
    public void fromAuthorative(final ToscaDataType toscaDataType) {
        super.fromAuthorative(toscaDataType);

        if (toscaDataType.getConstraints() != null) {
            constraints = new ArrayList<>();

            for (ToscaConstraint toscaConstraint: toscaDataType.getConstraints()) {
                constraints.add(JpaToscaConstraint.newInstance(toscaConstraint));
            }
        }

        if (toscaDataType.getProperties() != null) {
            properties = new LinkedHashMap<>();
            for (Entry<String, ToscaProperty> toscaPropertyEntry : toscaDataType.getProperties().entrySet()) {
                JpaToscaProperty jpaProperty = new JpaToscaProperty(toscaPropertyEntry.getValue());
                jpaProperty.setKey(new PfReferenceKey(getKey(), toscaPropertyEntry.getKey()));
                properties.put(toscaPropertyEntry.getKey(), jpaProperty);
            }
        }
    }

    @Override
    public List<PfKey> getKeys() {
        final List<PfKey> keyList = super.getKeys();

        if (properties != null) {
            for (JpaToscaProperty property : properties.values()) {
                keyList.addAll(property.getKeys());
            }
        }

        return keyList;
    }

    @Override
    public void clean() {
        super.clean();

        if (properties != null) {
            for (JpaToscaProperty property : properties.values()) {
                property.clean();
            }
        }
    }

    @Override
    public PfValidationResult validate(final PfValidationResult resultIn) {
        PfValidationResult result = super.validate(resultIn);

        if (constraints != null) {
            result = validateConstraints(result);
        }

        if (properties != null) {
            result = validateProperties(result);
        }

        return result;
    }

    /**
     * Validate the constraints.
     *
     * @param result The result of validations up to now
     * @return the validation result
     */
    private PfValidationResult validateConstraints(@NonNull final PfValidationResult resultIn) {
        PfValidationResult result = resultIn;

        for (JpaToscaConstraint constraint : constraints) {
            if (constraint == null) {
                result.addValidationMessage(new PfValidationMessage(getKey(), this.getClass(), ValidationResult.INVALID,
                        "data type constraint may not be null "));
            }
        }
        return result;
    }

    /**
     * Validate the properties.
     *
     * @param result The result of validations up to now
     * @return the validation result
     */
    private PfValidationResult validateProperties(final PfValidationResult resultIn) {
        PfValidationResult result = resultIn;

        for (JpaToscaProperty property : properties.values()) {
            if (property == null) {
                result.addValidationMessage(new PfValidationMessage(getKey(), this.getClass(), ValidationResult.INVALID,
                        "data type property may not be null "));
            } else {
                result = property.validate(result);
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
            return getClass().getName().compareTo(otherConcept.getClass().getName());
        }

        final JpaToscaDataType other = (JpaToscaDataType) otherConcept;
        if (!super.equals(other)) {
            return super.compareTo(other);
        }

        int result = PfUtils.compareObjects(constraints, other.constraints);
        if (result != 0) {
            return result;
        }

        result = PfUtils.compareObjects(properties, other.properties);
        if (result != 0) {
            return result;
        }

        return 0;
    }

    @Override
    public PfConcept copyTo(@NonNull PfConcept target) {
        return BeanCopyUtils.copyTo(this, target, this.getClass());
    }
}
