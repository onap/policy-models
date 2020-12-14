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

import java.util.ArrayList;
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
import org.onap.policy.models.base.PfAuthorative;
import org.onap.policy.models.base.PfConcept;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfKey;
import org.onap.policy.models.base.PfReferenceKey;
import org.onap.policy.models.base.PfUtils;
import org.onap.policy.models.base.Validation;
import org.onap.policy.models.tosca.authorative.concepts.ToscaDataType;
import org.onap.policy.models.tosca.authorative.concepts.ToscaProperty;
import org.onap.policy.models.tosca.utils.ToscaUtils;

/**
 * Class to represent custom data type in TOSCA definition.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 * @author Liam Fallon (liam.fallon@est.tech)
 */
@Entity
@Table(name = "ToscaDataType")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Data
@EqualsAndHashCode(callSuper = true)
public class JpaToscaDataType extends JpaToscaEntityType<ToscaDataType> implements PfAuthorative<ToscaDataType> {
    private static final long serialVersionUID = -3922690413436539164L;

    @ElementCollection
    private List<JpaToscaConstraint> constraints;

    @ElementCollection
    @Lob
    private Map<String, JpaToscaProperty> properties;

    /**
     * The Default Constructor creates a {@link JpaToscaDataType} object with a null key.
     */
    public JpaToscaDataType() {
        this(new PfConceptKey());
    }

    /**
     * The Key Constructor creates a {@link JpaToscaDataType} object with the given concept key.
     *
     * @param key the key
     */
    public JpaToscaDataType(@NonNull final PfConceptKey key) {
        super(key);
    }

    /**
     * Copy constructor.
     *
     * @param copyConcept the concept to copy from
     */
    public JpaToscaDataType(final JpaToscaDataType copyConcept) {
        super(copyConcept);
        // Constraints are immutable
        this.constraints = (copyConcept.constraints != null ? new ArrayList<>(copyConcept.constraints) : null);
        this.properties = PfUtils.mapMap(copyConcept.properties, JpaToscaProperty::new);
    }

    /**
     * Authorative constructor.
     *
     * @param authorativeConcept the authorative concept to copy from
     */
    public JpaToscaDataType(final ToscaDataType authorativeConcept) {
        this.fromAuthorative(authorativeConcept);
    }

    @Override
    public ToscaDataType toAuthorative() {
        ToscaDataType toscaDataType = new ToscaDataType();
        super.setToscaEntity(toscaDataType);
        super.toAuthorative();

        toscaDataType.setConstraints(PfUtils.mapList(constraints, JpaToscaConstraint::toAuthorative));
        toscaDataType.setProperties(PfUtils.mapMap(properties, JpaToscaProperty::toAuthorative));

        return toscaDataType;
    }

    @Override
    public void fromAuthorative(final ToscaDataType toscaDataType) {
        super.fromAuthorative(toscaDataType);

        constraints = PfUtils.mapList(toscaDataType.getConstraints(), JpaToscaConstraint::newInstance);

        if (toscaDataType.getProperties() != null) {
            properties = new LinkedHashMap<>();
            for (Entry<String, ToscaProperty> toscaPropertyEntry : toscaDataType.getProperties().entrySet()) {
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
    public BeanValidationResult validate(@NonNull final String fieldName) {
        BeanValidationResult result = super.validate(fieldName);

        Validation.validateItemsNotNull(result, "constraints", constraints);
        Validation.validateItems(result, "properties", properties, true);

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

        final JpaToscaDataType other = (JpaToscaDataType) otherConcept;
        int result = super.compareTo(other);
        if (result != 0) {
            return result;
        }

        result = PfUtils.compareCollections(constraints, other.constraints);
        if (result != 0) {
            return result;
        }

        return PfUtils.compareMaps(properties, other.properties);
    }

    /**
     * Get the data types referenced in a data type.
     *
     * @return the data types referenced in a data type
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
