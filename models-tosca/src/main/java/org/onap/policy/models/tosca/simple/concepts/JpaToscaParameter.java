/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2020, 2023 Nordix Foundation.
 * Modifications Copyright (C) 2020-2021 AT&T Intellectual Property. All rights reserved.
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
import jakarta.persistence.EmbeddedId;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.onap.policy.common.parameters.annotations.NotNull;
import org.onap.policy.common.utils.coder.YamlJsonTranslator;
import org.onap.policy.models.base.PfAuthorative;
import org.onap.policy.models.base.PfConcept;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfKey;
import org.onap.policy.models.base.PfReferenceKey;
import org.onap.policy.models.base.validation.annotations.VerifyKey;
import org.onap.policy.models.tosca.authorative.concepts.ToscaParameter;

/**
 * Class to represent the parameter in TOSCA definition.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class JpaToscaParameter extends PfConcept implements PfAuthorative<ToscaParameter> {
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
    private String value;

    /**
     * The Default Constructor creates a {@link JpaToscaParameter} object with a null key.
     */
    public JpaToscaParameter() {
        this(new PfReferenceKey());
    }

    /**
     * The Key Constructor creates a {@link JpaToscaParameter} object with the given concept key.
     *
     * @param key the key
     */
    public JpaToscaParameter(@NonNull final PfReferenceKey key) {
        this(key, new PfConceptKey());
    }

    /**
     * The Key Constructor creates a {@link JpaToscaParameter} object with the given concept key.
     *
     * @param key the key
     * @param type the key of the parameter type
     */
    public JpaToscaParameter(@NonNull final PfReferenceKey key, @NonNull final PfConceptKey type) {
        this.key = key;
        this.type = type;
    }

    /**
     * Copy constructor.
     *
     * @param copyConcept the concept to copy from
     */
    public JpaToscaParameter(final JpaToscaParameter copyConcept) {
        super(copyConcept);
        this.key = new PfReferenceKey(copyConcept.key);
        this.type = new PfConceptKey(copyConcept.type);
        this.value = copyConcept.value;
    }

    /**
     * Authorative constructor.
     *
     * @param authorativeConcept the authorative concept to copy from
     */
    public JpaToscaParameter(final ToscaParameter authorativeConcept) {
        this.fromAuthorative(authorativeConcept);
    }

    @Override
    public ToscaParameter toAuthorative() {
        var toscaParameter = new ToscaParameter();

        toscaParameter.setName(key.getLocalName());

        toscaParameter.setType(type.getName());
        toscaParameter.setTypeVersion(type.getVersion());

        if (!StringUtils.isBlank(value)) {
            toscaParameter.setValue(new YamlJsonTranslator().fromYaml(value, Object.class));
        }

        return toscaParameter;
    }

    @Override
    public void fromAuthorative(ToscaParameter toscaParameter) {
        this.setKey(new PfReferenceKey());
        getKey().setLocalName(toscaParameter.getName());

        if (toscaParameter.getTypeVersion() != null) {
            type = new PfConceptKey(toscaParameter.getType(), toscaParameter.getTypeVersion());
        } else {
            type = new PfConceptKey(toscaParameter.getType(), PfKey.NULL_KEY_VERSION);
        }

        value = new YamlJsonTranslator().toYaml(toscaParameter.getValue());
    }

    @Override
    public List<PfKey> getKeys() {
        final List<PfKey> keyList = getKey().getKeys();

        keyList.addAll(type.getKeys());

        return keyList;
    }

    @Override
    public void clean() {
        key.clean();

        type.clean();

        if (value != null) {
            value = value.trim();
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
            return getClass().getName().compareTo(otherConcept.getClass().getName());
        }

        final JpaToscaParameter other = (JpaToscaParameter) otherConcept;
        int result = key.compareTo(other.key);
        if (result != 0) {
            return result;
        }

        return value.compareTo(other.value);
    }
}
