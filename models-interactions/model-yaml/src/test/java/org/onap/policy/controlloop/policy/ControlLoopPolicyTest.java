/*-
 * ============LICENSE_START=======================================================
 * policy-yaml unit test
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import org.junit.Test;
import org.onap.policy.common.utils.coder.YamlJsonTranslator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ControlLoopPolicyTest {
    private static final Logger logger = LoggerFactory.getLogger(ControlLoopPolicyTest.class);

    @Test
    public void test1() throws Exception {
        this.test("src/test/resources/v1.0.0/policy_Test.yaml");
    }

    @Test
    public void testvService1() throws Exception {
        this.test("src/test/resources/v1.0.0/policy_vService.yaml");
    }

    @Test
    public void testOpenLoop() throws Exception {
        this.test("src/test/resources/v1.0.0/policy_OpenLoop.yaml");
    }

    @Test
    public void testvdns() throws Exception {
        this.test("src/test/resources/v2.0.0/policy_ONAP_demo_vDNS.yaml");
    }

    @Test
    public void testvFirewall() throws Exception {
        this.test("src/test/resources/v2.0.0/policy_ONAP_demo_vFirewall.yaml");
    }

    @Test
    public void testvcpe() throws Exception {
        this.test("src/test/resources/v2.0.0/policy_ONAP_UseCase_vCPE.yaml");
    }

    @Test
    public void testvpci() throws Exception {
        this.test("src/test/resources/v2.0.0/policy_ONAP_UseCase_vPCI.yaml");
    }

    @Test
    public void testvolte() throws Exception {
        this.test("src/test/resources/v2.0.0/policy_ONAP_UseCase_VOLTE.yaml");
    }

    /**
     * Does the actual test.
     *
     * @param testFile input file
     * @throws Exception if an error occurs
     */
    public void test(String testFile) throws Exception {
        try (InputStreamReader fileInputStream = new InputStreamReader(new FileInputStream(testFile))) {
            //
            // Read the yaml into our Java Object
            //
            ControlLoopPolicy controlLoopPolicy1 =
                new YamlJsonTranslator().fromYaml(fileInputStream, ControlLoopPolicy.class);
            assertNotNull(controlLoopPolicy1);
            dump(controlLoopPolicy1);

            //
            // Now dump it to a yaml string
            //
            String dumpedYaml = new YamlJsonTranslator().toYaml(controlLoopPolicy1);
            logger.debug(dumpedYaml);
            //
            // Read that string back into our java object
            //
            ControlLoopPolicy controlLoopPolicy2 =
                new YamlJsonTranslator().fromYaml(dumpedYaml, ControlLoopPolicy.class);
            assertNotNull(controlLoopPolicy2);
            dump(controlLoopPolicy2);

            // test serialization
            assertEquals(controlLoopPolicy1, controlLoopPolicy2);
        }
    }

    public void dump(Object obj) {
        logger.debug("Dumping ", obj.getClass().getName());
        logger.debug("{}", obj);
    }
}
