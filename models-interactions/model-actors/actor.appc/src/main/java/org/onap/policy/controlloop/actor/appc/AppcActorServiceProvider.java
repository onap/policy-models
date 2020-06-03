/*-
 * ============LICENSE_START=======================================================
 * APPCActorServiceProvider
 * ================================================================================
 * Copyright (C) 2017-2020 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.controlloop.actor.appc;

import org.onap.policy.controlloop.actorserviceprovider.impl.BidirectionalTopicActor;
import org.onap.policy.controlloop.actorserviceprovider.impl.BidirectionalTopicOperator;
import org.onap.policy.controlloop.actorserviceprovider.parameters.BidirectionalTopicActorParams;


public class AppcActorServiceProvider extends BidirectionalTopicActor<BidirectionalTopicActorParams> {
    public static final String NAME = "APPC";

    /**
     * Constructs the object.
     */
    public AppcActorServiceProvider() {
        super(NAME, BidirectionalTopicActorParams.class);

        addOperator(new BidirectionalTopicOperator(NAME, ModifyConfigOperation.NAME, this, AppcOperation.SELECTOR_KEYS,
                        ModifyConfigOperation::new));
    }
}
