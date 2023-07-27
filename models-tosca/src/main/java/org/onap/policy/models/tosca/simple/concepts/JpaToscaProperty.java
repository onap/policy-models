/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy Model
 * ================================================================================
 * Copyright (C) 2019-2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019-2021, 2023 Nordix Foundation.
 * Modifications Copyright (C) 2022 Bell Canada. All rights reserved.
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

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.EmbeddedId;
import java.io.Serial;
import java.util.ArrayList;
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
import org.onap.policy.common.utils.coder.YamlJsonTranslator;
import org.onap.policy.models.base.PfAuthorative;
import org.onap.policy.models.base.PfConcept;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfKey;
import org.onap.policy.models.base.PfReferenceKey;
import org.onap.policy.models.base.PfUtils;
import org.onap.policy.models.base.validation.annotations.VerifyKey;
import org.onap.policy.models.tosca.authorative.concepts.ToscaProperty;
import org.onap.policy.models.tosca.authorative.concepts.ToscaProperty.Status;

/**
 * Class to represent the property in TOSCA definition.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 * @author Liam Fallon (liam.fallon@est.tech)
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class JpaToscaProperty extends PfConcept implements PfAuthorative<ToscaProperty> {
    @Serial
    private static final long serialVersionUID = 1675770231921107988L;

    @EmbeddedId
    @VerifyKey
    @NotNull
    private PfReferenceKey key;

    @Column
    @VerifyKey
    @NotNull
    private PfConceptKey type;

    @Column
    @NotBlank
    private String description;

    @Column
    private boolean required = false;

    @Column
    @NotBlank
    private String defaultValue;

    @Column
    private Status status = Status.SUPPORTED;

    @ElementCollection
    private List<@NotNull @Valid JpaToscaConstraint> constraints;

    @Column
    @Valid
    private JpaToscaSchemaDefinition entrySchema;

    @ElementCollection
    private Map<String, String> metadata;

    /**
     * The Default Constructor creates a {@link JpaToscaProperty} object with a null key.
     */
    public JpaToscaProperty() {
        this(new PfReferenceKey());
    }

    /**
     * The Key Constructor creates a {@link JpaToscaProperty} object with the given concept key.
     *
     * @param key the key
     */
    public JpaToscaProperty(@NonNull final PfReferenceKey key) {
        this(key, new PfConceptKey());
    }

    /**
     * The Key Constructor creates a {@link JpaToscaProperty} object with the given concept key.
     *
     * @param key the key
     * @param type the key of the property type
     */
    public JpaToscaProperty(@NonNull final PfReferenceKey key, @NonNull final PfConceptKey type) {
        this.key = key;
        this.type = type;
    }

    /**
     * Copy constructor.
     *
     * @param copyConcept the concept to copy from
     */
    public JpaToscaProperty(final JpaToscaProperty copyConcept) {
        super(copyConcept);
        this.key = new PfReferenceKey(copyConcept.key);
        this.type = new PfConceptKey(copyConcept.type);
        this.description = copyConcept.description;
        this.required = copyConcept.required;
        this.defaultValue = copyConcept.defaultValue;
        this.status = copyConcept.status;
        // Constraints are immutable
        this.constraints = (copyConcept.constraints != null ? new ArrayList<>(copyConcept.constraints) : null);
        this.entrySchema =
                (copyConcept.entrySchema != null ? new JpaToscaSchemaDefinition(copyConcept.entrySchema) : null);
        this.metadata = (copyConcept.metadata != null ? new LinkedHashMap<>(copyConcept.metadata) : null);
    }

    /**
     * Authorative constructor.
     *
     * @param authorativeConcept the authorative concept to copy from
     */
    public JpaToscaProperty(final ToscaProperty authorativeConcept) {
        this.fromAuthorative(authorativeConcept);
    }

    @Override
    public ToscaProperty toAuthorative() {
        var toscaProperty = new ToscaProperty();

        toscaProperty.setName(key.getLocalName());

        toscaProperty.setType(type.getName());
        toscaProperty.setTypeVersion(type.getVersion());

        toscaProperty.setDescription(description);
        toscaProperty.setRequired(required);
        toscaProperty.setStatus(status);

        if (defaultValue != null) {
            toscaProperty.setDefaultValue(new YamlJsonTranslator().fromYaml(defaultValue, Object.class));
        }

        toscaProperty.setConstraints(PfUtils.mapList(constraints, JpaToscaConstraint::toAuthorative));

        if (entrySchema != null) {
            toscaProperty.setEntrySchema(entrySchema.toAuthorative());
        }

        toscaProperty.setMetadata(PfUtils.mapMap(metadata, metadataItem -> metadataItem));

        return toscaProperty;
    }

    @Override
    public void fromAuthorative(ToscaProperty toscaProperty) {
        this.setKey(new PfReferenceKey());
        getKey().setLocalName(toscaProperty.getName());

        if (toscaProperty.getTypeVersion() != null) {
            type = new PfConceptKey(toscaProperty.getType(), toscaProperty.getTypeVersion());
        } else {
            type = new PfConceptKey(toscaProperty.getType(), PfKey.NULL_KEY_VERSION);
        }

        description = toscaProperty.getDescription();
        required = toscaProperty.isRequired();
        status = toscaProperty.getStatus();

        if (toscaProperty.getDefaultValue() != null) {
            defaultValue = new YamlJsonTranslator().toYaml(toscaProperty.getDefaultValue()).trim();
        } else {
            defaultValue = null;
        }

        constraints = PfUtils.mapList(toscaProperty.getConstraints(), JpaToscaConstraint::newInstance);

        if (toscaProperty.getEntrySchema() != null) {
            entrySchema = new JpaToscaSchemaDefinition(toscaProperty.getEntrySchema());
        }

        metadata = PfUtils.mapMap(toscaProperty.getMetadata(), metadataItem -> metadataItem);
    }

    @Override
    public List<PfKey> getKeys() {
        final List<PfKey> keyList = getKey().getKeys();

        keyList.addAll(type.getKeys());

        if (entrySchema != null) {
            keyList.addAll(entrySchema.getKeys());
        }

        return keyList;
    }

    @Override
    public void clean() {
        key.clean();

        type.clean();

        if (description != null) {
            description = description.trim();
        }

        if (defaultValue != null) {
            defaultValue = defaultValue.trim();
        }

        if (entrySchema != null) {
            entrySchema.clean();
        }

        metadata = PfUtils.mapMap(metadata, String::trim);
    }

    @Override
    public int compareTo(@NonNull final PfConcept otherConcept) {
        if (this == otherConcept) {
            return 0;
        }
        if (getClass() != otherConcept.getClass()) {
            return getClass().getName().compareTo(otherConcept.getClass().getName());
        }

        final JpaToscaProperty other = (JpaToscaProperty) otherConcept;
        if (!key.equals(other.key)) {
            return key.compareTo(other.key);
        }

        return compareFields(other);
    }

    /**
     * Compare the fields of this ToscaProperty object with the fields of the other ToscaProperty object.
     *
     * @param other the other ToscaProperty object
     */
    private int compareFields(final JpaToscaProperty other) {
        if (!type.equals(other.type)) {
            return type.compareTo(other.type);
        }

        int result = ObjectUtils.compare(description, other.description);
        if (result != 0) {
            return result;
        }

        result = ObjectUtils.compare(required, other.required);
        if (result != 0) {
            return result;
        }

        result = ObjectUtils.compare(defaultValue, other.defaultValue);
        if (result != 0) {
            return result;
        }

        result = ObjectUtils.compare(status, other.status);
        if (result != 0) {
            return result;
        }

        result = PfUtils.compareCollections(constraints, other.constraints);
        if (result != 0) {
            return result;
        }

        result = entrySchema.compareTo(other.entrySchema);
        if (result != 0) {
            return result;
        }

        return PfUtils.compareMaps(metadata, other.metadata);
    }
}
