/*-
 * ============LICENSE_START=======================================================
 * policy-yaml unit test
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

package org.onap.policy.controlloop.policy.guard;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.onap.policy.controlloop.policy.builder.BuilderException;
import org.onap.policy.controlloop.policy.builder.Message;
import org.onap.policy.controlloop.policy.builder.MessageLevel;
import org.onap.policy.controlloop.policy.builder.Results;
import org.onap.policy.controlloop.policy.guard.builder.ControlLoopGuardBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

public class ControlLoopGuardBuilderTest {
    private static final Logger logger = LoggerFactory.getLogger(ControlLoopGuardBuilderTest.class);

    @Test
    public void testControlLoopGuard() throws BuilderException {
        //
        // Create a builder
        //
        ControlLoopGuardBuilder builder = ControlLoopGuardBuilder.Factory.buildControlLoopGuard(new Guard());
        //
        // Assert there is no guard policies yet
        //
        Results results = builder.buildSpecification();
        boolean noGuardPolicies = false;
        for (Message m : results.getMessages()) {
            if ("ControlLoop Guard should have at least one guard policies".equals(m.getMessage())
                            && m.getLevel() == MessageLevel.ERROR) {
                noGuardPolicies = true;
                break;
            }
        }
        assertTrue(noGuardPolicies);
        //
        // Add a guard policy without limit constraint
        //
        String clname = "CL_vUSP123";
        List<String> targets = new LinkedList<>();
        targets.add("s1");
        targets.add("s2");
        targets.add("s3");
        MatchParameters matchParameters = new MatchParameters(clname, "APPC", "Restart", targets);
        GuardPolicy policy1 = new GuardPolicy("id123", "guardpolicy1", "description aaa", matchParameters);
        builder = builder.addGuardPolicy(policy1);
        //
        // Assert there is no limit constraint associated with the only guard policy
        //
        results = builder.buildSpecification();
        boolean noConstraint = false;
        for (Message m : results.getMessages()) {
            if ("Guard policy guardpolicy1 does not have any limit constraint".equals(m.getMessage())
                            && m.getLevel() == MessageLevel.ERROR) {
                noConstraint = true;
                break;
            }
        }
        assertTrue(noConstraint);
        //
        // Add a constraint to policy1
        //
        Map<String, String> activeTimeRange = new HashMap<>();
        activeTimeRange.put("start", "00:00:00-05:00");
        activeTimeRange.put("end", "23:59:59-05:00");
        List<String> blacklist = new LinkedList<>();
        blacklist.add("eNodeB_common_id1");
        blacklist.add("eNodeB_common_id2");
        Map<String, String> timeWindow = new HashMap<>();
        timeWindow.put("value", "10");
        timeWindow.put("units", "minute");
        Constraint cons = new Constraint(5, timeWindow, activeTimeRange, blacklist);
        builder = builder.addLimitConstraint(policy1.getId(), cons);
        //
        // Add a duplicate constraint to policy1
        //
        builder = builder.addLimitConstraint(policy1.getId(), cons);
        //
        // Assert there are duplicate constraints associated with the only guard policy
        //
        results = builder.buildSpecification();
        boolean duplicateConstraint = false;
        for (Message m : results.getMessages()) {
            if ("Guard policy guardpolicy1 has duplicate limit constraints".equals(m.getMessage())
                            && m.getLevel() == MessageLevel.WARNING) {
                duplicateConstraint = true;
                break;
            }
        }
        assertTrue(duplicateConstraint);
        //
        // Remove the duplicate constraint
        //
        builder = builder.removeLimitConstraint(policy1.getId(), cons);
        //
        // Add a duplicate guard policy
        //
        builder = builder.addGuardPolicy(policy1);
        builder = builder.addLimitConstraint(policy1.getId(), cons);
        //
        // Assert there are duplicate guard policies
        //
        results = builder.buildSpecification();
        boolean duplicateGuardPolicy = false;
        for (Message m : results.getMessages()) {
            if ("There are duplicate guard policies".equals(m.getMessage())
                            && m.getLevel() == MessageLevel.WARNING) {
                duplicateGuardPolicy = true;
                break;
            }
        }
        assertTrue(duplicateGuardPolicy);
        //
        // Remove the duplicate guard policy
        //
        builder = builder.removeGuardPolicy(policy1);
        //
        // Assert there are no Error/Warning message
        //
        results = builder.buildSpecification();
        assertTrue(results.getMessages().size() == 1);
    }

    @Test
    public void test1() throws Exception {
        this.test("src/test/resources/v2.0.0-guard/policy_guard_ONAP_demo_vDNS.yaml");
    }

    @Test
    public void test2() throws Exception {
        this.test("src/test/resources/v2.0.0-guard/policy_guard_appc_restart.yaml");
    }

    /**
     * Do the actual test.
     *
     * @param testFile input test file
     * @throws Exception if an error occurs
     */
    public void test(String testFile) throws Exception {
        try (InputStream is = new FileInputStream(new File(testFile))) {
            //
            // Read the yaml into our Java Object
            //
            Yaml yaml = new Yaml(new Constructor(ControlLoopGuard.class));
            Object obj = yaml.load(is);
            assertNotNull(obj);
            assertTrue(obj instanceof ControlLoopGuard);
            ControlLoopGuard guardTobuild = (ControlLoopGuard) obj;
            //
            // Now we're going to try to use the builder to build this.
            //
            ControlLoopGuardBuilder builder =
                            ControlLoopGuardBuilder.Factory.buildControlLoopGuard(guardTobuild.getGuard());
            //
            // Add guard policy
            //
            if (guardTobuild.getGuards() != null) {
                builder = builder.addGuardPolicy(guardTobuild.getGuards().toArray(
                                new GuardPolicy[guardTobuild.getGuards().size()]));
            }
            //
            // Build the specification
            //
            Results results = builder.buildSpecification();
            //
            // Print out the specification
            //
            logger.debug(results.getSpecification());
        }
    }
}
