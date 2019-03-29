/*-
 * ============LICENSE_START=======================================================
 * so
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

package org.onap.policy.so;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SoRelatedInstanceTest {

    @Test
    public void testConstructor() {
        SoRelatedInstance obj = new SoRelatedInstance();

        assertTrue(obj.getInstanceId() == null);
        assertTrue(obj.getInstanceName() == null);
        assertTrue(obj.getModelInfo() == null);
    }

    @Test
    public void testSetGet() {
        SoRelatedInstance obj = new SoRelatedInstance();

        obj.setInstanceId("instanceId");
        assertEquals("instanceId", obj.getInstanceId());

        obj.setInstanceName("instanceName");
        assertEquals("instanceName", obj.getInstanceName());

        SoModelInfo modelInfo = new SoModelInfo();
        obj.setModelInfo(modelInfo);
        assertEquals(modelInfo, obj.getModelInfo());
    }
}
