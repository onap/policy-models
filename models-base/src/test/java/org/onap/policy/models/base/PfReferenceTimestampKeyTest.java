/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2021 Nordix Foundation.
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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.Instant;
import org.junit.Test;

public class PfReferenceTimestampKeyTest {

    private static final String PARENT_LOCAL_NAME = "ParentLocalName";
    private static final String LOCAL_NAME = "LocalName";
    private static final String VERSION002 = "0.0.2";
    private static final String VERSION001 = "0.0.1";
    private static final long timeStamp = 1613152081L;
    private static final Instant DEFAULT_TIMESTAMP = Instant.EPOCH;

    @Test
    public void testPfReferenceTimestampKeyConstruct() {
        assertThat(new PfReferenceTimestampKey().getLocalName()).isEqualTo(PfKey.NULL_KEY_NAME);
        assertEquals(PfKey.NULL_KEY_NAME, new PfReferenceTimestampKey(new PfConceptKey()).getParentKeyName());
        assertNotNull(new PfReferenceTimestampKey(new PfReferenceTimestampKey()).getTimeStamp());

        assertEquals(LOCAL_NAME, new PfReferenceTimestampKey(new PfReferenceKey(), LOCAL_NAME,
            Instant.ofEpochSecond(timeStamp)).getLocalName());
        assertEquals(Instant.ofEpochSecond(timeStamp), new PfReferenceTimestampKey(new PfConceptKey(),
            PARENT_LOCAL_NAME, LOCAL_NAME, Instant.ofEpochSecond(timeStamp)).getTimeStamp());

        assertThat(new PfReferenceTimestampKey("ParentKeyName", VERSION001, PARENT_LOCAL_NAME,
            LOCAL_NAME, Instant.ofEpochSecond(timeStamp))).isInstanceOf(PfReferenceTimestampKey.class);

        assertThat(new PfReferenceTimestampKey("ParentKeyName", VERSION001, LOCAL_NAME,
            Instant.ofEpochSecond(timeStamp)).getParentLocalName()).isEqualTo(PfKey.NULL_KEY_NAME);

        assertEquals(PfReferenceTimestampKey.getNullKey().getKey(), PfReferenceTimestampKey.getNullKey());
        assertEquals("NULL:0.0.0:NULL:NULL:" + Instant.EPOCH.getEpochSecond(),
            PfReferenceTimestampKey.getNullKey().getId());

        assertThatThrownBy(() -> new PfReferenceTimestampKey(new PfConceptKey(), null, null))
            .hasMessage("parameter \"localName\" is null");

        String id = "NULL:0.0.0:NULL:NULL:" + timeStamp;
        assertThat(new PfReferenceTimestampKey(id).getTimeStamp().getEpochSecond()).isEqualTo(timeStamp);

    }

    @Test
    public void testPfReferenceTimestampKey() {
        PfReferenceTimestampKey testReferenceKey = new PfReferenceTimestampKey();
        testReferenceKey.setParentConceptKey(new PfConceptKey("PN", VERSION001));
        assertEquals("PN:0.0.1", testReferenceKey.getParentConceptKey().getId());

        assertEquals(1, testReferenceKey.getKeys().size());
        assertFalse(testReferenceKey.isNullKey());

        testReferenceKey.setParentReferenceKey(new PfReferenceKey("PN", VERSION001,
            "LN"));
        assertEquals("PN:0.0.1:NULL:LN", testReferenceKey.getParentReferenceKey().getId());

        testReferenceKey.setParentKeyName("PKN");
        assertEquals("PKN", testReferenceKey.getParentKeyName());

        testReferenceKey.setParentKeyVersion(VERSION001);
        assertEquals(VERSION001, testReferenceKey.getParentKeyVersion());

        testReferenceKey.setParentLocalName(PARENT_LOCAL_NAME);
        assertEquals(PARENT_LOCAL_NAME, testReferenceKey.getParentLocalName());

        testReferenceKey.setLocalName("LN");
        assertEquals("LN", testReferenceKey.getLocalName());

        testReferenceKey.setTimeStamp(DEFAULT_TIMESTAMP);
        assertEquals(DEFAULT_TIMESTAMP, testReferenceKey.getTimeStamp());


        assertThatThrownBy(() -> testReferenceKey.isCompatible(null))
            .hasMessageMatching("^otherKey is marked .*on.*ull but is null$");

        assertFalse(testReferenceKey.isCompatible(PfConceptKey.getNullKey()));
        assertFalse(testReferenceKey.isCompatible(PfReferenceKey.getNullKey()));
        assertTrue(testReferenceKey.isCompatible(testReferenceKey));

        assertTrue(testReferenceKey.validate("").isValid());

        testReferenceKey.clean();

        PfReferenceTimestampKey clonedReferenceKey = new PfReferenceTimestampKey(testReferenceKey);

        assertEquals("PfReferenceTimestampKey(super=PfReferenceKey(parentKeyName=PKN, parentKeyVersion=0.0.1, "
                + "parentLocalName=ParentLocalName, localName=LN), timeStamp=" + Instant.EPOCH + ")",
            clonedReferenceKey.toString());

        assertNotEquals(0, testReferenceKey.hashCode());

        assertEquals(testReferenceKey, clonedReferenceKey);
        assertNotEquals(testReferenceKey, new PfReferenceTimestampKey("PKN", VERSION001,
            "PLN", "LN", Instant.ofEpochSecond(timeStamp)));
        testReferenceKey.setTimeStamp(Instant.ofEpochSecond(timeStamp));
        assertEquals(testReferenceKey, new PfReferenceTimestampKey("PKN", VERSION001,
            PARENT_LOCAL_NAME, "LN", Instant.ofEpochSecond(timeStamp)));

        assertNotEquals(0, testReferenceKey.compareTo(new PfConceptKey()));
        assertNotEquals(0, testReferenceKey.compareTo(new PfReferenceTimestampKey("PKN",
            VERSION002, "PLN", "LN", Instant.ofEpochSecond(timeStamp))));
        assertEquals(0, testReferenceKey.compareTo(new PfReferenceTimestampKey("PKN",
            VERSION001, PARENT_LOCAL_NAME, "LN", Instant.ofEpochSecond(timeStamp))));

        assertThatThrownBy(() -> new PfReferenceTimestampKey((PfReferenceTimestampKey) null))
            .isInstanceOf(NullPointerException.class);

        assertEquals(testReferenceKey, new PfReferenceTimestampKey(testReferenceKey));

    }

    @Test
    public void testNewerKey() {
        PfReferenceTimestampKey key1 = new PfReferenceTimestampKey("ParentKeyName", VERSION001,
            PARENT_LOCAL_NAME, LOCAL_NAME, Instant.ofEpochSecond(timeStamp));
        PfReferenceTimestampKey key2 = new PfReferenceTimestampKey(key1);
        assertFalse(key2.isNewerThan(key1));
        assertThatThrownBy(() -> key1.isNewerThan((PfKey) null)).isInstanceOf(NullPointerException.class)
            .hasMessageMatching("^otherKey is marked .*on.*ull but is null$");

        key2.setTimeStamp(Instant.ofEpochSecond(timeStamp).plusSeconds(80));
        assertTrue(key2.isNewerThan(key1));
    }

}
