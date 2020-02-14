/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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
 * ============LICENSE_END=========================================================
 */

package org.onap.policy.controlloop.actorserviceprovider.topic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.utils.coder.StandardCoderObject;
import org.onap.policy.controlloop.actorserviceprovider.Util;

public class SelectorKeyTest {
    private static final String FIELD1 = "map";
    private static final String FIELD2 = "abc";
    private static final String FIELDX = "abd";

    private SelectorKey key;

    @Before
    public void setUp() {
        key = new SelectorKey(FIELD1, FIELD2);
    }

    @Test
    public void testHashCode_testEquals() {
        SelectorKey key2 = new SelectorKey(FIELD1, FIELD2);
        assertEquals(key, key2);
        assertEquals(key.hashCode(), key2.hashCode());

        key2 = new SelectorKey(FIELD1, FIELDX);
        assertNotEquals(key, key2);
        assertNotEquals(key.hashCode(), key2.hashCode());

        // test empty key
        key = new SelectorKey();
        key2 = new SelectorKey();
        assertEquals(key, key2);
        assertEquals(key.hashCode(), key2.hashCode());
    }

    @Test
    public void testExtractField() {
        Map<String, Object> map = Map.of("hello", "world", FIELD1, Map.of("another", "", FIELD2, "value B"));
        StandardCoderObject sco = Util.translate("", map, StandardCoderObject.class);

        String result = key.extractField(sco);
        assertNotNull(result);
        assertEquals("value B", result);

        // shorter key
        assertEquals("world", new SelectorKey("hello").extractField(sco));
        assertNull(new SelectorKey("bye").extractField(sco));

        // not found
        assertNull(new SelectorKey(FIELD1, "not field 2").extractField(sco));

        // test with empty key
        assertNull(new SelectorKey().extractField(sco));
    }

    @Getter
    @Setter
    @Builder
    protected static class Data {
        private String text;
        private Map<String, String> map;
    }
}
