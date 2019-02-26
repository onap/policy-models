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

/**
 * This class is a base model run time exception from which all model run time exceptions are sub
 * classes.
 */
public class PfModelRuntimeException extends RuntimeException {
    private static final long serialVersionUID = -8507246953751956974L;

    // The object on which the exception was thrown
    private final transient Object object;

    /**
     * Instantiates a new model runtime exception.
     *
     * @param message the message on the exception
     */
    public PfModelRuntimeException(final String message) {
        this(message, null);
    }

    /**
     * Instantiates a new model runtime exception.
     *
     * @param message the message on the exception
     * @param object the object that the exception was thrown on
     */
    public PfModelRuntimeException(final String message, final Object object) {
        super(message);
        this.object = object;
    }

    /**
     * Instantiates a new model runtime exception.
     *
     * @param message the message on the exception
     * @param exception the exception that caused this model exception
     */
    public PfModelRuntimeException(final String message, final Exception exception) {
        this(message, exception, null);
    }

    /**
     * Instantiates a new model runtime exception.
     *
     * @param message the message on the exception
     * @param exception the exception that caused this model exception
     * @param object the object that the exception was thrown on
     */
    public PfModelRuntimeException(final String message, final Exception exception, final Object object) {
        super(message, exception);
        this.object = object;
    }

    /**
     * Get the message from this exception and its causes.
     *
     * @return the message of this exception and all the exceptions that caused this exception
     */
    public String getCascadedMessage() {
        return PfModelException.buildCascadedMessage(this);
    }

    /**
     * Get the object on which the exception was thrown.
     *
     * @return The object
     */
    public Object getObject() {
        return object;
    }
}
