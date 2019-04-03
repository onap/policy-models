/*-
 * ============LICENSE_START=======================================================
 * policy-yaml
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

package org.onap.policy.controlloop.policy;

public enum FinalResult {
    /**
     * The Control Loop Policy successfully completed its Operations.
     */
    FINAL_SUCCESS("Final_Success"),
    /**
     * The Control Loop Policy was an Open Loop and is finished.
     */
    FINAL_OPENLOOP("Final_OpenLoop"),
    /**
     * The Control Loop Policy failed in its last Operation Policy. 
     * NOTE: Previous Operation Policies may have been successful.
     */
    FINAL_FAILURE("Final_Failure"),
    /**
     * The Control Loop Policy failed because the overall timeout was met.
     */
    FINAL_FAILURE_TIMEOUT("Final_Failure_Timeout"),
    /**
     * The Control Loop Policy failed because an Operation Policy met its retry limit.
     */
    FINAL_FAILURE_RETRIES("Final_Failure_Retries"),
    /**
     * The Control Loop Policy failed due to an exception.
     */
    FINAL_FAILURE_EXCEPTION("Final_Failure_Exception"), 
    /**
     *  The Control Loop Policy failed due to guard denied.
     */
    FINAL_FAILURE_GUARD("Final_Failure_Guard")
    ;
    
    String result;
    
    private FinalResult(String result) {
        this.result = result;
    }
    
    /**
     * Converts to a result object.
     * 
     * @param result input string
     * @return result object
     */
    public static FinalResult toResult(String result) {
        if (result.equalsIgnoreCase(FINAL_SUCCESS.toString())) {
            return FINAL_SUCCESS;
        }
        if (result.equalsIgnoreCase(FINAL_OPENLOOP.toString())) {
            return FINAL_OPENLOOP;
        }
        if (result.equalsIgnoreCase(FINAL_FAILURE.toString())) {
            return FINAL_FAILURE;
        }
        if (result.equalsIgnoreCase(FINAL_FAILURE_TIMEOUT.toString())) {
            return FINAL_FAILURE_TIMEOUT;
        }
        if (result.equalsIgnoreCase(FINAL_FAILURE_RETRIES.toString())) {
            return FINAL_FAILURE_RETRIES;
        }
        if (result.equalsIgnoreCase(FINAL_FAILURE_EXCEPTION.toString())) {
            return FINAL_FAILURE_EXCEPTION;
        }
        if (result.equalsIgnoreCase(FINAL_FAILURE_GUARD.toString())) {
            return FINAL_FAILURE_GUARD;
        }
        return null;
    }
    
    /**
     * Check if the result really is a result.
     * 
     * @param result string
     * @param finalResult result object
     * @return true if a result
     */
    public static boolean isResult(String result, FinalResult finalResult) {
        FinalResult toResult = FinalResult.toResult(result);
        if (toResult == null) {
            return false;
        }
        return toResult.equals(finalResult);
    }

}
