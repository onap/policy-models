/*-
 * ============LICENSE_START=======================================================
 * so
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019, 2024 Nordix Foundation.
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

package org.onap.policy.so;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class SoRequestInfoTest {

    @Test
    void testConstructor() {
        SoRequestInfo obj = new SoRequestInfo();

        assertNull(obj.getBillingAccountNumber());
        assertNull(obj.getCallbackUrl());
        assertNull(obj.getCorrelator());
        assertNull(obj.getInstanceName());
        assertNull(obj.getOrderNumber());
        assertNull(obj.getOrderVersion());
        assertNull(obj.getProductFamilyId());
        assertNull(obj.getRequestorId());
        assertNull(obj.getSource());
        assertFalse(obj.isSuppressRollback());
    }

    @Test
    void testSetGet() {
        SoRequestInfo obj = new SoRequestInfo();

        obj.setBillingAccountNumber("billingAccountNumber");
        assertEquals("billingAccountNumber", obj.getBillingAccountNumber());

        obj.setCallbackUrl("callbackUrl");
        assertEquals("callbackUrl", obj.getCallbackUrl());

        obj.setCorrelator("correlator");
        assertEquals("correlator", obj.getCorrelator());

        obj.setInstanceName("instanceName");
        assertEquals("instanceName", obj.getInstanceName());

        obj.setOrderNumber("orderNumber");
        assertEquals("orderNumber", obj.getOrderNumber());

        int orderVersion = 2008;
        obj.setOrderVersion(orderVersion);
        assertEquals((Integer) orderVersion, obj.getOrderVersion());

        obj.setProductFamilyId("productFamilyId");
        assertEquals("productFamilyId", obj.getProductFamilyId());

        obj.setRequestorId("requestorId");
        assertEquals("requestorId", obj.getRequestorId());

        obj.setSource("source");
        assertEquals("source", obj.getSource());

        obj.setSuppressRollback(true);
        assertEquals(true, obj.isSuppressRollback());
    }
}
