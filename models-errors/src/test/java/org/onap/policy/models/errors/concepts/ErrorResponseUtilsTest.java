/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2020, 2024 Nordix Foundation.
 *  Modifications Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.models.errors.concepts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import org.junit.jupiter.api.Test;

/**
 * Test the {@link ErrorResponseUtils} class.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
class ErrorResponseUtilsTest {
    private static final String EXCEPTION1 = "Exception 1";

    @Test
    void testErrorResponseUtils() {
        final IOException ioe = new IOException(EXCEPTION1, new NumberFormatException("Exception 0"));
        final ErrorResponse errorResponse = new ErrorResponse();
        assertThat(ioe).hasMessage(EXCEPTION1);

        ErrorResponseUtils.getExceptionMessages(errorResponse, ioe);
        assertEquals(EXCEPTION1, errorResponse.getErrorMessage());
        assertEquals(EXCEPTION1, errorResponse.getErrorDetails().get(0));
        assertEquals("Exception 0", errorResponse.getErrorDetails().get(1));
    }
}
