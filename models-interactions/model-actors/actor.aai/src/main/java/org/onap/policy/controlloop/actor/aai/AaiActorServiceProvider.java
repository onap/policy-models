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

package org.onap.policy.controlloop.actor.aai;

import org.onap.policy.aai.AaiConstants;
import org.onap.policy.controlloop.actorserviceprovider.impl.HttpActor;
import org.onap.policy.controlloop.actorserviceprovider.impl.HttpOperator;

/**
 * A&AI Actor.
 */
public class AaiActorServiceProvider extends HttpActor {
    public static final String NAME = AaiConstants.ACTOR_NAME;

    /**
     * Constructs the object.
     */
    public AaiActorServiceProvider() {
        super(NAME);

        addOperator(HttpOperator.makeOperator(NAME, AaiCustomQueryOperation.NAME,
                        AaiCustomQueryOperation::new));

        // add all "get" operators
        for (String operation : AaiGetOperation.OPERATIONS) {
            addOperator(HttpOperator.makeOperator(NAME, operation, AaiGetOperation::new));
        }
    }
}
