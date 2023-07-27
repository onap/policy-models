/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019, 2023 Nordix Foundation.
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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.onap.policy.models.base.PfConceptContainer;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;

/**
 * This class is a container for TOSCA service templates.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
@Entity
@Table(name = "ToscaServiceTemplates")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Data
@EqualsAndHashCode(callSuper = true)
public class JpaToscaServiceTemplates extends PfConceptContainer<JpaToscaServiceTemplate, ToscaServiceTemplate> {
    private static final long serialVersionUID = -3053257884307604114L;

    /**
     * The Default Constructor creates a {@link JpaToscaServiceTemplates} object with a null artifact key and creates an
     * empty concept map.
     */
    public JpaToscaServiceTemplates() {
        super(new PfConceptKey());
    }

    /**
     * The Key Constructor creates a {@link JpaToscaServiceTemplates} object with the given artifact key and creates an
     * empty concept map.
     *
     * @param key the concept key
     */
    public JpaToscaServiceTemplates(final PfConceptKey key) {
        super(key, new TreeMap<>());
    }

    /**
     * This Constructor creates an concept container with all of its fields defined.
     *
     * @param key the concept container key
     * @param conceptMap the concepts to be stored in the concept container
     */
    public JpaToscaServiceTemplates(final PfConceptKey key,
            final Map<PfConceptKey, JpaToscaServiceTemplate> conceptMap) {
        super(key, conceptMap);
    }

    /**
     * Copy constructor.
     *
     * @param copyConcept the concept to copy from
     */
    public JpaToscaServiceTemplates(final JpaToscaServiceTemplates copyConcept) {
        super(copyConcept);
    }

    /**
     * Authorative constructor.
     *
     * @param authorativeConceptMapList the authorative concept to copy from
     */
    public JpaToscaServiceTemplates(final List<Map<String, ToscaServiceTemplate>> authorativeConceptMapList) {
        this.fromAuthorative(authorativeConceptMapList);
    }
}
