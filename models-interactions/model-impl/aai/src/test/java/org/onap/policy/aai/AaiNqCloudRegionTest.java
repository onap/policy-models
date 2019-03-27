/*-
 * ============LICENSE_START=======================================================
 * aai
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

package org.onap.policy.aai;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class AaiNqCloudRegionTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {}

    @AfterClass
    public static void tearDownAfterClass() throws Exception {}

    @Test
    public void test() {
        AaiNqCloudRegion aaiNqCloudRegion = new AaiNqCloudRegion();
        aaiNqCloudRegion.setCloudOwner("Rackspace");
        aaiNqCloudRegion.setCloudRegionId("DFW");
        aaiNqCloudRegion.setCloudRegionVersion("v1");
        aaiNqCloudRegion.setComplexName("SharedNode");
        aaiNqCloudRegion.setResourceVersion("1504789196021");
        assertNotNull(aaiNqCloudRegion);
        assertEquals("Rackspace", aaiNqCloudRegion.getCloudOwner());
        assertEquals("DFW", aaiNqCloudRegion.getCloudRegionId());
        assertEquals("v1", aaiNqCloudRegion.getCloudRegionVersion());
        assertEquals("SharedNode", aaiNqCloudRegion.getComplexName());
        assertEquals("1504789196021", aaiNqCloudRegion.getResourceVersion());
    }
}
