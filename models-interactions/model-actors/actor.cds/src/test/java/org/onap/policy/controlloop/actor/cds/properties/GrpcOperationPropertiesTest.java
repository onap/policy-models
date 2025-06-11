/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2022 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2024-2025 OpenInfra Foundation Europe. All rights reserved.
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

package org.onap.policy.controlloop.actor.cds.properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.onap.policy.controlloop.actorserviceprovider.TargetType;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;

class GrpcOperationPropertiesTest {

    @Test
     void build() {
        ControlLoopOperationParams params = ControlLoopOperationParams.builder().targetType(TargetType.VNF).build();
        assertEquals(GrpcOperationTargetVnfProperties.VNF_PROPERTY_NAMES,
                GrpcOperationProperties.build(params).getPropertyNames());

        Map<String, String> targetEntityIds = new HashMap<>();
        targetEntityIds.put(ControlLoopOperationParams.PARAMS_ENTITY_RESOURCEID, "R");

        params = ControlLoopOperationParams.builder()
                         .targetType(TargetType.VNF)
                         .targetEntityIds(targetEntityIds)
                         .build();
        assertEquals(GrcpOperationResourceVnfProperties.VNF_PROPERTY_NAMES,
                GrpcOperationProperties.build(params).getPropertyNames());

        params = ControlLoopOperationParams.builder().targetType(TargetType.PNF).build();
        assertEquals(GrpcOperationPnfProperties.PNF_PROPERTY_NAMES,
                GrpcOperationProperties.build(params).getPropertyNames());
    }
}