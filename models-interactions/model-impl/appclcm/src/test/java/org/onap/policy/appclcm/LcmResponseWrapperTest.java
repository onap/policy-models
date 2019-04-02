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

public class LcmResponseWrapperTest {

    @Test
    public void testLcmResponseWrapperWrapper() {
        LcmResponseWrapper responseWrapper = new LcmResponseWrapper();
        assertNotNull(responseWrapper);
        assertNotEquals(0, responseWrapper.hashCode());

        LcmResponse response = new LcmResponse();

        responseWrapper.setBody(response);
        assertEquals(response, responseWrapper.getBody());

        assertNotEquals(0, responseWrapper.hashCode());

        assertEquals("ResponseWrapper [body=Response [commonHeader=n", responseWrapper.toString().substring(0, 46));

        LcmResponseWrapper copiedLcmResponseWrapper = new LcmResponseWrapper();
        copiedLcmResponseWrapper.setBody(responseWrapper.getBody());

        assertTrue(responseWrapper.equals(responseWrapper));
        assertTrue(responseWrapper.equals(copiedLcmResponseWrapper));
        assertFalse(responseWrapper.equals(null));
        assertFalse(responseWrapper.equals("Hello"));

        responseWrapper.setBody(null);
        assertFalse(responseWrapper.equals(copiedLcmResponseWrapper));
        copiedLcmResponseWrapper.setBody(null);
        assertTrue(responseWrapper.equals(copiedLcmResponseWrapper));
        responseWrapper.setBody(response);
        assertFalse(responseWrapper.equals(copiedLcmResponseWrapper));
        copiedLcmResponseWrapper.setBody(response);
        assertTrue(responseWrapper.equals(copiedLcmResponseWrapper));
    }
}
