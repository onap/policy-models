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
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import org.onap.policy.common.utils.validation.Assertions;
import org.onap.policy.common.utils.validation.ParameterValidationUtils;
import org.onap.policy.models.base.PfConcept;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfKey;
import org.onap.policy.models.base.PfUtils;
import org.onap.policy.models.base.PfValidationMessage;
import org.onap.policy.models.base.PfValidationResult;
import org.onap.policy.models.base.PfValidationResult.ValidationResult;

/**
 * Class to represent the policy in TOSCA definition.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 * @author Liam Fallon (liam.fallon@est.tech)
 */
@Entity
@Table(name = "ToscaPolicy")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Data
@EqualsAndHashCode(callSuper = true)
public class ToscaPolicy extends ToscaEntityType {
    private static final long serialVersionUID = 3265174757061982805L;

    // @formatter:off
    @Column
    @AttributeOverrides({
        @AttributeOverride(name = "name",
                           column = @Column(name = "type_name")),
        @AttributeOverride(name = "version",
                           column = @Column(name = "type_version"))
        })
    private PfConceptKey type;

    @ElementCollection
    @Column(length = 10000)
    private Map<String, String> properties;

    @ElementCollection
    private List<PfConceptKey> targets;
    // @formatter:on

    /**
     * The Default Constructor creates a {@link ToscaPolicy} object with a null key.
     */
    public ToscaPolicy() {
        this(new PfConceptKey());
    }

    /**
     * The Key Constructor creates a {@link ToscaPolicy} object with the given concept key.
     *
     * @param key the key
     */
    public ToscaPolicy(@NonNull final PfConceptKey key) {
        this(key, new PfConceptKey());
    }

    /**
     * The full Constructor creates a {@link ToscaPolicy} object with all mandatory fields.
     *
     * @param key the key
     * @param type the type of the policy
     */
    public ToscaPolicy(@NonNull final PfConceptKey key, @NonNull final PfConceptKey type) {
        super(key);
        this.type = type;
    }

    /**
     * Copy constructor.
     *
     * @param copyConcept the concept to copy from
     */
    public ToscaPolicy(@NonNull final ToscaPolicy copyConcept) {
        super(copyConcept);
    }


    @Override
    public List<PfKey> getKeys() {
        final List<PfKey> keyList = super.getKeys();

        keyList.addAll(type.getKeys());

        if (targets != null) {
            keyList.addAll(targets);
        }

        return keyList;
    }

    @Override
    public void clean() {
        super.clean();

        type.clean();

        if (targets != null) {
            for (PfConceptKey target : targets) {
                target.clean();
            }
        }
    }

    @Override
    public PfValidationResult validate(@NonNull final PfValidationResult resultIn) {
        PfValidationResult result = super.validate(resultIn);

        if (type == null || type.isNullKey()) {
            result.addValidationMessage(new PfValidationMessage(type, this.getClass(), ValidationResult.INVALID,
                    "type is null or a null key"));
        } else {
            result = type.validate(result);
        }

        if (properties != null) {
            result = validateProperties(result);
        }

        if (targets != null) {
            result = validateTargets(result);
        }

        return result;
    }

    /**
     * Validate the policy properties.
     *
     * @param result The result of validations up to now
     * @return the validation result
     */
    private PfValidationResult validateProperties(@NonNull final PfValidationResult resultIn) {
        PfValidationResult result = resultIn;

        for (Entry<String, String> propertyEntry : properties.entrySet()) {
            if (!ParameterValidationUtils.validateStringParameter(propertyEntry.getKey())) {
                result.addValidationMessage(new PfValidationMessage(getKey(), this.getClass(), ValidationResult.INVALID,
                        "policy property key may not be null "));
            } else if (propertyEntry.getValue() == null) {
                result.addValidationMessage(new PfValidationMessage(getKey(), this.getClass(), ValidationResult.INVALID,
                        "policy property value may not be null "));
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

        final ToscaPolicy other = (ToscaPolicy) otherConcept;
        if (!super.equals(other)) {
            return super.compareTo(other);
        }

        if (!type.equals(other.type)) {
            return type.compareTo(other.type);
        }

        int retVal = PfUtils.compareObjects(properties, other.properties);
        if (retVal != 0) {
            return retVal;
        }

        return PfUtils.compareObjects(targets, other.targets);
    }

    @Override
    public PfConcept copyTo(@NonNull PfConcept target) {
        final Object copyObject = target;
        Assertions.instanceOf(copyObject, PfConcept.class);

        final ToscaPolicy copy = ((ToscaPolicy) copyObject);
        super.copyTo(target);

        copy.setType(new PfConceptKey(type));

        if (properties == null) {
            copy.setProperties(null);
        } else {
            copy.setProperties(properties);
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

        return copy;
    }
}
