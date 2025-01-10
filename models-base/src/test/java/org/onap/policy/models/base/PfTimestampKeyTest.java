/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2019-2021, 2024-2025 Nordix Foundation.
 * Modifications Copyright (C) 2021 AT&T Intellectual Property. All rights reserved.
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Date;
import org.junit.jupiter.api.Test;

class PfTimestampKeyTest {
    private static final String VERSION001 = "0.0.1";
    private static final String CONCEPT_IS_NULL = "^copyConcept is marked .*on.*ull but is null$";
    private static final String NAME_IS_NULL = "^name is marked .*on.*ull but is null$";
    private static final String VERSION_IS_NULL = "^version is marked .*on.*ull but is null$";
    private static final long TIME_STAMP = 1574832537641L;

    @Test
    void testTimestampKey() {
        PfTimestampKey someKey0 = new PfTimestampKey();
        assertEquals(PfTimestampKey.getNullKey(), someKey0);
        assertTrue(someKey0.isNullKey());
        assertEquals("PfTimestampKey(name=NULL, version=0.0.0, timeStamp=" + Date.from(Instant.EPOCH) + ")",
                someKey0.toString());

        PfTimestampKey someKey1 = new PfTimestampKey("my-name", VERSION001, Instant.ofEpochSecond(TIME_STAMP));
        PfTimestampKey someKey2 = new PfTimestampKey(someKey1);
        PfTimestampKey someKey3 = new PfTimestampKey(someKey1.getId());
        assertEquals(someKey1, someKey2);
        assertEquals(someKey1, someKey3);
        assertFalse(someKey1.isNullVersion());
        assertEquals("PfTimestampKey(name=my-name, version=0.0.1, timeStamp="
                + Date.from(Instant.ofEpochSecond(TIME_STAMP)) + ")", someKey1.toString());

        assertEquals("my-name", someKey1.getName());
        assertEquals(VERSION001, someKey1.getVersion());

        assertEquals(someKey2, someKey1.getKey());
        assertEquals(1, someKey1.getKeys().size());
        assertThatThrownBy(() -> someKey0.setName(null)).isInstanceOf(NullPointerException.class)
                .hasMessageMatching(NAME_IS_NULL);
        assertThatThrownBy(() -> someKey0.setVersion(null)).isInstanceOf(NullPointerException.class)
                .hasMessageMatching(VERSION_IS_NULL);
        assertThatThrownBy(() -> someKey0.setTimeStamp(null)).isInstanceOf(NullPointerException.class)
                .hasMessageMatching("^timeStamp is marked .*on.*ull but is null$");

        assertFalse(someKey1.isNewerThan(someKey2));
        assertThatThrownBy(() -> someKey1.isNewerThan((PfKey) null)).isInstanceOf(NullPointerException.class)
                .hasMessageMatching("^otherKey is marked .*on.*ull but is null$");
        someKey2.setTimeStamp(Date.from(Instant.ofEpochSecond(TIME_STAMP).plusMillis(90)));
        assertTrue(someKey2.isNewerThan(someKey1));
        someKey3.setName("my-name3");
        assertTrue(someKey3.isNewerThan(someKey1));

        assertEquals(-1, someKey1.compareTo(someKey2));
        assertEquals(-1, someKey1.compareTo(someKey3));
        assertThatThrownBy(() -> someKey1.compareTo((PfConcept) null)).isInstanceOf(NullPointerException.class)
                .hasMessageMatching("^otherObj is marked .*on.*ull but is null$");

        PfTimestampKey someKey4 = new PfTimestampKey("NULL", "0.0.0", Instant.ofEpochSecond(TIME_STAMP));
        assertFalse(someKey4.isNullKey());
        assertFalse(someKey1.isNullKey());
    }

    @Test
    void testTimestampKeyErrors() {
        assertThatThrownBy(() -> new PfTimestampKey((PfTimestampKey) null)).isInstanceOf(NullPointerException.class)
                .hasMessageMatching(CONCEPT_IS_NULL);
        assertThatThrownBy(() -> new PfTimestampKey(null, null, null)).isInstanceOf(NullPointerException.class)
                .hasMessageMatching(NAME_IS_NULL);
        assertThatThrownBy(() -> new PfTimestampKey("my-name", null, null)).isInstanceOf(NullPointerException.class)
                .hasMessageMatching(VERSION_IS_NULL);
        assertThatThrownBy(() -> new PfTimestampKey("my-name", VERSION001, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageMatching("^instant is marked .*on.*ull but is null$");
    }
}
