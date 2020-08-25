/*-
 * ============LICENSE_START=======================================================
 * sdnr
 * ================================================================================
 * Copyright (C) 2018 Wipro Limited Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019 Nordix Foundation.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class PciRequestWrapperTest {

    @Test
    public void testPciRequestWrapperWrapper() {
        assertNotNull(new PciRequestWrapper(new PciRequest()));
        PciRequestWrapper requestWrapper = new PciRequestWrapper();
        assertNotNull(requestWrapper);
        assertNotEquals(0, requestWrapper.hashCode());

        PciRequest request = new PciRequest();

        requestWrapper.setBody(request);
        assertEquals(request, requestWrapper.getBody());

        assertNotEquals(0, requestWrapper.hashCode());

        assertEquals("RequestWrapper [body=PciRequest[commonHeader=nul", requestWrapper.toString().substring(0, 48));

        PciRequestWrapper copiedPciRequestWrapper = new PciRequestWrapper();
        copiedPciRequestWrapper.setBody(requestWrapper.getBody());

        assertEquals(requestWrapper, (Object) requestWrapper);
        assertEquals(requestWrapper, copiedPciRequestWrapper);
        assertNotEquals(requestWrapper, null);
        assertNotEquals(requestWrapper, (Object) "Hello");

        requestWrapper.setBody(null);
        assertNotEquals(requestWrapper, copiedPciRequestWrapper);
        copiedPciRequestWrapper.setBody(null);
        assertEquals(requestWrapper, copiedPciRequestWrapper);
        requestWrapper.setBody(request);
        assertNotEquals(requestWrapper, copiedPciRequestWrapper);
        copiedPciRequestWrapper.setBody(request);
        assertEquals(requestWrapper, copiedPciRequestWrapper);
    }
}
