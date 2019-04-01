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

public enum ResponseValue {
    ACCEPT("ACCEPT"), ERROR("ERROR"), REJECT("REJECT"), SUCCESS("SUCCESS"), FAILURE("FAILURE");

    @SerializedName("Value")
    private String value;

    private ResponseValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    /**
     * Convert a String value to a ResponseValue.
     *
     * @param value the String value
     * @return the ResponseValue
     */
    public static ResponseValue toResponseValue(String value) {
        if (value == null) {
            return null;
        }

        if (value.equals(ACCEPT.toString())) {
            return ACCEPT;
        }
        if (value.equals(ERROR.toString())) {
            return ERROR;
        }
        if (value.equals(REJECT.toString())) {
            return REJECT;
        }
        if (value.equals(SUCCESS.toString())) {
            return SUCCESS;
        }
        if (value.equals(FAILURE.toString())) {
            return FAILURE;
        }

        return null;
    }

}
