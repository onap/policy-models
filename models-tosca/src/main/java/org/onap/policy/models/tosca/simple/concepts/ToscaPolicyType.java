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
public class ToscaPolicyType extends ToscaEntityType {
    private static final long serialVersionUID = -563659852901842616L;

    @ElementCollection
    private List<ToscaProperty> properties;

    @ElementCollection
    private List<PfConceptKey> targets;

    @ElementCollection
    private List<ToscaTrigger> triggers;

    /**
     * The Default Constructor creates a {@link ToscaPolicyType} object with a null key.
     */
    public ToscaPolicyType() {
        this(new PfConceptKey());
    }

    /**
     * The Key Constructor creates a {@link ToscaPolicyType} object with the given concept key.
     *
     * @param key the key
     */
    public ToscaPolicyType(@NonNull final PfConceptKey key) {
        super(key);
    }

    /**
     * Copy constructor.
     *
     * @param copyConcept the concept to copy from
     */
    public ToscaPolicyType(final ToscaPolicyType copyConcept) {
        super(copyConcept);
    }


    @Override
    public List<PfKey> getKeys() {
        final List<PfKey> keyList = super.getKeys();

        if (properties != null) {
            for (ToscaProperty property : properties) {
                keyList.addAll(property.getKeys());
            }
        }

        if (targets != null) {
            keyList.addAll(targets);
        }

        if (triggers != null) {
            for (ToscaTrigger trigger : triggers) {
                keyList.addAll(trigger.getKeys());
            }
        }

        return keyList;
    }

    @Override
    public void clean() {
        super.clean();

        if (properties != null) {
            for (ToscaProperty property : properties) {
                property.clean();
            }
        }

        if (targets != null) {
            for (PfConceptKey target : targets) {
                target.clean();
            }
        }

        if (triggers != null) {
            for (ToscaTrigger trigger : triggers) {
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

        for (ToscaProperty property : properties) {
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

        for (ToscaTrigger trigger : triggers) {
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

        final ToscaPolicyType other = (ToscaPolicyType) otherConcept;
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

        final ToscaPolicyType copy = ((ToscaPolicyType) copyObject);
        super.copyTo(target);

        final List<ToscaProperty> newProperties = new ArrayList<>();

        if (properties == null) {
            copy.setProperties(null);
        } else {
            for (final ToscaProperty property : properties) {
                newProperties.add(new ToscaProperty(property));
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
            final List<ToscaTrigger> newTriggers = new ArrayList<>();
            for (final ToscaTrigger trigger : triggers) {
                newTriggers.add(new ToscaTrigger(trigger));
            }
            copy.setTriggers(newTriggers);
        }

        return copy;
    }
}
