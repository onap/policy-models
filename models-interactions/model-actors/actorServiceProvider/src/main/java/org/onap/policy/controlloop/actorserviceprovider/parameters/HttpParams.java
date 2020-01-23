/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.controlloop.actorserviceprovider.parameters;

import lombok.Data;
import org.onap.policy.common.parameters.BeanValidationResult;
import org.onap.policy.common.parameters.ObjectValidationResult;
import org.onap.policy.common.parameters.ValidationResult;
import org.onap.policy.common.parameters.ValidationStatus;

/**
 * Parameters used by OperationManagers that connect to a server via HTTP.
 */
@Data
public class HttpParams {

    public static final String CLIENT_NAME_FIELD = "clientName";
    public static final String PATH_FIELD = "path";
    public static final String TIMEOUT_SEC_FIELD = "timeoutSec";

    /**
     * Name of the HttpClient, as found in the HttpClientFactory.
     */
    private String clientName;

    /**
     * URI path.
     */
    private String path;

    /**
     * Amount of time, in seconds to wait for the HTTP request to complete, where zero
     * indicates that it should wait forever. The default is zero.
     */
    private long timeoutSec = 0;


    /**
     * Validates the parameters.
     *
     * @param resultName name of the result
     *
     * @return the validation result
     */
    public ValidationResult validate(String resultName) {
        BeanValidationResult result = new BeanValidationResult(resultName, this);

        result.validateNotNull(CLIENT_NAME_FIELD, clientName);
        result.validateNotNull(PATH_FIELD, path);

        if (timeoutSec < 0) {
            result.addResult(new ObjectValidationResult(TIMEOUT_SEC_FIELD, timeoutSec, ValidationStatus.INVALID,
                            "is negative"));
        }

        return result;
    }
}
