/*-
 * ============LICENSE_START=======================================================
 * appc
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

public class LcmResponseStatusTest {

    @Test
    public void testResonseStatus() {
        LcmResponseStatus status = new LcmResponseStatus();
        assertNotNull(status);
        assertNotEquals(0, status.hashCode());

        status.setCode(1234);
        assertEquals(1234, status.getCode());

        status.setMessage("The wonderful land of Oz");
        assertEquals("The wonderful land of Oz", status.getMessage());

        assertEquals("ResponseStatus [code=1234, message=The wonderfu", status.toString().substring(0, 47));

        LcmResponseStatus copiedStatus = new LcmResponseStatus();
        copiedStatus.setCode(status.getCode());
        copiedStatus.setMessage(status.getMessage());

        assertTrue(status.equals(status));
        assertTrue(status.equals(copiedStatus));
        assertFalse(status.equals(null));
        assertFalse(status.equals("Hello"));

        status.setCode(-1);
        assertFalse(status.equals(copiedStatus));
        copiedStatus.setCode(-1);
        assertTrue(status.equals(copiedStatus));
        status.setCode(1234);
        assertFalse(status.equals(copiedStatus));
        copiedStatus.setCode(1234);
        assertTrue(status.equals(copiedStatus));

        status.setMessage(null);
        assertFalse(status.equals(copiedStatus));
        copiedStatus.setMessage(null);
        assertTrue(status.equals(copiedStatus));
        status.setMessage("The wonderful land of Oz");
        assertFalse(status.equals(copiedStatus));
        copiedStatus.setMessage("The wonderful land of Oz");
        assertTrue(status.equals(copiedStatus));
    }
}
