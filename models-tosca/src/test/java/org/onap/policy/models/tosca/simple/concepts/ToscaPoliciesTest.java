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

package org.onap.policy.models.tosca.simple.concepts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.TreeMap;

import org.junit.Test;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.tosca.simple.concepts.ToscaPolicies;
import org.onap.policy.models.tosca.simple.concepts.ToscaPolicy;

public class ToscaPoliciesTest {

    @Test
    public void testPolicies() {
        assertNotNull(new ToscaPolicies());
        assertNotNull(new ToscaPolicies(new PfConceptKey()));
        assertNotNull(new ToscaPolicies(new PfConceptKey(), new TreeMap<PfConceptKey, ToscaPolicy>()));
        assertNotNull(new ToscaPolicies(new ToscaPolicies()));

        try {
            new ToscaPolicies((PfConceptKey) null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("key is marked @NonNull but is null", exc.getMessage());
        }

        try {
            new ToscaPolicies((ToscaPolicies) null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("copyConcept is marked @NonNull but is null", exc.getMessage());
        }

        try {
            new ToscaPolicies(null, null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("key is marked @NonNull but is null", exc.getMessage());
        }

        try {
            new ToscaPolicies(new PfConceptKey(), null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("conceptMap is marked @NonNull but is null", exc.getMessage());
        }

        try {
            new ToscaPolicies(null, new TreeMap<PfConceptKey, ToscaPolicy>());
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("key is marked @NonNull but is null", exc.getMessage());
        }
    }
}
