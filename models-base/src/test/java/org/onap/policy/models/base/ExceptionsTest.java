/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2019, 2021, 2023, 2024 Nordix Foundation.
 * Modifications Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.models.base;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.onap.policy.models.errors.concepts.ErrorResponse;

class ExceptionsTest {

    private static final String STRING_TEXT = "String";
    private static final String MESSAGE = "Message";

    @Test
    void test() {
        assertNotNull(new PfModelException(Response.Status.OK, MESSAGE));
        assertNotNull(new PfModelException(Response.Status.OK, MESSAGE, STRING_TEXT));
        assertNotNull(new PfModelException(Response.Status.OK, MESSAGE, new IOException()));
        assertNotNull(new PfModelException(Response.Status.OK, MESSAGE, new IOException(), STRING_TEXT));

        String key = "A String";
        PfModelException ae =
            new PfModelException(Response.Status.OK, MESSAGE, new IOException("IO exception message"), key);
        ErrorResponse errorResponse = ae.getErrorResponse();
        assertEquals("Message\nIO exception message", String.join("\n", errorResponse.getErrorDetails()));
        assertEquals(key, ae.getObject());

        assertNotNull(new PfModelRuntimeException(Response.Status.OK, MESSAGE));
        assertNotNull(new PfModelRuntimeException(Response.Status.OK, MESSAGE, STRING_TEXT));
        assertNotNull(new PfModelRuntimeException(Response.Status.OK, MESSAGE, new IOException()));
        assertNotNull(new PfModelRuntimeException(Response.Status.OK, MESSAGE, new IOException(), STRING_TEXT));

        String rkey = "A String";
        PfModelRuntimeException re = new PfModelRuntimeException(Response.Status.OK, "Runtime Message",
            new IOException("IO runtime exception message"), rkey);
        errorResponse = re.getErrorResponse();
        assertEquals("Runtime Message\nIO runtime exception message",
            String.join("\n", errorResponse.getErrorDetails()));
        assertEquals(key, re.getObject());

        PfModelRuntimeException pfre = new PfModelRuntimeException(ae);
        assertEquals(ae.getErrorResponse().getResponseCode(), pfre.getErrorResponse().getResponseCode());
        assertEquals(ae.getMessage(), pfre.getMessage());

        try {
            try {
                throw new PfModelException(Status.BAD_GATEWAY, "An Exception");
            } catch (PfModelException pfme) {
                throw new PfModelRuntimeException(pfme);
            }
        } catch (PfModelRuntimeException pfmre) {
            assertEquals(Status.BAD_GATEWAY, pfmre.getErrorResponse().getResponseCode());
            assertEquals("An Exception", pfmre.getMessage());
            assertEquals(PfModelException.class.getName(), pfmre.getCause().getClass().getName());
        }
    }
}
