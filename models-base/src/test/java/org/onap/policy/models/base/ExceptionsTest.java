/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import javax.ws.rs.core.Response;

import org.junit.Test;
import org.onap.policy.models.errors.concepts.ErrorResponse;

public class ExceptionsTest {

    @Test
    public void test() {
        assertNotNull(new PfModelException(Response.Status.OK, "Message"));
        assertNotNull(new PfModelException(Response.Status.OK, "Message", "String"));
        assertNotNull(new PfModelException(Response.Status.OK, "Message", new IOException()));
        assertNotNull(new PfModelException(Response.Status.OK, "Message", new IOException(), "String"));

        String key = "A String";
        PfModelException ae =
                new PfModelException(Response.Status.OK, "Message", new IOException("IO exception message"), key);
        ErrorResponse errorResponse = ae.getErrorResponse();
        assertEquals("Message\nIO exception message", String.join("\n", errorResponse.getErrorDetails()));
        assertEquals(key, ae.getObject());

        assertNotNull(new PfModelRuntimeException(Response.Status.OK, "Message"));
        assertNotNull(new PfModelRuntimeException(Response.Status.OK, "Message", "String"));
        assertNotNull(new PfModelRuntimeException(Response.Status.OK, "Message", new IOException()));
        assertNotNull(new PfModelRuntimeException(Response.Status.OK, "Message", new IOException(), "String"));

        String rkey = "A String";
        PfModelRuntimeException re = new PfModelRuntimeException(Response.Status.OK, "Runtime Message",
                new IOException("IO runtime exception message"), rkey);
        errorResponse = re.getErrorResponse();
        assertEquals("Runtime Message\nIO runtime exception message",
                String.join("\n", errorResponse.getErrorDetails()));
        assertEquals(key, re.getObject());
    }
}
