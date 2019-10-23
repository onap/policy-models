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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class AppcLcmOutputTest {

    private static final String PAYLOAD = "payload";

    @Test
    public void testHashCode() {
        AppcLcmOutput response = new AppcLcmOutput();
        assertTrue(response.hashCode() != 0);
        response.setCommonHeader(new AppcLcmCommonHeader());
        assertTrue(response.hashCode() != 0);
        response.setPayload(PAYLOAD);
        assertTrue(response.hashCode() != 0);
        response.setStatus(null);
        assertTrue(response.hashCode() != 0);
    }

    @Test
    public void testAppcLcmOutput() {
        AppcLcmOutput response = new AppcLcmOutput();
        assertNull(response.getCommonHeader());
        assertNull(response.getPayload());
        assertNotNull(response.getStatus());
    }

    @Test
    public void testToString() {
        AppcLcmOutput response = new AppcLcmOutput();
        assertFalse(response.toString().isEmpty());
    }

    @Test
    public void testEqualsObject() {
        AppcLcmOutput response = new AppcLcmOutput();
        assertTrue(response.equals(response));
        assertFalse(response.equals(null));
        assertFalse(response.equals(new Object()));

        AppcLcmOutput response2 = new AppcLcmOutput();
        assertTrue(response.equals(response2));

        response.setCommonHeader(new AppcLcmCommonHeader());
        assertFalse(response.equals(response2));
        response2.setCommonHeader(response.getCommonHeader());
        assertTrue(response.equals(response2));

        response.setPayload(PAYLOAD);
        assertFalse(response.equals(response2));
        response2.setPayload(response.getPayload());
        assertTrue(response.equals(response2));

        response.setCommonHeader(null);
        assertFalse(response.equals(response2));
        response2.setCommonHeader(null);
        assertTrue(response.equals(response2));

        response.setPayload(null);
        assertFalse(response.equals(response2));
        response2.setPayload(response.getPayload());
        assertTrue(response.equals(response2));

        response.setStatus(null);
        assertFalse(response.equals(response2));
        response2.setStatus(response.getStatus());
        assertTrue(response.equals(response2));

        AppcLcmResponseStatus status = new AppcLcmResponseStatus();
        status.setCode(5);
        response.setStatus(status);
        response2.setStatus(new AppcLcmResponseStatus());
        assertFalse(response.equals(response2));
    }

    @Test
    public void testInputOutput() {
        AppcLcmInput request = new AppcLcmInput();
        request.setCommonHeader(new AppcLcmCommonHeader());
        request.setPayload(PAYLOAD);

        AppcLcmOutput response = new AppcLcmOutput(request);

        assertTrue(response.getPayload().equals(PAYLOAD));
    }

}
