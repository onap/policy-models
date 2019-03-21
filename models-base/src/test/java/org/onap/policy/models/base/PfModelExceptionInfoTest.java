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

import javax.ws.rs.core.Response;

import org.junit.Test;

/**
 * Test PfModelExceptionInfo interface.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class PfModelExceptionInfoTest {
    @Test
    public void exceptionInfoTest() {
        try {
            throw new PfModelException(Response.Status.ACCEPTED, "HELLO");
        } catch (PfModelException  pfme) {
            String errorMessage = getErrorMessage(pfme);
            assertEquals("Server returned: Accepted", errorMessage.substring(0, 25));
        }

        try {
            throw new PfModelRuntimeException(Response.Status.ACCEPTED, "HELLO");
        } catch (PfModelRuntimeException pfme) {
            String errorMessage = getErrorMessage(pfme);
            assertEquals("Server returned: Accepted", errorMessage.substring(0, 25));
        }
    }

    private String getErrorMessage(final PfModelExceptionInfo pfme) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("Server returned: ");
        stringBuilder.append(pfme.getStatusCode().toString());
        stringBuilder.append("\nDetailed Message:\n");
        stringBuilder.append(pfme.getCascadedMessage());
        stringBuilder.append("\nStack Trace:\n");
        stringBuilder.append(pfme.getStackTraceAsString());

        return stringBuilder.toString();
    }
}
