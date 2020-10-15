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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoderObject;

public class EqualsTest {

    @Test
    public void testTest() throws CoderException {
        StandardCoderObject sco = FilterSupport.makeSco();

        Equals filter = new Equals();

        // everything is null
        assertTrue(filter.test(sco));

        // field not found - should match null
        filter.setField("unknown-field");
        assertTrue(filter.test(sco));

        // field not found, but value is non-null - should not match
        filter.setValue("some value");
        assertFalse(filter.test(sco));

        // field exists, but value is null - should not match
        filter.setField("name");
        filter.setValue(null);
        assertFalse(filter.test(sco));

        // field matches value
        filter.setField("name");
        filter.setValue("john");
        assertTrue(filter.test(sco));

        // field does not match
        filter.setValue("hn");
        assertFalse(filter.test(sco));

        // zap field so we can test string comparison
        filter.setField(null);
    }
}
