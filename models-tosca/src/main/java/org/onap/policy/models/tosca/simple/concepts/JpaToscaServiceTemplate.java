/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2020 Nordix Foundation.
 *  Modifications Copyright (C) 2019-2020 AT&T Intellectual Property. All rights reserved.
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
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.apache.commons.lang3.ObjectUtils;
import org.onap.policy.common.parameters.BeanValidationResult;
import org.onap.policy.models.base.PfAuthorative;
import org.onap.policy.models.base.PfConcept;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfKey;
import org.onap.policy.models.tosca.authorative.concepts.ToscaCapabilityType;
import org.onap.policy.models.tosca.authorative.concepts.ToscaDataType;
import org.onap.policy.models.tosca.authorative.concepts.ToscaNodeType;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyType;
import org.onap.policy.models.tosca.authorative.concepts.ToscaRelationshipType;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;

/**
 * This class holds a full TOSCA service template. Note: Only the policy specific parts of the TOSCA service template
 * are implemented.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */

@Entity
@Table(name = "ToscaServiceTemplate")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Data
@EqualsAndHashCode(callSuper = true)
public class JpaToscaServiceTemplate extends JpaToscaEntityType<ToscaServiceTemplate>
        implements PfAuthorative<ToscaServiceTemplate> {
    private static final long serialVersionUID = 8084846046148349401L;

    public static final String DEFAULT_TOSCA_DEFINTIONS_VERISON = "tosca_simple_yaml_1_1_0";
    public static final String DEFAULT_NAME = "ToscaServiceTemplateSimple";
    public static final String DEFAULT_VERSION = "1.0.0";

    // @formatter:off
    @Column
    @SerializedName("tosca_definitions_version")
    private String toscaDefinitionsVersion;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumns(
            {
                @JoinColumn(name = "dataTypesName",    referencedColumnName = "name"),
                @JoinColumn(name = "dataTypesVersion", referencedColumnName = "version")
            }
        )
    @SerializedName("data_types")
    private JpaToscaDataTypes dataTypes;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumns(
            {
                @JoinColumn(name = "capabilityTypesName",    referencedColumnName = "name"),
                @JoinColumn(name = "capabilityTypesVersion", referencedColumnName = "version")
            }
        )
    @SerializedName("capability_types")
    private JpaToscaCapabilityTypes capabilityTypes;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumns(
            {
                @JoinColumn(name = "relationshipTypesName",    referencedColumnName = "name"),
                @JoinColumn(name = "relationshipTypesVersion", referencedColumnName = "version")
            }
        )
    @SerializedName("relationship_types")
    private JpaToscaRelationshipTypes relationshipTypes;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumns(
            {
                @JoinColumn(name = "nodeTypesName",    referencedColumnName = "name"),
                @JoinColumn(name = "nodeTypesVersion", referencedColumnName = "version")
            }
        )
    @SerializedName("node_types")
    private JpaToscaNodeTypes nodeTypes;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumns(
            {
                @JoinColumn(name = "policyTypesName",    referencedColumnName = "name"),
                @JoinColumn(name = "policyTypesVersion", referencedColumnName = "version")
            }
        )
    @SerializedName("policy_types")
    private JpaToscaPolicyTypes policyTypes;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumns(
            {
                @JoinColumn(name = "topologyTemplateParentKeyName",    referencedColumnName = "parentKeyName"),
                @JoinColumn(name = "topologyTemplateParentKeyVersion", referencedColumnName = "parentKeyVersion"),
                @JoinColumn(name = "topologyTemplateParentLocalName",  referencedColumnName = "parentLocalName"),
                @JoinColumn(name = "topologyTemplateLocalName",        referencedColumnName = "localName")
            }
        )
    @SerializedName("topology_template")
    private JpaToscaTopologyTemplate topologyTemplate;
    // @formatter:on

    /**
     * The Default Constructor creates a {@link JpaToscaServiceTemplate} object with a null key.
     */
    public JpaToscaServiceTemplate() {
        this(new PfConceptKey(DEFAULT_NAME, DEFAULT_VERSION));
    }

    /**
     * The Key Constructor creates a {@link JpaToscaServiceTemplate} object with the given concept key.
     *
     * @param key the key
     */
    public JpaToscaServiceTemplate(@NonNull final PfConceptKey key) {
        this(key, DEFAULT_TOSCA_DEFINTIONS_VERISON);
    }

    /**
     * The full constructor creates a {@link JpaToscaServiceTemplate} object with all mandatory parameters.
     *
     * @param key the key
     * @param toscaDefinitionsVersion the TOSCA version string
     */
    public JpaToscaServiceTemplate(@NonNull final PfConceptKey key, @NonNull final String toscaDefinitionsVersion) {
        super(key);
        this.toscaDefinitionsVersion = toscaDefinitionsVersion;
    }

    /**
     * Copy constructor.
     *
     * @param copyConcept the concept to copy from
     */
    public JpaToscaServiceTemplate(final JpaToscaServiceTemplate copyConcept) {
        super(copyConcept);
        this.toscaDefinitionsVersion = copyConcept.toscaDefinitionsVersion;
        this.dataTypes = (copyConcept.dataTypes != null ? new JpaToscaDataTypes(copyConcept.dataTypes) : null);
        this.capabilityTypes =
                (copyConcept.capabilityTypes != null ? new JpaToscaCapabilityTypes(copyConcept.capabilityTypes) : null);
        this.relationshipTypes =
                (copyConcept.relationshipTypes != null ? new JpaToscaRelationshipTypes(copyConcept.relationshipTypes)
                        : null);
        this.nodeTypes = (copyConcept.nodeTypes != null ? new JpaToscaNodeTypes(copyConcept.nodeTypes) : null);
        this.policyTypes = (copyConcept.policyTypes != null ? new JpaToscaPolicyTypes(copyConcept.policyTypes) : null);
        this.topologyTemplate =
                (copyConcept.topologyTemplate != null ? new JpaToscaTopologyTemplate(copyConcept.topologyTemplate)
                        : null);
    }

    /**
     * Authorative constructor.
     *
     * @param authorativeConcept the authorative concept to copy from
     */
    public JpaToscaServiceTemplate(final ToscaServiceTemplate authorativeConcept) {
        this.fromAuthorative(authorativeConcept);
    }

    @Override
    public ToscaServiceTemplate toAuthorative() {
        final ToscaServiceTemplate toscaServiceTemplate = new ToscaServiceTemplate();

        super.setToscaEntity(toscaServiceTemplate);
        super.toAuthorative();

        toscaServiceTemplate.setToscaDefinitionsVersion(toscaDefinitionsVersion);

        if (dataTypes != null) {
            toscaServiceTemplate.setDataTypes(new LinkedHashMap<>());
            List<Map<String, ToscaDataType>> dataTypeMapList = dataTypes.toAuthorative();
            for (Map<String, ToscaDataType> dataTypeMap : dataTypeMapList) {
                toscaServiceTemplate.getDataTypes().putAll(dataTypeMap);
            }
        }

        if (capabilityTypes != null) {
            toscaServiceTemplate.setCapabilityTypes(new LinkedHashMap<>());
            List<Map<String, ToscaCapabilityType>> capabilityTypeMapList = capabilityTypes.toAuthorative();
            for (Map<String, ToscaCapabilityType> capabilityTypeMap : capabilityTypeMapList) {
                toscaServiceTemplate.getCapabilityTypes().putAll(capabilityTypeMap);
            }
        }

        if (relationshipTypes != null) {
            toscaServiceTemplate.setRelationshipTypes(new LinkedHashMap<>());
            List<Map<String, ToscaRelationshipType>> relationshipTypeMapList = relationshipTypes.toAuthorative();
            for (Map<String, ToscaRelationshipType> relationshipTypeMap : relationshipTypeMapList) {
                toscaServiceTemplate.getRelationshipTypes().putAll(relationshipTypeMap);
            }
        }

        if (nodeTypes != null) {
            toscaServiceTemplate.setNodeTypes(new LinkedHashMap<>());
            List<Map<String, ToscaNodeType>> nodeTypeMapList = nodeTypes.toAuthorative();
            for (Map<String, ToscaNodeType> nodeTypeMap : nodeTypeMapList) {
                toscaServiceTemplate.getNodeTypes().putAll(nodeTypeMap);
            }
        }

        if (policyTypes != null) {
            toscaServiceTemplate.setPolicyTypes(new LinkedHashMap<>());
            List<Map<String, ToscaPolicyType>> policyTypeMapList = policyTypes.toAuthorative();
            for (Map<String, ToscaPolicyType> policyTypeMap : policyTypeMapList) {
                toscaServiceTemplate.getPolicyTypes().putAll(policyTypeMap);
            }
        }

        if (topologyTemplate != null) {
            toscaServiceTemplate.setToscaTopologyTemplate(topologyTemplate.toAuthorative());
        }

        return toscaServiceTemplate;
    }

    @Override
    public void fromAuthorative(ToscaServiceTemplate toscaServiceTemplate) {
        super.fromAuthorative(toscaServiceTemplate);

        if (PfKey.NULL_KEY_NAME.equals(getKey().getName())) {
            getKey().setName(DEFAULT_NAME);
        }

        if (PfKey.NULL_KEY_VERSION.equals(getKey().getVersion())) {
            getKey().setVersion(DEFAULT_VERSION);
        }

        toscaDefinitionsVersion = toscaServiceTemplate.getToscaDefinitionsVersion();

        if (toscaServiceTemplate.getDataTypes() != null) {
            dataTypes = new JpaToscaDataTypes();
            dataTypes.fromAuthorative(Collections.singletonList(toscaServiceTemplate.getDataTypes()));
        }

        if (toscaServiceTemplate.getCapabilityTypes() != null) {
            capabilityTypes = new JpaToscaCapabilityTypes();
            capabilityTypes.fromAuthorative(Collections.singletonList(toscaServiceTemplate.getCapabilityTypes()));
        }

        if (toscaServiceTemplate.getRelationshipTypes() != null) {
            relationshipTypes = new JpaToscaRelationshipTypes();
            relationshipTypes.fromAuthorative(Collections.singletonList(toscaServiceTemplate.getRelationshipTypes()));
        }

        if (toscaServiceTemplate.getNodeTypes() != null) {
            nodeTypes = new JpaToscaNodeTypes();
            nodeTypes.fromAuthorative(Collections.singletonList(toscaServiceTemplate.getNodeTypes()));
        }

        if (toscaServiceTemplate.getPolicyTypes() != null) {
            policyTypes = new JpaToscaPolicyTypes();
            policyTypes.fromAuthorative(Collections.singletonList(toscaServiceTemplate.getPolicyTypes()));
        }

        if (toscaServiceTemplate.getToscaTopologyTemplate() != null) {
            topologyTemplate = new JpaToscaTopologyTemplate();
            topologyTemplate.fromAuthorative(toscaServiceTemplate.getToscaTopologyTemplate());
        }
    }

    @Override
    public List<PfKey> getKeys() {
        final List<PfKey> keyList = super.getKeys();

        if (dataTypes != null) {
            keyList.addAll(dataTypes.getKeys());
        }

        if (capabilityTypes != null) {
            keyList.addAll(capabilityTypes.getKeys());
        }

        if (relationshipTypes != null) {
            keyList.addAll(relationshipTypes.getKeys());
        }

        if (nodeTypes != null) {
            keyList.addAll(nodeTypes.getKeys());
        }

        if (policyTypes != null) {
            keyList.addAll(policyTypes.getKeys());
        }

        if (topologyTemplate != null) {
            keyList.addAll(topologyTemplate.getKeys());
        }

        return keyList;
    }

    @Override
    public void clean() {
        toscaDefinitionsVersion = toscaDefinitionsVersion.trim();

        if (dataTypes != null) {
            dataTypes.clean();
        }

        if (capabilityTypes != null) {
            capabilityTypes.clean();
        }

        if (relationshipTypes != null) {
            relationshipTypes.clean();
        }

        if (nodeTypes != null) {
            nodeTypes.clean();
        }

        if (policyTypes != null) {
            policyTypes.clean();
        }

        if (topologyTemplate != null) {
            topologyTemplate.clean();
        }
    }

    @Override
    public BeanValidationResult validate(String fieldName) {
        BeanValidationResult result = super.validate(fieldName);

        result.addResult(validateNotBlank("toscaDefinitionsVersion", toscaDefinitionsVersion, true));

        validateOptional(result, "dataTypes", dataTypes);
        validateOptional(result, "capabilityTypes", capabilityTypes);
        validateOptional(result, "relationshipTypes", relationshipTypes);
        validateOptional(result, "nodeTypes", nodeTypes);
        validateOptional(result, "policyTypes", policyTypes);
        validateOptional(result, "topologyTemplate", topologyTemplate);

        // No point in validating cross references if the structure of the individual parts are not valid
        if (!result.isValid()) {
            return result;
        }

        validateReferencedDataTypes(result);

        validatePolicyTypesInPolicies(result);

        return result;
    }

    @Override
    public int compareTo(final PfConcept otherConcept) {
        int result = compareToWithoutEntities(otherConcept);
        if (result != 0) {
            return result;
        }

        final JpaToscaServiceTemplate other = (JpaToscaServiceTemplate) otherConcept;

        result = ObjectUtils.compare(dataTypes, other.dataTypes);
        if (result != 0) {
            return result;
        }

        result = ObjectUtils.compare(capabilityTypes, other.capabilityTypes);
        if (result != 0) {
            return result;
        }

        result = ObjectUtils.compare(relationshipTypes, other.relationshipTypes);
        if (result != 0) {
            return result;
        }

        result = ObjectUtils.compare(nodeTypes, other.nodeTypes);
        if (result != 0) {
            return result;
        }

        result = ObjectUtils.compare(policyTypes, other.policyTypes);
        if (result != 0) {
            return result;
        }

        return ObjectUtils.compare(topologyTemplate, other.topologyTemplate);
    }

    /**
     * Compare this service template to another service template, ignoring contained entitites.
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

        final JpaToscaServiceTemplate other = (JpaToscaServiceTemplate) otherConcept;
        if (!super.equals(other)) {
            return super.compareTo(other);
        }

        return ObjectUtils.compare(toscaDefinitionsVersion, other.toscaDefinitionsVersion);
    }

    /**
     * Validate that all data types referenced in policy types exist.
     *
     * @param result the validation result object to use for the validation result
     * @param result where the results are added
     */
    private void validateReferencedDataTypes(final BeanValidationResult result) {
        if (policyTypes == null) {
            return;
        }

        if (dataTypes != null) {
            for (JpaToscaDataType dataType : dataTypes.getAll(null)) {
                validateReferencedDataTypesExists(dataType.getReferencedDataTypes(), result);
            }
        }

        for (JpaToscaPolicyType policyType : policyTypes.getAll(null)) {
            validateReferencedDataTypesExists(policyType.getReferencedDataTypes(), result);
        }
    }

    /**
     * Validate that the referenced data types exist for a collection of data type keys.
     *
     * @param dataTypeKeyCollection the data type key collection
     * @param result where the results are added
     */
    private void validateReferencedDataTypesExists(
            final Collection<PfConceptKey> dataTypeKeyCollection, final BeanValidationResult result) {
        for (PfConceptKey dataTypeKey : dataTypeKeyCollection) {
            if (dataTypes == null || dataTypes.get(dataTypeKey) == null) {
                addResult(result, "data type", dataTypeKey.getId(), NOT_FOUND);
            }
        }
    }

    /**
     * Validate that all policy types referenced in policies exist.
     *
     * @param result where the results are added
     */
    private void validatePolicyTypesInPolicies(BeanValidationResult result) {
        if (topologyTemplate == null || topologyTemplate.getPolicies() == null
                || topologyTemplate.getPolicies().getConceptMap().isEmpty()) {
            return;
        }

        if (policyTypes == null || policyTypes.getConceptMap().isEmpty()) {
            addResult(result, "policyTypes", policyTypes,
                    "no policy types are defined on the service template for the policies in the topology template");
            return;
        }

        for (JpaToscaPolicy policy : topologyTemplate.getPolicies().getAll(null)) {
            if (policyTypes.get(policy.getType()) == null) {
                addResult(result, "policy type", policy.getType().getId(), NOT_FOUND);
            }
        }
    }
}
