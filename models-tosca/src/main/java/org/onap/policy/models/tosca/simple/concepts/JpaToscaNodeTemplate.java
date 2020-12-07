/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2020 Nordix Foundation.
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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.CascadeType;
import javax.persistence.Column;
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
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.onap.policy.common.utils.coder.YamlJsonTranslator;
import org.onap.policy.models.base.PfAuthorative;
import org.onap.policy.models.base.PfConcept;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfKey;
import org.onap.policy.models.base.PfUtils;
import org.onap.policy.models.base.PfValidationMessage;
import org.onap.policy.models.base.PfValidationResult;
import org.onap.policy.models.base.PfValidationResult.ValidationResult;
import org.onap.policy.models.tosca.authorative.concepts.ToscaCapabilityAssignment;
import org.onap.policy.models.tosca.authorative.concepts.ToscaNodeTemplate;

/**
 * Class to represent the node template in TOSCA definition.
 */
@Entity
@Table(name = "ToscaNodeTemplate")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Data
@EqualsAndHashCode(callSuper = false)
public class JpaToscaNodeTemplate extends JpaToscaEntityType<ToscaNodeTemplate>
    implements PfAuthorative<ToscaNodeTemplate> {
    private static final long serialVersionUID = 1675770231921107988L;

    @Column
    private String type;

    @ElementCollection
    @Lob
    private Map<String, String> properties = new LinkedHashMap<>();

    // formatter:off
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumns(
        {
            @JoinColumn(name = "requirementsName", referencedColumnName = "name"),
            @JoinColumn(name = "requirementsVersion", referencedColumnName = "version")
        }
    )
    private JpaToscaRequirements requirements;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumns(
        {
            @JoinColumn(name = "capabilitiesName", referencedColumnName = "name"),
            @JoinColumn(name = "capabilitiesVersion", referencedColumnName = "version")
        }
    )
    private JpaToscaCapabilityAssignments capabilities;
    // @formatter:on

    /**
     * The Default Constructor creates a {@link JpaToscaNodeTemplate} object with a null key.
     */
    public JpaToscaNodeTemplate() {
        this(new PfConceptKey());
    }

    /**
     * The Key Constructor creates a {@link JpaToscaNodeTemplate} object with the given concept key.
     *
     * @param key the key
     */
    public JpaToscaNodeTemplate(@NonNull final PfConceptKey key) {
        this(key, null);
    }

    /**
     * Copy constructor.
     *
     * @param copyConcept the concept to copy from
     */
    public JpaToscaNodeTemplate(final JpaToscaNodeTemplate copyConcept) {
        super(copyConcept);
        this.type = copyConcept.type;
        this.properties = PfUtils.mapMap(copyConcept.properties, String::new);
        this.requirements =
            (copyConcept.requirements != null ? new JpaToscaRequirements(copyConcept.requirements) : null);
        this.capabilities =
            (copyConcept.capabilities != null ? new JpaToscaCapabilityAssignments(copyConcept.capabilities) : null);
    }

    /**
     * The Key Constructor creates a {@link JpaToscaParameter} object with the given concept key.
     *
     * @param key  the key
     * @param type the node template type
     */
    public JpaToscaNodeTemplate(@NonNull final PfConceptKey key, final String type) {
        super(key);
        this.type = type;
    }

    /**
     * Authorative constructor.
     *
     * @param authorativeConcept the authorative concept to copy from
     */
    public JpaToscaNodeTemplate(final ToscaNodeTemplate authorativeConcept) {
        this.fromAuthorative(authorativeConcept);
    }

    @Override
    public ToscaNodeTemplate toAuthorative() {
        ToscaNodeTemplate toscaNodeTemplate = new ToscaNodeTemplate();
        super.setToscaEntity(toscaNodeTemplate);
        super.toAuthorative();

        toscaNodeTemplate.setType(type);

        if (properties != null) {
            Map<String, Object> propertiesMap = new LinkedHashMap<>();

            for (Map.Entry<String, String> entry : properties.entrySet()) {
                propertiesMap.put(entry.getKey(), new YamlJsonTranslator().fromYaml(entry.getValue(), Object.class));
            }

            toscaNodeTemplate.setProperties(propertiesMap);
        }

        if (requirements != null) {
            toscaNodeTemplate.setRequirements(requirements.toAuthorative());
        }

        if (capabilities != null) {
            toscaNodeTemplate.setCapabilities(new LinkedHashMap<>());
            List<Map<String, ToscaCapabilityAssignment>> capabilityAssignmentMapList = capabilities.toAuthorative();
            for (Map<String, ToscaCapabilityAssignment> capabilityAssignmentMap : capabilityAssignmentMapList) {
                toscaNodeTemplate.getCapabilities().putAll(capabilityAssignmentMap);
            }
        }

        return toscaNodeTemplate;
    }

    @Override
    public void fromAuthorative(ToscaNodeTemplate toscaNodeTemplate) {
        super.fromAuthorative(toscaNodeTemplate);

        type = toscaNodeTemplate.getType();

        if (toscaNodeTemplate.getProperties() != null) {
            properties = new LinkedHashMap<>();
            for (Map.Entry<String, Object> toscaPropertyEntry : toscaNodeTemplate.getProperties().entrySet()) {
                String jpaProperty = new YamlJsonTranslator().toYaml(toscaPropertyEntry.getValue());
                properties.put(toscaPropertyEntry.getKey(), jpaProperty);
            }
        }

        if (toscaNodeTemplate.getRequirements() != null) {
            requirements = new JpaToscaRequirements();
            requirements.fromAuthorative(toscaNodeTemplate.getRequirements());
        }

        if (toscaNodeTemplate.getCapabilities() != null) {
            capabilities = new JpaToscaCapabilityAssignments();
            capabilities.fromAuthorative(Collections.singletonList(toscaNodeTemplate.getCapabilities()));
        }

    }

    @Override
    public List<PfKey> getKeys() {
        final List<PfKey> keyList = super.getKeys();

        if (requirements != null) {
            keyList.addAll(requirements.getKeys());
        }

        if (capabilities != null) {
            keyList.addAll(capabilities.getKeys());
        }

        return keyList;
    }

    @Override
    public void clean() {
        super.clean();

        type.trim();

        if (properties != null) {
            for (String property : properties.values()) {
                property.trim();
            }
        }

        if (requirements != null) {
            requirements.clean();
        }

        if (capabilities != null) {
            capabilities.clean();
        }
    }

    @Override
    public PfValidationResult validate(final PfValidationResult resultIn) {
        PfValidationResult result = super.validate(resultIn);

        if (StringUtils.isBlank(type)) {
            result.addValidationMessage(new PfValidationMessage(getKey(), this.getClass(), ValidationResult.INVALID,
                "node template type may not be null"));
        }

        if (properties != null) {
            result = validateProperties(result);
        }

        if (requirements != null) {
            result = requirements.validate(result);
        }

        if (capabilities != null) {
            result = capabilities.validate(result);
        }

        return result;
    }

    /**
     * Validate the properties.
     *
     * @param resultIn The result of validations up to now
     * @return the validation result
     */
    private PfValidationResult validateProperties(final PfValidationResult resultIn) {
        PfValidationResult result = resultIn;

        for (String property : properties.values()) {
            if (property == null) {
                result.addValidationMessage(new PfValidationMessage(getKey(), this.getClass(), ValidationResult.INVALID,
                    "topology template property may not be null "));
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
            return getClass().getName().compareTo(otherConcept.getClass().getName());
        }

        final JpaToscaNodeTemplate other = (JpaToscaNodeTemplate) otherConcept;
        if (!super.equals(other)) {
            return super.compareTo(other);
        }

        int result = type.compareTo(other.type);
        if (result != 0) {
            return result;
        }

        result = PfUtils.compareObjects(properties, other.properties);
        if (result != 0) {
            return result;
        }

        result = ObjectUtils.compare(requirements, other.requirements);
        if (result != 0) {
            return result;
        }

        return ObjectUtils.compare(capabilities, other.capabilities);
    }
}
