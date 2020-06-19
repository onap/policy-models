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

import lombok.NonNull;
import org.onap.policy.models.base.PfConceptKey;

/**
 * Bad dummy concept throws exception on default constructor.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class DummyBadPfConcept extends DummyPfConcept {
    private static final long serialVersionUID = 1L;

    public DummyBadPfConcept() {
        throw new NumberFormatException();
    }

    /**
     * The Key Constructor creates a {@link DummyPfConcept} object with the given concept key.
     *
     * @param key the key
     */
    public DummyBadPfConcept(@NonNull final PfConceptKey key) {
        throw new NumberFormatException();
    }

    /**
     * Copy constructor.
     *
     * @param copyConcept the concept to copy from
     */
    public DummyBadPfConcept(final DummyBadPfConcept copyConcept) {
        throw new NumberFormatException();
    }
}
