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

package org.onap.policy.models.base;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import lombok.Getter;
import lombok.ToString;
import org.junit.Test;

/**
 * Test the PfUtils class.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class PfUtilsTest {

    private static final String HELLO = "hello";

    @Test
    public void testCompareObjects() {
        assertEquals(0, PfUtils.compareObjects(null, null));
        assertEquals(-1, PfUtils.compareObjects(HELLO, null));
        assertEquals(1, PfUtils.compareObjects(null, HELLO));
        assertFalse(PfUtils.compareObjects(HELLO, "goodbye") == 0);
        assertEquals(0, PfUtils.compareObjects(HELLO, HELLO));
    }

    @Test
    public void testMapList() {
        List<Object> resultList = PfUtils.mapList(null, item -> {
            throw new RuntimeException("should not be invoked");
        });
        assertNull(resultList);

        List<String> origList = Arrays.asList("abc", "def");
        List<String> newList = PfUtils.mapList(origList, text -> text + "X");

        assertEquals(Arrays.asList("abcX", "defX"), newList);

        // verify that we can modify the list without throwing an exception
        newList.remove("abcX");
        newList.add("something else");
    }

    @Test
    public void testMapMap() {
        Map<String,String> resultMap = PfUtils.mapMap(null, item -> {
            throw new RuntimeException("should not be invoked");
        });
        assertNull(resultMap);

        Map<String,String> origMap = new TreeMap<>();
        origMap.put("key2A", "xyz2");
        origMap.put("key2B", "pdq2");
        Map<String,String> newMap = PfUtils.mapMap(origMap, text -> text + "X");

        assertEquals("{key2A=xyz2X, key2B=pdq2X}", newMap.toString());

        // verify that we can modify the map without throwing an exception
        newMap.remove("abcX");
        newMap.put("something", "else");
    }

    @Test
    public void testMakeCopy() {
        assertNull(PfUtils.makeCopy((MyObject) null));

        MyObject origObject = new MyObject();
        origObject.name = HELLO;
        assertEquals(origObject.toString(), PfUtils.makeCopy(origObject).toString());

        assertThatThrownBy(() -> PfUtils.makeCopy(new NoCopyConstructor())).isInstanceOf(PfModelRuntimeException.class);
    }

    @Getter
    @ToString
    private static class MyObject {
        private String name;

        public MyObject() {
            // do nothing
        }

        @SuppressWarnings("unused")
        public MyObject(MyObject source) {
            this.name = source.name;
        }
    }

    @Getter
    private static class NoCopyConstructor {
        private String name;
    }
}
