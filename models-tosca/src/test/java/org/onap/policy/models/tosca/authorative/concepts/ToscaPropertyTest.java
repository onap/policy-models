/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2020 Nordix Foundation.
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

package org.onap.policy.models.tosca.authorative.concepts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaProperty;

public class ToscaPropertyTest {
    @Test
    public void testToscaPropertyDefaultValue() {
        ToscaProperty property = new ToscaProperty();
        property.setName("test");
        property.setType("testType");

        JpaToscaProperty jpaProperty = new JpaToscaProperty(property);
        assertNull(jpaProperty.getDefaultValue());
        ToscaProperty outProperty = jpaProperty.toAuthorative();
        assertNull(outProperty.getDefaultValue());

        List<String> testList = new ArrayList<>();
        property.setDefaultValue(testList);
        jpaProperty = new JpaToscaProperty(property);
        assertEquals("[]", jpaProperty.getDefaultValue());
        outProperty = jpaProperty.toAuthorative();
        assertEquals("[]", outProperty.getDefaultValue().toString());

        testList.add("Foo");
        testList.add("Bar");
        jpaProperty = new JpaToscaProperty(property);
        assertEquals("- Foo\n- Bar", jpaProperty.getDefaultValue());
        outProperty = jpaProperty.toAuthorative();
        assertEquals("[Foo, Bar]", outProperty.getDefaultValue().toString());
    }
}
