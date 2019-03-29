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

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import org.onap.policy.common.utils.validation.Assertions;
import org.onap.policy.models.base.PfConcept;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfKey;
import org.onap.policy.models.base.PfUtils;
import org.onap.policy.models.base.PfValidationMessage;
import org.onap.policy.models.base.PfValidationResult;
import org.onap.policy.models.base.PfValidationResult.ValidationResult;

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
public class JpaToscaDataType extends JpaToscaEntityType {
    private static final long serialVersionUID = -3922690413436539164L;

    @ElementCollection
    private List<JpaToscaConstraint> constraints;

    @ElementCollection
    private List<JpaToscaProperty> properties;

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

    @Override
    public List<PfKey> getKeys() {
        final List<PfKey> keyList = super.getKeys();

        if (constraints != null) {
            for (JpaToscaConstraint constraint : constraints) {
                keyList.addAll(constraint.getKeys());
            }
        }

        if (properties != null) {
            for (JpaToscaProperty property : properties) {
                keyList.addAll(property.getKeys());
            }
        }

        return keyList;
    }

    @Override
    public void clean() {
        super.clean();

        if (constraints != null) {
            for (JpaToscaConstraint constraint : constraints) {
                constraint.clean();
            }
        }

        if (properties != null) {
            for (JpaToscaProperty property : properties) {
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
            } else {
                result = constraint.validate(result);
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

        for (JpaToscaProperty property : properties) {
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
            return this.hashCode() - otherConcept.hashCode();
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
        final Object copyObject = target;
        Assertions.instanceOf(copyObject, PfConcept.class);

        final JpaToscaDataType copy = ((JpaToscaDataType) copyObject);
        super.copyTo(target);

        if (constraints == null) {
            copy.setConstraints(null);
        }
        else {
            final List<JpaToscaConstraint> newConstraints = new ArrayList<>();
            for (final JpaToscaConstraint constraint : constraints) {
                newConstraints.add(constraint); // Constraints are immutable
            }
            copy.setConstraints(newConstraints);
        }

        if (properties == null) {
            copy.setProperties(null);
        }
        else {
            final List<JpaToscaProperty> newProperties = new ArrayList<>();
            for (final JpaToscaProperty property : properties) {
                newProperties.add(new JpaToscaProperty(property));
            }
            copy.setProperties(newProperties);
        }

        return copy;
    }
}