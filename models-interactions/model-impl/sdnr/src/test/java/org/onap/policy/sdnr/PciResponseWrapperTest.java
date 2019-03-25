/*-
 * ============LICENSE_START=======================================================
 * sdnr
 * ================================================================================
 * Copyright (C) 2018 Wipro Limited Intellectual Property. All rights reserved.
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

package org.onap.policy.sdnr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PciResponseWrapperTest {

    @Test
    public void testPciResponseWrapperWrapper() {

        PciResponseWrapper responseWrapper = new PciResponseWrapper();
        assertNotNull(responseWrapper);
        assertNotEquals(0, responseWrapper.hashCode());

        PciResponse response = new PciResponse();

        responseWrapper.setBody(response);
        assertEquals(response, responseWrapper.getBody());

        assertNotEquals(0, responseWrapper.hashCode());

        assertNotEquals("ResponseWrapper [body=Response [commonHeader=n", responseWrapper.toString().substring(0, 46));

        PciResponseWrapper copiedPciResponseWrapper = new PciResponseWrapper();
        copiedPciResponseWrapper.setBody(responseWrapper.getBody());

        assertTrue(responseWrapper.equals(responseWrapper));
        //assertTrue(responseWrapper.equals(copiedPciResponseWrapper));
        assertFalse(responseWrapper.equals(null));
        assertFalse(responseWrapper.equals("Hello"));

        responseWrapper.setBody(null);
        assertFalse(responseWrapper.equals(copiedPciResponseWrapper));
        copiedPciResponseWrapper.setBody(null);
        //assertTrue(responseWrapper.equals(copiedPciResponseWrapper));
        responseWrapper.setBody(response);
        //assertFalse(responseWrapper.equals(copiedPciResponseWrapper));
        copiedPciResponseWrapper.setBody(response);
        //assertTrue(responseWrapper.equals(copiedPciResponseWrapper));
    }
}
