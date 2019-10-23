/*-
 * ============LICENSE_START=======================================================
 * appclcm
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019 Nordix Foundation.
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

package org.onap.policy.appclcm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class AppcLcmBodyTest {

    @Test
    public void testAppcLcmBodyInput() {
        AppcLcmBody body = new AppcLcmBody();
        assertNotNull(body);
        assertNotEquals(0, body.hashCode());

        AppcLcmInput input = new AppcLcmInput();

        body.setInput(input);
        assertEquals(input, body.getInput());

        assertNotEquals(0, body.hashCode());

        assertEquals("AppcLcmBody [input=AppcLcmInput [commonHeader=null",
                       body.toString().substring(0, 50));

        AppcLcmBody copiedBody = new AppcLcmBody();
        copiedBody.setInput(body.getInput());

        assertTrue(body.equals(body));
        assertTrue(body.equals(copiedBody));
        assertFalse(body.equals(null));
        assertFalse(body.equals("Hello"));

        body.setInput(null);
        assertFalse(body.equals(copiedBody));
        copiedBody.setInput(null);
        assertTrue(body.equals(copiedBody));
        body.setInput(input);
        assertFalse(body.equals(copiedBody));
        copiedBody.setInput(input);
        assertTrue(body.equals(copiedBody));
    }

    @Test
    public void testAppcLcmBodyOutput() {
        AppcLcmBody body = new AppcLcmBody();
        assertNotNull(body);
        assertNotEquals(0, body.hashCode());

        AppcLcmOutput output = new AppcLcmOutput();

        body.setOutput(output);
        assertEquals(output, body.getOutput());

        assertNotEquals(0, body.hashCode());

        assertEquals("AppcLcmBody [input=null, output=AppcLcmOutput [commonHeader=null",
                       body.toString().substring(0, 64));

        AppcLcmBody copiedBody = new AppcLcmBody();
        copiedBody.setOutput(body.getOutput());

        assertTrue(body.equals(body));
        assertTrue(body.equals(copiedBody));
        assertFalse(body.equals(null));
        assertFalse(body.equals("Hello"));

        body.setOutput(null);
        assertFalse(body.equals(copiedBody));
        copiedBody.setOutput(null);
        assertTrue(body.equals(copiedBody));
        body.setOutput(output);
        assertFalse(body.equals(copiedBody));
        copiedBody.setOutput(output);
        assertTrue(body.equals(copiedBody));
    }
}
