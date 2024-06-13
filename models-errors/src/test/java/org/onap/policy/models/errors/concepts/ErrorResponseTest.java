/*
 * ============LICENSE_START=======================================================
 * ONAP Policy Decision Models
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2023, 2024 Nordix Foundation.
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

package org.onap.policy.models.errors.concepts;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.ws.rs.core.Response;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ErrorResponseTest {

    static final Logger logger = LoggerFactory.getLogger(ErrorResponseTest.class);

    @Test
    void test() {
        assertThatCode(() -> {
            ErrorResponse error = new ErrorResponse();

            error.setResponseCode(Response.Status.NOT_ACCEPTABLE);
            error.setErrorMessage("Missing metadata section");

            error.setErrorDetails(List.of("You must have a metadata section with policy-id value"));

            error.setWarningDetails(List.of("Please make sure topology template field is included."));

            StandardCoder coder = new StandardCoder();
            String jsonOutput = coder.encode(error);

            logger.debug("Resulting json output {}", jsonOutput);

            ErrorResponse deserializedResponse = coder.decode(jsonOutput, ErrorResponse.class);

            assertEquals(deserializedResponse, error);
        }).doesNotThrowAnyException();
    }

}
