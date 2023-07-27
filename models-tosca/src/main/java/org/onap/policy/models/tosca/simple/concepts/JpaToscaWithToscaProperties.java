/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2023 Nordix Foundation.
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

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Lob;
import jakarta.persistence.MappedSuperclass;
import java.io.Serial;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.apache.commons.collections4.CollectionUtils;
import org.onap.policy.common.parameters.BeanValidationResult;
import org.onap.policy.common.parameters.annotations.NotBlank;
import org.onap.policy.common.parameters.annotations.NotNull;
import org.onap.policy.common.parameters.annotations.Valid;
import org.onap.policy.models.base.PfAuthorative;
import org.onap.policy.models.base.PfConcept;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfKey;
import org.onap.policy.models.base.PfReferenceKey;
import org.onap.policy.models.base.PfUtils;
import org.onap.policy.models.tosca.authorative.concepts.ToscaProperty;
import org.onap.policy.models.tosca.authorative.concepts.ToscaWithToscaProperties;
import org.onap.policy.models.tosca.utils.ToscaUtils;

/**
 * Class to represent JPA TOSCA classes containing TOSCA properties.
 */
@MappedSuperclass
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class JpaToscaWithToscaProperties<T extends ToscaWithToscaProperties> extends JpaToscaEntityType<T>
                implements PfAuthorative<T> {
    @Serial
    private static final long serialVersionUID = -563659852901842616L;

    @ElementCollection
    @Lob
    private Map<@NotNull @NotBlank String, @NotNull @Valid JpaToscaProperty> properties;

    /**
     * The Default Constructor creates a {@link JpaToscaWithToscaProperties} object with a
     * null key.
     */
    protected JpaToscaWithToscaProperties() {
        this(new PfConceptKey());
    }

    /**
     * The Key Constructor creates a {@link JpaToscaWithToscaProperties} object with the
     * given concept key.
     *
     * @param key the key
     */
    protected JpaToscaWithToscaProperties(@NonNull final PfConceptKey key) {
        super(key);
    }

    /**
     * Copy constructor.
     *
     * @param copyConcept the concept to copy from
     */
    protected JpaToscaWithToscaProperties(final JpaToscaWithToscaProperties<T> copyConcept) {
        super(copyConcept);
        this.properties = copyConcept.properties == null ? null : new LinkedHashMap<>(copyConcept.properties);
    }

    /**
     * Authorative constructor.
     *
     * @param authorativeConcept the authorative concept to copy from
     */
    protected JpaToscaWithToscaProperties(final T authorativeConcept) {
        this.fromAuthorative(authorativeConcept);
    }

    @Override
    public void fromAuthorative(final T authorativeConcept) {
        super.fromAuthorative(authorativeConcept);

        // Set properties
        if (authorativeConcept.getProperties() != null) {
            properties = new LinkedHashMap<>();
            for (Entry<String, ToscaProperty> toscaPropertyEntry : authorativeConcept.getProperties().entrySet()) {
                var jpaProperty = new JpaToscaProperty(toscaPropertyEntry.getValue());
                jpaProperty.setKey(new PfReferenceKey(getKey(), toscaPropertyEntry.getKey()));
                properties.put(toscaPropertyEntry.getKey(), jpaProperty);
            }
        }
    }

    @Override
    public T toAuthorative() {
        var tosca = super.toAuthorative();

        tosca.setProperties(PfUtils.mapMap(properties, JpaToscaProperty::toAuthorative));

        return tosca;
    }

    @Override
    public List<PfKey> getKeys() {
        final List<PfKey> keyList = super.getKeys();

        PfUtils.mapMap(properties, property -> keyList.addAll(property.getKeys()));

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

    /**
     * Validates the fields of the object, including its key.
     *
     * @param fieldName name of the field containing this
     * @return the result, or {@code null}
     */
    protected BeanValidationResult validateWithKey(@NonNull String fieldName) {
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
        final JpaToscaWithToscaProperties<T> other = (JpaToscaWithToscaProperties<T>) otherConcept;

        return PfUtils.compareMaps(properties, other.properties);
    }

    /**
     * Get the referenced data types.
     *
     * @return the referenced data types
     */
    public Collection<PfConceptKey> getReferencedDataTypes() {
        if (properties == null) {
            return CollectionUtils.emptyCollection();
        }

        Set<PfConceptKey> referencedDataTypes = new LinkedHashSet<>();

        for (JpaToscaProperty property : properties.values()) {
            referencedDataTypes.add(property.getType());

            if (property.getEntrySchema() != null) {
                referencedDataTypes.add(property.getEntrySchema().getType());
            }
        }

        referencedDataTypes.removeAll(ToscaUtils.getPredefinedDataTypes());

        return referencedDataTypes;
    }
}
