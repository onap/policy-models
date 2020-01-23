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

package org.onap.policy.controlloop.actorserviceprovider.spi;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.onap.policy.aai.ControlLoopAaiData;
import org.onap.policy.common.capabilities.Startable;
import org.onap.policy.controlloop.VirtualControlLoopEvent;
import org.onap.policy.controlloop.policy.Policy;
import org.onap.policy.controlloop.policy.PolicyResult;

/**
 * This is the service interface for defining an Actor operation used in Control Loop
 * Operational Policies for performing actions on runtime entities.
 */
public interface OperationManager extends Startable, Configurable<Map<String,Object>> {

    /**
     * Gets the name of the associated actor.
     *
     * @return the name of the associated actor
     */
    String getActorName();

    /**
     * Gets the name of the operation.
     *
     * @return the operation name
     */
    String getName();

    /**
     * Called by enforcement PDP engine to start the specified operation. The operation is
     * executed asynchronously.
     *
     * @param aaiData associated A&AI data
     * @param onset associated event
     * @param policy operation to be performed
     * @param subRequestId sub request id to be used in the operation
     * @return a future that can be used to cancel, or await the result of, the operation
     */
    CompletableFuture<PolicyResult> start(ControlLoopAaiData aaiData, VirtualControlLoopEvent onset, Policy policy,
                    String subRequestId);

}
