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

package org.onap.policy.models.tosca.simple.concepts.testconcepts;

import javax.ws.rs.core.Response;

import org.onap.policy.models.base.PfModelRuntimeException;
import org.onap.policy.models.base.PfReferenceKey;
import org.onap.policy.models.tosca.simple.concepts.ToscaConstraint;

/**
 * Dummy constraint to test abstract ToscaConstraint class.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class DummyToscaConstraint extends ToscaConstraint {
    private static final long serialVersionUID = 1L;

    /**
     * The Default Constructor creates a {@link DummyToscaConstraint} object with a null key.
     */
    public DummyToscaConstraint() {
        super(new PfReferenceKey());
    }

    /**
     * The Key Constructor creates a {@link DummyToscaConstraint} object with the given concept key.
     *
     * @param key the key of the constraint
     */
    public DummyToscaConstraint(final PfReferenceKey key) {
        super(key);
    }

    /**
     * Copy constructor.
     *
     * @param copyConcept the concept to copy from
     */
    public DummyToscaConstraint(final DummyToscaConstraint copyConcept) {
        super(copyConcept);
        throw new PfModelRuntimeException(Response.Status.INTERNAL_SERVER_ERROR, "cannot copy an immutable constraint");
    }
}
