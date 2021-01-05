/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2020 Nordix Foundation.
 * Modifications Copyright (C) 2020-2021 AT&T Intellectual Property. All rights reserved.
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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Lob;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.apache.commons.collections4.CollectionUtils;
import org.onap.policy.common.parameters.BeanValidationResult;
import org.onap.policy.common.parameters.annotations.NotNull;
import org.onap.policy.common.parameters.annotations.Valid;
import org.onap.policy.models.base.PfAuthorative;
import org.onap.policy.models.base.PfConcept;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfKey;
import org.onap.policy.models.base.PfReferenceKey;
import org.onap.policy.models.base.PfUtils;
import org.onap.policy.models.tosca.authorative.concepts.ToscaCapabilityType;
import org.onap.policy.models.tosca.authorative.concepts.ToscaProperty;
import org.onap.policy.models.tosca.utils.ToscaUtils;

/**
 * Class to represent the capability type in TOSCA definition.
 */

@Entity
@Table(name = "ToscaCapabilityType")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Data
@EqualsAndHashCode(callSuper = true)
public class JpaToscaCapabilityType extends JpaToscaEntityType<ToscaCapabilityType>
        implements PfAuthorative<ToscaCapabilityType> {
    private static final long serialVersionUID = -563659852901842616L;

    @ElementCollection
    @Lob
    private Map<@NotNull String, @NotNull @Valid JpaToscaProperty> properties;

    /**
     * The Default Constructor creates a {@link JpaToscaCapabilityType} object with a null key.
     */
    public JpaToscaCapabilityType() {
        this(new PfConceptKey());
    }

    /**
     * The Key Constructor creates a {@link JpaToscaCapabilityType} object with the given concept key.
     *
     * @param key the key
     */
    public JpaToscaCapabilityType(@NonNull final PfConceptKey key) {
        super(key);
    }

    /**
     * Copy constructor.
     *
     * @param copyConcept the concept to copy from
     */
    public JpaToscaCapabilityType(final JpaToscaCapabilityType copyConcept) {
        super(copyConcept);
        this.properties = copyConcept.properties == null ? null : new LinkedHashMap<>(copyConcept.properties);
    }

    /**
     * Authorative constructor.
     *
     * @param authorativeConcept the authorative concept to copy from
     */
    public JpaToscaCapabilityType(final ToscaCapabilityType authorativeConcept) {
        this.fromAuthorative(authorativeConcept);
    }

    @Override
    public ToscaCapabilityType toAuthorative() {
        ToscaCapabilityType toscaCapabilityType = new ToscaCapabilityType();
        super.setToscaEntity(toscaCapabilityType);
        super.toAuthorative();

        toscaCapabilityType.setProperties(PfUtils.mapMap(properties, JpaToscaProperty::toAuthorative));

        return toscaCapabilityType;
    }

    @Override
    public void fromAuthorative(final ToscaCapabilityType toscaCapabilityType) {
        super.fromAuthorative(toscaCapabilityType);

        // Set properties
        if (toscaCapabilityType.getProperties() != null) {
            properties = new LinkedHashMap<>();
            for (Entry<String, ToscaProperty> toscaPropertyEntry : toscaCapabilityType.getProperties().entrySet()) {
                JpaToscaProperty jpaProperty = new JpaToscaProperty(toscaPropertyEntry.getValue());
                jpaProperty.setKey(new PfReferenceKey(getKey(), toscaPropertyEntry.getKey()));
                properties.put(toscaPropertyEntry.getKey(), jpaProperty);
            }
        }
    }

    @Override
    public List<PfKey> getKeys() {
        final List<PfKey> keyList = super.getKeys();

        PfUtils.mapMap(properties, property -> keyList.addAll(property.getKeys()));


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
    public BeanValidationResult validate(@NonNull String fieldName) {
        BeanValidationResult result = super.validate(fieldName);

        validateKeyVersionNotNull(result, "key", getKey());

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

        final JpaToscaCapabilityType other = (JpaToscaCapabilityType) otherConcept;
        int result = super.compareTo(other);
        if (result != 0) {
            return result;
        }

        return PfUtils.compareMaps(properties, other.properties);
    }

    /**
     * Get the data types referenced in a capability type.
     *
     * @return the data types referenced in a capability type
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
