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

package org.onap.policy.models.sim.dmaap.filter;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import org.junit.Test;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoderObject;

public class FilterTypeAdapterFactoryTest {
    private final Gson gson = new GsonBuilder().registerTypeAdapterFactory(new FilterTypeAdapterFactory()).create();

    @Test
    public void testAndFilterArray_testTest() throws CoderException {
        final StandardCoderObject sco = FilterSupport.makeSco();

        Equals equals1 = new Equals();
        equals1.setField("name");
        equals1.setValue("john");

        Equals equals2 = new Equals();
        equals2.setField("text");
        equals2.setValue("some data");

        And filter1 = new And(equals1, equals2);

        String encoded = gson.toJson(filter1);
        Filter filter2 = gson.fromJson(encoded, Filter.class);

        assertEquals(filter1, filter2);

        // decoded filter should still pass
        assertTrue(filter2.test(sco));
    }

    @Test
    public void testWriteUnknownClass() {
        // create an annonymous class
        Equals filter = new MyFilter();
        assertThatThrownBy(() -> gson.toJson(filter)).isInstanceOf(JsonParseException.class)
                        .hasMessage("Unknown 'filter' class: " + MyFilter.class.getSimpleName());
    }

    @Test
    public void testReadUnknownClass() {
        Equals filter = new Equals();
        String encoded = gson.toJson(filter);
        String encoded2 = encoded.replace(Equals.class.getSimpleName(), "different");

        assertThatThrownBy(() -> gson.fromJson(encoded2, Filter.class)).isInstanceOf(JsonParseException.class)
                        .hasMessage("Unknown 'filter' class: different");
    }

    @Test
    public void testReadNotAnObject() {
        assertThatThrownBy(() -> gson.fromJson("[]", Filter.class)).isInstanceOf(JsonParseException.class)
                        .hasMessage("Expecting a Filter object");
    }

    public static class MyFilter extends Equals {

    }
}
