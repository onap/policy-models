/*
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2017-2020 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019 Nordix Foundation.
 * Modifications Copyright (C) 2020 Wipro Limited.
 * Modifications Copyright (C) 2022 CTC, Inc. and others.
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

package org.onap.policy.controlloop.actor.so;

import org.onap.policy.controlloop.actorserviceprovider.impl.HttpActor;
import org.onap.policy.controlloop.actorserviceprovider.impl.HttpPollingOperator;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpPollingActorParams;

public class SoActor extends HttpActor<HttpPollingActorParams> {
    public static final String NAME = "SO";

    /**
     * Constructs the object.
     */
    public SoActor() {
        super(NAME, HttpPollingActorParams.class);

        addOperator(new HttpPollingOperator(NAME, VfModuleCreate.NAME, VfModuleCreate::new));
        addOperator(new HttpPollingOperator(NAME, VfModuleDelete.NAME, VfModuleDelete::new));
        addOperator(new HttpPollingOperator(NAME, ModifyNssi.NAME, ModifyNssi::new));
        addOperator(new HttpPollingOperator(NAME, ModifyNssi.NAME, ModifyCll::new));
    }
}
