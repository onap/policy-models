/*-
 * ============LICENSE_START=======================================================
 * TestActorServiceProvider
 * ================================================================================
 * Copyright (C) 2018 Ericsson. All rights reserved.
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

package org.onap.policy.controlloop.actorserviceprovider;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.onap.policy.aai.AaiCqResponse;
import org.onap.policy.controlloop.ControlLoopOperation;
import org.onap.policy.controlloop.actorserviceprovider.spi.Actor;
import org.onap.policy.controlloop.actorserviceprovider.spi.ActorOperationCallback;

public class DummyActor implements Actor {

    private final List<String> operations = Arrays.asList("Dorothy", "Wizard");


    @Override
    public String actor() {
        return this.getClass().getSimpleName();
    }

    @Override
    public List<String> operations() {
        return operations;
    }

    @Override
    public ControlLoopOperation startOperation(String operation, AaiCqResponse aaiCqResponse,
            Map<String, Object> payload, ActorOperationCallback callback) {
        if (! this.operations.contains(operation)) {
            throw new UnsupportedOperationException();
        }
        ControlLoopOperation clOperation = new ControlLoopOperation();
        clOperation.setOperation(operation);
        return clOperation;
    }

    @Override
    public void cancelOperation(ControlLoopOperation operation) {

    }
}
