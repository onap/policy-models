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

public class LcmRequestWrapperTest {

    @Test
    public void testLcmRequestWrapperWrapper() {
        assertNotNull(new LcmRequestWrapper(new LcmRequest()));
        LcmRequestWrapper requestWrapper = new LcmRequestWrapper();
        assertNotNull(requestWrapper);
        assertNotEquals(0, requestWrapper.hashCode());

        LcmRequest request = new LcmRequest();

        requestWrapper.setBody(request);
        assertEquals(request, requestWrapper.getBody());

        assertNotEquals(0, requestWrapper.hashCode());

        assertEquals("RequestWrapper [body=Request [commonHeader=nul", requestWrapper.toString().substring(0, 46));

        LcmRequestWrapper copiedLcmRequestWrapper = new LcmRequestWrapper();
        copiedLcmRequestWrapper.setBody(requestWrapper.getBody());

        assertTrue(requestWrapper.equals(requestWrapper));
        assertTrue(requestWrapper.equals(copiedLcmRequestWrapper));
        assertFalse(requestWrapper.equals(null));
        assertFalse(requestWrapper.equals("Hello"));

        requestWrapper.setBody(null);
        assertFalse(requestWrapper.equals(copiedLcmRequestWrapper));
        copiedLcmRequestWrapper.setBody(null);
        assertTrue(requestWrapper.equals(copiedLcmRequestWrapper));
        requestWrapper.setBody(request);
        assertFalse(requestWrapper.equals(copiedLcmRequestWrapper));
        copiedLcmRequestWrapper.setBody(request);
        assertTrue(requestWrapper.equals(copiedLcmRequestWrapper));
    }
}
