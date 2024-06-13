/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019, 2024 Nordix Foundation.
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

package org.onap.policy.models.base;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.TreeSet;
import org.junit.jupiter.api.Test;

/**
 * Test the given concept class.
 */
class PfConceptGetterImplTest {

    private static final String VERSION002 = "0.0.2";
    private static final String VERSION001 = "0.0.1";

    @Test
    void testPfConceptGetterImpl() {
        NavigableMap<PfConceptKey, PfConceptKey> keyMap = new TreeMap<>();

        PfConceptGetterImpl<PfConceptKey> getter = new PfConceptGetterImpl<>(keyMap);
        assertNotNull(getter);

        PfConceptKey keyA = new PfConceptKey("A", VERSION001);
        assertNull(getter.get(keyA));

        assertThatThrownBy(() -> getter.get((String) null)).hasMessage("conceptKeyName may not be null");

        assertNull(getter.get("W"));

        PfConceptKey keyZ = new PfConceptKey("Z", VERSION001);
        keyMap.put(keyZ, keyZ);
        assertNull(getter.get("W"));

        PfConceptKey keyW001 = new PfConceptKey("W", VERSION001);
        keyMap.put(keyW001, keyW001);
        assertEquals(keyW001, getter.get("W"));

        PfConceptKey keyW002 = new PfConceptKey("W", VERSION002);
        keyMap.put(keyW002, keyW002);
        assertEquals(keyW002, getter.get("W"));

        keyMap.remove(keyZ);
        assertEquals(keyW002, getter.get("W"));

        assertThatThrownBy(() -> getter.get((String) null, VERSION001)).hasMessage("conceptKeyName may not be null");

        assertEquals(keyW002, getter.get("W", VERSION002));
        assertEquals(keyW002, getter.get("W", (String) null));

        assertEquals(new TreeSet<PfConceptKey>(keyMap.values()), getter.getAll(null));
        assertEquals(new TreeSet<PfConceptKey>(keyMap.values()), getter.getAll(null, null));

        assertEquals(keyW001, getter.getAll("W", null).iterator().next());
        assertEquals(keyW002, getter.getAll("W", VERSION002).iterator().next());
        assertEquals(0, getter.getAll("A", null).size());
        assertEquals(0, getter.getAll("Z", null).size());

        keyMap.put(keyZ, keyZ);
        assertEquals(keyW002, getter.getAll("W", VERSION002).iterator().next());
    }
}
