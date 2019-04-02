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

public class RelationshipTest {
    private static final Logger logger = LoggerFactory.getLogger(RelationshipTest.class);

    @Test
    public void test() throws Exception {
        // deserialize json and verify fields are populated properly
        String json = new String(Files
                        .readAllBytes(new File("src/test/resources/org/onap/policy/aai/Relationship.json").toPath()));

        Relationship relationship = Serialization.gsonPretty.fromJson(json, Relationship.class);

        assertEquals("/aai/v11/network/generic-vnfs/generic-vnf/807a3f02-f878-436b-870c-f0e91e81570d",
                        relationship.getRelatedLink());
        assertEquals("generic-vnf", relationship.getRelatedTo());

        // don't need to verify this in depth, as it has its own tests that do that
        List<RelatedToProperty> relatedToProperty = relationship.getRelatedToProperty();
        assertNotNull(relatedToProperty);
        assertEquals(2, relatedToProperty.size());
        assertEquals("vLoadBalancerMS-Vnf-0809-1", relatedToProperty.get(0).getPropertyValue());
        assertEquals("vLoadBalancerMS-Vnf-0809-2", relatedToProperty.get(1).getPropertyValue());

        // don't need to verify this in depth, as it has its own tests that do that
        List<RelationshipData> relationshipData = relationship.getRelationshipData();
        assertNotNull(relationshipData);
        assertEquals(2, relationshipData.size());
        assertEquals("807a3f02-f878-436b-870c-f0e91e81570d", relationshipData.get(0).getRelationshipValue());
        assertEquals("907a3f02-f878-436b-870c-f0e91e81570e", relationshipData.get(1).getRelationshipValue());

        logger.info(Serialization.gsonPretty.toJson(relationship));

        // verify that setXxx methods work
        relatedToProperty = new LinkedList<>();
        relatedToProperty.add(new RelatedToProperty());
        relatedToProperty.add(new RelatedToProperty());
        relatedToProperty.add(new RelatedToProperty());

        relationshipData = new LinkedList<>();
        relationshipData.add(new RelationshipData());
        relationshipData.add(new RelationshipData());
        relationshipData.add(new RelationshipData());

        relationship.setRelatedLink("related-link");
        relationship.setRelatedTo("related-to");
        relationship.setRelatedToProperty(relatedToProperty);
        relationship.setRelationshipData(relationshipData);

        assertEquals("related-link", relationship.getRelatedLink());
        assertEquals("related-to", relationship.getRelatedTo());
        assertEquals(relatedToProperty, relationship.getRelatedToProperty());
        assertEquals(relationshipData, relationship.getRelationshipData());
    }

}
