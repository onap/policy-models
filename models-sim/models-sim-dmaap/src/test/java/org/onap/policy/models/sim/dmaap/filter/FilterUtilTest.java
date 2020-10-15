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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoderObject;

public class FilterUtilTest {

    @Test
    public void testBuild() throws CoderException {
        StandardCoderObject sco = FilterSupport.makeSco();

        // null
        assertNull(FilterUtil.build(sco, null));

        // empty
        assertEquals("", FilterUtil.build(sco, ""));

        // entire string is a literal
        assertEquals("all literal", FilterUtil.build(sco, "all literal"));

        // only a field name
        assertEquals("some data", FilterUtil.build(sco, "${text}"));

        // mix
        assertEquals("give john some data", FilterUtil.build(sco, "give ${name} ${text}"));

        // unknown field
        assertEquals("unknown field", FilterUtil.build(sco, "unknown ${notFound}field"));

        // nested
        assertEquals("joe", FilterUtil.build(sco, "${nested.name}"));
    }

    @Test
    public void testExtract() throws CoderException {
        StandardCoderObject sco = FilterSupport.makeSco();

        assertNull(FilterUtil.extract(sco, null));

        // unknown field
        assertNull(FilterUtil.extract(sco, "${unknownField}"));

        // literal field name
        assertEquals("some data", FilterUtil.extract(sco, "text"));

        // indirect
        assertEquals("john", FilterUtil.extract(sco, "${indirect}"));

        // nested
        assertEquals("joe", FilterUtil.extract(sco, "nested.name"));
    }

    @Test
    public void testSplitName() {
        // one component
        assertThat(FilterUtil.splitName("one")).isEqualTo(new Object[]{"one"});

        // multiple components
        assertThat(FilterUtil.splitName("abc.def.ghi")).isEqualTo(new Object[]{"abc", "def", "ghi"});
    }
}
