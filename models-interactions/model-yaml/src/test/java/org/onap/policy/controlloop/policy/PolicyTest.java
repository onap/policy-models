/*
 * ============LICENSE_START=======================================================
 * policy-yaml unit test
 * ================================================================================
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.controlloop.policy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.utils.io.Serializer;

public class PolicyTest {
    private Policy policy;

    @Before
    public void setUp() {
        policy = new Policy();
    }

    @Test
    public void testHashCode() {
        assertTrue(policy.hashCode() != 0);

        policy.setActor("a");
        int hc1 = policy.hashCode();

        policy.setActor("b");
        assertTrue(hc1 != policy.hashCode());
    }

    @Test
    public void test() throws IOException {
        OperationsAccumulateParams operationsAccumulateParams = new OperationsAccumulateParams();
        operationsAccumulateParams.setLimit(10);

        Map<String, String> payload = new TreeMap<>();
        payload.put("mykey", "myvalue");

        Target target = new Target();
        target.setResourceID("myresource");

        policy.setActor("act");
        policy.setDescription("desc");
        policy.setFailure("fail");
        policy.setFailure_exception("failex");
        policy.setFailure_guard("failguard");
        policy.setFailure_retries("failretry");
        policy.setFailure_timeout("failtimeout");
        policy.setId("myid");
        policy.setName("myname");
        policy.setOperationsAccumulateParams(operationsAccumulateParams);
        policy.setPayload(payload);
        policy.setRecipe("myrecipe");
        policy.setRetry(20);
        policy.setSuccess("succ");
        policy.setTarget(target);
        policy.setTimeout(30);

        assertEquals("act", policy.getActor());
        assertEquals("desc", policy.getDescription());
        assertEquals("fail", policy.getFailure());
        assertEquals("failex", policy.getFailure_exception());
        assertEquals("failguard", policy.getFailure_guard());
        assertEquals("failretry", policy.getFailure_retries());
        assertEquals("failtimeout", policy.getFailure_timeout());
        assertEquals("myid", policy.getId());
        assertEquals("myname", policy.getName());
        assertEquals(operationsAccumulateParams, policy.getOperationsAccumulateParams());
        assertEquals(payload, policy.getPayload());
        assertEquals("myrecipe", policy.getRecipe());
        assertEquals(20, policy.getRetry().intValue());
        assertEquals("succ", policy.getSuccess());
        assertEquals(target, policy.getTarget());
        assertEquals(30, policy.getTimeout().intValue());

        assertTrue(policy.equals(policy));
        assertTrue(policy.hashCode() != new Policy().hashCode());
        assertFalse(policy.equals(new Policy()));

        Policy policy2 = Serializer.roundTrip(policy);
        assertTrue(policy.equals(policy2));
        assertEquals(policy.hashCode(), policy2.hashCode());

        policy2 = new Policy(policy);
        assertTrue(policy.equals(policy2));
        assertEquals(policy.hashCode(), policy2.hashCode());
    }

    @Test
    public void testPolicyString() {
        policy = new Policy("justId");
        assertEquals("justId", policy.getId());
    }

    @Test
    public void testPolicyStringStringStringMapOfStringStringTarget() {
        Map<String, String> payload = new TreeMap<>();
        payload.put("mykeyB", "myvalueB");

        Target target = new Target();
        target.setResourceID("myresourceB");

        policy = new Policy("nameB", "actorB", "recipeB", payload, target);
        assertEquals("nameB", policy.getName());
        assertEquals("actorB", policy.getActor());
        assertEquals("recipeB", policy.getRecipe());
        assertEquals(payload, policy.getPayload());
        assertEquals(target, policy.getTarget());

        assertTrue(policy.hashCode() != new Policy().hashCode());
    }

    @Test
    public void testPolicyStringStringStringMapOfStringStringTargetIntegerInteger() {
        Map<String, String> payload = new TreeMap<>();
        payload.put("mykeyC", "myvalueC");

        Target target = new Target();
        target.setResourceID("myresourceC");

        policy = new Policy("nameC", "actorC", "recipeC", payload, target, 201, 202);
        assertEquals("nameC", policy.getName());
        assertEquals("actorC", policy.getActor());
        assertEquals("recipeC", policy.getRecipe());
        assertEquals(payload, policy.getPayload());
        assertEquals(target, policy.getTarget());
        assertEquals(201, policy.getRetry().intValue());
        assertEquals(202, policy.getTimeout().intValue());

        assertTrue(policy.hashCode() != new Policy().hashCode());
    }

    @Test
    public void testPolicyStringStringStringStringMapOfStringStringTargetStringIntegerInteger() {
        Map<String, String> payload = new TreeMap<>();
        payload.put("mykeyD", "myvalueD");

        Target target = new Target();
        target.setResourceID("myresourceD");

        policy = new Policy(
                PolicyParam.builder().id("idD")
                .name("nameD")
                .description("descD")
                .actor("actorD")
                .payload(payload)
                .target(target)
                .recipe("recipeD")
                .retries(301)
                .timeout(302)
                .build());
        assertEquals("idD", policy.getId());
        assertEquals("nameD", policy.getName());
        assertEquals("descD", policy.getDescription());
        assertEquals("actorD", policy.getActor());
        assertEquals(payload, policy.getPayload());
        assertEquals(target, policy.getTarget());
        assertEquals("recipeD", policy.getRecipe());
        assertEquals(301, policy.getRetry().intValue());
        assertEquals(302, policy.getTimeout().intValue());

        assertTrue(policy.hashCode() != new Policy().hashCode());
    }

    @Test
    public void testIsValid() {
        assertFalse(policy.isValid());

        Target target = new Target();
        target.setResourceID("myresourceV");

        policy = new Policy("nameV", "actorV", "recipeV", null, target);
        assertEquals(null, policy.getPayload());
        assertTrue(policy.isValid());
    }

    @Test
    public void testToString() {
        assertNotNull(policy.toString());
    }

    @Test
    public void testEqualsObject() {
        assertTrue(policy.equals(policy));

        policy.setId("idE");
        assertFalse(policy.equals(new Policy()));

        Policy policy2 = new Policy();
        policy2.setId(policy.getId());
        assertTrue(policy.equals(policy2));

        policy2.setId("idX");
        assertFalse(policy.equals(policy2));
    }

}
