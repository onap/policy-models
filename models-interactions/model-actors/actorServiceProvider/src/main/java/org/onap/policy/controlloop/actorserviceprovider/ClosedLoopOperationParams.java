/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.controlloop.actorserviceprovider;

import java.util.concurrent.Executor;
import org.onap.policy.controlloop.policy.Policy;

/**
 * Arguments used by closed loop operations.
 */
public interface ClosedLoopOperationParams {

    /**
     * Gets the event for which the operation applies.
     *
     * @return the onset event
     */
    ClosedLoopEventContext getContext();

    /**
     * Gets the executor to use to run the operation.
     *
     * @return the executor
     */
    Executor getExecutor();

    /**
     * Gets the policy associated with the operation.
     *
     * @return the operational policy
     */
    Policy getPolicy();

    /**
     * Gets the function to invoke when the operation starts.
     *
     * @return the function to invoke when the operation starts
     */
    Runnable getStartCallback();

    /**
     * Gets the sub request ID that should be associated with the operation.
     *
     * @return the new operation's sub request ID
     */
    String getSubRequestId();
}
