/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2020 Nordix Foundation.
 *  Modifications Copyright (C) 2019-2020 AT&T Intellectual Property. All rights reserved.
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
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyType;
import org.onap.policy.models.tosca.utils.ToscaUtils;

/**
 * This class is a container for TOSCA policy types.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
@Entity
@Table(name = "ToscaPolicyTypes")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class JpaToscaPolicyTypes extends PfConceptContainer<JpaToscaPolicyType, ToscaPolicyType> {
    private static final long serialVersionUID = -4157979965271220098L;

    public static final String DEFAULT_NAME = "ToscaPolicyTypesSimple";
    public static final String DEFAULT_VERSION = "1.0.0";

    /**
     * The Default Constructor creates a {@link JpaToscaPolicyTypes} object with a null artifact key and creates an
     * empty concept map.
     */
    public JpaToscaPolicyTypes() {
        super(new PfConceptKey(DEFAULT_NAME, DEFAULT_VERSION));
    }

    /**
     * The Key Constructor creates a {@link JpaToscaPolicyTypes} object with the given artifact key and creates an empty
     * concept map.
     *
     * @param key the concept key
     */
    public JpaToscaPolicyTypes(final PfConceptKey key) {
        super(key, new TreeMap<>());
    }

    /**
     * This Constructor creates an concept container with all of its fields defined.
     *
     * @param key the concept container key
     * @param conceptMap the concepts to be stored in the concept container
     */
    public JpaToscaPolicyTypes(final PfConceptKey key, final Map<PfConceptKey, JpaToscaPolicyType> conceptMap) {
        super(key, conceptMap);
    }

    /**
     * Copy constructor.
     *
     * @param copyConcept the concept to copy from
     */
    public JpaToscaPolicyTypes(final JpaToscaPolicyTypes copyConcept) {
        super(copyConcept);
    }

    /**
     * Authorative constructor.
     *
     * @param authorativeConceptMapList the authorative concept to copy from
     */
    public JpaToscaPolicyTypes(final List<Map<String, ToscaPolicyType>> authorativeConceptMapList) {
        super(new PfConceptKey(DEFAULT_NAME, DEFAULT_VERSION));
        this.fromAuthorative(authorativeConceptMapList);
    }

    @Override
    public PfValidationResult validate(@NonNull final PfValidationResult resultIn) {
        PfValidationResult result = super.validate(resultIn);

        // Check that all ancestors of this policy type exist
        for (JpaToscaPolicyType policyType : this.getConceptMap().values()) {
            ToscaUtils.getEntityTypeAncestors(this, policyType, result);
        }

        return result;
    }
}
