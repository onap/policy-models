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

package org.onap.policy.models.base.testconcepts;

import java.util.Map;
import lombok.NonNull;
import org.onap.policy.models.base.PfConceptContainer;
import org.onap.policy.models.base.PfConceptKey;

/**
 * Dummy container for PF concepts.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class DummyBadPfConceptContainer extends PfConceptContainer<DummyBadPfConcept, DummyAuthorativeConcept> {
    private static final long serialVersionUID = -3018432331484294280L;


    /**
     * The Default Constructor creates a {@link DummyBadPfConceptContainer} object with a null artifact key
     * and creates an empty concept map.
     */
    public DummyBadPfConceptContainer() {
        super();
    }

    /**
     * The Key Constructor creates a {@link DummyBadPfConceptContainer} object with the given artifact key and
     * creates an empty concept map.
     *
     * @param key the concept key
     */
    public DummyBadPfConceptContainer(@NonNull final PfConceptKey key) {
        super(key);
    }

    /**
     * This Constructor creates an concept container with all of its fields defined.
     *
     * @param key the concept container key
     * @param conceptMap the concepts to be stored in the concept container
     */
    public DummyBadPfConceptContainer(@NonNull final PfConceptKey key,
            @NonNull final Map<PfConceptKey, DummyBadPfConcept> conceptMap) {
        super(key, conceptMap);
    }

    /**
     * Copy constructor.
     *
     * @param copyConcept the concept to copy from
     */
    public DummyBadPfConceptContainer(@NonNull final DummyBadPfConceptContainer copyConcept) {
        super(copyConcept);
    }

}
