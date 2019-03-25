/*-
 * ============LICENSE_START=======================================================
 * appc
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

package org.onap.policy.appc;

import com.google.gson.annotations.SerializedName;

public enum ResponseCode {
    ACCEPT(100), ERROR(200), REJECT(300), SUCCESS(400), FAILURE(500);

    @SerializedName("Code")
    private Integer code;

    private ResponseCode(int code) {
        this.code = code;
    }

    public int getValue() {
        return this.code;
    }

    @Override
    public String toString() {
        return Integer.toString(this.code);
    }

    /**
     * Convert an integer code to a ResponseCode.
     * 
     * @param code the integer code
     * @return the ResponseCode
     */
    public static ResponseCode toResponseCode(int code) {
        if (code == ACCEPT.code) {
            return ACCEPT;
        }
        if (code == ERROR.code) {
            return ERROR;
        }
        if (code == REJECT.code) {
            return REJECT;
        }
        if (code == SUCCESS.code) {
            return SUCCESS;
        }
        if (code == FAILURE.code) {
            return FAILURE;
        }
        return null;
    }
}
