/*
 * ============LICENSE_START=======================================================
 * ONAP Policy Decision Models
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2023 Nordix Foundation.
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

package org.onap.policy.models.decisions.concepts;

import jakarta.ws.rs.core.Response;
import java.io.Serial;
import lombok.Getter;
import lombok.ToString;
import org.onap.policy.models.errors.concepts.ErrorResponse;
import org.onap.policy.models.errors.concepts.ErrorResponseInfo;
import org.onap.policy.models.errors.concepts.ErrorResponseUtils;

@Getter
@ToString
public class DecisionException extends RuntimeException implements ErrorResponseInfo {
    @Serial
    private static final long serialVersionUID = -1255351537691201052L;
    private final ErrorResponse errorResponse = new ErrorResponse();

    /**
     * Constructor.
     *
     * @param statusCode HTTP Response code object
     * @param message exception message
     */
    public DecisionException(final Response.Status statusCode, final String message) {
        super(message);
        errorResponse.setResponseCode(statusCode);
        ErrorResponseUtils.getExceptionMessages(errorResponse, this);
    }
}
