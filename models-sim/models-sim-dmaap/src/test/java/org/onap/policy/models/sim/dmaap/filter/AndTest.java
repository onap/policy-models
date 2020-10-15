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

public class AndTest {

    @Test
    public void testAnd() throws CoderException {
        And filter = new And();
        assertTrue(filter.test(null));
    }

    @Test
    public void testAndFilterArray_testTest() throws CoderException {
        final StandardCoderObject sco = FilterSupport.makeSco();

        Equals filt1 = new Equals();
        filt1.setField("name");
        filt1.setValue("john");

        Equals filt2 = new Equals();
        filt2.setField("text");
        filt2.setValue("some data");

        And filter = new And(filt1, filt2);

        // both pass
        assertTrue(filter.test(sco));

        // first fails
        filt1.setValue("doe");
        assertFalse(filter.test(sco));

        // both pass
        filt1.setValue("john");
        assertTrue(filter.test(sco));

        // second fails
        filt2.setValue("different data");
        assertFalse(filter.test(sco));

        // both fail
        filt1.setValue("smith");
        assertFalse(filter.test(sco));
    }
}
