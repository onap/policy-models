/*-
 * ============LICENSE_START=======================================================
 * Actor
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019 Nordix Foundation.
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

import java.util.Collection;
import java.util.Map;
import org.onap.policy.aai.AaiCqResponse;
import org.onap.policy.controlloop.ControlLoopOperation;

/**
 * This is the service interface for defining an Actor used in Control Loop Operational Policies for performing
 * actions on runtime entities.
 *
 * @author pameladragosh
 *
 */
public interface Actor {

    /**
     * Returns the unique Actor string. This must match what is defined in an operational policy.
     *
     * @return String representation of the Actor
     */
    String actor();

    /**
     * Returns a collection of Operations this actor supports.
     *
     * @return collection of strings for each operation supported.
     */
    Collection<String> operations();

    /**
     * Called by enforcement PDP engine to start the specified operation.
     *
     * @param operation - Operation to start
     * @param aaiCqResponse - A&AI Custom Query results
     * @param payload - Payload passed from the operation policy
     * @param targetEntity - The A&AI "node.attribute" targeted entity to perform the operation on.
     * @param callback - The callback method to be called when operation is finished.
     * @return ControlLoopOperation object
     */
    ControlLoopOperation startOperation(String operation, AaiCqResponse aaiCqResponse, Map<String, Object> payload,
            String targetEntity, ActorOperationCallback callback);

    /**
     * Called by enforcement PDP engine to cancel an operation that has been started.
     *
     * @param operation The ControlLoopOperation object specifying the operation to cancel
     */
    void cancelOperation(ControlLoopOperation operation);
}
