/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2020, 2023, 2024 Nordix Foundation.
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import org.onap.policy.models.errors.concepts.ErrorResponseInfo;

/**
 * Test PfModelExceptionInfo interface.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
class PfModelExceptionInfoTest {

    @Test
    void testExceptionInfo() {
        final PfModelException pfme = new PfModelException(Response.Status.ACCEPTED, "HELLO");
        assertThat(pfme).hasMessage("HELLO");
        assertEquals("Server returned: Accepted", getErrorMessage(pfme).substring(0, 25));
        assertNotNull(pfme.toString());

        final PfModelRuntimeException pfmr = new PfModelRuntimeException(Response.Status.ACCEPTED, "HELLO");
        assertThat(pfmr).hasMessage("HELLO");
        assertEquals("Server returned: Accepted", getErrorMessage(pfmr).substring(0, 25));
        assertNotNull(pfmr.toString());
    }

    private String getErrorMessage(final ErrorResponseInfo eri) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("Server returned: ");
        stringBuilder.append(eri.getErrorResponse().getResponseCode().toString());
        stringBuilder.append("Error Message:\n");
        stringBuilder.append(eri.getErrorResponse().getErrorMessage());
        stringBuilder.append("\nDetailed Message:\n");
        stringBuilder.append(String.join("\n", eri.getErrorResponse().getErrorDetails()));

        return stringBuilder.toString();
    }
}
