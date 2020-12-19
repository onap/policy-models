/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy Model
 * ================================================================================
 * Copyright (C) 2019-2020 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019-2020 Nordix Foundation.
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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
import org.onap.policy.common.parameters.BeanValidationResult;
import org.onap.policy.common.parameters.annotations.Entries;
import org.onap.policy.common.parameters.annotations.Items;
import org.onap.policy.common.parameters.annotations.NotBlank;
import org.onap.policy.common.parameters.annotations.NotNull;
import org.onap.policy.common.parameters.annotations.Valid;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.models.base.PfAuthorative;
import org.onap.policy.models.base.PfConcept;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfKey;
import org.onap.policy.models.base.PfModelRuntimeException;
import org.onap.policy.models.base.PfUtils;
import org.onap.policy.models.base.validation.annotations.Key;
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

    private static final StandardCoder STANDARD_CODER = new StandardCoder();

    // @formatter:off
    @Column
    @AttributeOverrides({
        @AttributeOverride(name = "name",
                           column = @Column(name = "type_name")),
        @AttributeOverride(name = "version",
                           column = @Column(name = "type_version"))
        })
    @Key
    @NotNull
    private PfConceptKey type;

    @ElementCollection
    @Lob
    @Entries(key = @Items(notNull = {@NotNull}, notBlank = {@NotBlank}), value = @Items(notNull = {@NotNull}))
    private Map<String, String> properties;

    @ElementCollection
    @Items(notNull = {@NotNull}, valid = {@Valid})
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
        this.type = new PfConceptKey(copyConcept.type);
        this.properties = (copyConcept.properties != null ? new LinkedHashMap<>(copyConcept.properties) : null);
        this.targets = PfUtils.mapList(copyConcept.targets, PfConceptKey::new);
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

        toscaPolicy.setProperties(PfUtils.mapMap(properties, property -> {
            try {
                return STANDARD_CODER.decode(property, Object.class);
            } catch (CoderException ce) {
                String errorMessage = "error decoding property JSON value read from database: " + property;
                throw new PfModelRuntimeException(Response.Status.INTERNAL_SERVER_ERROR, errorMessage, ce);
            }
        }));

        return toscaPolicy;
    }

    @Override
    public void fromAuthorative(@NonNull final ToscaPolicy toscaPolicy) {
        super.fromAuthorative(toscaPolicy);

        if (toscaPolicy.getType() != null) {
            type.setName(toscaPolicy.getType());
        } else {
            throw new PfModelRuntimeException(Response.Status.BAD_REQUEST,
                    "PolicyType type not specified, the type of the PolicyType for this policy must be specified in "
                            + "the type field");
        }

        if (toscaPolicy.getTypeVersion() != null) {
            type.setVersion(toscaPolicy.getTypeVersion());
        } else {
            throw new PfModelRuntimeException(Response.Status.BAD_REQUEST,
                    "PolicyType version not specified, the version of the PolicyType for this policy must be specified"
                            + " in the type_version field");
        }

        properties = PfUtils.mapMap(toscaPolicy.getProperties(), property -> {
            try {
                return STANDARD_CODER.encode(property);
            } catch (CoderException ce) {
                String errorMessage = "error encoding property JSON value for database: " + property;
                throw new PfModelRuntimeException(Response.Status.INTERNAL_SERVER_ERROR, errorMessage, ce);
            }
        });

        // Add the property metadata if it doesn't exist already
        if (toscaPolicy.getMetadata() == null) {
            setMetadata(new LinkedHashMap<>());
        }

        // Add the policy name and version fields to the metadata
        getMetadata().put(METADATA_POLICY_ID_TAG, getKey().getName());
        getMetadata().put(METADATA_POLICY_VERSION_TAG, getKey().getVersion());
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
    public BeanValidationResult validate(String fieldName) {
        BeanValidationResult result = super.validate(fieldName);

        result.addResult(validateKeyVersionNotNull("key", getKey()));

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

        final JpaToscaPolicy other = (JpaToscaPolicy) otherConcept;
        int result = super.compareTo(other);
        if (result != 0) {
            return result;
        }

        result = type.compareTo(other.type);
        if (result != 0) {
            return result;
        }

        result = PfUtils.compareMaps(properties, other.properties);
        if (result != 0) {
            return result;
        }

        return PfUtils.compareCollections(targets, other.targets);
    }
}
