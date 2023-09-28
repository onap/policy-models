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

package org.onap.policy.controlloop.actor.appc;

import java.util.List;
import org.onap.policy.appc.Request;
import org.onap.policy.controlloop.actorserviceprovider.OperationProperties;
import org.onap.policy.controlloop.actorserviceprovider.parameters.BidirectionalTopicConfig;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;

public class ModifyConfigOperation extends AppcOperation {
    public static final String NAME = "ModifyConfig";

    private static final List<String> PROPERTY_NAMES = List.of(OperationProperties.AAI_RESOURCE_VNF);

    /**
     * Constructs the object.
     *
     * @param params operation parameters
     * @param config configuration for this operation
     */
    public ModifyConfigOperation(ControlLoopOperationParams params, BidirectionalTopicConfig config) {
        super(params, config, PROPERTY_NAMES);
    }

    @Override
    protected Request makeRequest(int attempt) {
        return makeRequest(getRequiredProperty(OperationProperties.AAI_RESOURCE_VNF, "resource VNF"));
    }
}
