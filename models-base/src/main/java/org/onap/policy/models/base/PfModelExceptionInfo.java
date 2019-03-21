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

/**
 * Interface implemented bu Policy framework model exceptions to allow uniform reading of status codes and cascaded
 * messages.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public interface PfModelExceptionInfo {

    /**
     * Get the status code associated with an exception.
     * @return the status code
     */
    public Response.Status getStatusCode();

    /**
     * Get the messages for all the cascaded exceptions in an exception.
     *
     * @return the cascaded message
     */
    public String getCascadedMessage();

    /**
     * Get the object associated with an exception.
     *
     * @return the object associated with an exception
     */
    public Object getObject();

    /**
     * Get the stack trace of the exception as a string.
     *
     * @return the stack trace of this message as a string
     */
    public String getStackTraceAsString();
}
