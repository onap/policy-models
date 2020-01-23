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

package org.onap.policy.controlloop.actorserviceprovider.impl;

import java.util.Map;
import lombok.Getter;
import org.onap.policy.controlloop.actorserviceprovider.OperationManager;

/**
 * Partial implementation of an operation manager.
 */
public abstract class OperationManagerPartial extends ConfigImpl<Map<String, Object>> implements OperationManager {

    @Getter
    private final String actorName;

    @Getter
    private final String name;

    /**
     * Constructs the object.
     *
     * @param actorName name of the actor with which this manager is associated
     * @param name operation name
     */
    public OperationManagerPartial(String actorName, String name) {
        super(actorName + "." + name);
        this.actorName = actorName;
        this.name = name;
    }

    /**
     * This method does nothing.
     */
    @Override
    protected void doConfigure(Map<String, Object> parameters) {
        // do nothing
    }

    @Override
    protected void doStart() {
        // do nothing
    }

    @Override
    protected void doStop() {
        // do nothing
    }

    @Override
    protected void doShutdown() {
        // do nothing
    }
}
