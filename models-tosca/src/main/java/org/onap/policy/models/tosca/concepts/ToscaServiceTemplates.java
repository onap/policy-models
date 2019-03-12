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

import java.util.Map;
import java.util.TreeMap;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.onap.policy.models.base.PfConceptContainer;
import org.onap.policy.models.base.PfConceptKey;

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
public class ToscaServiceTemplates extends PfConceptContainer<ToscaServiceTemplate> {
    private static final long serialVersionUID = -3053257884307604114L;

    /**
     * The Default Constructor creates a {@link ToscaServiceTemplates} object with a null artifact
     * key and creates an empty concept map.
     */
    public ToscaServiceTemplates() {
        super(new PfConceptKey());
    }

    /**
     * The Key Constructor creates a {@link ToscaServiceTemplates} object with the given artifact
     * key and creates an empty concept map.
     *
     * @param key the concept key
     */
    public ToscaServiceTemplates(final PfConceptKey key) {
        super(key, new TreeMap<PfConceptKey, ToscaServiceTemplate>());
    }

    /**
     * This Constructor creates an concept container with all of its fields defined.
     *
     * @param key the concept container key
     * @param conceptMap the concepts to be stored in the concept container
     */
    public ToscaServiceTemplates(final PfConceptKey key, final Map<PfConceptKey, ToscaServiceTemplate> conceptMap) {
        super(key, conceptMap);
    }

    /**
     * Copy constructor.
     *
     * @param copyConcept the concept to copy from
     */
    public ToscaServiceTemplates(final ToscaServiceTemplates copyConcept) {
        super(copyConcept);
    }
}
