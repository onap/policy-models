/*-
 * ============LICENSE_START=======================================================
 * policy-yaml unit test
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
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

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import org.onap.policy.controlloop.guard.compiler.ControlLoopGuardCompiler;

public class ControlLoopGuardCompilerTest {

    @Test 
    public void testTest1() {
        try {
            this.test("src/test/resources/v2.0.0-guard/policy_guard_ONAP_demo_vDNS.yaml");
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test 
    public void testTest2() {
        try {
            this.test("src/test/resources/v2.0.0-guard/policy_guard_appc_restart.yaml");
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test 
    public void testBad1() {
        try {
            this.test("src/test/resources/v2.0.0-guard/no_guard_policy.yaml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test 
    public void testBad2() {
        try {
            this.test("src/test/resources/v2.0.0-guard/duplicate_guard_policy.yaml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test 
    public void testBad3() {
        try {
            this.test("src/test/resources/v2.0.0-guard/no_guard_constraint.yaml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test 
    public void testBad4() {
        try {
            this.test("src/test/resources/v2.0.0-guard/duplicate_guard_constraint.yaml");
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        } catch (FileNotFoundException e) {
            fail(e.getMessage());
        } catch (IOException e) {
            fail(e.getMessage());
        } catch (Exception e) {
            throw e;
        }
    }

}
