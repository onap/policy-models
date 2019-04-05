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
import javax.persistence.Lob;
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
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyType;
import org.onap.policy.models.tosca.authorative.concepts.ToscaProperty;

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
public class JpaToscaPolicyType extends JpaToscaEntityType<ToscaPolicyType> implements PfAuthorative<ToscaPolicyType> {
    private static final long serialVersionUID = -563659852901842616L;

    @ElementCollection
    @Lob
    private Map<String, JpaToscaProperty> properties;

    @ElementCollection
    private List<PfConceptKey> targets;

    @ElementCollection
    private List<JpaToscaTrigger> triggers;

    /**
     * The Default Constructor creates a {@link JpaToscaPolicyType} object with a null key.
     */
    public JpaToscaPolicyType() {
        this(new PfConceptKey());
    }

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
    }

    /**
     * Authorative constructor.
     *
     * @param authorativeConcept the authorative concept to copy from
     */
    public JpaToscaPolicyType(final ToscaPolicyType authorativeConcept) {
        this.fromAuthorative(authorativeConcept);
    }

    @Override
    public ToscaPolicyType toAuthorative() {
        ToscaPolicyType toscaPolicyType = new ToscaPolicyType();
        super.setToscaEntity(toscaPolicyType);
        super.toAuthorative();

        if (properties != null) {
            Map<String, ToscaProperty> propertyMap = new LinkedHashMap<>();

            for (Entry<String, JpaToscaProperty> entry : properties.entrySet()) {
                propertyMap.put(entry.getKey(), entry.getValue().toAuthorative());
            }

            toscaPolicyType.setProperties(propertyMap);
        }

        return toscaPolicyType;
    }

    @Override
    public void fromAuthorative(final ToscaPolicyType toscaPolicyType) {
        super.fromAuthorative(toscaPolicyType);

        // Set properties
        if (toscaPolicyType.getProperties() != null) {
            properties = new LinkedHashMap<>();
            for (Entry<String, ToscaProperty> toscaPropertyEntry : toscaPolicyType.getProperties().entrySet()) {
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

        if (properties != null) {
            for (JpaToscaProperty property : properties.values()) {
                property.clean();
            }
        }

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
    public PfValidationResult validate(@NonNull final PfValidationResult resultIn) {
        PfValidationResult result = super.validate(resultIn);

        if (properties != null) {
            result = validateProperties(result);
        }

        if (targets != null) {
            result = validateTargets(result);
        }

        if (triggers != null) {
            result = validateTriggers(result);
        }

        return result;
    }

    /**
     * Validate the policy properties.
     *
     * @param result The result of validations up to now
     * @return the validation result
     */
    private PfValidationResult validateProperties(final PfValidationResult resultIn) {
        PfValidationResult result = resultIn;

        for (JpaToscaProperty property : properties.values()) {
            if (property == null) {
                result.addValidationMessage(new PfValidationMessage(getKey(), this.getClass(), ValidationResult.INVALID,
                        "policy property may not be null "));
            } else {
                result = property.validate(result);
            }
        }
        return result;
    }

    /**
     * Validate the policy targets.
     *
     * @param result The result of validations up to now
     * @return the validation result
     */
    private PfValidationResult validateTargets(final PfValidationResult resultIn) {
        PfValidationResult result = resultIn;

        for (PfConceptKey target : targets) {
            if (target == null) {
                result.addValidationMessage(new PfValidationMessage(getKey(), this.getClass(), ValidationResult.INVALID,
                        "policy target may not be null "));
            } else {
                result = target.validate(result);
            }
        }
        return result;
    }

    /**
     * Validate the policy triggers.
     *
     * @param result The result of validations up to now
     * @return the validation result
     */
    private PfValidationResult validateTriggers(final PfValidationResult resultIn) {
        PfValidationResult result = resultIn;

        for (JpaToscaTrigger trigger : triggers) {
            if (trigger == null) {
                result.addValidationMessage(new PfValidationMessage(getKey(), this.getClass(), ValidationResult.INVALID,
                        "policy trigger may not be null "));
            } else {
                result = trigger.validate(result);
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

        final JpaToscaPolicyType other = (JpaToscaPolicyType) otherConcept;
        if (!super.equals(other)) {
            return super.compareTo(other);
        }

        int retVal = PfUtils.compareObjects(properties, other.properties);
        if (retVal != 0) {
            return retVal;
        }

        retVal = PfUtils.compareObjects(targets, other.targets);
        if (retVal != 0) {
            return retVal;
        }

        return PfUtils.compareObjects(triggers, other.triggers);
    }

    @Override
    public PfConcept copyTo(@NonNull PfConcept target) {
        final Object copyObject = target;
        Assertions.instanceOf(copyObject, PfConcept.class);

        final JpaToscaPolicyType copy = ((JpaToscaPolicyType) copyObject);
        super.copyTo(target);

        if (properties == null) {
            copy.setProperties(null);
        } else {
            final Map<String, JpaToscaProperty> newProperties = new LinkedHashMap<>();
            for (final Entry<String, JpaToscaProperty> propertyEntry : properties.entrySet()) {
                newProperties.put(propertyEntry.getKey(), new JpaToscaProperty(propertyEntry.getValue()));
            }
            copy.setProperties(newProperties);
        }

        if (targets == null) {
            copy.setTargets(null);
        } else {
            final List<PfConceptKey> newTargets = new ArrayList<>();
            for (final PfConceptKey oldTarget : targets) {
                newTargets.add(new PfConceptKey(oldTarget));
            }
            copy.setTargets(newTargets);
        }

        if (triggers == null) {
            copy.setTargets(null);
        } else {
            final List<JpaToscaTrigger> newTriggers = new ArrayList<>();
            for (final JpaToscaTrigger trigger : triggers) {
                newTriggers.add(new JpaToscaTrigger(trigger));
            }
            copy.setTriggers(newTriggers);
        }

        return copy;
    }
}
