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

package org.onap.policy.appc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ResponseStatusTest {

    private static final String THERE_S_NO_PLACE_LIKE_HOME = "There's no place like home";
    private static final String THE_WONDERFUL_LAND_OF_OZ = "The wonderful land of Oz";

    @Test
    public void testResonseStatus() {
        ResponseStatus status = new ResponseStatus();
        assertNotNull(status);
        assertNotEquals(0, status.hashCode());

        status.setCode(1234);
        assertEquals(1234, status.getCode());

        status.setDescription(THE_WONDERFUL_LAND_OF_OZ);
        assertEquals(THE_WONDERFUL_LAND_OF_OZ, status.getDescription());

        status.setValue(THERE_S_NO_PLACE_LIKE_HOME);
        assertEquals(THERE_S_NO_PLACE_LIKE_HOME, status.getValue());
        assertNotEquals(0, status.hashCode());

        assertEquals("ResponseStatus [Code=1234, Value=There's no pla", status.toString().substring(0, 47));

        ResponseStatus copiedStatus = new ResponseStatus();
        copiedStatus.setCode(status.getCode());
        copiedStatus.setDescription(status.getDescription());
        copiedStatus.setValue(status.getValue());

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

        status.setDescription(null);
        assertFalse(status.equals(copiedStatus));
        copiedStatus.setDescription(null);
        assertTrue(status.equals(copiedStatus));
        status.setDescription(THE_WONDERFUL_LAND_OF_OZ);
        assertFalse(status.equals(copiedStatus));
        copiedStatus.setDescription(THE_WONDERFUL_LAND_OF_OZ);
        assertTrue(status.equals(copiedStatus));

        status.setValue(null);
        assertFalse(status.equals(copiedStatus));
        copiedStatus.setValue(null);
        assertTrue(status.equals(copiedStatus));
        status.setValue(THERE_S_NO_PLACE_LIKE_HOME);
        assertFalse(status.equals(copiedStatus));
        copiedStatus.setValue(THERE_S_NO_PLACE_LIKE_HOME);
        assertTrue(status.equals(copiedStatus));
    }
}
