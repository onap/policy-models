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

package org.onap.policy.models.tosca.concepts;

import com.google.gson.annotations.SerializedName;

import java.util.List;
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

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import org.apache.commons.lang3.ObjectUtils;
import org.onap.policy.common.utils.validation.Assertions;
import org.onap.policy.common.utils.validation.ParameterValidationUtils;
import org.onap.policy.models.base.PfConcept;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfKey;
import org.onap.policy.models.base.PfValidationMessage;
import org.onap.policy.models.base.PfValidationResult;
import org.onap.policy.models.base.PfValidationResult.ValidationResult;

/**
 * Class to represent the EntrySchema of list/map property in TOSCA definition.
 */
@Entity
@Table(name = "ToscaEntityType")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Data
@EqualsAndHashCode(callSuper = false)
public class ToscaEntityType extends PfConcept {
    private static final long serialVersionUID = -1330661834220739393L;

    @EmbeddedId
    private PfConceptKey key;

    @SerializedName("derived_from")
    @Column(name = "derivedFrom")
    private PfConceptKey derivedFrom;

    @OneToMany(cascade = CascadeType.ALL)
    private Map<String, String> metadata;

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
    public List<PfKey> getKeys() {
        final List<PfKey> keyList = getKey().getKeys();
        keyList.addAll(derivedFrom.getKeys());
        return keyList;
    }

    @Override
    public void clean() {
        key.clean();

        derivedFrom.clean();

        for (Entry<String, String> metadataEntry : metadata.entrySet()) {
            metadataEntry.setValue(metadataEntry.getValue().trim());
        }

        description = (description != null ? description.trim() : null);
    }

    @Override
    public PfValidationResult validate(PfValidationResult resultIn) {
        PfValidationResult result = resultIn;

        if (key.isNullKey()) {
            result.addValidationMessage(
                    new PfValidationMessage(key, this.getClass(), ValidationResult.INVALID, "key is a null key"));
        }

        result = key.validate(result);

        if (derivedFrom != null && derivedFrom.isNullKey()) {
            result.addValidationMessage(new PfValidationMessage(key, this.getClass(), ValidationResult.INVALID,
                    "derived from key is a null key"));
        }

        if (metadata != null) {
            for (Entry<String, String> metadataEntry : metadata.entrySet()) {
                if (!ParameterValidationUtils.validateStringParameter(metadataEntry.getKey())) {
                    result.addValidationMessage(new PfValidationMessage(key, this.getClass(), ValidationResult.INVALID,
                            "property metadata key may not be null"));
                }
                if (!ParameterValidationUtils.validateStringParameter(metadataEntry.getValue())) {
                    result.addValidationMessage(new PfValidationMessage(key, this.getClass(), ValidationResult.INVALID,
                            "property metadata value may not be null"));
                }
            }
        }

        if (description != null && description.trim().length() == 0) {
            result.addValidationMessage(new PfValidationMessage(key, this.getClass(), ValidationResult.INVALID,
                    "property description may not be blank"));
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

        final ToscaEntityType other = (ToscaEntityType) otherConcept;
        if (!key.equals(other.key)) {
            return key.compareTo(other.key);
        }

        int result = ObjectUtils.compare(derivedFrom, other.derivedFrom);
        if (result != 0) {
            return result;
        }

        if (!metadata.equals(other.metadata)) {
            return (metadata.hashCode() - other.metadata.hashCode());
        }

        return ObjectUtils.compare(description, other.description);
    }

    @Override
    public PfConcept copyTo(@NonNull PfConcept target) {
        final Object copyObject = target;
        Assertions.instanceOf(copyObject, PfConcept.class);

        final ToscaEntityType copy = ((ToscaEntityType) copyObject);
        copy.setKey(new PfConceptKey(key));
        copy.setDerivedFrom(new PfConceptKey(derivedFrom));

        final Map<String, String> newMatadata = new TreeMap<>();
        for (final Entry<String, String> metadataEntry : metadata.entrySet()) {
            newMatadata.put(metadataEntry.getKey(), metadataEntry.getValue());
        }
        copy.setMetadata(newMatadata);

        copy.setDescription(description);

        return copy;
    }
}
