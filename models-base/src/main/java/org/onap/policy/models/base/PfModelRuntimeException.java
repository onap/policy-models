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

import javax.ws.rs.core.Response;

import lombok.Getter;
import lombok.ToString;

/**
 * This class is a base model run time exception from which all model run time exceptions are sub
 * classes.
 */
@Getter
@ToString
public class PfModelRuntimeException extends RuntimeException {
    private static final long serialVersionUID = -8507246953751956974L;

    // The return code on the exception
    private final Response.Status statusCode;

    // The object on which the exception was thrown
    private final transient Object object;

    /**
     * Instantiates a new model runtime exception.
     *
     * @param statusCode the return code for the exception
     * @param message the message on the exception
     */
    public PfModelRuntimeException(final Response.Status statusCode, final String message) {
        this(statusCode, message, null);
    }

    /**
     * Instantiates a new model runtime exception.
     *
     * @param statusCode the return code for the exception
     * @param message the message on the exception
     * @param object the object that the exception was thrown on
     */
    public PfModelRuntimeException(final Response.Status statusCode, final String message, final Object object) {
        super(message);
        this.object = object;
        this.statusCode = statusCode;
    }

    /**
     * Instantiates a new model runtime exception.
     *
     * @param statusCode the return code for the exception
     * @param message the message on the exception
     * @param exception the exception that caused this model exception
     */
    public PfModelRuntimeException(final Response.Status statusCode, final String message, final Exception exception) {
        this(statusCode, message, exception, null);
    }

    /**
     * Instantiates a new model runtime exception.
     *
     * @param statusCode the return code for the exception
     * @param message the message on the exception
     * @param exception the exception that caused this model exception
     * @param object the object that the exception was thrown on
     */
    public PfModelRuntimeException(final Response.Status statusCode, final String message, final Exception exception,
            final Object object) {
        super(message, exception);
        this.object = object;
        this.statusCode = statusCode;
    }

    /**
     * Get the message from this exception and its causes.
     *
     * @return the message of this exception and all the exceptions that caused this exception
     */
    public String getCascadedMessage() {
        return PfModelException.buildCascadedMessage(this);
    }
}
