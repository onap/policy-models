/*-
 * ============LICENSE_START=======================================================
 * sdnr
 * ================================================================================
 * Copyright (C) 2018 Wipro Limited Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019, 2024 Nordix Foundation.
 * Modifications Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class PciResponseWrapperTest {

    @Test
    void testPciResponseWrapperWrapper() {

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

        assertEquals(responseWrapper, (Object) responseWrapper);
        //assertEquals(responseWrapper, copiedPciResponseWrapper);
        assertNotEquals(responseWrapper, null);
        assertNotEquals(responseWrapper, (Object) "Hello");

        responseWrapper.setBody(null);
        assertNotEquals(responseWrapper, copiedPciResponseWrapper);
        copiedPciResponseWrapper.setBody(null);
        //assertEquals(responseWrapper, copiedPciResponseWrapper);
        responseWrapper.setBody(response);
        //assertNotEquals(responseWrapper, copiedPciResponseWrapper);
        copiedPciResponseWrapper.setBody(response);
        //assertEquals(responseWrapper, copiedPciResponseWrapper);
    }
}
