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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Test;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyType;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaPolicyType;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaPolicyTypes;

public class JpaToscaPolicyTypesTest {

    @Test
    public void testPolicyTypes() {
        assertNotNull(new JpaToscaPolicyTypes());
        assertNotNull(new JpaToscaPolicyTypes(new PfConceptKey()));
        assertNotNull(new JpaToscaPolicyTypes(new PfConceptKey(), new TreeMap<PfConceptKey, JpaToscaPolicyType>()));
        assertNotNull(new JpaToscaPolicyTypes(new JpaToscaPolicyTypes()));

        try {
            new JpaToscaPolicyTypes((PfConceptKey) null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("key is marked @NonNull but is null", exc.getMessage());
        }

        try {
            new JpaToscaPolicyTypes((JpaToscaPolicyTypes) null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("copyConcept is marked @NonNull but is null", exc.getMessage());
        }

        try {
            new JpaToscaPolicyTypes(null, null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("key is marked @NonNull but is null", exc.getMessage());
        }

        try {
            new JpaToscaPolicyTypes(new PfConceptKey(), null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("conceptMap is marked @NonNull but is null", exc.getMessage());
        }

        try {
            new JpaToscaPolicyTypes(null, new TreeMap<PfConceptKey, JpaToscaPolicyType>());
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("key is marked @NonNull but is null", exc.getMessage());
        }

        List<Map<String, ToscaPolicyType>> ptMapList = new ArrayList<>();
        ptMapList.add(new LinkedHashMap<>());
        ptMapList.get(0).put("policyType", new ToscaPolicyType());
        assertNotNull(new JpaToscaPolicyTypes(ptMapList));
    }
}
