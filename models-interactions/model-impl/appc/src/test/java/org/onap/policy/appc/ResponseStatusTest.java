/*-
 * ============LICENSE_START=======================================================
 * appc
 * ================================================================================
 * Copyright (C) 2017-2020 AT&T Intellectual Property. All rights reserved.
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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

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

        assertEquals(status, (Object) status);
        assertEquals(status, copiedStatus);
        assertNotEquals(status, null);
        assertNotEquals(status, (Object) "Hello");

        status.setCode(-1);
        assertNotEquals(status, copiedStatus);
        copiedStatus.setCode(-1);
        assertEquals(status, copiedStatus);
        status.setCode(1234);
        assertNotEquals(status, copiedStatus);
        copiedStatus.setCode(1234);
        assertEquals(status, copiedStatus);

        status.setDescription(null);
        assertNotEquals(status, copiedStatus);
        copiedStatus.setDescription(null);
        assertEquals(status, copiedStatus);
        status.setDescription(THE_WONDERFUL_LAND_OF_OZ);
        assertNotEquals(status, copiedStatus);
        copiedStatus.setDescription(THE_WONDERFUL_LAND_OF_OZ);
        assertEquals(status, copiedStatus);

        status.setValue(null);
        assertNotEquals(status, copiedStatus);
        copiedStatus.setValue(null);
        assertEquals(status, copiedStatus);
        status.setValue(THERE_S_NO_PLACE_LIKE_HOME);
        assertNotEquals(status, copiedStatus);
        copiedStatus.setValue(THERE_S_NO_PLACE_LIKE_HOME);
        assertEquals(status, copiedStatus);
    }
}
