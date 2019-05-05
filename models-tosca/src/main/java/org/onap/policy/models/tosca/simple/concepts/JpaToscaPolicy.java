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

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.ws.rs.core.Response;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.validation.Assertions;
import org.onap.policy.common.utils.validation.ParameterValidationUtils;
import org.onap.policy.models.base.PfAuthorative;
import org.onap.policy.models.base.PfConcept;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfKey;
import org.onap.policy.models.base.PfModelRuntimeException;
import org.onap.policy.models.base.PfUtils;
import org.onap.policy.models.base.PfValidationMessage;
import org.onap.policy.models.base.PfValidationResult;
import org.onap.policy.models.base.PfValidationResult.ValidationResult;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicy;

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
public class JpaToscaPolicy extends JpaToscaEntityType<ToscaPolicy> implements PfAuthorative<ToscaPolicy> {
    private static final long serialVersionUID = 3265174757061982805L;

    // Tags for metadata
    private static final String METADATA_POLICY_ID_TAG = "policy-id";
    private static final String METADATA_POLICY_VERSION_TAG = "policy-version";

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
    @Lob
    private Map<String, String> properties;

    @ElementCollection
    private List<PfConceptKey> targets;
    // @formatter:on

    /**
     * The Default Constructor creates a {@link JpaToscaPolicy} object with a null key.
     */
    public JpaToscaPolicy() {
        this(new PfConceptKey());
    }

    /**
     * The Key Constructor creates a {@link JpaToscaPolicy} object with the given concept key.
     *
     * @param key the key
     */
    public JpaToscaPolicy(@NonNull final PfConceptKey key) {
        this(key, new PfConceptKey());
    }

    /**
     * The full Constructor creates a {@link JpaToscaPolicy} object with all mandatory fields.
     *
     * @param key the key
     * @param type the type of the policy
     */
    public JpaToscaPolicy(@NonNull final PfConceptKey key, @NonNull final PfConceptKey type) {
        super(key);
        this.type = type;
    }

    /**
     * Copy constructor.
     *
     * @param copyConcept the concept to copy from
     */
    public JpaToscaPolicy(@NonNull final JpaToscaPolicy copyConcept) {
        super(copyConcept);
    }

    /**
     * Authorative constructor.
     *
     * @param authorativeConcept the authorative concept to copy from
     */
    public JpaToscaPolicy(final ToscaPolicy authorativeConcept) {
        super(new PfConceptKey());
        type = new PfConceptKey();
        this.fromAuthorative(authorativeConcept);
    }

    @Override
    public ToscaPolicy toAuthorative() {
        ToscaPolicy toscaPolicy = new ToscaPolicy();
        super.setToscaEntity(toscaPolicy);
        super.toAuthorative();

        toscaPolicy.setType(type.getName());

        if (!PfKey.NULL_KEY_VERSION.equals(type.getVersion())) {
            toscaPolicy.setTypeVersion(type.getVersion());
        } else {
            toscaPolicy.setTypeVersion(null);
        }

        if (properties != null) {
            Map<String, Object> propertyMap = new LinkedHashMap<>();

            for (Entry<String, String> entry : properties.entrySet()) {
                try {
                    // TODO: This is a HACK, we need to validate the properties against their
                    // TODO: their data type in their policy type definition in TOSCA, which means reading
                    // TODO: the policy type from the database and parsing the property value object correctly
                    // TODO: Here we are simply reading a JSON string from the database and deserializing the
                    // TODO: property value from JSON
                    propertyMap.put(entry.getKey(), new StandardCoder().decode(entry.getValue(), Object.class));
                } catch (CoderException ce) {
                    String errorMessage = "error decoding property JSON value read from database: key=" + entry.getKey()
                            + ", value=" + entry.getValue();
                    throw new PfModelRuntimeException(Response.Status.INTERNAL_SERVER_ERROR, errorMessage, ce);
                }
            }

            toscaPolicy.setProperties(propertyMap);
        }

        return toscaPolicy;
    }

    @Override
    public void fromAuthorative(@NonNull final ToscaPolicy toscaPolicy) {
        super.fromAuthorative(toscaPolicy);

        type.setName(toscaPolicy.getType());
        type.setVersion(toscaPolicy.getTypeVersion());
        if (type.getVersion() == null) {
            type.setVersion(PfKey.NULL_KEY_VERSION);
        }

        if (toscaPolicy.getProperties() != null) {
            properties = new LinkedHashMap<>();

            for (Entry<String, Object> propertyEntry : toscaPolicy.getProperties().entrySet()) {
                // TODO: This is a HACK, we need to validate the properties against their
                // TODO: their data type in their policy type definition in TOSCA, which means reading
                // TODO: the policy type from the database and parsing the property value object correctly
                // TODO: Here we are simply serializing the property value into a string and storing it
                // TODO: unvalidated into the database
                try {
                    properties.put(propertyEntry.getKey(), new StandardCoder().encode(propertyEntry.getValue()));
                } catch (CoderException ce) {
                    String errorMessage = "error encoding property JSON value for database: key="
                            + propertyEntry.getKey() + ", value=" + propertyEntry.getValue();
                    throw new PfModelRuntimeException(Response.Status.INTERNAL_SERVER_ERROR, errorMessage, ce);
                }
            }
        }

        // Add the property metadata if it doesn't exist already
        if (toscaPolicy.getMetadata() == null) {
            setMetadata(new LinkedHashMap<>());
        }

        // Add the policy name and version fields to the metadata
        getMetadata().put(METADATA_POLICY_ID_TAG, getKey().getName());
        getMetadata().put(METADATA_POLICY_VERSION_TAG, Integer.toString(getKey().getMajorVersion()));
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
            result.addValidationMessage(new PfValidationMessage(getKey(), this.getClass(), ValidationResult.INVALID,
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
    private PfValidationResult validateProperties(final PfValidationResult resultIn) {
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

        final JpaToscaPolicy other = (JpaToscaPolicy) otherConcept;
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

        final JpaToscaPolicy copy = ((JpaToscaPolicy) copyObject);
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
