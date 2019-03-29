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
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.policy.aai.util.Serialization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AaiNqVServerTest {
    private static final Logger logger = LoggerFactory.getLogger(AaiNqVServerTest.class);


    @BeforeClass
    public static void setUpBeforeClass() throws Exception {}

    @AfterClass
    public static void tearDownAfterClass() throws Exception {}

    @Test
    public void test() throws Exception {
        // deserialize json and verify fields are populated properly
        String json = new String(Files
                        .readAllBytes(new File("src/test/resources/org/onap/policy/aai/AaiNqVServer.json").toPath()));

        AaiNqVServer resp = Serialization.gsonPretty.fromJson(json, AaiNqVServer.class);

        assertEquals(false, resp.getInMaint());
        assertEquals(true, resp.getIsClosedLoopDisabled());
        assertEquals("ACTIVE", resp.getProvStatus());
        assertEquals("1533850964910", resp.getResourceVersion());
        assertEquals("1c94da3f-16f1-4fc7-9ed1-e018dfa62774", resp.getVserverId());
        assertEquals("vlb-ms-0809-1", resp.getVserverName());
        assertEquals("vlb-ms-0809-7", resp.getVserverName2());
        assertEquals("http://localhost:8774/v2.1/4086f396c5e04caf9502c5fdeca575c4/servers/1c94da3f-16f1-4fc7-9ed1-e018dfa62774",
                        resp.getVserverSelflink());

        // don't need to verify this in depth, as it has its own tests that do that
        RelationshipList relationshipList = resp.getRelationshipList();
        assertNotNull(relationshipList);

        List<Relationship> lst = relationshipList.getRelationships();
        assertNotNull(lst);

        assertEquals(3, lst.size());
        assertEquals("generic-vnf", lst.get(0).getRelatedTo());
        assertEquals("image", lst.get(1).getRelatedTo());

        logger.info(Serialization.gsonPretty.toJson(resp));

        // verify that setXxx methods work
        relationshipList = new RelationshipList();

        resp.setInMaint(true);
        resp.setIsClosedLoopDisabled(false);
        resp.setProvStatus("inactive");
        resp.setRelationshipList(relationshipList);
        resp.setResourceVersion("vers");
        resp.setVserverId("vid");
        resp.setVserverName("vname");
        resp.setVserverName2("vname2");
        resp.setVserverSelflink("link");

        assertEquals(true, resp.getInMaint());
        assertEquals(false, resp.getIsClosedLoopDisabled());
        assertEquals("inactive", resp.getProvStatus());
        assertEquals("vers", resp.getResourceVersion());
        assertEquals("vid", resp.getVserverId());
        assertEquals("vname", resp.getVserverName());
        assertEquals("vname2", resp.getVserverName2());
        assertEquals("link", resp.getVserverSelflink());
        assertEquals(relationshipList, resp.getRelationshipList());
    }

}
