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

package org.onap.policy.controlloop.compiler;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import org.junit.Test;
import org.onap.policy.controlloop.guard.compiler.ControlLoopGuardCompiler;

public class ControlLoopGuardCompilerTest {

    private static final String ACTOR_ERROR = "Unable to find property 'actor'";

    @Test
    public void testTest1() throws Exception {
        this.test("src/test/resources/v2.0.0-guard/policy_guard_ONAP_demo_vDNS.yaml");
    }

    @Test
    public void testTest2() throws Exception {
        this.test("src/test/resources/v2.0.0-guard/policy_guard_appc_restart.yaml");
    }

    @Test
    public void testBad1() {
        assertThatThrownBy(() -> this.test("src/test/resources/v2.0.0-guard/no_guard_policy.yaml"))
                        .hasMessage("Guard policies should not be null");
    }

    @Test
    public void testBad2() {
        assertThatThrownBy(() -> this.test("src/test/resources/v2.0.0-guard/duplicate_guard_policy.yaml"))
                        .hasMessageContaining(ACTOR_ERROR);
    }

    @Test
    public void testBad3() {
        assertThatThrownBy(() -> this.test("src/test/resources/v2.0.0-guard/no_guard_constraint.yaml"))
                        .hasMessageContaining(ACTOR_ERROR);
    }

    @Test
    public void testBad4() {
        assertThatThrownBy(() -> this.test("src/test/resources/v2.0.0-guard/duplicate_guard_constraint.yaml"))
                        .hasMessageContaining(ACTOR_ERROR);
    }

    /**
     * Does the actual test.
     *
     * @param testFile input test file
     * @throws Exception exception thrown
     */
    public void test(String testFile) throws Exception {
        try (InputStream is = new FileInputStream(new File(testFile))) {
            ControlLoopGuardCompiler.compile(is, null);
        }
    }

}
