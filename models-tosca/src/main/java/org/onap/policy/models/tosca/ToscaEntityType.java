/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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

package org.onap.policy.models.tosca;

import com.google.gson.annotations.SerializedName;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

import org.onap.policy.common.utils.validation.Assertions;
import org.onap.policy.models.base.PfConcept;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfValidationResult;

/**
 * Class to represent the EntrySchema of list/map property in TOSCA definition.
 */
@Entity
@Table(name = "ToscaEntityType")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@ToString
@EqualsAndHashCode(callSuper = true)
public class ToscaEntityType extends PfConcept {
    private static final long serialVersionUID = -1330661834220739393L;

    @Getter
    @Setter
    @SerializedName("key")
    @EmbeddedId
    private PfConceptKey key;

    @Getter
    @Setter
    @SerializedName("derived_from")
    @Column(name = "derivedFrom")
    private PfConceptKey derivedFrom;

    @Getter
    @Setter
    @SerializedName("metadata")
    @OneToMany(cascade = CascadeType.ALL)
    private Map<String, String> metadata;

    @Getter
    @Setter
    @SerializedName("description")
    @Column(name = "description")
    private String description;

    /**
     * The Default Constructor creates a {@link ToscaEntityType} object with a null key.
     */
    public ToscaEntityType() {
        this(new PfConceptKey());
    }

    /**
     * The Key Constructor creates a {@link ToscaEntityType} object with the given concept key.
     *
     * @param key the key
     */
    public ToscaEntityType(@NonNull final PfConceptKey key) {
        this.key = key;
    }

    /**
     * Copy constructor.
     *
     * @param copyConcept the concept to copy from
     */
    public ToscaEntityType(final ToscaEntityType copyConcept) {
        super(copyConcept);
    }

    @Override
    public void clean() {
        description = description.trim();

        for (Entry<String, String> metadataEntry : metadata.entrySet()) {
            metadataEntry.setValue(metadataEntry.getValue().trim());
        }
    }

    @Override
    public PfValidationResult validate(PfValidationResult result) {
        return null;
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

        final ToscaEntityType other = (ToscaEntityType) otherConcept;
        if (!key.equals(other.key)) {
            return key.compareTo(other.key);
        }

        if (!derivedFrom.equals(other.derivedFrom)) {
            return derivedFrom.compareTo(other.derivedFrom);
        }

        if (!metadata.equals(other.metadata)) {
            return (metadata.hashCode() - other.metadata.hashCode());
        }

        return description.compareTo(other.description);
    }

    @Override
    public PfConcept copyTo(@NonNull PfConcept target) {
        final Object copyObject = target;
        Assertions.instanceOf(copyObject, PfConcept.class);

        final ToscaEntityType copy = ((ToscaEntityType) copyObject);
        copy.key = new PfConceptKey(key);
        copy.derivedFrom = new PfConceptKey(derivedFrom);

        final Map<String, String> newMatadata = new TreeMap<>();
        for (final Entry<String, String> metadataEntry : metadata.entrySet()) {
            newMatadata.put(metadataEntry.getKey(), metadataEntry.getValue());
        }
        copy.metadata = newMatadata;

        copy.description = description;

        return copy;
    }
}
