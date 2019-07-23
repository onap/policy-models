/*-
 * ============LICENSE_START=======================================================
 * Actor
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

import org.onap.policy.controlloop.ControlLoopOperation;

/**
 * This callback is used by enforcement PDP engines (eg Drools, Apex, etc)
 * for getting notification of status of an operation.
 *
 * @author pameladragosh
 *
 */
public interface ActorOperationCallback {

    /**
     * Called by actor service provider when an operation starts.
     *
     * @param operation The ControlLoopOperation object being started
     */
    void    operationStarted(ControlLoopOperation operation);

    /**
     * Called by actor service provider when an operation is finished.
     *
     * @param operation The ControlLoopOperation object that is finished.
     */
    void    operationFinished(ControlLoopOperation operation);


}
