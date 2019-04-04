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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.onap.policy.controlloop.policy.ControlLoopPolicy;
import org.onap.policy.controlloop.policy.FinalResult;

public class ControlLoopCompilerTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test 
    public void testTest() throws Exception {
        List<String> expectedOnErrorMessages = new ArrayList<>();
        expectedOnErrorMessages.add("Operational Policy has an bad ID");
        expectedOnErrorMessages.add("Policy id is set to a PolicyResult SUCCESS");
        expectedOnErrorMessages.add("Policy id is set to a FinalResult FINAL_SUCCESS");
        expectedOnErrorMessages.add("Policy actor is null");
        expectedOnErrorMessages.add("Policy actor is invalid");
        expectedOnErrorMessages.add("Policy recipe is null");
        expectedOnErrorMessages.add("Policy recipe is invalid");
        expectedOnErrorMessages.add("Policy recipe is invalid");
        expectedOnErrorMessages.add("Policy recipe is invalid");
        expectedOnErrorMessages.add("Policy target is null");
        expectedOnErrorMessages.add("Policy target is invalid");
        expectedOnErrorMessages.add("Policy success is neither another policy nor FINAL_SUCCESS");
        expectedOnErrorMessages.add("Policy failure is neither another policy nor FINAL_FAILURE");
        expectedOnErrorMessages.add("Policy failure retries is neither another policy nor FINAL_FAILURE_RETRIES");
        expectedOnErrorMessages.add("Policy failure timeout is neither another policy nor FINAL_FAILURE_TIMEOUT");
        expectedOnErrorMessages.add("Policy failure exception is neither another policy nor FINAL_FAILURE_EXCEPTION");
        expectedOnErrorMessages.add("Policy failure guard is neither another policy nor FINAL_FAILURE_GUARD");
        expectedOnErrorMessages.add("Unsupported version for this compiler");
        expectedOnErrorMessages.add("controlLoop overall timeout is less than the sum of operational policy timeouts.");

        TestControlLoopCompilerCallback testControlLoopCompilerCallback = 
                        new TestControlLoopCompilerCallback(expectedOnErrorMessages);
        ControlLoopPolicy controlLoopPolicy = this.test("src/test/resources/v1.0.0/test.yaml", 
                        testControlLoopCompilerCallback);
        assertEquals(22, controlLoopPolicy.getPolicies().size());
        assertTrue(testControlLoopCompilerCallback.areAllExpectedOnErrorsReceived());
    }

    @Test
    public void testSuccessConnectedToUnknownPolicy() throws Exception {
        expectedException.expect(CompilerException.class);
        expectedException.expectMessage(
                        "Operation Policy unique-policy-id-1-restart is connected to unknown policy unknown-policy");
        this.test("src/test/resources/v1.0.0/bad_policy_success_connected_to_unknown_policy.yaml");
    }

    @Test
    public void testFailureConnectedToUnknownPolicy() throws Exception {
        expectedException.expect(CompilerException.class);
        expectedException.expectMessage(
                        "Operation Policy unique-policy-id-1-restart is connected to unknown policy unknown-policy");
        this.test("src/test/resources/v1.0.0/bad_policy_failure_connected_to_unknown_policy.yaml");
    }

    @Test
    public void testFailureTimeoutToUnknownPolicy() throws Exception {
        expectedException.expect(CompilerException.class);
        expectedException.expectMessage(
                        "Operation Policy unique-policy-id-1-restart is connected to unknown policy unknown-policy");
        this.test("src/test/resources/v1.0.0/bad_policy_failure_timeout_connected_to_unknown_policy.yaml");
    }

    @Test
    public void testFailureRetriesToUnknownPolicy() throws Exception {
        expectedException.expect(CompilerException.class);
        expectedException.expectMessage(
                        "Operation Policy unique-policy-id-1-restart is connected to unknown policy unknown-policy");
        this.test("src/test/resources/v1.0.0/bad_policy_failure_retries_connected_to_unknown_policy.yaml");
    }

    @Test
    public void testFailureExceptionToUnknownPolicy() throws Exception {
        expectedException.expect(CompilerException.class);
        expectedException.expectMessage(
                        "Operation Policy unique-policy-id-1-restart is connected to unknown policy unknown-policy");
        this.test("src/test/resources/v1.0.0/bad_policy_failure_exception_connected_to_unknown_policy.yaml");
    }

    @Test
    public void testFailureGuardToUnknownPolicy() throws Exception {
        expectedException.expect(CompilerException.class);
        expectedException.expectMessage(
                        "Operation Policy unique-policy-id-1-restart is connected to unknown policy unknown-policy");
        this.test("src/test/resources/v1.0.0/bad_policy_failure_guard_connected_to_unknown_policy.yaml");
    }

    @Test 
    public void testInvalidTriggerPolicyId() throws Exception {
        expectedException.expect(CompilerException.class);
        expectedException.expectMessage(
                        "Unexpected value for trigger_policy, should only be " 
                        + FinalResult.FINAL_OPENLOOP.toString() + " or a valid Policy ID");
        this.test("src/test/resources/v1.0.0/bad_trigger_1.yaml");
    }

    @Test 
    public void testNoTriggerPolicyId() throws Exception {
        expectedException.expect(CompilerException.class);
        this.test("src/test/resources/v1.0.0/bad_trigger_no_trigger_id.yaml");
    }

    @Test 
    public void testNoControlLoopName() throws Exception {
        List<String> expectedOnErrorMessages = new ArrayList<>();
        expectedOnErrorMessages.add("Missing controlLoopName");
        expectedOnErrorMessages.add("Unsupported version for this compiler");
        TestControlLoopCompilerCallback testControlLoopCompilerCallback = 
                        new TestControlLoopCompilerCallback(expectedOnErrorMessages);
        this.test("src/test/resources/v1.0.0/bad_control_loop_no_control_loop_name.yaml", 
                        testControlLoopCompilerCallback);
        assertTrue(testControlLoopCompilerCallback.areAllExpectedOnErrorsReceived());
    }

    @Test 
    public void testInvalidFinalResult() throws Exception {
        expectedException.expect(CompilerException.class);
        expectedException.expectMessage(
                     "Unexpected Final Result for trigger_policy, should only be FINAL_OPENLOOP or a valid Policy ID");
        this.test("src/test/resources/v1.0.0/bad_trigger_2.yaml");
    }

    @Test 
    public void testCompileEmptyFile() throws Exception {
        expectedException.expect(CompilerException.class);
        expectedException.expectMessage("Could not parse yaml specification.");
        this.test("src/test/resources/v1.0.0/empty.yaml");
    }

    public ControlLoopPolicy test(String testFile) throws Exception {
        return test(testFile, null);
    }

    /**
     * Does the actual test.
     * 
     * @param testFile test file
     * @param controlLoopCompilerCallback callback method
     * @return the policy object
     * @throws Exception exception
     */
    public ControlLoopPolicy test(String testFile, 
                    ControlLoopCompilerCallback controlLoopCompilerCallback) throws Exception {
        try (InputStream is = new FileInputStream(new File(testFile))) {
            return ControlLoopCompiler.compile(is, controlLoopCompilerCallback);
        } catch (FileNotFoundException e) {
            fail(e.getMessage());
        } catch (IOException e) {
            fail(e.getMessage());
        } catch (Exception e) {
            throw e;
        }
        return null;
    }

    class TestControlLoopCompilerCallback implements ControlLoopCompilerCallback {

        private List<String> expectedOnErrorMessages;

        public TestControlLoopCompilerCallback(List<String> expectedOnErrorMessages) {
            this.expectedOnErrorMessages = expectedOnErrorMessages;
        }

        @Override
        public boolean onWarning(String message) {
            return true;
        }

        @Override
        public boolean onError(String message) {
            if (!expectedOnErrorMessages.remove(message)) {
                fail("Unexpected onError message: " + message);
            }
            return true;
        }

        public boolean areAllExpectedOnErrorsReceived() {
            return expectedOnErrorMessages.size() == 0;
        }

    }

}
