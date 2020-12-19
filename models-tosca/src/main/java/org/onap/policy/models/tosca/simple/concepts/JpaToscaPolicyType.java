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
import org.onap.policy.common.parameters.annotations.Entries;
import org.onap.policy.common.parameters.annotations.Items;
import org.onap.policy.common.parameters.annotations.NotNull;
import org.onap.policy.common.parameters.annotations.Valid;
import org.onap.policy.models.base.PfAuthorative;
import org.onap.policy.models.base.PfConcept;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfKey;
import org.onap.policy.models.base.PfReferenceKey;
import org.onap.policy.models.base.PfUtils;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyType;
import org.onap.policy.models.tosca.authorative.concepts.ToscaProperty;
import org.onap.policy.models.tosca.utils.ToscaUtils;

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
public class JpaToscaPolicyType extends JpaToscaEntityType<ToscaPolicyType> implements PfAuthorative<ToscaPolicyType> {
    private static final long serialVersionUID = -563659852901842616L;

    @ElementCollection
    @Lob
    @Entries(key = @Items(notNull = {@NotNull}), value = @Items(notNull = {@NotNull}, valid = {@Valid}))
    private Map<String, JpaToscaProperty> properties;

    @ElementCollection
    @Items(notNull = {@NotNull}, valid = {@Valid})
    private List<PfConceptKey> targets;

    @ElementCollection
    @Items(notNull = {@NotNull}, valid = {@Valid})
    private List<JpaToscaTrigger> triggers;

    /**
     * The Default Constructor creates a {@link JpaToscaPolicyType} object with a null key.
     */
    public JpaToscaPolicyType() {
        this(new PfConceptKey());
    }

    /**
     * The Key Constructor creates a {@link JpaToscaPolicyType} object with the given concept key.
     *
     * @param key the key
     */
    public JpaToscaPolicyType(@NonNull final PfConceptKey key) {
        super(key);
    }

    /**
     * Copy constructor.
     *
     * @param copyConcept the concept to copy from
     */
    public JpaToscaPolicyType(final JpaToscaPolicyType copyConcept) {
        super(copyConcept);
        this.properties = PfUtils.mapMap(copyConcept.properties, JpaToscaProperty::new);
        this.targets = PfUtils.mapList(copyConcept.targets, PfConceptKey::new);
        this.triggers = PfUtils.mapList(copyConcept.triggers, JpaToscaTrigger::new);
    }

    /**
     * Authorative constructor.
     *
     * @param authorativeConcept the authorative concept to copy from
     */
    public JpaToscaPolicyType(final ToscaPolicyType authorativeConcept) {
        this.fromAuthorative(authorativeConcept);
    }

    @Override
    public ToscaPolicyType toAuthorative() {
        ToscaPolicyType toscaPolicyType = new ToscaPolicyType();
        super.setToscaEntity(toscaPolicyType);
        super.toAuthorative();

        toscaPolicyType.setProperties(PfUtils.mapMap(properties, JpaToscaProperty::toAuthorative));

        return toscaPolicyType;
    }

    @Override
    public void fromAuthorative(final ToscaPolicyType toscaPolicyType) {
        super.fromAuthorative(toscaPolicyType);

        // Set properties
        if (toscaPolicyType.getProperties() != null) {
            properties = new LinkedHashMap<>();
            for (Entry<String, ToscaProperty> toscaPropertyEntry : toscaPolicyType.getProperties().entrySet()) {
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

        if (targets != null) {
            keyList.addAll(targets);
        }

        if (triggers != null) {
            for (JpaToscaTrigger trigger : triggers) {
                keyList.addAll(trigger.getKeys());
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

        if (targets != null) {
            for (PfConceptKey target : targets) {
                target.clean();
            }
        }

        if (triggers != null) {
            for (JpaToscaTrigger trigger : triggers) {
                trigger.clean();
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

        final JpaToscaPolicyType other = (JpaToscaPolicyType) otherConcept;
        int result = super.compareTo(other);
        if (result != 0) {
            return result;
        }

        result = PfUtils.compareMaps(properties, other.properties);
        if (result != 0) {
            return result;
        }

        result = PfUtils.compareCollections(targets, other.targets);
        if (result != 0) {
            return result;
        }

        return PfUtils.compareCollections(triggers, other.triggers);
    }

    /**
     * Get the data types referenced in a policy type.
     *
     * @return the data types referenced in a policy type
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
