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

package org.onap.policy.models.tosca.simple.concepts;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.EmbeddedId;
import javax.persistence.MappedSuperclass;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import org.apache.commons.lang3.ObjectUtils;
import org.onap.policy.common.utils.validation.Assertions;
import org.onap.policy.common.utils.validation.ParameterValidationUtils;
import org.onap.policy.models.base.PfAuthorative;
import org.onap.policy.models.base.PfConcept;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfKey;
import org.onap.policy.models.base.PfUtils;
import org.onap.policy.models.base.PfValidationMessage;
import org.onap.policy.models.base.PfValidationResult;
import org.onap.policy.models.base.PfValidationResult.ValidationResult;
import org.onap.policy.models.tosca.authorative.concepts.ToscaEntity;

/**
 * Class to represent the EntrySchema of list/map property in TOSCA definition.
 */
@MappedSuperclass
@Data
@EqualsAndHashCode(callSuper = false)
public class JpaToscaEntityType<T extends ToscaEntity> extends PfConcept implements PfAuthorative<T> {
    private static final long serialVersionUID = -1330661834220739393L;

    @EmbeddedId
    private PfConceptKey key;

    // @formatter:off
    @Column
    @AttributeOverrides({
        @AttributeOverride(name = "name",
                           column = @Column(name = "derived_from_name")),
        @AttributeOverride(name = "version",
                           column = @Column(name = "derived_from_version"))
        })
    private PfConceptKey derivedFrom;

    @ElementCollection
    private Map<String, String> metadata;

    @Column
    private String description;

    private transient T toscaEntity;
    // @formatter:on

    /**
     * The Default Constructor creates a {@link JpaToscaEntityType} object with a null key.
     */
    public JpaToscaEntityType() {
        this(new PfConceptKey());
    }

    /**
     * The Key Constructor creates a {@link JpaToscaEntityType} object with the given concept key.
     *
     * @param key the key
     */
    public JpaToscaEntityType(@NonNull final PfConceptKey key) {
        this.key = key;
    }

    /**
     * Copy constructor.
     *
     * @param copyConcept the concept to copy from
     */
    public JpaToscaEntityType(final JpaToscaEntityType<T> copyConcept) {
        super(copyConcept);
    }

    /**
     * Authorative constructor.
     *
     * @param authorativeConcept the authorative concept to copy from
     */
    public JpaToscaEntityType(final T authorativeConcept) {
        this.fromAuthorative(authorativeConcept);
    }

    @Override
    public T toAuthorative() {
        toscaEntity.setName(getKey().getName());
        toscaEntity.setVersion(getKey().getVersion());

        if (derivedFrom != null) {
            toscaEntity.setDerivedFrom(derivedFrom.getId());
        }

        if (description != null) {
            toscaEntity.setDescription(description);
        }

        if (metadata != null) {
            Map<String, String> metadataMap = new LinkedHashMap<>();

            for (Entry<String, String> entry : metadata.entrySet()) {
                metadataMap.put(entry.getKey(), entry.getValue());
            }

            toscaEntity.setMetadata(metadataMap);
        }

        return toscaEntity;
    }

    @Override
    public void fromAuthorative(T toscaEntity) {
        key = new PfConceptKey();

        if (toscaEntity.getName() != null) {
            key.setName(toscaEntity.getName());
        }

        if (toscaEntity.getVersion() != null) {
            key.setVersion(toscaEntity.getVersion());
        }


        if (toscaEntity.getDerivedFrom() != null) {
            // CHeck if the derived from field contains a name-version ID
            if (toscaEntity.getDerivedFrom().contains(":")) {
                derivedFrom = new PfConceptKey(toscaEntity.getDerivedFrom());
            } else {
                derivedFrom = new PfConceptKey(toscaEntity.getDerivedFrom(), PfKey.NULL_KEY_VERSION);
            }
        }

        if (toscaEntity.getDescription() != null) {
            description = toscaEntity.getDescription();
        }

        if (toscaEntity.getMetadata() != null) {
            metadata = new LinkedHashMap<>();

            for (Entry<String, String> metadataEntry : toscaEntity.getMetadata().entrySet()) {
                metadata.put(metadataEntry.getKey(), metadataEntry.getValue());
            }
        }
    }

    @Override
    public List<PfKey> getKeys() {
        final List<PfKey> keyList = getKey().getKeys();
        if (derivedFrom != null) {
            keyList.addAll(derivedFrom.getKeys());
        }
        return keyList;
    }

    @Override
    public void clean() {
        key.clean();

        if (derivedFrom != null) {
            derivedFrom.clean();
        }

        if (metadata != null) {
            for (Entry<String, String> metadataEntry : metadata.entrySet()) {
                metadataEntry.setValue(metadataEntry.getValue().trim());
            }
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

        @SuppressWarnings("unchecked")
        final JpaToscaEntityType<T> other = (JpaToscaEntityType<T>) otherConcept;
        if (!key.equals(other.key)) {
            return key.compareTo(other.key);
        }

        int result = ObjectUtils.compare(derivedFrom, other.derivedFrom);
        if (result != 0) {
            return result;
        }

        result = PfUtils.compareObjects(metadata, other.metadata);
        if (result != 0) {
            return result;
        }

        return ObjectUtils.compare(description, other.description);
    }

    @Override
    public PfConcept copyTo(@NonNull PfConcept target) {
        final Object copyObject = target;
        Assertions.instanceOf(copyObject, PfConcept.class);

        @SuppressWarnings("unchecked")
        final JpaToscaEntityType<T> copy = ((JpaToscaEntityType<T>) copyObject);
        copy.setKey(new PfConceptKey(key));
        copy.setDerivedFrom(derivedFrom != null ? new PfConceptKey(derivedFrom) : null);

        if (metadata != null) {
            final Map<String, String> newMatadata = new TreeMap<>();
            for (final Entry<String, String> metadataEntry : metadata.entrySet()) {
                newMatadata.put(metadataEntry.getKey(), metadataEntry.getValue());
            }
            copy.setMetadata(newMatadata);
        }

        copy.setDescription(description);

        return copy;
    }
}
