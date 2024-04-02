/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019, 2024 Nordix Foundation.
 *  Modifications Copyright (C) 2021 AT&T Intellectual Property. All rights reserved.
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

import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Utility class for managing {@link ErrorResponse objects}.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ErrorResponseUtils {

    /**
     * Store the cascaded messages from an exception and all its nested exceptions in an ErrorResponse object.
     *
     * @param throwable the top level exception
     */
    public static void getExceptionMessages(final ErrorResponse errorResponse, final Throwable throwable) {
        errorResponse.setErrorMessage(throwable.getMessage());

        List<String> cascadedErrorMessages = new ArrayList<>();

        for (var t = throwable; t != null; t = t.getCause()) {
            cascadedErrorMessages.add(t.getMessage());
        }

        if (!cascadedErrorMessages.isEmpty()) {
            errorResponse.setErrorDetails(cascadedErrorMessages);
        }
    }
}
