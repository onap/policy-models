/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2017-2020 AT&T Intellectual Property. All rights reserved.
 * Modifications copyright (c) 2018 Nokia
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

package org.onap.policy.controlloop.actor.appclcm;

import org.onap.policy.controlloop.actor.appc.AppcOperation;
import org.onap.policy.controlloop.actor.appc.ModifyConfigOperation;
import org.onap.policy.controlloop.actorserviceprovider.impl.BidirectionalTopicActor;
import org.onap.policy.controlloop.actorserviceprovider.impl.BidirectionalTopicOperator;
import org.onap.policy.controlloop.actorserviceprovider.parameters.BidirectionalTopicActorParams;

public class AppcLcmActorServiceProvider extends BidirectionalTopicActor<BidirectionalTopicActorParams> {

    /*
     * Confirmed by Daniel, should be 'APPC'.
     * The actor name defined in the yaml for both legacy operations and lcm operations is still “APPC”. Perhaps in a
     * future review it would be better to distinguish them as two separate actors in the yaml but it should be okay for
     * now.
     */
    public static final String NAME = "APPC";

    /**
     * Constructs the object.
     */
    public AppcLcmActorServiceProvider() {
        super(NAME, BidirectionalTopicActorParams.class);

        // add LCM operations first as they take precedence
        for (String opname : AppcLcmConstants.OPERATION_NAMES) {
            addOperator(new BidirectionalTopicOperator(NAME, opname, this, AppcLcmOperation.SELECTOR_KEYS,
                            AppcLcmOperation::new));
        }

        // add legacy operations
        addOperator(new BidirectionalTopicOperator(NAME, ModifyConfigOperation.NAME, this, AppcOperation.SELECTOR_KEYS,
                        ModifyConfigOperation::new));
    }

    /**
     * This actor should take precedence.
     */
    @Override
    public int getSequenceNumber() {
        return -1;
    }
}
