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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.LinkedList;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;


public class ControlLoopGuardTest {
    private static final Logger logger = LoggerFactory.getLogger(ControlLoopGuardTest.class);

    @Test
    public void testGuardvdns() throws Exception {
        this.test("src/test/resources/v2.0.0-guard/policy_guard_ONAP_demo_vDNS.yaml");
    }

    @Test
    public void testGuardvusp() throws Exception {
        this.test("src/test/resources/v2.0.0-guard/policy_guard_appc_restart.yaml");
    }

    @Test
    public void testConstructorControlLoopGuard() {
        Guard guard1 = new Guard();
        GuardPolicy guardPolicy1 = new GuardPolicy();
        GuardPolicy guardPolicy2 = new GuardPolicy();
        LinkedList<GuardPolicy> guardPolicies = new LinkedList<>();
        guardPolicies.add(guardPolicy1);
        guardPolicies.add(guardPolicy2);

        ControlLoopGuard controlLoopGuard1 = new ControlLoopGuard();
        controlLoopGuard1.setGuard(guard1);
        controlLoopGuard1.setGuards(guardPolicies);
        ControlLoopGuard controlLoopGuard2 = new ControlLoopGuard(controlLoopGuard1);

        assertEquals(guard1, controlLoopGuard2.getGuard());
        assertEquals(guardPolicies, controlLoopGuard2.getGuards());
    }

    @Test
    public void testEqualsAndHashCode() {
        final Guard guard1 = new Guard();
        GuardPolicy guardPolicy1 = new GuardPolicy();
        GuardPolicy guardPolicy2 = new GuardPolicy();
        LinkedList<GuardPolicy> guardPolicies = new LinkedList<>();
        guardPolicies.add(guardPolicy1);
        guardPolicies.add(guardPolicy2);

        ControlLoopGuard controlLoopGuard1 = new ControlLoopGuard();
        ControlLoopGuard controlLoopGuard2 = new ControlLoopGuard();

        assertTrue(controlLoopGuard1.equals(controlLoopGuard2));
        assertEquals(controlLoopGuard1.hashCode(), controlLoopGuard2.hashCode());

        controlLoopGuard1.setGuard(guard1);
        assertFalse(controlLoopGuard1.equals(controlLoopGuard2));
        controlLoopGuard2.setGuard(guard1);
        assertTrue(controlLoopGuard1.equals(controlLoopGuard2));
        assertEquals(controlLoopGuard1.hashCode(), controlLoopGuard2.hashCode());

        controlLoopGuard1.setGuards(guardPolicies);
        assertFalse(controlLoopGuard1.equals(controlLoopGuard2));
        controlLoopGuard2.setGuards(guardPolicies);
        assertTrue(controlLoopGuard1.equals(controlLoopGuard2));
        assertEquals(controlLoopGuard1.hashCode(), controlLoopGuard2.hashCode());
    }

    @Test
    public void testEqualsSameObject() {
        ControlLoopGuard controlLoopGuard = new ControlLoopGuard();
        assertTrue(controlLoopGuard.equals(controlLoopGuard));
    }

    @Test
    public void testEqualsNull() {
        ControlLoopGuard controlLoopGuard = new ControlLoopGuard();
        assertFalse(controlLoopGuard.equals(null));
    }

    @Test
    public void testEqualsInstanceOfDiffClass() {
        ControlLoopGuard controlLoopGuard = new ControlLoopGuard();
        assertFalse(controlLoopGuard.equals(""));
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
            Yaml yaml = new Yaml(new Constructor(ControlLoopGuard.class));
            Object obj = yaml.load(is);
            assertNotNull(obj);
            assertTrue(obj instanceof ControlLoopGuard);
            dump(obj);
            //
            // Now dump it to a yaml string
            //
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(FlowStyle.BLOCK);
            options.setPrettyFlow(true);
            yaml = new Yaml(options);
            String dumpedYaml = yaml.dump(obj);
            logger.debug(dumpedYaml);
            //
            // Read that string back into our java object
            //
            Object newObject = yaml.load(dumpedYaml);
            dump(newObject);
            assertNotNull(newObject);
            assertTrue(newObject instanceof ControlLoopGuard);

            assertEquals(obj, newObject);
        }
    }

    public void dump(Object obj) {
        logger.debug("Dumping {}", obj.getClass().getCanonicalName());
        logger.debug("{}", obj);
    }
}
