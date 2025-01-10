/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2021, 2024-2025 Nordix Foundation.
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Date;
import org.junit.jupiter.api.Test;

class PfReferenceTimestampKeyTest {

    private static final String PARENT_LOCAL_NAME = "ParentLocalName";
    private static final String LOCAL_NAME = "LocalName";
    private static final String VERSION002 = "0.0.2";
    private static final String VERSION001 = "0.0.1";
    private static final long TIME_STAMP = 1613152081L;
    private static final Instant DEFAULT_TIMESTAMP = Instant.EPOCH;

    @Test
    void testPfReferenceTimestampKeyConstruct() {
        assertThat(new PfReferenceTimestampKey().getReferenceKey().getLocalName()).isEqualTo(PfKey.NULL_KEY_NAME);
        assertEquals(PfKey.NULL_KEY_NAME, new PfReferenceTimestampKey(new PfConceptKey()).getReferenceKey()
            .getParentKeyName());
        assertNotNull(new PfReferenceTimestampKey(new PfReferenceTimestampKey()).getTimeStamp());

        assertEquals(LOCAL_NAME,
                new PfReferenceTimestampKey(new PfReferenceKey(), LOCAL_NAME, Instant.ofEpochSecond(TIME_STAMP))
                        .getReferenceKey().getLocalName());
        assertEquals(Date.from(Instant.ofEpochSecond(TIME_STAMP)), new PfReferenceTimestampKey(new PfConceptKey(),
                PARENT_LOCAL_NAME, LOCAL_NAME, Instant.ofEpochSecond(TIME_STAMP)).getTimeStamp());

        assertThat(new PfReferenceTimestampKey("ParentKeyName", VERSION001, PARENT_LOCAL_NAME, LOCAL_NAME,
                Instant.ofEpochSecond(TIME_STAMP))).isInstanceOf(PfReferenceTimestampKey.class);

        assertThat(
                new PfReferenceTimestampKey("ParentKeyName", VERSION001, LOCAL_NAME, Instant.ofEpochSecond(TIME_STAMP))
                        .getReferenceKey().getParentLocalName()).isEqualTo(PfKey.NULL_KEY_NAME);

        assertEquals(PfReferenceTimestampKey.getNullKey().getKey(), PfReferenceTimestampKey.getNullKey());
        assertEquals("NULL:0.0.0:NULL:NULL:" + Instant.EPOCH.getEpochSecond(),
                PfReferenceTimestampKey.getNullKey().getId());

        assertThatThrownBy(() -> new PfReferenceTimestampKey(new PfConceptKey(), null, null))
                .hasMessage("parameter \"localName\" is null");

        PfReferenceTimestampKey testNullKey = new PfReferenceTimestampKey();
        assertTrue(testNullKey.isNullKey());

        String id = "NULL:0.0.0:NULL:NULL:" + TIME_STAMP;
        assertThat(new PfReferenceTimestampKey(id).getTimeStamp().getTime()).isEqualTo(TIME_STAMP);
    }

    @Test
    void testPfReferenceTimestampKey() {
        PfReferenceTimestampKey testReferenceKey = new PfReferenceTimestampKey();
        testReferenceKey.setReferenceKey(new PfReferenceKey(new PfConceptKey("PN", VERSION001)));
        assertEquals("PN:0.0.1", testReferenceKey.getReferenceKey().getParentConceptKey().getId());

        assertEquals(1, testReferenceKey.getKeys().size());
        assertFalse(testReferenceKey.isNullKey());

        testReferenceKey.setReferenceKey(new PfReferenceKey("PN", VERSION001, "LN"));
        assertEquals("PN:0.0.1:NULL:LN", testReferenceKey.getReferenceKey().getId());

        testReferenceKey.getReferenceKey().setParentKeyName("PKN");
        assertEquals("PKN", testReferenceKey.getReferenceKey().getParentKeyName());

        testReferenceKey.getReferenceKey().setParentKeyVersion(VERSION001);
        assertEquals(VERSION001, testReferenceKey.getReferenceKey().getParentKeyVersion());

        testReferenceKey.getReferenceKey().setParentLocalName(PARENT_LOCAL_NAME);
        assertEquals(PARENT_LOCAL_NAME, testReferenceKey.getReferenceKey().getParentLocalName());

        testReferenceKey.getReferenceKey().setLocalName("LN");
        assertEquals("LN", testReferenceKey.getReferenceKey().getLocalName());

        testReferenceKey.setTimeStamp(Date.from(DEFAULT_TIMESTAMP));
        assertEquals(Date.from(DEFAULT_TIMESTAMP), testReferenceKey.getTimeStamp());


        assertThatThrownBy(() -> testReferenceKey.isCompatible(null))
                .hasMessageMatching("^otherKey is marked .*on.*ull but is null$");

        assertFalse(testReferenceKey.isCompatible(PfConceptKey.getNullKey()));
        assertFalse(testReferenceKey.isCompatible(PfReferenceKey.getNullKey()));
        assertTrue(testReferenceKey.isCompatible(testReferenceKey));

        assertTrue(testReferenceKey.validate("").isValid());

        testReferenceKey.clean();

        PfReferenceTimestampKey clonedReferenceKey = new PfReferenceTimestampKey(testReferenceKey);

        assertEquals("PfReferenceTimestampKey(timeStamp=" + Date.from(Instant.EPOCH) + ","
                + " referenceKey=PfReferenceKey(parentKeyName=PKN, parentKeyVersion=0.0.1, "
                + "parentLocalName=ParentLocalName, localName=LN))", clonedReferenceKey.toString());

        assertNotEquals(0, testReferenceKey.hashCode());

        assertEquals(testReferenceKey, clonedReferenceKey);
        assertNotEquals(testReferenceKey, new PfReferenceTimestampKey("PKN", VERSION001, "PLN",
            "LN", Instant.ofEpochSecond(TIME_STAMP)));
        testReferenceKey.setTimeStamp(Date.from(Instant.ofEpochSecond(TIME_STAMP)));
        assertEquals(testReferenceKey, new PfReferenceTimestampKey("PKN", VERSION001, PARENT_LOCAL_NAME, "LN",
                Instant.ofEpochSecond(TIME_STAMP)));

        assertNotEquals(0, testReferenceKey.compareTo(new PfReferenceTimestampKey()));
        assertNotEquals(0, testReferenceKey.compareTo(new PfReferenceTimestampKey("PKN", VERSION002,
            "PLN", "LN", Instant.ofEpochSecond(TIME_STAMP))));

        assertEquals(0, testReferenceKey.compareTo(new PfReferenceTimestampKey("PKN", VERSION001, PARENT_LOCAL_NAME,
                "LN", Instant.ofEpochSecond(TIME_STAMP))));

        assertThatThrownBy(() -> new PfReferenceTimestampKey((PfReferenceTimestampKey) null))
                .isInstanceOf(NullPointerException.class);

        assertEquals(testReferenceKey, new PfReferenceTimestampKey(testReferenceKey));

    }

    @Test
    void testNewerKey() {
        PfReferenceTimestampKey key1 = new PfReferenceTimestampKey("ParentKeyName", VERSION001, PARENT_LOCAL_NAME,
                LOCAL_NAME, Instant.ofEpochSecond(TIME_STAMP));
        PfReferenceTimestampKey key2 = new PfReferenceTimestampKey(key1);
        assertFalse(key2.isNewerThan(key1));
        assertThatThrownBy(() -> key1.isNewerThan((PfKey) null)).isInstanceOf(NullPointerException.class)
                .hasMessageMatching("^otherKey is marked .*on.*ull but is null$");

        key2.setTimeStamp(Date.from(Instant.ofEpochSecond(TIME_STAMP).plusSeconds(80)));
        assertTrue(key2.isNewerThan(key1));
    }

}
