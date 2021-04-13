/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation.
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

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PfGeneratedIdKeyTest {
    private static final String VERSION001 = "0.0.1";
    private static final String CONCEPT_IS_NULL = "^copyConcept is marked .*on.*ull but is null$";
    private static final String NAME_IS_NULL = "^name is marked .*on.*ull but is null$";
    private static final String VERSION_IS_NULL = "^version is marked .*on.*ull but is null$";
    private static final long generatedId = 10001L;

    @Test
    public void testGeneratedIdKey() {
        PfGeneratedIdKey someKey0 = new PfGeneratedIdKey();
        assertEquals(PfGeneratedIdKey.getNullKey(), someKey0);
        assertTrue(someKey0.isNullKey());
        assertEquals("PfGeneratedIdKey(name=NULL, version=0.0.0, generatedId=null)",
                someKey0.toString());

        PfGeneratedIdKey someKey1 = new PfGeneratedIdKey("my-name", VERSION001, generatedId);
        PfGeneratedIdKey someKey2 = new PfGeneratedIdKey(someKey1);
        PfGeneratedIdKey someKey3 = new PfGeneratedIdKey(someKey1.getId());
        assertEquals(someKey1, someKey2);
        assertEquals(someKey1, someKey3);
        assertFalse(someKey1.isNullVersion());
        assertEquals("PfGeneratedIdKey(name=my-name, version=0.0.1, generatedId="
                + generatedId + ")", someKey1.toString());

        assertEquals("my-name", someKey1.getName());
        assertEquals(VERSION001, someKey1.getVersion());

        assertEquals(someKey2, someKey1.getKey());
        assertEquals(1, someKey1.getKeys().size());
        assertThatThrownBy(() -> someKey0.setName(null)).isInstanceOf(NullPointerException.class)
                .hasMessageMatching(NAME_IS_NULL);
        assertThatThrownBy(() -> someKey0.setVersion(null)).isInstanceOf(NullPointerException.class)
                .hasMessageMatching(VERSION_IS_NULL);
        assertThatCode(() -> someKey0.setGeneratedId(null)).doesNotThrowAnyException();

        assertFalse(someKey1.isNewerThan(someKey2));
        assertThatThrownBy(() -> someKey1.isNewerThan((PfKey) null)).isInstanceOf(NullPointerException.class)
                .hasMessageMatching("^otherKey is marked .*on.*ull but is null$");
        someKey2.setGeneratedId(generatedId + 1);
        assertTrue(someKey2.isNewerThan(someKey1));
        someKey3.setName("my-name3");
        assertTrue(someKey3.isNewerThan(someKey1));

        assertEquals(-1, someKey1.compareTo(someKey2));
        assertEquals(-1, someKey1.compareTo(someKey3));
        assertThatThrownBy(() -> someKey1.compareTo((PfConcept) null)).isInstanceOf(NullPointerException.class)
                .hasMessageMatching("^otherObj is marked .*on.*ull but is null$");

        PfGeneratedIdKey someKey4 = new PfGeneratedIdKey("NULL", "0.0.0", generatedId);
        assertFalse(someKey4.isNullKey());
        assertFalse(someKey1.isNullKey());
    }

    @Test
    public void testTimestampKeyErrors() {
        assertThatThrownBy(() -> new PfGeneratedIdKey((PfGeneratedIdKey) null)).isInstanceOf(NullPointerException.class)
                .hasMessageMatching(CONCEPT_IS_NULL);
        assertThatThrownBy(() -> new PfGeneratedIdKey(null, null, null)).isInstanceOf(NullPointerException.class)
                .hasMessageMatching(NAME_IS_NULL);
        assertThatThrownBy(() -> new PfGeneratedIdKey("my-name", null, null)).isInstanceOf(NullPointerException.class)
                .hasMessageMatching(VERSION_IS_NULL);
        assertThatCode(() -> new PfGeneratedIdKey("my-name", VERSION001, null))
            .doesNotThrowAnyException();
    }
}
