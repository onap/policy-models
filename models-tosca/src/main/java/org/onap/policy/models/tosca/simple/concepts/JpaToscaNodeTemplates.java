/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix Foundation.
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

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import org.onap.policy.models.base.PfConceptContainer;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfValidationResult;
import org.onap.policy.models.tosca.authorative.concepts.ToscaNodeTemplate;
import org.onap.policy.models.tosca.utils.ToscaUtils;

/**
 * This class is a container for TOSCA node templates.
 */
@Entity
@Table(name = "ToscaNodeTemplates")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class JpaToscaNodeTemplates extends PfConceptContainer<JpaToscaNodeTemplate, ToscaNodeTemplate> {
    public static final String DEFAULT_NAME = "ToscaNodeTemplatesSimple";
    public static final String DEFAULT_VERSION = "1.0.0";
    private static final long serialVersionUID = -4157979965271220098L;

    /**
     * The Default Constructor creates a {@link JpaToscaNodeTemplates} object with a null artifact key and creates an
     * empty concept map.
     */
    public JpaToscaNodeTemplates() {
        super(new PfConceptKey(DEFAULT_NAME, DEFAULT_VERSION));
    }

    /**
     * The Key Constructor creates a {@link JpaToscaNodeTemplates} object with the given artifact key and creates an
     * empty concept map.
     *
     * @param key the concept key
     */
    public JpaToscaNodeTemplates(final PfConceptKey key) {
        super(key, new TreeMap<>());
    }

    /**
     * This Constructor creates an concept container with all of its fields defined.
     *
     * @param key        the concept container key
     * @param conceptMap the concepts to be stored in the concept container
     */
    public JpaToscaNodeTemplates(final PfConceptKey key, final Map<PfConceptKey, JpaToscaNodeTemplate> conceptMap) {
        super(key, conceptMap);
    }

    /**
     * Copy constructor.
     *
     * @param copyConcept the concept to copy from
     */
    public JpaToscaNodeTemplates(final JpaToscaNodeTemplates copyConcept) {
        super(copyConcept);
    }

    /**
     * Authorative constructor.
     *
     * @param authorativeConceptMapList the authorative concept to copy from
     */
    public JpaToscaNodeTemplates(final List<Map<String, ToscaNodeTemplate>> authorativeConceptMapList) {
        super(new PfConceptKey(DEFAULT_NAME, DEFAULT_VERSION));
        this.fromAuthorative(authorativeConceptMapList);
    }

    @Override
    public PfValidationResult validate(@NonNull final PfValidationResult resultIn) {
        PfValidationResult result = super.validate(resultIn);

        // Check that all ancestors of this node template exist
        for (JpaToscaNodeTemplate nodeTemplate : this.getConceptMap().values()) {
            ToscaUtils.getEntityTypeAncestors(this, nodeTemplate, result);
        }

        return result;
    }
}
