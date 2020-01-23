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

package org.onap.policy.controlloop.actorserviceprovider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Builder;
import lombok.Data;
import org.junit.Test;

public class UtilTest {

    @Test
    public void testIdent() {
        Object object = new Object();
        String result = Util.ident(object).toString();

        assertNotEquals(object.toString(), result);
        assertThat(result).startsWith("@");
        assertTrue(result.length() > 1);
    }

    @Test
    public void testLogException() {
        // no exception, no log
        AtomicInteger count = new AtomicInteger();
        Util.logException(() -> count.incrementAndGet(), "no error");
        assertEquals(1, count.get());

        // with an exception
        Runnable runnable = () -> {
            count.incrementAndGet();
            throw new IllegalStateException("expected exception");
        };

        Util.logException(runnable, "error with no args");
        Util.logException(runnable, "error {} {} arg(s)", "with", 1);
    }

    @Test
    public void testTranslate() {
        // Abc => Abc
        final Abc abc = Abc.builder().intValue(1).strValue("hello").anotherString("another").build();
        Abc abc2 = Util.translate("abc to abc", abc, Abc.class);
        assertEquals(abc, abc2);

        // Abc => Similar
        Similar sim = Util.translate("abc to similar", abc, Similar.class);
        assertEquals(abc.getIntValue(), sim.getIntValue());
        assertEquals(abc.getStrValue(), sim.getStrValue());

        // Abc => Map
        @SuppressWarnings("unchecked")
        Map<String, Object> map = Util.translate("abc to map", abc, TreeMap.class);
        assertEquals("{anotherString=another, intValue=1, strValue=hello}", map.toString());

        // Map => Map
        @SuppressWarnings("unchecked")
        Map<String, Object> map2 = Util.translate("map to map", map, LinkedHashMap.class);
        assertEquals(map.toString(), map2.toString());

        // Map => Abc
        abc2 = Util.translate("map to abc", map, Abc.class);
        assertEquals(abc, abc2);
    }

    @Test
    public void testTranslateToMap() {
        assertNull(Util.translateToMap("map: null", null));

        // Abc => Map
        final Abc abc = Abc.builder().intValue(2).strValue("world").anotherString("some").build();
        Map<String, Object> map = new TreeMap<>(Util.translateToMap("map: abc to map", abc));
        assertEquals("{anotherString=some, intValue=2, strValue=world}", map.toString());

        // Map => Map
        Map<String, Object> map2 = Util.translateToMap("map: map to map", map);
        assertEquals(map.toString(), map2.toString());

        assertThatIllegalArgumentException().isThrownBy(() -> Util.translateToMap("map: string", "some string"))
                        .withMessageContaining("map: string");
    }

    @Data
    @Builder
    public static class Abc {
        private int intValue;
        private String strValue;
        private String anotherString;
    }

    // this shares some fields with Abc so the data should transfer
    @Data
    @Builder
    public static class Similar {
        private int intValue;
        private String strValue;
    }
}
