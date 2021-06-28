/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy Model
 * Modifications Copyright (C) 2021 Nordix Foundation.
 * ================================================================================
 * Copyright (C) 2019-2021 AT&T Intellectual Property. All rights reserved.
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
import org.onap.policy.common.parameters.annotations.NotBlank;
import org.onap.policy.common.parameters.annotations.NotNull;
import org.onap.policy.common.parameters.annotations.Valid;
import org.onap.policy.models.base.PfAuthorative;
import org.onap.policy.models.base.PfConcept;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfKey;
import org.onap.policy.models.base.PfReferenceKey;
import org.onap.policy.models.base.PfUtils;
import org.onap.policy.models.base.validation.annotations.VerifyKey;
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
    @VerifyKey
    @NotNull
    private PfConceptKey key;

    @Column
    @NotBlank
    private String description;

    @Column
    @NotNull
    private PdpState pdpGroupState;

    @ElementCollection
    private Map<@NotNull @NotBlank String, @NotNull @NotBlank String> properties;

    // @formatter:off
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @CollectionTable(joinColumns = {
        @JoinColumn(name = "pdpGroupParentKeyName",    referencedColumnName = "parentKeyName"),
        @JoinColumn(name = "pdpGroupParentKeyVersion", referencedColumnName = "parentKeyVersion"),
        @JoinColumn(name = "pdpGroupParentLocalName",  referencedColumnName = "parentLocalName"),
        @JoinColumn(name = "pdpGroupLocalName",        referencedColumnName = "localName")
    })
    // @formatter:on
    @NotNull
    private List<@NotNull @Valid JpaPdpSubGroup> pdpSubGroups;

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
        this.key = new PfConceptKey(copyConcept.key);
        this.description = copyConcept.description;
        this.pdpGroupState = copyConcept.pdpGroupState;
        this.properties = (copyConcept.properties == null ? null : new LinkedHashMap<>(copyConcept.properties));
        this.pdpSubGroups = PfUtils.mapList(copyConcept.pdpSubGroups, JpaPdpSubGroup::new, new ArrayList<>(0));
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
        var pdpGroup = new PdpGroup();

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
            var jpaPdpSubGroup = new JpaPdpSubGroup();
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
    public int compareTo(final PfConcept otherConcept) {
        if (otherConcept == null) {
            return -1;
        }
        if (this == otherConcept) {
            return 0;
        }
        if (getClass() != otherConcept.getClass()) {
            return this.getClass().getName().compareTo(otherConcept.getClass().getName());
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
}
