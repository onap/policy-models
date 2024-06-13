/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2022 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2024 Nordix Foundation
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

import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.aai.domain.yang.ServiceInstance;
import org.onap.policy.cds.properties.CdsServerProperties;
import org.onap.policy.common.utils.time.PseudoExecutor;
import org.onap.policy.controlloop.actor.cds.GrpcConfig;
import org.onap.policy.controlloop.actor.cds.GrpcOperation;
import org.onap.policy.controlloop.actorserviceprovider.OperationProperties;
import org.onap.policy.controlloop.actorserviceprovider.TargetType;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;

class GrcpOperationResourceVnfPropertiesTest {

    @Test
     void getPropertyNames() {
        assertEquals(GrcpOperationResourceVnfProperties.VNF_PROPERTY_NAMES,
                new GrcpOperationResourceVnfProperties().getPropertyNames());
    }

    @Test
     void convertToAaiProperties() {
        ControlLoopOperationParams params =
                ControlLoopOperationParams.builder()
                        .targetType(TargetType.VNF)
                        .build();

        GrpcOperation operation =
                new GrpcOperation(params, new GrpcConfig(new PseudoExecutor(), new CdsServerProperties()));

        GrcpOperationResourceVnfProperties properties = new GrcpOperationResourceVnfProperties();
        assertThatIllegalStateException()
                .isThrownBy(() -> properties.convertToAaiProperties(operation));

        GenericVnf genvnf = new GenericVnf();
        genvnf.setVnfId("v");
        operation.setProperty(OperationProperties.AAI_RESOURCE_VNF, genvnf);

        ServiceInstance serviceInstance = new ServiceInstance();
        serviceInstance.setServiceInstanceId("s");
        operation.setProperty(OperationProperties.AAI_SERVICE, serviceInstance);

        assertEquals("s",
                properties.convertToAaiProperties(operation)
                        .get(GrcpOperationResourceVnfProperties.AAI_SERVICE_INSTANCE_ID_KEY));

        assertEquals("v",
                properties.convertToAaiProperties(operation)
                        .get(GrcpOperationResourceVnfProperties.AAI_VNF_ID_KEY));

        operation.setProperty(OperationProperties.OPT_CDS_GRPC_AAI_PROPERTIES, Collections.emptyMap());

        assertNull(properties.convertToAaiProperties(operation)
                           .get(GrcpOperationResourceVnfProperties.AAI_SERVICE_INSTANCE_ID_KEY));

        assertNull(properties.convertToAaiProperties(operation)
                           .get(GrcpOperationResourceVnfProperties.AAI_VNF_ID_KEY));

        operation.setProperty(OperationProperties.OPT_CDS_GRPC_AAI_PROPERTIES,
                Map.of("aa", "AA"));
        assertEquals("AA",
                properties.convertToAaiProperties(operation)
                        .get("aa"));
    }
}