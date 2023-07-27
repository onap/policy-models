/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2020, 2022-2023 Nordix Foundation.
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

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Lob;
import jakarta.persistence.MappedSuperclass;
import jakarta.ws.rs.core.Response;
import java.io.Serial;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.apache.commons.lang3.ObjectUtils;
import org.onap.policy.common.parameters.annotations.NotBlank;
import org.onap.policy.common.parameters.annotations.NotNull;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.models.base.PfAuthorative;
import org.onap.policy.models.base.PfConcept;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfKey;
import org.onap.policy.models.base.PfModelRuntimeException;
import org.onap.policy.models.base.PfUtils;
import org.onap.policy.models.base.validation.annotations.VerifyKey;
import org.onap.policy.models.tosca.authorative.concepts.ToscaEntity;

/**
 * Class to represent the EntrySchema of list/map property in TOSCA definition.
 */
@MappedSuperclass
@Data
@EqualsAndHashCode(callSuper = false)
public class JpaToscaEntityType<T extends ToscaEntity> extends PfConcept implements PfAuthorative<T> {
    @Serial
    private static final long serialVersionUID = -1330661834220739393L;

    private static final StandardCoder STANDARD_CODER = new StandardCoder();

    @EmbeddedId
    @VerifyKey
    @NotNull
    private PfConceptKey key;

    // @formatter:off
    @Column
    @AttributeOverride(name = "name", column = @Column(name = "derived_from_name"))
    @AttributeOverride(name = "version", column = @Column(name = "derived_from_version"))
    @VerifyKey
    private PfConceptKey derivedFrom;

    @ElementCollection
    @Lob
    private Map<@NotNull @NotBlank String, @NotNull @NotBlank String> metadata;

    @Column
    @NotBlank
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
        this.key = new PfConceptKey(copyConcept.key);
        this.derivedFrom = (copyConcept.derivedFrom != null ? new PfConceptKey(copyConcept.derivedFrom) : null);
        this.metadata = (copyConcept.metadata != null ? new TreeMap<>(copyConcept.metadata) : null);
        this.description = copyConcept.description;
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
            toscaEntity.setDerivedFrom(derivedFrom.getName());
        }

        if (description != null) {
            toscaEntity.setDescription(description);
        }

        toscaEntity.setMetadata(PfUtils.mapMap(metadata, this::deserializeMetadataValue));

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
            // Check if the derived from field contains a name-version ID
            if (toscaEntity.getDerivedFrom().contains(":")) {
                derivedFrom = new PfConceptKey(toscaEntity.getDerivedFrom());
            } else {
                derivedFrom = new PfConceptKey(toscaEntity.getDerivedFrom(), PfKey.NULL_KEY_VERSION);
            }
        }

        if (toscaEntity.getDescription() != null) {
            description = toscaEntity.getDescription();
        }

        metadata = PfUtils.mapMap(toscaEntity.getMetadata(), this::serializeMetadataValue);
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

        @SuppressWarnings("unchecked")
        final JpaToscaEntityType<T> other = (JpaToscaEntityType<T>) otherConcept;

        int result = key.compareTo(other.key);
        if (result != 0) {
            return result;
        }

        result = ObjectUtils.compare(derivedFrom, other.derivedFrom);
        if (result != 0) {
            return result;
        }

        result = PfUtils.compareMaps(metadata, other.metadata);
        if (result != 0) {
            return result;
        }

        return ObjectUtils.compare(description, other.description);
    }

    protected Object deserializeMetadataValue(String metadataValue) {
        try {
            return STANDARD_CODER.decode(metadataValue, Object.class);
        } catch (CoderException ce) {
            String errorMessage = "error decoding metadata JSON value read from database: " + metadataValue;
            throw new PfModelRuntimeException(Response.Status.INTERNAL_SERVER_ERROR, errorMessage, ce);
        }
    }

    protected String serializeMetadataValue(Object metadataValue) {
        try {
            return STANDARD_CODER.encode(metadataValue);
        } catch (CoderException ce) {
            String errorMessage = "error encoding metadata JSON value for database: " + metadataValue;
            throw new PfModelRuntimeException(Response.Status.INTERNAL_SERVER_ERROR, errorMessage, ce);
        }
    }
}
