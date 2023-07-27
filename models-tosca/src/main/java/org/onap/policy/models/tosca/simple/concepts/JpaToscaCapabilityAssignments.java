/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020, 2023 Nordix Foundation.
 *  Modifications Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import java.io.Serial;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import org.onap.policy.common.parameters.BeanValidationResult;
import org.onap.policy.models.base.PfAuthorative;
import org.onap.policy.models.base.PfConceptContainer;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.tosca.authorative.concepts.ToscaCapabilityAssignment;
import org.onap.policy.models.tosca.utils.ToscaUtils;

/**
 * This class is a container for TOSCA capability assignments.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
@Entity
@Table(name = "ToscaCapabilityAssignments")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class JpaToscaCapabilityAssignments
                extends PfConceptContainer<JpaToscaCapabilityAssignment, ToscaCapabilityAssignment>
                implements PfAuthorative<List<Map<String, ToscaCapabilityAssignment>>> {
    public static final String DEFAULT_NAME = "ToscaCapabilityAssignmentsSimple";
    public static final String DEFAULT_VERSION = "1.0.0";
    @Serial
    private static final long serialVersionUID = -7526648702327776101L;

    /**
     * The Default Constructor creates a {@link JpaToscaCapabilityAssignments} object with a null
     * artifact key and creates an empty concept map.
     */
    public JpaToscaCapabilityAssignments() {
        super(new PfConceptKey(DEFAULT_NAME, DEFAULT_VERSION));
    }

    /**
     * The Key Constructor creates a {@link JpaToscaCapabilityAssignments} object with the given
     * artifact key and creates an empty concept map.
     *
     * @param key the concept key
     */
    public JpaToscaCapabilityAssignments(final PfConceptKey key) {
        super(key, new TreeMap<>());
    }

    /**
     * This Constructor creates a concept container with all of its fields defined.
     *
     * @param key the concept container key
     * @param conceptMap the concepts to be stored in the concept container
     */
    public JpaToscaCapabilityAssignments(final PfConceptKey key,
                    final Map<PfConceptKey, JpaToscaCapabilityAssignment> conceptMap) {
        super(key, conceptMap);
    }

    /**
     * Copy constructor.
     *
     * @param copyConcept the concept to copy from
     */
    public JpaToscaCapabilityAssignments(final JpaToscaCapabilityAssignments copyConcept) {
        super(copyConcept);
    }

    /**
     * Authorative constructor.
     *
     * @param authorativeConceptMapList the authorative concept to copy from
     */
    public JpaToscaCapabilityAssignments(final List<Map<String, ToscaCapabilityAssignment>> authorativeConceptMapList) {
        super(new PfConceptKey(DEFAULT_NAME, DEFAULT_VERSION));
        this.fromAuthorative(authorativeConceptMapList);
    }

    @Override
    public BeanValidationResult validate(@NonNull String fieldName) {
        BeanValidationResult result = super.validate(fieldName);

        for (JpaToscaCapabilityAssignment assignment : this.getConceptMap().values()) {
            ToscaUtils.getEntityTypeAncestors(this, assignment, result);
        }

        return result;
    }
}
