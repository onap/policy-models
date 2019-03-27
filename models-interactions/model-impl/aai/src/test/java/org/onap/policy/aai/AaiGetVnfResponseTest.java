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

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import org.junit.Test;
import org.onap.policy.aai.util.Serialization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AaiGetVnfResponseTest {
    private static final Logger logger = LoggerFactory.getLogger(AaiGetVnfResponseTest.class);

    @Test
    public void test() throws Exception {
        // deserialize json and verify fields are populated properly
        String json = new String(Files.readAllBytes(
                        new File("src/test/resources/org/onap/policy/aai/AaiGetVnfResponse.json").toPath()));

        AaiGetVnfResponse resp = Serialization.gsonPretty.fromJson(json, AaiGetVnfResponse.class);

        assertEquals("807a3f02-f878-436b-870c-f0e91e81570d", resp.getVnfId());
        assertEquals("vLoadBalancerMS-Vnf-0809-2", resp.getVnfName());
        assertEquals("vLoadBalancerMS/vLoadBalancerMS 0", resp.getVnfType());
        assertEquals("1533850960381", resp.getResourceVersion());
        assertEquals(false, resp.getInMaint());
        assertEquals(true, resp.getIsClosedLoopDisabled());
        assertEquals("53638a85-361a-437d-8830-4b0d5329225e", resp.getModelInvariantId());
        assertEquals("PROV", resp.getProvStatus());
        assertEquals("Active", resp.getOrchestrationStatus());
        assertEquals("50e1b0be-e0c9-48e2-9f42-15279a783ee8", resp.getServiceId());

        // don't need to verify this in depth, as it has its own tests that do that
        RelationshipList relationshipList = resp.getRelationshipList();
        assertNotNull(relationshipList);

        List<Relationship> lst = relationshipList.getRelationships();
        assertNotNull(lst);

        assertEquals(5, lst.size());
        assertEquals("service-instance", lst.get(0).getRelatedTo());
        assertEquals("line-of-business", lst.get(1).getRelatedTo());

        logger.info(Serialization.gsonPretty.toJson(resp));

        // verify that setXxx methods work
        relationshipList = new RelationshipList();

        resp.setInMaint(true);
        resp.setIsClosedLoopDisabled(false);
        resp.setModelInvariantId("modiv");
        resp.setOrchestrationStatus("orch");
        resp.setProvStatus("mystatus");
        resp.setRelationshipList(relationshipList);
        resp.setResourceVersion("vers");
        resp.setServiceId("svc");
        resp.setVnfId("vnfid");
        resp.setVnfName("vnfname");
        resp.setVnfType("vnftype");

        assertEquals("vnfid", resp.getVnfId());
        assertEquals("vnfname", resp.getVnfName());
        assertEquals("vnftype", resp.getVnfType());
        assertEquals("vers", resp.getResourceVersion());
        assertEquals(true, resp.getInMaint());
        assertEquals(false, resp.getIsClosedLoopDisabled());
        assertEquals("modiv", resp.getModelInvariantId());
        assertEquals("mystatus", resp.getProvStatus());
        assertEquals("orch", resp.getOrchestrationStatus());
        assertEquals("svc", resp.getServiceId());
        assertEquals(relationshipList, resp.getRelationshipList());


        // test error case
        json = new String(Files.readAllBytes(
                        new File("src/test/resources/org/onap/policy/aai/AaiGetResponseError.json").toPath()));
        resp = Serialization.gsonPretty.fromJson(json, AaiGetVnfResponse.class);

        // don't need to verify this in depth, as it has its own tests that do that
        assertNotNull(resp.getRequestError());
        assertNotNull(resp.getRequestError().getServiceExcept());
        assertEquals("SVC3001", resp.getRequestError().getServiceExcept().getMessageId());
    }
}
