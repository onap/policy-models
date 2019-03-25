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

public class AaiNqVfModuleTest {
    private static final Logger logger = LoggerFactory.getLogger(AaiNqVfModuleTest.class);


    @BeforeClass
    public static void setUpBeforeClass() throws Exception {}

    @AfterClass
    public static void tearDownAfterClass() throws Exception {}

    @Test
    public void test() {
        AaiNqVfModule aaiNqVfModule = new AaiNqVfModule();
        aaiNqVfModule.setVfModuleId("example-vf-module-id-val-49261");
        aaiNqVfModule.setVfModuleName("example-vf-module-name-val-73074");
        aaiNqVfModule.setHeatStackId("example-heat-stack-id-val-86300");
        aaiNqVfModule.setOrchestrationStatus("example-orchestration-status-val-56523");
        aaiNqVfModule.setIsBaseVfModule(true);
        aaiNqVfModule.setResourceVersion("1485366450");
        aaiNqVfModule.setPersonaModelId("ef86f9c5-2165-44f3-8fc3-96018b609ea5");
        aaiNqVfModule.setPersonaModelVersion("1.0");
        aaiNqVfModule.setWidgetModelId("example-widget-model-id-val-92571");
        aaiNqVfModule.setWidgetModelVersion("example-widget-model-version-val-83317");
        aaiNqVfModule.setContrailServiceInstanceFqdn("example-contrail-service-instance-fqdn-val-86796");
        aaiNqVfModule.setModelInvariantId("SomeId");
        aaiNqVfModule.setModelVersionId("SomeVersion");
        aaiNqVfModule.setModelCustomizationId("SomeCustomizationId");
        assertNotNull(aaiNqVfModule);
        assertEquals("example-vf-module-id-val-49261", aaiNqVfModule.getVfModuleId());
        assertEquals("example-vf-module-name-val-73074", aaiNqVfModule.getVfModuleName());
        assertEquals("example-heat-stack-id-val-86300", aaiNqVfModule.getHeatStackId());
        assertEquals("example-orchestration-status-val-56523", aaiNqVfModule.getOrchestrationStatus());
        assertEquals(true, aaiNqVfModule.getIsBaseVfModule());
        assertEquals("1485366450", aaiNqVfModule.getResourceVersion());
        assertEquals("ef86f9c5-2165-44f3-8fc3-96018b609ea5", aaiNqVfModule.getPersonaModelId());
        assertEquals("1.0", aaiNqVfModule.getPersonaModelVersion());
        assertEquals("example-widget-model-id-val-92571", aaiNqVfModule.getWidgetModelId());
        assertEquals("example-widget-model-version-val-83317", aaiNqVfModule.getWidgetModelVersion());
        assertEquals("example-contrail-service-instance-fqdn-val-86796",
                        aaiNqVfModule.getContrailServiceInstanceFqdn());
        assertEquals("SomeId", aaiNqVfModule.getModelInvariantId());
        assertEquals("SomeVersion", aaiNqVfModule.getModelVersionId());
        assertEquals("SomeCustomizationId", aaiNqVfModule.getModelCustomizationId());
        logger.info(Serialization.gsonPretty.toJson(aaiNqVfModule));
    }

}
