/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2020-2021, 2023 Nordix Foundation.
 * Modifications Copyright (C) 2020-2021 AT&T Intellectual Property. All rights reserved.
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

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.ws.rs.core.Response;
import java.io.Serial;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.apache.commons.lang3.ObjectUtils;
import org.onap.policy.common.parameters.annotations.Valid;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.models.base.PfConcept;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfKey;
import org.onap.policy.models.base.PfModelRuntimeException;
import org.onap.policy.models.tosca.authorative.concepts.ToscaCapabilityAssignment;
import org.onap.policy.models.tosca.authorative.concepts.ToscaNodeTemplate;

/**
 * Class to represent the node template in TOSCA definition.
 */
@Entity
@Table(name = "ToscaNodeTemplate")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Data
@EqualsAndHashCode(callSuper = false)
public class JpaToscaNodeTemplate extends JpaToscaWithTypeAndStringProperties<ToscaNodeTemplate> {
    @Serial
    private static final long serialVersionUID = 1675770231921107988L;

    private static final StandardCoder STANDARD_CODER = new StandardCoder();

    // formatter:off
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "requirementsName", referencedColumnName = "name")
    @JoinColumn(name = "requirementsVersion", referencedColumnName = "version")
    @Valid
    private JpaToscaRequirements requirements;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "capabilitiesName", referencedColumnName = "name")
    @JoinColumn(name = "capabilitiesVersion", referencedColumnName = "version")
    @Valid
    private JpaToscaCapabilityAssignments capabilities;
    // @formatter:on

    /**
     * The Default Constructor creates a {@link JpaToscaNodeTemplate} object with a null key.
     */
    public JpaToscaNodeTemplate() {
        this(new PfConceptKey());
    }

    /**
     * The Key Constructor creates a {@link JpaToscaNodeTemplate} object with the given concept key.
     *
     * @param key the key
     */
    public JpaToscaNodeTemplate(@NonNull final PfConceptKey key) {
        this(key, null);
    }

    /**
     * Copy constructor.
     *
     * @param copyConcept the concept to copy from
     */
    public JpaToscaNodeTemplate(final JpaToscaNodeTemplate copyConcept) {
        super(copyConcept);

        this.requirements =
                (copyConcept.requirements != null ? new JpaToscaRequirements(copyConcept.requirements) : null);
        this.capabilities =
                (copyConcept.capabilities != null ? new JpaToscaCapabilityAssignments(copyConcept.capabilities) : null);
    }

    /**
     * The Key Constructor creates a {@link JpaToscaParameter} object with the given concept key.
     *
     * @param key the key
     * @param type the node template type
     */
    public JpaToscaNodeTemplate(@NonNull final PfConceptKey key, final String type) {
        super(key);
    }

    /**
     * Authorative constructor.
     *
     * @param authorativeConcept the authorative concept to copy from
     */
    public JpaToscaNodeTemplate(final ToscaNodeTemplate authorativeConcept) {
        this.fromAuthorative(authorativeConcept);
    }

    @Override
    public ToscaNodeTemplate toAuthorative() {
        var toscaNodeTemplate = new ToscaNodeTemplate();
        super.setToscaEntity(toscaNodeTemplate);
        super.toAuthorative();

        if (requirements != null) {
            toscaNodeTemplate.setRequirements(requirements.toAuthorative());
        }

        if (capabilities != null) {
            toscaNodeTemplate.setCapabilities(new LinkedHashMap<>());
            List<Map<String, ToscaCapabilityAssignment>> capabilityAssignmentMapList = capabilities.toAuthorative();
            for (Map<String, ToscaCapabilityAssignment> capabilityAssignmentMap : capabilityAssignmentMapList) {
                toscaNodeTemplate.getCapabilities().putAll(capabilityAssignmentMap);
            }
        }

        return toscaNodeTemplate;
    }

    @Override
    public void fromAuthorative(ToscaNodeTemplate toscaNodeTemplate) {
        super.fromAuthorative(toscaNodeTemplate);

        if (toscaNodeTemplate.getRequirements() != null) {
            requirements = new JpaToscaRequirements();
            requirements.fromAuthorative(toscaNodeTemplate.getRequirements());
        }

        if (toscaNodeTemplate.getCapabilities() != null) {
            capabilities = new JpaToscaCapabilityAssignments();
            capabilities.fromAuthorative(Collections.singletonList(toscaNodeTemplate.getCapabilities()));
        }
    }

    @Override
    protected Object deserializePropertyValue(String propValue) {
        try {
            return STANDARD_CODER.decode(propValue, Object.class);
        } catch (CoderException ce) {
            String errorMessage = "error decoding property JSON value read from database: " + propValue;
            throw new PfModelRuntimeException(Response.Status.INTERNAL_SERVER_ERROR, errorMessage, ce);
        }
    }

    @Override
    protected String serializePropertyValue(Object propValue) {
        try {
            return STANDARD_CODER.encode(propValue);
        } catch (CoderException ce) {
            String errorMessage = "error encoding property JSON value for database: " + propValue;
            throw new PfModelRuntimeException(Response.Status.INTERNAL_SERVER_ERROR, errorMessage, ce);
        }
    }

    @Override
    public List<PfKey> getKeys() {
        final List<PfKey> keyList = super.getKeys();

        if (requirements != null) {
            keyList.addAll(requirements.getKeys());
        }

        if (capabilities != null) {
            keyList.addAll(capabilities.getKeys());
        }

        return keyList;
    }

    @Override
    public void clean() {
        super.clean();

        if (requirements != null) {
            requirements.clean();
        }

        if (capabilities != null) {
            capabilities.clean();
        }
    }

    @Override
    public int compareTo(final PfConcept otherConcept) {
        if (this == otherConcept) {
            return 0;
        }

        int result = super.compareTo(otherConcept);
        if (result != 0) {
            return result;
        }

        final JpaToscaNodeTemplate other = (JpaToscaNodeTemplate) otherConcept;

        result = ObjectUtils.compare(requirements, other.requirements);
        if (result != 0) {
            return result;
        }

        return ObjectUtils.compare(capabilities, other.capabilities);
    }
}
