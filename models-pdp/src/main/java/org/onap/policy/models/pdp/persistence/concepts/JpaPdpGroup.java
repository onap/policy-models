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

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.onap.policy.common.utils.validation.Assertions;
import org.onap.policy.common.utils.validation.ParameterValidationUtils;
import org.onap.policy.models.base.PfAuthorative;
import org.onap.policy.models.base.PfConcept;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfKey;
import org.onap.policy.models.base.PfReferenceKey;
import org.onap.policy.models.base.PfUtils;
import org.onap.policy.models.base.PfValidationMessage;
import org.onap.policy.models.base.PfValidationResult;
import org.onap.policy.models.base.PfValidationResult.ValidationResult;
import org.onap.policy.models.pdp.concepts.PdpGroup;
import org.onap.policy.models.pdp.concepts.PdpSubGroup;
import org.onap.policy.models.pdp.enums.PdpState;

/**
 * Class to represent a PDP group in the database.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
@Entity
@Table(name = "PdpGroup")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Data
@EqualsAndHashCode(callSuper = false)
public class JpaPdpGroup extends PfConcept implements PfAuthorative<PdpGroup> {
    private static final long serialVersionUID = -357224425637789775L;

    @EmbeddedId
    private PfConceptKey key;

    @Column
    private String description;

    @Column
    private PdpState pdpGroupState;

    @ElementCollection
    private Map<String, String> properties;

    // @formatter:off
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @CollectionTable(joinColumns = {
            @JoinColumn(name = "pdpGroupParentKeyName",    referencedColumnName = "parentKeyName"),
            @JoinColumn(name = "pdpGroupParentKeyVersion", referencedColumnName = "parentKeyVersion"),
            @JoinColumn(name = "pdpGroupParentLocalName",  referencedColumnName = "parentLocalName"),
            @JoinColumn(name = "pdpGroupLocalName",        referencedColumnName = "localName")
        })
    // @formatter:on
    private List<JpaPdpSubGroup> pdpSubGroups;

    /**
     * The Default Constructor creates a {@link JpaPdpGroup} object with a null key.
     */
    public JpaPdpGroup() {
        this(new PfConceptKey());
    }

    /**
     * The Key Constructor creates a {@link JpaPdpGroup} object with the given concept key.
     *
     * @param key the key
     */
    public JpaPdpGroup(@NonNull final PfConceptKey key) {
        this(key, PdpState.PASSIVE, new ArrayList<>());
    }

    /**
     * The Key Constructor creates a {@link JpaPdpGroup} object with all mandatory fields.
     *
     * @param key the key
     * @param pdpGroupState State of the PDP group
     */
    public JpaPdpGroup(@NonNull final PfConceptKey key, @NonNull final PdpState pdpGroupState,
            @NonNull final List<JpaPdpSubGroup> pdpSubGroups) {
        this.key = key;
        this.pdpGroupState = pdpGroupState;
        this.pdpSubGroups = pdpSubGroups;
    }

    /**
     * Copy constructor.
     *
     * @param copyConcept the concept to copy from
     */
    public JpaPdpGroup(@NonNull final JpaPdpGroup copyConcept) {
        super(copyConcept);
    }

    /**
     * Authorative constructor.
     *
     * @param authorativeConcept the authorative concept to copy from
     */
    public JpaPdpGroup(@NonNull final PdpGroup authorativeConcept) {
        this.fromAuthorative(authorativeConcept);
    }

    @Override
    public PdpGroup toAuthorative() {
        PdpGroup pdpGroup = new PdpGroup();

        pdpGroup.setName(getKey().getName());
        pdpGroup.setVersion(getKey().getVersion());
        pdpGroup.setDescription(description);
        pdpGroup.setPdpGroupState(pdpGroupState);

        pdpGroup.setProperties(properties == null ? null : new LinkedHashMap<>(properties));

        pdpGroup.setPdpSubgroups(new ArrayList<>(pdpSubGroups.size()));
        for (JpaPdpSubGroup jpaPdpSubgroup : pdpSubGroups) {
            pdpGroup.getPdpSubgroups().add(jpaPdpSubgroup.toAuthorative());
        }

        return pdpGroup;
    }

    @Override
    public void fromAuthorative(@NonNull final PdpGroup pdpGroup) {
        if (this.key == null || this.getKey().isNullKey()) {
            this.setKey(new PfConceptKey(pdpGroup.getName(), pdpGroup.getVersion()));
        }

        this.description = pdpGroup.getDescription();
        this.pdpGroupState = pdpGroup.getPdpGroupState();

        this.properties =
                (pdpGroup.getProperties() == null ? null : new LinkedHashMap<>(pdpGroup.getProperties()));

        this.pdpSubGroups = new ArrayList<>();
        for (PdpSubGroup pdpSubgroup : pdpGroup.getPdpSubgroups()) {
            JpaPdpSubGroup jpaPdpSubGroup = new JpaPdpSubGroup();
            jpaPdpSubGroup.setKey(new PfReferenceKey(getKey(), pdpSubgroup.getPdpType()));
            jpaPdpSubGroup.fromAuthorative(pdpSubgroup);
            this.pdpSubGroups.add(jpaPdpSubGroup);
        }
    }

    @Override
    public List<PfKey> getKeys() {
        List<PfKey> keyList = getKey().getKeys();

        for (JpaPdpSubGroup jpaPdpSubgroup : pdpSubGroups) {
            keyList.addAll(jpaPdpSubgroup.getKeys());
        }

        return keyList;
    }

    @Override
    public void clean() {
        key.clean();

        description = (description == null ? null : description.trim());

        if (properties != null) {
            Map<String, String> cleanedPropertyMap = new LinkedHashMap<>();
            for (Entry<String, String> propertyEntry : properties.entrySet()) {
                cleanedPropertyMap.put(propertyEntry.getKey().trim(), propertyEntry.getValue().trim());
            }
            properties = cleanedPropertyMap;
        }

        for (JpaPdpSubGroup jpaPdpSubgroup : pdpSubGroups) {
            jpaPdpSubgroup.clean();
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

        if (description != null && StringUtils.isBlank(description)) {
            result.addValidationMessage(new PfValidationMessage(key, this.getClass(), ValidationResult.INVALID,
                    "description may not be blank"));
        }

        if (pdpGroupState == null) {
            result.addValidationMessage(new PfValidationMessage(key, this.getClass(), ValidationResult.INVALID,
                    "pdpGroupState may not be null"));
        }

        if (properties != null) {
            result = validateProperties(result);
        }

        if (pdpSubGroups == null) {
            result.addValidationMessage(new PfValidationMessage(key, this.getClass(), ValidationResult.INVALID,
                    "a PDP group must have a list of PDP subgroups"));
        } else {
            for (JpaPdpSubGroup jpaPdpSubgroup : pdpSubGroups) {
                result = jpaPdpSubgroup.validate(result);
            }
        }

        return result;
    }

    /**
     * Validate the properties.
     *
     * @param resultIn the incoming validation results so far
     * @return the revalidation results including the property validation results
     */
    private PfValidationResult validateProperties(PfValidationResult resultIn) {
        PfValidationResult result = resultIn;

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
            return this.getClass().getCanonicalName().compareTo(otherConcept.getClass().getCanonicalName());
        }

        final JpaPdpGroup other = (JpaPdpGroup) otherConcept;
        if (!key.equals(other.key)) {
            return key.compareTo(other.key);
        }

        int result = ObjectUtils.compare(description, other.description);
        if (result != 0) {
            return result;
        }

        result = ObjectUtils.compare(pdpGroupState, other.pdpGroupState);
        if (result != 0) {
            return result;
        }

        result = PfUtils.compareObjects(properties, other.properties);
        if (result != 0) {
            return result;
        }

        return PfUtils.compareObjects(pdpSubGroups, other.pdpSubGroups);
    }

    @Override
    public PfConcept copyTo(@NonNull final PfConcept target) {
        Assertions.instanceOf(target, JpaPdpGroup.class);

        final JpaPdpGroup copy = ((JpaPdpGroup) target);
        copy.setKey(new PfConceptKey(key));

        copy.setDescription(description);
        copy.setPdpGroupState(pdpGroupState);
        copy.setProperties(properties == null ? null : new LinkedHashMap<>(properties));
        copy.setPdpSubGroups(PfUtils.mapList(pdpSubGroups, JpaPdpSubGroup::new));

        return copy;
    }
}
