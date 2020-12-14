/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2020 Nordix Foundation.
 * Modifications Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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
import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.onap.policy.common.parameters.BeanValidationResult;
import org.onap.policy.models.base.PfAuthorative;
import org.onap.policy.models.base.PfConcept;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfKey;
import org.onap.policy.models.base.PfReferenceKey;
import org.onap.policy.models.base.PfUtils;
import org.onap.policy.models.base.Validation;
import org.onap.policy.models.tosca.authorative.concepts.ToscaNodeType;
import org.onap.policy.models.tosca.authorative.concepts.ToscaProperty;
import org.onap.policy.models.tosca.utils.ToscaUtils;

/**
 * Class to represent the node type in TOSCA definition.
 */

@Entity
@Table(name = "ToscaNodeType")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Data
@EqualsAndHashCode(callSuper = true)
public class JpaToscaNodeType extends JpaToscaEntityType<ToscaNodeType> implements PfAuthorative<ToscaNodeType> {
    private static final long serialVersionUID = -563659852901842616L;

    @ElementCollection
    @Lob
    private Map<String, JpaToscaProperty> properties;


    // formatter:off
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumns({@JoinColumn(name = "requirementsName", referencedColumnName = "name"),
        @JoinColumn(name = "requirementsVersion", referencedColumnName = "version")})
    // @formatter:on
    private JpaToscaRequirements requirements;

    /**
     * The Default Constructor creates a {@link JpaToscaNodeType} object with a null key.
     */
    public JpaToscaNodeType() {
        this(new PfConceptKey());
    }

    /**
     * The Key Constructor creates a {@link JpaToscaNodeType} object with the given concept key.
     *
     * @param key the key
     */
    public JpaToscaNodeType(@NonNull final PfConceptKey key) {
        super(key);
    }

    /**
     * Copy constructor.
     *
     * @param copyConcept the concept to copy from
     */
    public JpaToscaNodeType(final JpaToscaNodeType copyConcept) {
        super(copyConcept);
        this.properties = PfUtils.mapMap(copyConcept.properties, JpaToscaProperty::new);
        this.requirements =
                (copyConcept.requirements != null ? new JpaToscaRequirements(copyConcept.requirements) : null);
    }

    /**
     * Authorative constructor.
     *
     * @param authorativeConcept the authorative concept to copy from
     */
    public JpaToscaNodeType(final ToscaNodeType authorativeConcept) {
        this.fromAuthorative(authorativeConcept);
    }

    @Override
    public ToscaNodeType toAuthorative() {
        ToscaNodeType toscaNodeType = new ToscaNodeType();
        super.setToscaEntity(toscaNodeType);
        super.toAuthorative();

        toscaNodeType.setProperties(PfUtils.mapMap(properties, JpaToscaProperty::toAuthorative));

        if (requirements != null) {
            toscaNodeType.setRequirements(requirements.toAuthorative());
        }

        return toscaNodeType;
    }

    @Override
    public void fromAuthorative(final ToscaNodeType toscaNodeType) {
        super.fromAuthorative(toscaNodeType);

        // Set properties
        if (toscaNodeType.getProperties() != null) {
            properties = new LinkedHashMap<>();
            for (Entry<String, ToscaProperty> toscaPropertyEntry : toscaNodeType.getProperties().entrySet()) {
                JpaToscaProperty jpaProperty = new JpaToscaProperty(toscaPropertyEntry.getValue());
                jpaProperty.setKey(new PfReferenceKey(getKey(), toscaPropertyEntry.getKey()));
                properties.put(toscaPropertyEntry.getKey(), jpaProperty);
            }
        }

        if (toscaNodeType.getRequirements() != null) {
            requirements = new JpaToscaRequirements();
            requirements.fromAuthorative(toscaNodeType.getRequirements());
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

        if (requirements != null) {
            keyList.addAll(requirements.getKeys());
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

        if (requirements != null) {
            requirements.clean();
        }
    }

    @Override
    public BeanValidationResult validate(final String fieldName) {
        BeanValidationResult result = super.validate(fieldName);

        result.addResult(getKey().validateNotNull("key"));

        Validation.validateItems(result, "properties", properties.values(), true);
        Validation.validateItem(result, "requirements", requirements, false);

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

        final JpaToscaNodeType other = (JpaToscaNodeType) otherConcept;
        int result = super.compareTo(other);
        if (result != 0) {
            return result;
        }

        result = PfUtils.compareMaps(properties, other.properties);
        if (result != 0) {
            return result;
        }

        return ObjectUtils.compare(requirements, other.requirements);
    }

    /**
     * Get the data types referenced in a node type.
     *
     * @return the data types referenced in a node type
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
