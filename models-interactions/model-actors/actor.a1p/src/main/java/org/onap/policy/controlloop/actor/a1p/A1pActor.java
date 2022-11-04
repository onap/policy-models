/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2022 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.controlloop.actor.a1p;

import org.onap.policy.controlloop.actor.sdnr.SdnrOperation;
import org.onap.policy.controlloop.actorserviceprovider.Operator;
import org.onap.policy.controlloop.actorserviceprovider.impl.BidirectionalTopicActor;
import org.onap.policy.controlloop.actorserviceprovider.impl.BidirectionalTopicOperator;
import org.onap.policy.controlloop.actorserviceprovider.parameters.BidirectionalTopicActorParams;

/**
 * A1-PMS actor extends the SDN-R Actor and observes the same protocol but over
 * different topics.
 */
public class A1pActor extends BidirectionalTopicActor<BidirectionalTopicActorParams> {
    public static final String NAME = "A1P";

    /**
     * Constructor.
     */
    public A1pActor() {
        super(NAME, BidirectionalTopicActorParams.class);

        addOperator(new BidirectionalTopicOperator(NAME, SdnrOperation.NAME, this, SdnrOperation.SELECTOR_KEYS,
            A1pOperation::new));
    }

    @Override
    public Operator getOperator(String name) {
        /*
         * All operations are managed by the same operator, regardless of the name.
         */
        return super.getOperator(SdnrOperation.NAME);
    }
}
