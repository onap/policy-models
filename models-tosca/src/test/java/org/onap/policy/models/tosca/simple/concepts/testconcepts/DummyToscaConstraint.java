/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
 *  Modifications Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

import org.onap.policy.models.tosca.authorative.concepts.ToscaConstraint;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaConstraint;

/**
 * Dummy constraint to test abstract ToscaConstraint class.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class DummyToscaConstraint extends JpaToscaConstraint {
    private static final long serialVersionUID = 1L;

    /**
     * The Default Constructor creates a {@link DummyToscaConstraint} object with a null key.
     */
    public DummyToscaConstraint() {
        // do nothing
    }

    @Override
    public ToscaConstraint toAuthorative() {
        return null;
    }

    @Override
    public void fromAuthorative(ToscaConstraint authorativeConcept) {
        // do nothing
    }

    @Override
    public int compareTo(JpaToscaConstraint otherConstraint) {
        return 0;
    }
}
