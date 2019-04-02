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
import java.util.LinkedList;
import java.util.List;
import org.junit.Test;
import org.onap.policy.aai.util.Serialization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RelationshipListTest {
    private static final Logger logger = LoggerFactory.getLogger(RelationshipListTest.class);

    @Test
    public void test() throws Exception {
        // deserialize json and verify fields are populated properly
        String json = new String(Files.readAllBytes(
                        new File("src/test/resources/org/onap/policy/aai/RelationshipList.json").toPath()));

        RelationshipList relationshipList = Serialization.gsonPretty.fromJson(json, RelationshipList.class);

        List<Relationship> lst = relationshipList.getRelationships();
        assertNotNull(lst);
        assertEquals(3, lst.size());

        // don't need to verify this in depth, as it has its own tests that do that
        assertEquals("generic-vnf", lst.get(0).getRelatedTo());
        assertEquals("image", lst.get(1).getRelatedTo());
        assertEquals("flavor", lst.get(2).getRelatedTo());

        logger.info(Serialization.gsonPretty.toJson(relationshipList));

        // verify that setXxx methods work
        lst = new LinkedList<>();
        lst.add(new Relationship());
        lst.add(new Relationship());

        relationshipList.setRelationships(lst);
        assertEquals(lst, relationshipList.getRelationships());
    }

}
