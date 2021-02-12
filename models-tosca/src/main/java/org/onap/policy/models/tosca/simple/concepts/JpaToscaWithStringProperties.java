/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2021 AT&T Intellectual Property. All rights reserved.
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
import java.util.Map;
import javax.persistence.ElementCollection;
import javax.persistence.Lob;
import javax.persistence.MappedSuperclass;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.onap.policy.common.parameters.BeanValidationResult;
import org.onap.policy.common.parameters.annotations.NotNull;
import org.onap.policy.models.base.PfAuthorative;
import org.onap.policy.models.base.PfConcept;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfUtils;
import org.onap.policy.models.tosca.authorative.concepts.ToscaWithObjectProperties;

/**
 * Class to represent JPA TOSCA classes containing property maps whose values are Strings.
 */
@MappedSuperclass
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class JpaToscaWithStringProperties<T extends ToscaWithObjectProperties> extends JpaToscaEntityType<T>
                implements PfAuthorative<T> {

    private static final long serialVersionUID = 2785481541573683089L;

    @ElementCollection
    @Lob
    private Map<@NotNull String, @NotNull String> properties;

    /**
     * The Default Constructor creates a {@link JpaToscaWithStringProperties} object with
     * a null key.
     */
    public JpaToscaWithStringProperties() {
        this(new PfConceptKey());
    }

    /**
     * The Key Constructor creates a {@link JpaToscaWithStringProperties} object with the
     * given concept key.
     *
     * @param key the key
     */
    public JpaToscaWithStringProperties(@NonNull final PfConceptKey key) {
        super(key);
    }

    /**
     * Copy constructor.
     *
     * @param copyConcept the concept to copy from
     */
    public JpaToscaWithStringProperties(@NonNull final JpaToscaWithStringProperties<T> copyConcept) {
        super(copyConcept);
        this.properties = (copyConcept.properties != null ? new LinkedHashMap<>(copyConcept.properties) : null);
    }

    /**
     * Authorative constructor.
     *
     * @param authorativeConcept the authorative concept to copy from
     */
    public JpaToscaWithStringProperties(final T authorativeConcept) {
        super(new PfConceptKey());
        this.fromAuthorative(authorativeConcept);
    }

    @Override
    public T toAuthorative() {
        T tosca = super.toAuthorative();

        tosca.setProperties(PfUtils.mapMap(properties, this::deserializePropertyValue));

        return tosca;
    }

    @Override
    public void fromAuthorative(@NonNull final T authorativeConcept) {
        super.fromAuthorative(authorativeConcept);

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
    public void clean() {
        super.clean();

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
        final JpaToscaWithStringProperties<T> other = (JpaToscaWithStringProperties<T>) otherConcept;

        return PfUtils.compareMaps(properties, other.properties);
    }
}
