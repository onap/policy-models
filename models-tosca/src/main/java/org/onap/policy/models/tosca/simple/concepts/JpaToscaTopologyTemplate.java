/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2020, 2023 Nordix Foundation.
 *  Modifications Copyright (C) 2019-2021 AT&T Intellectual Property. All rights reserved.
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

import com.google.gson.annotations.SerializedName;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.io.Serial;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.apache.commons.lang3.ObjectUtils;
import org.onap.policy.common.parameters.annotations.NotBlank;
import org.onap.policy.common.parameters.annotations.NotNull;
import org.onap.policy.common.parameters.annotations.Valid;
import org.onap.policy.models.base.PfAuthorative;
import org.onap.policy.models.base.PfConcept;
import org.onap.policy.models.base.PfKey;
import org.onap.policy.models.base.PfReferenceKey;
import org.onap.policy.models.base.PfUtils;
import org.onap.policy.models.base.validation.annotations.VerifyKey;
import org.onap.policy.models.tosca.authorative.concepts.ToscaNodeTemplate;
import org.onap.policy.models.tosca.authorative.concepts.ToscaParameter;
import org.onap.policy.models.tosca.authorative.concepts.ToscaTopologyTemplate;

/**
 * This class holds a TOSCA topology template. Note: Only the policy specific parts of the TOSCA topology template are
 * implemented.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
@Entity
@Table(name = "ToscaTopologyTemplate")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Data
@EqualsAndHashCode(callSuper = false)
public class JpaToscaTopologyTemplate extends PfConcept implements PfAuthorative<ToscaTopologyTemplate> {
    @Serial
    private static final long serialVersionUID = 8969698734673232603L;

    public static final String DEFAULT_LOCAL_NAME = "ToscaTopologyTemplateSimple";

    @EmbeddedId
    @VerifyKey
    @NotNull
    private PfReferenceKey key;

    @Column(name = "description")
    @NotBlank
    private String description;

    @ElementCollection
    @Lob
    private Map<@NotNull String, @NotNull @Valid JpaToscaParameter> inputs;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "nodeTemplatesName", referencedColumnName = "name")
    @JoinColumn(name = "nodeTemplatessVersion", referencedColumnName = "version")
    @SerializedName("data_types")
    @Valid
    private JpaToscaNodeTemplates nodeTemplates;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "policyName", referencedColumnName = "name")
    @JoinColumn(name = "policyVersion", referencedColumnName = "version")
    @Valid
    private JpaToscaPolicies policies;

    /**
     * The Default Constructor creates a {@link JpaToscaTopologyTemplate} object with a null key.
     */
    public JpaToscaTopologyTemplate() {
        this(new PfReferenceKey(JpaToscaServiceTemplate.DEFAULT_NAME, JpaToscaServiceTemplate.DEFAULT_VERSION,
            DEFAULT_LOCAL_NAME));
    }

    /**
     * The Key Constructor creates a {@link JpaToscaTopologyTemplate} object with the given concept key.
     *
     * @param key the key
     */
    public JpaToscaTopologyTemplate(@NonNull final PfReferenceKey key) {
        this.key = key;
    }

    /**
     * Copy constructor.
     *
     * @param copyConcept the concept to copy from
     */
    public JpaToscaTopologyTemplate(final JpaToscaTopologyTemplate copyConcept) {
        super(copyConcept);
        this.key = new PfReferenceKey(copyConcept.key);
        this.description = copyConcept.description;
        this.inputs = PfUtils.mapMap(copyConcept.inputs, JpaToscaParameter::new);
        this.nodeTemplates =
            (copyConcept.nodeTemplates != null ? new JpaToscaNodeTemplates(copyConcept.nodeTemplates) : null);
        this.policies = (copyConcept.policies != null ? new JpaToscaPolicies(copyConcept.policies) : null);
    }

    /**
     * Authorative constructor.
     *
     * @param authorativeConcept the authorative concept to copy from
     */
    public JpaToscaTopologyTemplate(final ToscaTopologyTemplate authorativeConcept) {
        this.fromAuthorative(authorativeConcept);
    }

    @Override
    public ToscaTopologyTemplate toAuthorative() {
        final var toscaTopologyTemplate = new ToscaTopologyTemplate();

        toscaTopologyTemplate.setDescription(description);

        if (inputs != null) {
            Map<String, ToscaParameter> inputMap = new LinkedHashMap<>();

            for (Map.Entry<String, JpaToscaParameter> entry : inputs.entrySet()) {
                inputMap.put(entry.getKey(), entry.getValue().toAuthorative());
            }

            toscaTopologyTemplate.setInputs(inputMap);
        }

        if (nodeTemplates != null) {
            toscaTopologyTemplate.setNodeTemplates(new LinkedHashMap<>());
            List<Map<String, ToscaNodeTemplate>> nodeTemplateMapList = nodeTemplates.toAuthorative();
            for (Map<String, ToscaNodeTemplate> nodeTemplateMap : nodeTemplateMapList) {
                toscaTopologyTemplate.getNodeTemplates().putAll(nodeTemplateMap);
            }
        }

        if (policies != null) {
            toscaTopologyTemplate.setPolicies(policies.toAuthorative());
        }

        return toscaTopologyTemplate;
    }

    @Override
    public void fromAuthorative(ToscaTopologyTemplate toscaTopologyTemplate) {
        description = toscaTopologyTemplate.getDescription();

        if (toscaTopologyTemplate.getInputs() != null) {
            inputs = new LinkedHashMap<>();
            for (Map.Entry<String, ToscaParameter> toscaInputEntry : toscaTopologyTemplate.getInputs().entrySet()) {
                var jpaInput = new JpaToscaParameter(toscaInputEntry.getValue());
                jpaInput.setKey(new PfReferenceKey(getKey(), toscaInputEntry.getKey()));
                inputs.put(toscaInputEntry.getKey(), jpaInput);
            }
        }

        if (toscaTopologyTemplate.getNodeTemplates() != null) {
            nodeTemplates = new JpaToscaNodeTemplates();
            nodeTemplates.fromAuthorative(Collections.singletonList(toscaTopologyTemplate.getNodeTemplates()));
        }

        if (toscaTopologyTemplate.getPolicies() != null) {
            policies = new JpaToscaPolicies();
            policies.fromAuthorative(toscaTopologyTemplate.getPolicies());
        }
    }

    @Override
    public List<PfKey> getKeys() {
        final List<PfKey> keyList = getKey().getKeys();

        if (inputs != null) {
            for (JpaToscaParameter input : inputs.values()) {
                keyList.addAll(input.getKeys());
            }
        }

        if (nodeTemplates != null) {
            keyList.addAll(nodeTemplates.getKeys());
        }

        if (policies != null) {
            keyList.addAll(policies.getKeys());
        }

        return keyList;
    }

    @Override
    public void clean() {
        key.clean();

        description = (description != null ? description.trim() : null);

        if (inputs != null) {
            for (JpaToscaParameter input : inputs.values()) {
                input.clean();
            }
        }

        if (nodeTemplates != null) {
            nodeTemplates.clean();
        }

        if (policies != null) {
            policies.clean();
        }
    }

    @Override
    public int compareTo(final PfConcept otherConcept) {
        int result = compareToWithoutEntities(otherConcept);
        if (result != 0) {
            return result;
        }

        final JpaToscaTopologyTemplate other = (JpaToscaTopologyTemplate) otherConcept;

        result = PfUtils.compareObjects(inputs, other.inputs);
        if (result != 0) {
            return result;
        }

        result = ObjectUtils.compare(nodeTemplates, other.nodeTemplates);
        if (result != 0) {
            return result;
        }

        return ObjectUtils.compare(policies, other.policies);
    }

    /**
     * Compare this topology template to another topology template, ignoring contained entities.
     *
     * @param otherConcept the other topology template
     * @return the result of the comparison
     */
    public int compareToWithoutEntities(final PfConcept otherConcept) {
        if (otherConcept == null) {
            return -1;
        }
        if (this == otherConcept) {
            return 0;
        }
        if (getClass() != otherConcept.getClass()) {
            return getClass().getName().compareTo(otherConcept.getClass().getName());
        }

        final JpaToscaTopologyTemplate other = (JpaToscaTopologyTemplate) otherConcept;
        if (!key.equals(other.key)) {
            return key.compareTo(other.key);
        }

        return ObjectUtils.compare(description, other.description);
    }
}
