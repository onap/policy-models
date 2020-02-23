/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2018 Samsung Electronics Co., Ltd. All rights reserved.
 *  Modifications Copyright (C) 2019 Nordix Foundation.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.policy.sdnr.util;

public enum StatusCodeEnum {
    ACCEPTED("ACCEPTED"), ERROR("ERROR"), REJECT("REJECT"), SUCCESS("SUCCESS"), FAILURE("FAILURE"),
    PARTIAL_SUCCESS("PARTIAL SUCCESS"), PARTIAL_FAILURE("PARTIAL FAILURE");

    private String name;

    StatusCodeEnum(final String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }

    /**
     * Determine status enum from the code.
     *
     * @param statusCode integer code indicating the status
     * @return enum representation of status code
     */
    public static StatusCodeEnum fromStatusCode(final int statusCode) {
        if (statusCode == 100) {
            return ACCEPTED;
        }

        if (statusCode == 200) {
            return SUCCESS;
        }

        if (isRejectStatusCode(statusCode)) {
            return REJECT;
        }

        if (statusCode == 400) {
            return ERROR;
        }

        if (isFailureStatusCode(statusCode)) {
            return FAILURE;
        }

        if (statusCode == 500) {
            return PARTIAL_SUCCESS;
        }

        if (isPartialFailureStatusCode(statusCode)) {
            return PARTIAL_FAILURE;
        }

        return null;
    }

    /**
     * Best guess on a value, but since some of these
     * codes could be any value this may be difficult to
     * use during runtime.

     * @param code input StatusCodeEnum
     * @return integer value
     */
    public static int toValue(StatusCodeEnum code) {
        switch (code) {
            case ACCEPTED:
                return 100;

            case SUCCESS:
                return 200;

            case REJECT:
                return 300;

            case ERROR:
                return 400;

            case FAILURE:
                return 450;

            case PARTIAL_SUCCESS:
                return 500;

            case PARTIAL_FAILURE:
                return 501;

            default:
                return 0;
        }
    }

    private static boolean isRejectStatusCode(final int statusCode) {
        return statusCode >= 300 && statusCode <= 313;
    }

    private static boolean isFailureStatusCode(final int statusCode) {
        return statusCode == 450 || (statusCode >= 401 && statusCode <= 406);
    }

    private static boolean isPartialFailureStatusCode(final int statusCode) {
        return statusCode >= 501 && statusCode <= 599;
    }

}
