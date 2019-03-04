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

package org.onap.policy.models.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.TreeSet;

import org.junit.Test;

/**
 * Test the AxConceptGetterImpl class.
 */
public class PfConceptGetterImplTest {

    @Test
    public void testAxConceptGetterImpl() {
        NavigableMap<PfConceptKey, PfConceptKey> keyMap = new TreeMap<>();

        PfConceptGetterImpl<PfConceptKey> getter = new PfConceptGetterImpl<>(keyMap);
        assertNotNull(getter);

        PfConceptKey keyA = new PfConceptKey("A", "0.0.1");
        assertNull(getter.get(keyA));

        try {
            getter.get((String)null);
            fail("test should throw an exception here");
        }
        catch (Exception getException) {
            assertEquals("conceptKeyName may not be null", getException.getMessage());
        }

        assertNull(getter.get("W"));

        PfConceptKey keyZ = new PfConceptKey("Z", "0.0.1");
        keyMap.put(keyZ, keyZ);
        assertNull(getter.get("W"));

        PfConceptKey keyW001 = new PfConceptKey("W", "0.0.1");
        keyMap.put(keyW001, keyW001);
        assertEquals(keyW001, getter.get("W"));

        PfConceptKey keyW002 = new PfConceptKey("W", "0.0.2");
        keyMap.put(keyW002, keyW002);
        assertEquals(keyW002, getter.get("W"));

        keyMap.remove(keyZ);
        assertEquals(keyW002, getter.get("W"));

        try {
            getter.get((String)null, "0.0.1");
            fail("test should throw an exception here");
        }
        catch (Exception getException) {
            assertEquals("conceptKeyName may not be null", getException.getMessage());
        }

        assertEquals(keyW002, getter.get("W", "0.0.2"));
        assertEquals(keyW002, getter.get("W", (String)null));

        assertEquals(new TreeSet<PfConceptKey>(keyMap.values()), getter.getAll(null));
        assertEquals(new TreeSet<PfConceptKey>(keyMap.values()), getter.getAll(null, null));

        assertEquals(keyW001, getter.getAll("W", null).iterator().next());
        assertEquals(keyW002, getter.getAll("W", "0.0.2").iterator().next());
        assertEquals(0, getter.getAll("A", null).size());
        assertEquals(0, getter.getAll("Z", null).size());

        keyMap.put(keyZ, keyZ);
        assertEquals(keyW002, getter.getAll("W", "0.0.2").iterator().next());
    }
}
