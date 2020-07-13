/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2017-2018 Intel Corp. All rights reserved.
 * Modifications Copyright (C) 2018-2020 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.controlloop.actor.vfc;

import org.onap.policy.controlloop.actorserviceprovider.impl.HttpActor;
import org.onap.policy.controlloop.actorserviceprovider.impl.HttpPollingOperator;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpPollingActorParams;

public class VfcActor extends HttpActor<HttpPollingActorParams> {
    public static final String NAME = "VFC";

    /**
     * Constructor.
     */
    public VfcActor() {
        super(NAME, HttpPollingActorParams.class);

        addOperator(new HttpPollingOperator(NAME, Restart.NAME, Restart::new));
    }
}
