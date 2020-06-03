/*-
 * ============LICENSE_START=======================================================
 * SdnrActorServiceProvider
 * ================================================================================
 * Copyright (C) 2018 Wipro Limited Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019-2020 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.controlloop.actor.sdnr;

import org.onap.policy.controlloop.actorserviceprovider.Operator;
import org.onap.policy.controlloop.actorserviceprovider.impl.BidirectionalTopicActor;
import org.onap.policy.controlloop.actorserviceprovider.impl.BidirectionalTopicOperator;
import org.onap.policy.controlloop.actorserviceprovider.parameters.BidirectionalTopicActorParams;

/**
 * SDNR is an unusual actor in that it uses a single, generic operator to initiate all
 * operation types. The action taken is always the same, only the operation name changes.
 */
public class SdnrActorServiceProvider extends BidirectionalTopicActor<BidirectionalTopicActorParams>  {
    public static final String NAME = "SDNR";

    /**
     * Constructor.
     */
    public SdnrActorServiceProvider() {
        super(NAME, BidirectionalTopicActorParams.class);

        addOperator(new BidirectionalTopicOperator(NAME, SdnrOperation.NAME, this, SdnrOperation.SELECTOR_KEYS,
                        SdnrOperation::new));
    }

    @Override
    public Operator getOperator(String name) {
        /*
         * All operations are managed by the same operator, regardless of the name.
         */
        return super.getOperator(SdnrOperation.NAME);
    }
}
