/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2021, 2023 Nordix Foundation.
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

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Lob;
import jakarta.persistence.MappedSuperclass;
import jakarta.ws.rs.core.Response;
import java.io.Serial;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.onap.policy.common.parameters.BeanValidationResult;
import org.onap.policy.common.parameters.annotations.NotNull;
import org.onap.policy.models.base.PfAuthorative;
import org.onap.policy.models.base.PfConcept;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfKey;
import org.onap.policy.models.base.PfModelRuntimeException;
import org.onap.policy.models.base.PfUtils;
import org.onap.policy.models.base.validation.annotations.VerifyKey;
import org.onap.policy.models.tosca.authorative.concepts.ToscaWithTypeAndObjectProperties;

/**
 * Class to represent JPA TOSCA classes containing property maps whose values are Strings.
 */
@MappedSuperclass
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class JpaToscaWithTypeAndStringProperties<T extends ToscaWithTypeAndObjectProperties>
        extends JpaToscaEntityType<T> implements PfAuthorative<T> {

    @Serial
    private static final long serialVersionUID = 2785481541573683089L;

    @Column
    @AttributeOverride(name = "name", column = @Column(name = "type_name"))
    @AttributeOverride(name = "version", column = @Column(name = "type_version"))
    @VerifyKey
    @NotNull
    private PfConceptKey type;

    @ElementCollection
    @Lob
    private Map<@NotNull String, @NotNull String> properties;

    /**
     * The Default Constructor creates a {@link JpaToscaWithTypeAndStringProperties} object with a null key.
     */
    protected JpaToscaWithTypeAndStringProperties() {
        this(new PfConceptKey());
    }

    /**
     * The Key Constructor creates a {@link JpaToscaWithTypeAndStringProperties} object with the given concept key.
     *
     * @param key the key
     */
    protected JpaToscaWithTypeAndStringProperties(@NonNull final PfConceptKey key) {
        this(key, new PfConceptKey());
    }

    /**
     * The full Constructor creates a {@link JpaToscaWithTypeAndStringProperties} object with all mandatory fields.
     *
     * @param key the key
     * @param type the type of the policy
     */
    protected JpaToscaWithTypeAndStringProperties(@NonNull final PfConceptKey key, @NonNull final PfConceptKey type) {
        super(key);
        this.type = type;
    }

    /**
     * Copy constructor.
     *
     * @param copyConcept the concept to copy from
     */
    protected JpaToscaWithTypeAndStringProperties(@NonNull final JpaToscaWithTypeAndStringProperties<T> copyConcept) {
        super(copyConcept);
        this.type = new PfConceptKey(copyConcept.type);
        this.properties = (copyConcept.properties != null ? new LinkedHashMap<>(copyConcept.properties) : null);
    }

    /**
     * Authorative constructor.
     *
     * @param authorativeConcept the authorative concept to copy from
     */
    protected JpaToscaWithTypeAndStringProperties(final T authorativeConcept) {
        super(new PfConceptKey());
        type = new PfConceptKey();
        this.fromAuthorative(authorativeConcept);
    }

    @Override
    public T toAuthorative() {
        var tosca = super.toAuthorative();

        tosca.setType(type.getName());

        if (!PfKey.NULL_KEY_VERSION.equals(type.getVersion())) {
            tosca.setTypeVersion(type.getVersion());
        } else {
            tosca.setTypeVersion(null);
        }

        tosca.setProperties(PfUtils.mapMap(properties, this::deserializePropertyValue));

        return tosca;
    }

    @Override
    public void fromAuthorative(@NonNull final T authorativeConcept) {
        super.fromAuthorative(authorativeConcept);

        if (authorativeConcept.getType() != null) {
            type.setName(authorativeConcept.getType());
        } else {
            throw new PfModelRuntimeException(Response.Status.BAD_REQUEST,
                    "Type not specified, the type of this TOSCA entity must be specified in the type field");
        }

        if (authorativeConcept.getTypeVersion() != null) {
            type.setVersion(authorativeConcept.getTypeVersion());
        } else {
            throw new PfModelRuntimeException(Response.Status.BAD_REQUEST,
                    "Version not specified, the version of this TOSCA entity must be specified"
                            + " in the type_version field");
        }

        properties = PfUtils.mapMap(authorativeConcept.getProperties(), this::serializePropertyValue);
    }

    /**
     * Deserializes a property value.
     *
     * @param propValue value to be deserialized
     * @return the deserialized property value
     */
    protected abstract Object deserializePropertyValue(String propValue);

    /**
     * Serializes a property value.
     *
     * @param propValue value to be serialized
     * @return the serialized property value
     */
    protected abstract String serializePropertyValue(Object propValue);

    @Override
    public List<PfKey> getKeys() {
        final List<PfKey> keyList = super.getKeys();

        keyList.addAll(type.getKeys());

        return keyList;
    }

    @Override
    public void clean() {
        super.clean();

        type.clean();

        properties = PfUtils.mapMap(properties, String::trim);
    }

    /**
     * Validates the fields of the object, including its key.
     *
     * @param fieldName name of the field containing this
     * @return the result, or {@code null}
     */
    protected BeanValidationResult validateWithKey(String fieldName) {
        BeanValidationResult result = super.validate(fieldName);

        validateKeyVersionNotNull(result, "key", getKey());

        return result;
    }

    @Override
    public int compareTo(final PfConcept otherConcept) {
        if (this == otherConcept) {
            return 0;
        }

        int result = super.compareTo(otherConcept);
        if (result != 0) {
            return result;
        }

        @SuppressWarnings("unchecked")
        final JpaToscaWithTypeAndStringProperties<T> other = (JpaToscaWithTypeAndStringProperties<T>) otherConcept;

        result = type.compareTo(other.type);
        if (result != 0) {
            return result;
        }

        return PfUtils.compareMaps(properties, other.properties);
    }
}
