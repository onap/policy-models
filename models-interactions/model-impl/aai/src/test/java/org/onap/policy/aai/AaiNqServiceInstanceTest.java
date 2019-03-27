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
import org.onap.policy.aai.util.Serialization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AaiNqServiceInstanceTest {
    private static final Logger logger = LoggerFactory.getLogger(AaiNqServiceInstanceTest.class);

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {}

    @AfterClass
    public static void tearDownAfterClass() throws Exception {}

    @Test
    public void test() {
        AaiNqServiceInstance aaiNqServiceInstance = new AaiNqServiceInstance();
        aaiNqServiceInstance.setServiceInstanceId("dhv-test-vhnfportal-service-instance-id");
        aaiNqServiceInstance.setServiceInstanceName("dhv-test-service-instance-name1");
        aaiNqServiceInstance.setPersonaModelId("82194af1-3c2c-485a-8f44-420e22a9eaa4");
        aaiNqServiceInstance.setPersonaModelVersion("1.0");
        aaiNqServiceInstance.setServiceInstanceLocationId("dhv-test-service-instance-location-id1");
        aaiNqServiceInstance.setResourceVersion("1485366092");
        aaiNqServiceInstance.setModelInvariantId("SomeID");
        aaiNqServiceInstance.setModelVersionId("SomeVersion");
        assertNotNull(aaiNqServiceInstance);
        assertEquals("dhv-test-vhnfportal-service-instance-id", aaiNqServiceInstance.getServiceInstanceId());
        assertEquals("dhv-test-service-instance-name1", aaiNqServiceInstance.getServiceInstanceName());
        assertEquals("82194af1-3c2c-485a-8f44-420e22a9eaa4", aaiNqServiceInstance.getPersonaModelId());
        assertEquals("1.0", aaiNqServiceInstance.getPersonaModelVersion());
        assertEquals("dhv-test-service-instance-location-id1", aaiNqServiceInstance.getServiceInstanceLocationId());
        assertEquals("1485366092", aaiNqServiceInstance.getResourceVersion());
        assertEquals("SomeID", aaiNqServiceInstance.getModelInvariantId());
        assertEquals("SomeVersion", aaiNqServiceInstance.getModelVersionId());
        logger.info(Serialization.gsonPretty.toJson(aaiNqServiceInstance));
    }

}
