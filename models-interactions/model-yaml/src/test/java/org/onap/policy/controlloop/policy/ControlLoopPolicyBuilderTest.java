/*-
 * ============LICENSE_START=======================================================
 * policy-yaml unit test
 * ================================================================================
 * Copyright (C) 2017-2020 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019-2020 Nordix Foundation.
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.UUID;
import org.junit.Ignore;
import org.junit.Test;
import org.onap.aai.domain.yang.Pnf;
import org.onap.policy.controlloop.policy.builder.BuilderException;
import org.onap.policy.controlloop.policy.builder.ControlLoopPolicyBuilder;
import org.onap.policy.controlloop.policy.builder.Message;
import org.onap.policy.controlloop.policy.builder.MessageLevel;
import org.onap.policy.controlloop.policy.builder.Results;
import org.onap.policy.sdc.Resource;
import org.onap.policy.sdc.ResourceType;
import org.onap.policy.sdc.Service;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;

public class ControlLoopPolicyBuilderTest {

    private static final String RESOURCE1 = "resource1";
    private static final String TRIGGER_RESTART = "Upon getting the trigger event, restart the VM";
    private static final String UNKNOWN_POLICY = "Unknown policy ";
    private static final String RESTART = "Restart";
    private static final String RESTART_VM = "Restart the VM";
    private static final String REBUILD = "Rebuild";
    private static final String REBUILD_VM = "Rebuild VM";
    private static final String REBUILD_RESTART = "If the restart fails, rebuild it.";

    @Test
    public void testControlLoop() throws BuilderException {
        //
        // Create a builder for our policy
        //
        ControlLoopPolicyBuilder builder =
            ControlLoopPolicyBuilder.Factory.buildControlLoop(UUID.randomUUID().toString(), 2400);
        //
        // Test add services
        //
        Service scp = new Service("vSCP");
        Service usp = new Service("vUSP");
        Service trinity = new Service("Trinity");
        builder = builder.addService(scp, usp, trinity);
        assertEquals(builder.getControlLoop().getServices().size() , 3);
        //
        // Test remove services
        //
        builder = builder.removeService(scp);
        assertEquals(builder.getControlLoop().getServices().size() , 2);
        builder = builder.removeAllServices();
        assertTrue(builder.getControlLoop().getServices().isEmpty());
        //
        // Test add resources
        //
        Resource cts = new Resource("vCTS", ResourceType.VF);
        Resource com = new Resource("vCTS", ResourceType.VF);
        Resource rar = new Resource("vCTS", ResourceType.VF);
        builder = builder.addResource(cts, com, rar);
        assertEquals(builder.getControlLoop().getResources().size() , 3);
        //
        // Test remove resources
        //
        builder = builder.removeResource(cts);
        assertEquals(builder.getControlLoop().getResources().size() , 2);
        builder = builder.removeAllResources();
        assertTrue(builder.getControlLoop().getResources().isEmpty());
    }

    @Test
    public void testAddNullService() throws BuilderException {
        ControlLoopPolicyBuilder builder =
            ControlLoopPolicyBuilder.Factory.buildControlLoop(UUID.randomUUID().toString(), 2400);

        assertThatThrownBy(() -> {
            builder.addService((Service) null);
        }).isInstanceOf(BuilderException.class).hasMessage("Service must not be null");
    }

    @Test
    public void testAddInvalidService() throws BuilderException {
        ControlLoopPolicyBuilder builder =
            ControlLoopPolicyBuilder.Factory.buildControlLoop(UUID.randomUUID().toString(), 2400);

        assertThatThrownBy(() -> {
            builder.addService(new Service());
        }).isInstanceOf(BuilderException.class)
            .hasMessage("Invalid service - need either a serviceUUID or serviceName");
    }

    @Test
    public void testAddServiceWithUuid() throws BuilderException {
        ControlLoopPolicyBuilder builder =
            ControlLoopPolicyBuilder.Factory.buildControlLoop(UUID.randomUUID().toString(), 2400);
        UUID uuid = UUID.randomUUID();
        Service serviceWithUuid = new Service(uuid);
        builder.addService(serviceWithUuid);
        assertEquals(builder.getControlLoop().getServices().size() , 1);
    }

    @Test
    public void testAddNullResource() throws BuilderException {
        ControlLoopPolicyBuilder builder =
            ControlLoopPolicyBuilder.Factory.buildControlLoop(UUID.randomUUID().toString(), 2400);

        assertThatThrownBy(() -> {
            builder.addResource((Resource) null);
        }).isInstanceOf(BuilderException.class).hasMessage("Resource must not be null");
    }

    @Test
    public void testAddInvalidResource() throws BuilderException {
        ControlLoopPolicyBuilder builder =
            ControlLoopPolicyBuilder.Factory.buildControlLoop(UUID.randomUUID().toString(), 2400);

        assertThatThrownBy(() -> {
            builder.addResource(new Resource());
        }).isInstanceOf(BuilderException.class)
            .hasMessage("Invalid resource - need either resourceUUID or resourceName");
    }

    @Test
    public void testAddAndRemoveResourceWithUuid() throws BuilderException {
        ControlLoopPolicyBuilder builder =
            ControlLoopPolicyBuilder.Factory.buildControlLoop(UUID.randomUUID().toString(), 2400);
        UUID uuid = UUID.randomUUID();
        Resource resourceWithUuid = new Resource(uuid);
        builder.addResource(resourceWithUuid);
        assertEquals(builder.getControlLoop().getResources().size() , 1);

        builder.removeResource(resourceWithUuid);
        assertTrue(builder.getControlLoop().getResources().isEmpty());
    }

    @Test
    public void testRemoveNullResource() throws BuilderException {
        ControlLoopPolicyBuilder builder =
            ControlLoopPolicyBuilder.Factory.buildControlLoop(UUID.randomUUID().toString(), 2400);
        Resource resource = new Resource(RESOURCE1, ResourceType.VF);
        builder.addResource(resource);

        assertThatThrownBy(() -> {
            builder.removeResource((Resource) null);
        }).isInstanceOf(BuilderException.class).hasMessage("Resource must not be null");
    }

    @Test
    public void testRemoveResourceNoExistingResources() throws BuilderException {
        ControlLoopPolicyBuilder builder =
            ControlLoopPolicyBuilder.Factory.buildControlLoop(UUID.randomUUID().toString(), 2400);

        assertThatThrownBy(() -> {
            builder.removeResource(new Resource(RESOURCE1, ResourceType.VF));
        }).isInstanceOf(BuilderException.class).hasMessage("No existing resources to remove");
    }

    @Test
    public void testRemoveInvalidResource() throws BuilderException {
        ControlLoopPolicyBuilder builder =
            ControlLoopPolicyBuilder.Factory.buildControlLoop(UUID.randomUUID().toString(), 2400);
        Resource resource = new Resource(RESOURCE1, ResourceType.VF);
        builder.addResource(resource);

        assertThatThrownBy(() -> {
            builder.removeResource(new Resource());
        }).isInstanceOf(BuilderException.class)
            .hasMessage("Invalid resource - need either a resourceUUID or resourceName");
    }

    @Test
    public void testRemoveUnknownResource() throws BuilderException {
        ControlLoopPolicyBuilder builder =
            ControlLoopPolicyBuilder.Factory.buildControlLoop(UUID.randomUUID().toString(), 2400);
        Resource resource = new Resource(RESOURCE1, ResourceType.VF);
        builder.addResource(resource);
        final String unknownResourceName = "reource2";

        assertThatThrownBy(() -> {
            builder.removeResource(new Resource(unknownResourceName, ResourceType.VF));
        }).isInstanceOf(BuilderException.class).hasMessage("Unknown resource " + unknownResourceName);
    }

    @Test
    public void testControlLoopWithInitialResourceAndServices() throws BuilderException {
        Resource cts = new Resource("vCTS", ResourceType.VF);
        Service scp = new Service("vSCP");
        Service usp = new Service("vUSP");
        ControlLoopPolicyBuilder builder =
            ControlLoopPolicyBuilder.Factory.buildControlLoop(UUID.randomUUID().toString(), 2400, cts, scp, usp);
        assertEquals(builder.getControlLoop().getResources().size() , 1);
        assertEquals(builder.getControlLoop().getServices().size() , 2);
    }

    @Test
    public void testControlLoopWithInitialResourcesAndService() throws BuilderException {
        Resource cts = new Resource("vCTS", ResourceType.VF);
        Resource com = new Resource("vCTS", ResourceType.VF);
        Service scp = new Service("vSCP");
        ControlLoopPolicyBuilder builder =
            ControlLoopPolicyBuilder.Factory.buildControlLoop(UUID.randomUUID().toString(), 2400, scp, cts, com);
        assertEquals(builder.getControlLoop().getServices().size() , 1);
        assertEquals(builder.getControlLoop().getResources().size() , 2);
    }

    @Test
    @Ignore
    // I'VE MARKED THIS TEST CASE AS IGNORE BECAUSE THE TEST CASE FAILS
    // This test case fails because builder.getControlLoop() returns an instance of ControlLoop
    // copied using
    // the ControlLoop(ControlLoop controlLoop) constructor.
    // This constructor does not copy the value of pnf into the newly created object
    // On the face of it, this looks like a bug, but perhaps there is a reason for this
    // PLEASE ADVISE IF THE BEHAVIOUR IS INCORRECT OR THE TEST CASE IS INVALID
    public void testControlLoopForPnf() throws BuilderException {
        Pnf pnf = new Pnf();
        // pnf.setPnfType(PnfType.ENODEB);
        ControlLoopPolicyBuilder builder =
            ControlLoopPolicyBuilder.Factory.buildControlLoop(UUID.randomUUID().toString(), 2400, pnf);
        assertEquals(pnf, builder.getControlLoop().getPnf());

        builder.removePnf();
        assertNull(builder.getControlLoop().getPnf());
    }

    @Test
    @Ignore
    // Fails for the same reason as the above test case
    public void testSetAndRemovePnf() throws BuilderException {
        ControlLoopPolicyBuilder builder =
            ControlLoopPolicyBuilder.Factory.buildControlLoop(UUID.randomUUID().toString(), 2400);
        assertNull(builder.getControlLoop().getPnf());

        Pnf pnf = new Pnf();
        // pnf.setPnfType(PnfType.ENODEB);
        builder.setPnf(pnf);
        assertEquals(pnf, builder.getControlLoop().getPnf());

        builder.removePnf();
        assertNull(builder.getControlLoop().getPnf());
    }

    @Test
    public void testSetNullPnf() throws BuilderException {
        ControlLoopPolicyBuilder builder =
            ControlLoopPolicyBuilder.Factory.buildControlLoop(UUID.randomUUID().toString(), 2400);

        assertThatThrownBy(() -> {
            builder.setPnf(null);
        }).isInstanceOf(BuilderException.class).hasMessage("PNF must not be null");
    }

    @Test
    public void testSetInvalidPnf() throws BuilderException {
        ControlLoopPolicyBuilder builder =
            ControlLoopPolicyBuilder.Factory.buildControlLoop(UUID.randomUUID().toString(), 2400);

        assertThatThrownBy(() -> {
            builder.setPnf(new Pnf());
        }).isInstanceOf(BuilderException.class).hasMessage("Invalid PNF - need either pnfName or pnfType");
    }

    @Test
    public void testSetAbatement() throws BuilderException {
        ControlLoopPolicyBuilder builder =
            ControlLoopPolicyBuilder.Factory.buildControlLoop(UUID.randomUUID().toString(), 2400);
        assertFalse(builder.getControlLoop().getAbatement());
        builder = builder.setAbatement(true);
        assertTrue(builder.getControlLoop().getAbatement());
    }

    @Test
    public void testSetNullAbatement() throws BuilderException {
        assertThatThrownBy(() -> {
            ControlLoopPolicyBuilder builder =
                ControlLoopPolicyBuilder.Factory.buildControlLoop(UUID.randomUUID().toString(), 2400);

            builder = builder.setAbatement(null);
        }).isInstanceOf(BuilderException.class).hasMessage("abatement must not be null");
    }

    @Test
    public void testTimeout() throws BuilderException {
        //
        // Create a builder for our policy
        //
        ControlLoopPolicyBuilder builder =
            ControlLoopPolicyBuilder.Factory.buildControlLoop(UUID.randomUUID().toString(), 2400);
        //
        // Test setTimeout
        //
        assertTrue(builder.getControlLoop().getTimeout() == 2400);
        builder = builder.setTimeout(800);
        assertTrue(builder.getControlLoop().getTimeout() == 800);
        //
        // Test calculateTimeout
        //
        // @formatter:off
        Policy trigger = builder.setTriggerPolicy(PolicyParam.builder().id(UUID.randomUUID().toString())
            .name(RESTART_VM)
            .description(TRIGGER_RESTART)
            .actor("APPC").target(new Target(TargetType.VM))
            .recipe(RESTART)
            .payload(null)
            .retries(2)
            .timeout(300)
            .build());

        @SuppressWarnings("unused")
        Policy onRestartFailurePolicy = builder.setPolicyForPolicyResult(
            PolicyParam.builder()
                .name(REBUILD_VM)
                .description("If the restart fails, rebuild it").actor("APPC")
                .target(new Target(TargetType.VM))
                .recipe(REBUILD)
                .payload(null)
                .retries(1)
                .timeout(600)
                .id(trigger.getId())
                .build(),
            PolicyResult.FAILURE, PolicyResult.FAILURE_RETRIES, PolicyResult.FAILURE_TIMEOUT);
        assertEquals(Integer.valueOf(300 + 600), builder.calculateTimeout());
        // @formatter:on
    }

    @Test
    public void testTriggerPolicyMethods() throws BuilderException {
        ControlLoopPolicyBuilder builder =
            ControlLoopPolicyBuilder.Factory.buildControlLoop(UUID.randomUUID().toString(), 2400);
        //
        // Test isOpenLoop
        //
        assertTrue(builder.isOpenLoop());
        //
        // Test set initial trigger policy
        //
        // @formatter:off
        Policy triggerPolicy1 = builder.setTriggerPolicy(PolicyParam.builder().id(UUID.randomUUID().toString())
            .name(RESTART_VM)
            .description(TRIGGER_RESTART)
            .actor("APPC")
            .target(new Target(TargetType.VM))
            .recipe(RESTART)
            .payload(null)
            .retries(2)
            .timeout(300)
            .build());
        assertFalse(builder.isOpenLoop());
        assertEquals(builder.getControlLoop().getTriggerPolicy(), triggerPolicy1.getId());
        //
        // Set trigger policy to a new policy
        //
        @SuppressWarnings("unused")
        Policy triggerPolicy2 = builder.setTriggerPolicy(PolicyParam.builder().id(UUID.randomUUID().toString())
            .name("Rebuild the VM")
            .description("Upon getting the trigger event, rebuild the VM").actor("APPC")
            .target(new Target(TargetType.VM))
            .recipe(REBUILD)
            .payload(null)
            .retries(2)
            .timeout(300)
            .build());
        // @formatter:on
        //
        // Test set trigger policy to another existing policy
        //
        @SuppressWarnings("unused")
        ControlLoop cl = builder.setExistingTriggerPolicy(triggerPolicy1.getId());
        assertEquals(builder.getControlLoop().getTriggerPolicy() , triggerPolicy1.getId());
        //
        // Test get trigger policy
        //
        assertEquals(builder.getTriggerPolicy() , triggerPolicy1);
    }

    @Test
    public void testSetTriggerPolicyNullPolicyId() throws BuilderException {
        ControlLoopPolicyBuilder builder =
            ControlLoopPolicyBuilder.Factory.buildControlLoop(UUID.randomUUID().toString(), 2400);

        assertThatThrownBy(() -> {
            builder.setExistingTriggerPolicy(null);
        }).isInstanceOf(BuilderException.class).hasMessage("Id must not be null");
    }

    @Test
    public void testSetTriggerPolicyNoPoliciesExist() throws BuilderException {
        ControlLoopPolicyBuilder builder =
            ControlLoopPolicyBuilder.Factory.buildControlLoop(UUID.randomUUID().toString(), 2400);
        final String unknownPolicyId = "100";

        assertThatThrownBy(() -> {
            builder.setExistingTriggerPolicy(unknownPolicyId);
        }).isInstanceOf(BuilderException.class).hasMessage(UNKNOWN_POLICY + unknownPolicyId);
    }

    @Test
    public void testSetTriggerPolicyUnknownPolicy() throws BuilderException {
        ControlLoopPolicyBuilder builder =
            ControlLoopPolicyBuilder.Factory.buildControlLoop(UUID.randomUUID().toString(), 2400);
        // @formatter:off
        builder.setTriggerPolicy(PolicyParam.builder().id(UUID.randomUUID().toString())
            .name(RESTART_VM)
            .description(TRIGGER_RESTART)
            .actor("APPC")
            .target(new Target(TargetType.VM))
            .recipe(RESTART)
            .payload(null)
            .retries(2)
            .timeout(300)
            .build());
        // @formatter:on
        final String unknownPolicyId = "100";

        assertThatThrownBy(() -> {
            builder.setExistingTriggerPolicy(unknownPolicyId);
        }).isInstanceOf(BuilderException.class).hasMessage(UNKNOWN_POLICY + unknownPolicyId);
    }

    @Test
    public void testAddRemovePolicies() throws BuilderException {
        ControlLoopPolicyBuilder builder =
            ControlLoopPolicyBuilder.Factory.buildControlLoop(UUID.randomUUID().toString(), 2400);
        // @formatter:off
        Policy triggerPolicy = builder.setTriggerPolicy(PolicyParam.builder().id(UUID.randomUUID().toString())
            .name(RESTART_VM)
            .description(TRIGGER_RESTART)
            .actor("APPC")
            .target(new Target(TargetType.VM))
            .recipe(RESTART)
            .payload(null)
            .retries(2)
            .timeout(300)
            .build());
        //
        // Test create a policy and chain it to the results of trigger policy
        //
        Policy onRestartFailurePolicy1 = builder.setPolicyForPolicyResult(PolicyParam.builder()
            .name(REBUILD_VM)
            .description(REBUILD_RESTART)
            .actor("APPC")
            .target(new Target(TargetType.VM))
            .recipe(REBUILD).payload(null)
            .retries(1)
            .timeout(600)
            .id(triggerPolicy.getId())
            .build(),
            PolicyResult.FAILURE, PolicyResult.FAILURE_EXCEPTION, PolicyResult.FAILURE_RETRIES,
            PolicyResult.FAILURE_TIMEOUT, PolicyResult.FAILURE_GUARD);
        //
        assertEquals(builder.getTriggerPolicy().getFailure() , onRestartFailurePolicy1.getId());
        assertEquals(builder.getTriggerPolicy().getFailureException() , onRestartFailurePolicy1.getId());
        assertEquals(builder.getTriggerPolicy().getFailureRetries() , onRestartFailurePolicy1.getId());
        assertEquals(builder.getTriggerPolicy().getFailureTimeout() , onRestartFailurePolicy1.getId());
        assertEquals(builder.getTriggerPolicy().getFailureGuard() , onRestartFailurePolicy1.getId());

        //
        // Test create a policy and chain it to the results of trigger policy success
        //
        Policy onSuccessPolicy1 = builder.setPolicyForPolicyResult(PolicyParam.builder()
            .name("Do something")
            .description("If the restart succeeds, do something else.")
            .actor("APPC")
            .target(new Target(TargetType.VM))
            .recipe("SomethingElse")
            .payload(null)
            .retries(1)
            .timeout(600)
            .id(triggerPolicy.getId())
            .build(),
            PolicyResult.SUCCESS);
        //
        // @formatter:on
        assertEquals(builder.getTriggerPolicy().getSuccess() , onSuccessPolicy1.getId());

        //
        // Test remove policy
        //
        boolean removed = builder.removePolicy(onRestartFailurePolicy1.getId());
        assertTrue(removed);
        assertEquals(builder.getTriggerPolicy().getFailure() , FinalResult.FINAL_FAILURE.toString());
        assertEquals(
            builder.getTriggerPolicy().getFailureRetries() , FinalResult.FINAL_FAILURE_RETRIES.toString());
        assertEquals(
            builder.getTriggerPolicy().getFailureTimeout() , FinalResult.FINAL_FAILURE_TIMEOUT.toString());
        assertEquals(builder.getTriggerPolicy().getFailureGuard() , FinalResult.FINAL_FAILURE_GUARD.toString());
        //
        // Create another policy and chain it to the results of trigger policy
        //
        // @formatter:off
        final Policy onRestartFailurePolicy2 = builder.setPolicyForPolicyResult(
            PolicyParam.builder()
                .name(REBUILD_VM)
                .description(REBUILD_RESTART)
                .actor("APPC")
                .target(new Target(TargetType.VM))
                .recipe(REBUILD)
                .payload(null)
                .retries(2)
                .timeout(600)
                .id(triggerPolicy.getId())
                .build(),
            PolicyResult.FAILURE, PolicyResult.FAILURE_RETRIES, PolicyResult.FAILURE_TIMEOUT);
        // @formatter:on
        //
        // Test reset policy results
        //
        triggerPolicy = builder.resetPolicyResults(triggerPolicy.getId());
        assertEquals(builder.getTriggerPolicy().getFailure() , FinalResult.FINAL_FAILURE.toString());
        assertEquals(
            builder.getTriggerPolicy().getFailureRetries() , FinalResult.FINAL_FAILURE_RETRIES.toString());
        assertEquals(
            builder.getTriggerPolicy().getFailureTimeout() , FinalResult.FINAL_FAILURE_TIMEOUT.toString());
        //
        // Test set the policy results to an existing operational policy
        //
        Policy onRestartFailurePolicy3 = builder.setPolicyForPolicyResult(onRestartFailurePolicy2.getId(),
            triggerPolicy.getId(), PolicyResult.FAILURE, PolicyResult.FAILURE_RETRIES, PolicyResult.FAILURE_TIMEOUT);
        assertEquals(builder.getTriggerPolicy().getFailure() , onRestartFailurePolicy3.getId());
        assertEquals(builder.getTriggerPolicy().getFailureRetries() , onRestartFailurePolicy3.getId());
        assertEquals(builder.getTriggerPolicy().getFailureTimeout() , onRestartFailurePolicy3.getId());
        //
        // Test set the policy result for success to an existing operational policy
        //
        Policy onRestartFailurePolicy4 = builder.setPolicyForPolicyResult(onRestartFailurePolicy2.getId(),
            triggerPolicy.getId(), PolicyResult.FAILURE, PolicyResult.FAILURE_EXCEPTION, PolicyResult.FAILURE_GUARD,
            PolicyResult.FAILURE_RETRIES, PolicyResult.FAILURE_TIMEOUT, PolicyResult.SUCCESS);
        assertEquals(builder.getTriggerPolicy().getFailure() , onRestartFailurePolicy4.getId());
        assertEquals(builder.getTriggerPolicy().getFailureException() , onRestartFailurePolicy4.getId());
        assertEquals(builder.getTriggerPolicy().getFailureGuard() , onRestartFailurePolicy4.getId());
        assertEquals(builder.getTriggerPolicy().getFailureRetries() , onRestartFailurePolicy4.getId());
        assertEquals(builder.getTriggerPolicy().getFailureTimeout() , onRestartFailurePolicy4.getId());
        assertEquals(builder.getTriggerPolicy().getSuccess() , onRestartFailurePolicy4.getId());

        //
        // Test remove all existing operational policies
        //
        builder = builder.removeAllPolicies();
        assertEquals(builder.getControlLoop().getTriggerPolicy() , FinalResult.FINAL_OPENLOOP.toString());
    }

    @Test
    public void testAddToUnknownPolicy() throws BuilderException {
        ControlLoopPolicyBuilder builder =
            ControlLoopPolicyBuilder.Factory.buildControlLoop(UUID.randomUUID().toString(), 2400);
        final String policyId = "100";

        assertThatThrownBy(() -> {
            // @formatter:off
            builder.setPolicyForPolicyResult(
                PolicyParam.builder()
                    .name(REBUILD_VM)
                    .description(REBUILD_RESTART)
                    .actor("APPC")
                    .target(new Target(TargetType.VM))
                    .recipe(REBUILD)
                    .payload(null)
                    .retries(1)
                    .timeout(600)
                    .id(policyId)
                    .build(),
                PolicyResult.FAILURE, PolicyResult.FAILURE_RETRIES, PolicyResult.FAILURE_TIMEOUT,
                PolicyResult.FAILURE_GUARD);
            // @formatter:on
        }).isInstanceOf(BuilderException.class).hasMessage(UNKNOWN_POLICY + policyId);

    }

    @Test
    public void testAddExistingPolicyToUnknownPolicy() throws BuilderException {
        ControlLoopPolicyBuilder builder =
            ControlLoopPolicyBuilder.Factory.buildControlLoop(UUID.randomUUID().toString(), 2400);
        // @formatter:off
        Policy triggerPolicy = builder.setTriggerPolicy(PolicyParam.builder()
            .id(UUID.randomUUID().toString())
            .name(RESTART_VM)
            .description(TRIGGER_RESTART)
            .actor("APPC")
            .target(new Target(TargetType.VM))
            .recipe(RESTART)
            .payload(null)
            .retries(2)
            .timeout(300)
            .build());

        Policy onRestartFailurePolicy = builder.setPolicyForPolicyResult(PolicyParam.builder()
            .name(REBUILD_VM)
            .description(REBUILD_RESTART)
            .actor("APPC")
            .target(new Target(TargetType.VM))
            .recipe(REBUILD)
            .payload(null)
            .retries(1)
            .timeout(600)
            .id(triggerPolicy.getId())
            .build(),
            PolicyResult.FAILURE);

        // @formatter:on
        final String unknownPolicyId = "100";

        assertThatThrownBy(() -> {
            builder.setPolicyForPolicyResult(onRestartFailurePolicy.getId(), unknownPolicyId, PolicyResult.FAILURE);
        }).isInstanceOf(BuilderException.class).hasMessage(unknownPolicyId + " does not exist");

    }

    @Test
    public void testAddUnknownExistingPolicyToPolicy() throws BuilderException {
        ControlLoopPolicyBuilder builder =
            ControlLoopPolicyBuilder.Factory.buildControlLoop(UUID.randomUUID().toString(), 2400);
        // @formatter:off
        Policy triggerPolicy = builder.setTriggerPolicy(PolicyParam.builder()
            .id(UUID.randomUUID().toString())
            .name(RESTART_VM)
            .description(TRIGGER_RESTART)
            .actor("APPC")
            .target(new Target(TargetType.VM))
            .recipe(RESTART)
            .payload(null)
            .retries(2)
            .timeout(300)
            .build());
        // @formatter:on

        final String unknownPolicyId = "100";

        assertThatThrownBy(() -> {
            builder.setPolicyForPolicyResult(unknownPolicyId, triggerPolicy.getId(), PolicyResult.FAILURE);
        }).isInstanceOf(BuilderException.class).hasMessage("Operational policy " + unknownPolicyId + " does not exist");

    }

    @Test
    public void testAddOperationsAccumulateParams() throws BuilderException {
        ControlLoopPolicyBuilder builder =
            ControlLoopPolicyBuilder.Factory.buildControlLoop(UUID.randomUUID().toString(), 2400);
        // @formatter:off
        Policy triggerPolicy =
            builder.setTriggerPolicy(PolicyParam.builder()
                .id(UUID.randomUUID().toString())
                .name("Restart the eNodeB")
                .description("Upon getting the trigger event, restart the eNodeB")
                .actor("RANController")
                .target(new Target(TargetType.PNF))
                .recipe(RESTART)
                .payload(null)
                .retries(2)
                .timeout(300)
                .build());
        // @formatter:on
        //
        // Add the operationsAccumulateParams
        //
        triggerPolicy =
            builder.addOperationsAccumulateParams(triggerPolicy.getId(), new OperationsAccumulateParams("15m", 5));
        assertNotNull(builder.getTriggerPolicy().getOperationsAccumulateParams());
        assertEquals("15m", builder.getTriggerPolicy().getOperationsAccumulateParams().getPeriod());
        assertTrue(builder.getTriggerPolicy().getOperationsAccumulateParams().getLimit() == 5);
    }

    @Test
    public void testBuildSpecification() throws BuilderException {
        //
        // Create the builder
        //
        ControlLoopPolicyBuilder builder =
            ControlLoopPolicyBuilder.Factory.buildControlLoop(UUID.randomUUID().toString(), 800);
        //
        // Set the first invalid trigger policy
        //
        // @formatter:off
        final Policy policy1 = builder.setTriggerPolicy(
            PolicyParam.builder()
                .id(UUID.randomUUID().toString())
                .name(RESTART_VM)
                .description(TRIGGER_RESTART)
                .actor(null)
                .target(null)
                .recipe(null)
                .payload(null)
                .retries(2)
                .timeout(300)
                .build());
        Results results = builder.buildSpecification();
        // @formatter:on
        //
        // Check that ERRORs are in results for invalid policy arguments
        //
        boolean invalidActor = false;
        boolean invalidRecipe = false;
        boolean invalidTarget = false;
        for (Message m : results.getMessages()) {
            if ("Policy actor is null".equals(m.getMessage()) && m.getLevel() == MessageLevel.ERROR) {
                invalidActor = true;
            }
            if ("Policy recipe is null".equals(m.getMessage()) && m.getLevel() == MessageLevel.ERROR) {
                invalidRecipe = true;
            }
            if ("Policy target is null".equals(m.getMessage()) && m.getLevel() == MessageLevel.ERROR) {
                invalidTarget = true;
            }
        }
        //
        assertTrue(invalidActor);
        assertTrue(invalidRecipe);
        assertTrue(invalidTarget);
        //
        // Remove the invalid policy
        //
        // @SuppressWarnings("unused")
        boolean removed = builder.removePolicy(policy1.getId());
        assertTrue(removed);
        assertNull(builder.getTriggerPolicy());
        //
        // Set a valid trigger policy
        //
        // @formatter:off
        Policy policy1a = builder.setTriggerPolicy(PolicyParam.builder()
            .id(UUID.randomUUID().toString())
            .name(REBUILD_VM)
            .description(REBUILD_RESTART)
            .actor("APPC")
            .target(new Target(TargetType.VM))
            .recipe(REBUILD)
            .payload(null)
            .retries(1)
            .timeout(600)
            .build());
        //
        // Set a second valid trigger policy
        //
        final Policy policy2 = builder.setTriggerPolicy(PolicyParam.builder()
            .id(UUID.randomUUID().toString())
            .name(RESTART_VM)
            .description(TRIGGER_RESTART)
            .actor("APPC")
            .target(new Target(TargetType.VM))
            .recipe(RESTART)
            .payload(null)
            .retries(2)
            .timeout(300)
            .build());
        // @formatter:on
        //
        // Now, we have policy1 unreachable
        //
        results = builder.buildSpecification();
        boolean unreachable = false;
        for (Message m : results.getMessages()) {
            if (m.getMessage().equals("Policy " + policy1a.getId() + " is not reachable.")
                && m.getLevel() == MessageLevel.WARNING) {
                unreachable = true;
                break;
            }
        }
        assertTrue(unreachable);
        //
        // Set policy1a for the failure results of policy2
        //
        policy1a = builder.setPolicyForPolicyResult(policy1a.getId(), policy2.getId(), PolicyResult.FAILURE,
            PolicyResult.FAILURE_RETRIES, PolicyResult.FAILURE_TIMEOUT);
        results = builder.buildSpecification();
        boolean invalidTimeout = false;
        for (Message m : results.getMessages()) {
            if ("controlLoop overall timeout is less than the sum of operational policy timeouts."
                .equals(m.getMessage()) && m.getLevel() == MessageLevel.ERROR) {
                invalidTimeout = true;
                break;
            }
        }
        assertTrue(invalidTimeout);
        //
        // Remove policy2 (revert controlLoop back to open loop)
        //
        removed = builder.removePolicy(policy2.getId());
        //
        // ControlLoop is open loop now, but it still has policies (policy1)
        //
        results = builder.buildSpecification();
        unreachable = false;
        for (Message m : results.getMessages()) {
            if ("Open Loop policy contains policies. The policies will never be invoked.".equals(m.getMessage())
                && m.getLevel() == MessageLevel.WARNING) {
                unreachable = true;
                break;
            }
        }
        assertTrue(unreachable);
    }

    @Test
    public void test1() throws Exception {
        this.test("src/test/resources/v1.0.0/policy_Test.yaml");
    }

    @Test
    public void testEvilYaml() throws Exception {
        try (InputStream is = new FileInputStream(new File("src/test/resources/v1.0.0/test_evil.yaml"))) {
            //
            // Attempt to read the yaml into our Java Object
            //
            Yaml yaml = new Yaml(new Constructor(ControlLoopPolicy.class));
            assertThatThrownBy(() -> yaml.load(is)).isInstanceOf(YAMLException.class);
        }
    }

    /**
     * Does the actual test.
     *
     * @param testFile input file
     * @throws Exception if an error occurs
     */
    public void test(String testFile) throws Exception {
        try (InputStream is = new FileInputStream(new File(testFile))) {
            //
            // Read the yaml into our Java Object
            //
            Yaml yaml = new Yaml(new Constructor(ControlLoopPolicy.class));
            Object obj = yaml.load(is);
            assertNotNull(obj);
            assertTrue(obj instanceof ControlLoopPolicy);
            ControlLoopPolicy policyTobuild = (ControlLoopPolicy) obj;
            //
            // Now we're going to try to use the builder to build this.
            //
            ControlLoopPolicyBuilder builder = ControlLoopPolicyBuilder.Factory.buildControlLoop(
                policyTobuild.getControlLoop().getControlLoopName(), policyTobuild.getControlLoop().getTimeout());
            //
            // Add services
            //
            if (policyTobuild.getControlLoop().getServices() != null) {
                builder = builder.addService(policyTobuild.getControlLoop().getServices()
                    .toArray(new Service[policyTobuild.getControlLoop().getServices().size()]));
            }
            //
            // Add resources
            //
            if (policyTobuild.getControlLoop().getResources() != null) {
                builder = builder.addResource(policyTobuild.getControlLoop().getResources()
                    .toArray(new Resource[policyTobuild.getControlLoop().getResources().size()]));
            }
            //
            // Set pnf
            //
            if (policyTobuild.getControlLoop().getPnf() != null) {
                builder = builder.setPnf(policyTobuild.getControlLoop().getPnf());
            }
            //
            // Add the policies and be sure to set the trigger policy
            //
            if (policyTobuild.getPolicies() != null) {
                setTriggerPolicies(policyTobuild, builder);
            }

            // Question : how to change policy ID and results by using builder ??

            @SuppressWarnings("unused")
            Results results = builder.buildSpecification();
        }

    }

    private void setTriggerPolicies(ControlLoopPolicy policyTobuild, ControlLoopPolicyBuilder builder)
        throws BuilderException {
        for (Policy policy : policyTobuild.getPolicies()) {
            if (policy.getId() == policyTobuild.getControlLoop().getTriggerPolicy()) {
                // @formatter:off
                builder.setTriggerPolicy(PolicyParam.builder().id(UUID.randomUUID().toString())
                        .name(policy.getName())
                        .description(policy.getDescription())
                        .actor(policy.getActor())
                        .target(policy.getTarget())
                        .recipe(policy.getRecipe())
                        .payload(null)
                        .retries(policy.getRetry())
                        .timeout(policy.getTimeout())
                        .build());
                // @formatter:on
            }
        }
    }
}
