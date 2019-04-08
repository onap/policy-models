/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy Model
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019 Nordix Foundation.
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

package org.onap.policy.models.pdp.persistence.concepts;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.onap.policy.common.utils.validation.Assertions;
import org.onap.policy.common.utils.validation.ParameterValidationUtils;
import org.onap.policy.models.base.PfAuthorative;
import org.onap.policy.models.base.PfConcept;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfKey;
import org.onap.policy.models.base.PfKeyUse;
import org.onap.policy.models.base.PfReferenceKey;
import org.onap.policy.models.base.PfUtils;
import org.onap.policy.models.base.PfValidationMessage;
import org.onap.policy.models.base.PfValidationResult;
import org.onap.policy.models.base.PfValidationResult.ValidationResult;
import org.onap.policy.models.pdp.concepts.Pdp;
import org.onap.policy.models.pdp.concepts.PdpSubGroup;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyIdentifier;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyTypeIdentifier;

/**
 * Class to represent a PDP subgroup in the database.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
@Entity
@Table(name = "PdpSubGroup")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Data
@EqualsAndHashCode(callSuper = false)
public class JpaPdpSubGroup extends PfConcept implements PfAuthorative<PdpSubGroup> {
    private static final long serialVersionUID = -357224425637789775L;

    @EmbeddedId
    private PfReferenceKey key;

    @ElementCollection
    private List<PfConceptKey> supportedPolicyTypes;

    @ElementCollection
    private List<PfConceptKey> policies;

    @Column
    private int currentInstanceCount;

    @Column
    private int desiredInstanceCount;

    @ElementCollection
    private Map<String, String> properties;

    // @formatter:ofF
    @OneToMany
    @CollectionTable(
            joinColumns = { @JoinColumn(name = "pdpSubGroupParentKeyName", referencedColumnName = "parentKeyName"),
            @JoinColumn(name = "pdpSubGroupParentKeyVersion", referencedColumnName = "parentKeyVersion"),
            @JoinColumn(name = "pdpSubGroupParentLocalName", referencedColumnName = "parentLocalName"),
            @JoinColumn(name = "pdpSubGroupLocalName", referencedColumnName = "localName") })
    // formatter:on
    private List<JpaPdp> pdpInstances;

    /**
     * The Default Constructor creates a {@link JpaPdpSubGroup} object with a null key.
     */
    public JpaPdpSubGroup() {
        this(new PfReferenceKey());
    }

    /**
     * The Key Constructor creates a {@link JpaPdpSubGroup} object with the given concept key.
     *
     * @param key the key
     */
    public JpaPdpSubGroup(@NonNull final PfReferenceKey key) {
        this(key, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    /**
     * The Key Constructor creates a {@link JpaPdpSubGroup} object with all mandatory fields.
     *
     * @param key the key
     * @param supportedPolicyTypes Supported policy types
     * @param policies policies deployed on this PDP subgroups
     * @param pdpInstances the PDP instances on this PDP subgroups
     */
    public JpaPdpSubGroup(@NonNull final PfReferenceKey key, @NonNull final List<PfConceptKey> supportedPolicyTypes,
            @NonNull List<PfConceptKey> policies, @NonNull final List<JpaPdp> pdpInstances) {
        this.key = key;
        this.supportedPolicyTypes = supportedPolicyTypes;
        this.policies = policies;
        this.pdpInstances = pdpInstances;
    }

    /**
     * Copy constructor.
     *
     * @param copyConcept the concept to copy from
     */
    public JpaPdpSubGroup(@NonNull final JpaPdpSubGroup copyConcept) {
        super(copyConcept);
    }

    /**
     * Authorative constructor.
     *
     * @param authorativeConcept the authorative concept to copy from
     */
    public JpaPdpSubGroup(@NonNull final PdpSubGroup authorativeConcept) {
        this.fromAuthorative(authorativeConcept);
    }

    @Override
    public PdpSubGroup toAuthorative() {
        PdpSubGroup pdpSubgroup = new PdpSubGroup();

        pdpSubgroup.setPdpType(getKey().getLocalName());

        pdpSubgroup.setSupportedPolicyTypes(new ArrayList<>());
        for (PfConceptKey supportedPolicyTypeKey : supportedPolicyTypes) {
            ToscaPolicyTypeIdentifier supportedPolicyTypeIdent = new ToscaPolicyTypeIdentifier(
                    supportedPolicyTypeKey.getName(), supportedPolicyTypeKey.getVersion());
            pdpSubgroup.getSupportedPolicyTypes().add(supportedPolicyTypeIdent);
        }

        pdpSubgroup.setPolicies(new ArrayList<>());
        for (PfConceptKey policyKey : policies) {
            ToscaPolicyIdentifier toscaPolicyIdentifier = new ToscaPolicyIdentifier();
            toscaPolicyIdentifier.setName(policyKey.getName());
            toscaPolicyIdentifier.setVersion(policyKey.getVersion());
            pdpSubgroup.getPolicies().add(toscaPolicyIdentifier);
        }

        pdpSubgroup.setCurrentInstanceCount(currentInstanceCount);
        pdpSubgroup.setDesiredInstanceCount(desiredInstanceCount);
        pdpSubgroup.setProperties(properties == null ? null : new LinkedHashMap<>(properties));

        pdpSubgroup.setPdpInstances(new ArrayList<>());
        for (JpaPdp jpaPdp : pdpInstances) {
            pdpSubgroup.getPdpInstances().add(jpaPdp.toAuthorative());
        }

        return pdpSubgroup;
    }

    @Override
    public void fromAuthorative(@NonNull final PdpSubGroup pdpSubgroup) {
        if (this.key == null || this.getKey().isNullKey()) {
            this.setKey(new PfReferenceKey());
            getKey().setLocalName(pdpSubgroup.getPdpType());
        }

        this.supportedPolicyTypes = new ArrayList<>();
        if (pdpSubgroup.getSupportedPolicyTypes() != null) {
            for (ToscaPolicyTypeIdentifier supportedPolicyType : pdpSubgroup.getSupportedPolicyTypes()) {
                this.supportedPolicyTypes
                        .add(new PfConceptKey(supportedPolicyType.getName(), supportedPolicyType.getVersion()));
            }
        }

        this.policies = new ArrayList<>();
        if (pdpSubgroup.getPolicies() != null) {
            for (ToscaPolicyIdentifier toscaPolicyIdentifier : pdpSubgroup.getPolicies()) {
                this.policies
                        .add(new PfConceptKey(toscaPolicyIdentifier.getName(), toscaPolicyIdentifier.getVersion()));
            }
        }
        this.currentInstanceCount = pdpSubgroup.getCurrentInstanceCount();
        this.desiredInstanceCount = pdpSubgroup.getDesiredInstanceCount();
        this.properties =
                (pdpSubgroup.getProperties() == null ? null : new LinkedHashMap<>(pdpSubgroup.getProperties()));

        this.pdpInstances = new ArrayList<>();
        if (pdpSubgroup.getPdpInstances() != null) {
            for (Pdp pdp : pdpSubgroup.getPdpInstances()) {
                JpaPdp jpaPdp = new JpaPdp();
                jpaPdp.setKey(new PfReferenceKey(getKey(), pdp.getInstanceId()));
                jpaPdp.fromAuthorative(pdp);
                this.pdpInstances.add(jpaPdp);
            }
        }
    }

    @Override
    public List<PfKey> getKeys() {
        List<PfKey> keyList = getKey().getKeys();

        for (PfConceptKey ptkey : supportedPolicyTypes) {
            keyList.add(new PfKeyUse(ptkey));
        }

        for (PfConceptKey pkey : policies) {
            keyList.add(new PfKeyUse(pkey));
        }

        for (JpaPdp jpaPdp : pdpInstances) {
            keyList.addAll(jpaPdp.getKeys());
        }


        return keyList;
    }

    @Override
    public void clean() {
        key.clean();

        for (PfConceptKey ptkey : supportedPolicyTypes) {
            ptkey.clean();
        }

        for (PfConceptKey pkey : policies) {
            pkey.clean();
        }

        if (properties != null) {
            Map<String, String> cleanedPropertyMap = new LinkedHashMap<>();
            for (Entry<String, String> propertyEntry : properties.entrySet()) {
                cleanedPropertyMap.put(propertyEntry.getKey().trim(), propertyEntry.getValue().trim());
            }
            properties = cleanedPropertyMap;
        }

        for (JpaPdp jpaPdp : pdpInstances) {
            jpaPdp.clean();
        }
    }

    @Override
    public PfValidationResult validate(@NonNull final PfValidationResult resultIn) {
        PfValidationResult result = resultIn;

        if (key.isNullKey()) {
            result.addValidationMessage(
                    new PfValidationMessage(key, this.getClass(), ValidationResult.INVALID, "key is a null key"));
        }

        result = key.validate(result);

        if (key.getParentConceptKey().isNullKey()) {
            result.addValidationMessage(new PfValidationMessage(key, this.getClass(), ValidationResult.INVALID,
                    "parent of key is a null key"));
        }

        if (currentInstanceCount < 0) {
            result.addValidationMessage(new PfValidationMessage(key, this.getClass(), ValidationResult.INVALID,
                    "the current instance count of a PDP group may not be negative"));
        }

        if (desiredInstanceCount < 0) {
            result.addValidationMessage(new PfValidationMessage(key, this.getClass(), ValidationResult.INVALID,
                    "the desired instance count of a PDP group may not be negative"));
        }

        if (properties != null) {
            for (Entry<String, String> propertyEntry : properties.entrySet()) {
                if (!ParameterValidationUtils.validateStringParameter(propertyEntry.getKey())) {
                    result.addValidationMessage(new PfValidationMessage(key, this.getClass(), ValidationResult.INVALID,
                            "a property key may not be null or blank"));
                }
                if (!ParameterValidationUtils.validateStringParameter(propertyEntry.getValue())) {
                    result.addValidationMessage(new PfValidationMessage(key, this.getClass(), ValidationResult.INVALID,
                            "a property value may not be null or blank"));
                }
            }
        }


        return validateSubConcepts(result);
    }

    /**
     * Validate collections of sub concepts.
     *
     * @param result the result in which to store the validation result
     * @return the validation result including the results of this method
     */
    private PfValidationResult validateSubConcepts(PfValidationResult result) {
        if (supportedPolicyTypes == null || supportedPolicyTypes.isEmpty()) {
            result.addValidationMessage(new PfValidationMessage(key, this.getClass(), ValidationResult.INVALID,
                    "a PDP subgroup must support at least one policy type"));
        } else {
            for (PfConceptKey supportedPolicyType : supportedPolicyTypes) {
                result = supportedPolicyType.validate(result);
            }
        }

        if (policies == null) {
            result.addValidationMessage(new PfValidationMessage(key, this.getClass(), ValidationResult.INVALID,
                    "a PDP subgroup must have a list of policies"));
        } else {
            for (PfConceptKey policyKey : policies) {
                result = policyKey.validate(result);
            }
        }

        if (pdpInstances == null) {
            result.addValidationMessage(new PfValidationMessage(key, this.getClass(), ValidationResult.INVALID,
                    "a PDP subgroup must have a list of PDPs"));
        } else {
            for (JpaPdp jpaPdp : pdpInstances) {
                result = jpaPdp.validate(result);
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
            return this.hashCode() - otherConcept.hashCode();
        }

        final JpaPdpSubGroup other = (JpaPdpSubGroup) otherConcept;
        if (!key.equals(other.key)) {
            return key.compareTo(other.key);
        }

        int result = PfUtils.compareObjects(supportedPolicyTypes, other.supportedPolicyTypes);
        if (result != 0) {
            return result;
        }

        result = PfUtils.compareObjects(policies, other.policies);
        if (result != 0) {
            return result;
        }

        if (currentInstanceCount != other.currentInstanceCount) {
            return currentInstanceCount - other.currentInstanceCount;
        }

        if (desiredInstanceCount != other.desiredInstanceCount) {
            return desiredInstanceCount - other.desiredInstanceCount;
        }

        result = PfUtils.compareObjects(properties, other.properties);
        if (result != 0) {
            return result;
        }

        return PfUtils.compareObjects(pdpInstances, other.pdpInstances);
    }

    @Override
    public PfConcept copyTo(@NonNull final PfConcept target) {
        Assertions.instanceOf(target, JpaPdpSubGroup.class);

        final JpaPdpSubGroup copy = ((JpaPdpSubGroup) target);
        copy.setKey(new PfReferenceKey(key));

        copy.setSupportedPolicyTypes(PfUtils.mapList(supportedPolicyTypes, PfConceptKey::new));
        copy.setPolicies(PfUtils.mapList(policies, PfConceptKey::new));
        copy.setCurrentInstanceCount(currentInstanceCount);
        copy.setDesiredInstanceCount(desiredInstanceCount);
        copy.setProperties(properties == null ? null : new LinkedHashMap<>(properties));
        copy.setPdpInstances(PfUtils.mapList(pdpInstances, JpaPdp::new));

        return copy;
    }
}
