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
import org.onap.policy.cds.properties.CdsServerProperties;
import org.onap.policy.common.utils.coder.Coder;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.coder.StandardCoderObject;
import org.onap.policy.common.utils.time.PseudoExecutor;
import org.onap.policy.controlloop.actor.cds.GrpcConfig;
import org.onap.policy.controlloop.actor.cds.GrpcOperation;
import org.onap.policy.controlloop.actorserviceprovider.OperationProperties;
import org.onap.policy.controlloop.actorserviceprovider.TargetType;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;

class GrpcOperationPnfPropertiesTest {
    private static final Coder coder = new StandardCoder();

    @Test
     void getPropertyNames() {
        assertEquals(GrpcOperationPnfProperties.PNF_PROPERTY_NAMES,
                new GrpcOperationPnfProperties().getPropertyNames());
    }

    @Test
     void convertToAaiProperties() throws CoderException {
        ControlLoopOperationParams params =
                ControlLoopOperationParams.builder()
                        .targetType(TargetType.PNF)
                        .build();

        GrpcOperation operation =
                new GrpcOperation(params, new GrpcConfig(new PseudoExecutor(), new CdsServerProperties()));

        GrpcOperationPnfProperties properties = new GrpcOperationPnfProperties();
        assertThatIllegalStateException()
                .isThrownBy(() -> properties.convertToAaiProperties(operation));

        String pnf = "{'dataA': 'valueA', 'dataB': 'valueB'}".replace('\'', '"');
        StandardCoderObject sco = coder.decode(pnf, StandardCoderObject.class);
        operation.setProperty(OperationProperties.AAI_PNF, sco);

        assertEquals("valueA",
                properties.convertToAaiProperties(operation)
                        .get(GrpcOperationPnfProperties.AAI_PNF_PREFIX + "dataA"));

        assertEquals("valueB",
                properties.convertToAaiProperties(operation)
                        .get(GrpcOperationPnfProperties.AAI_PNF_PREFIX + "dataB"));

        operation.setProperty(OperationProperties.OPT_CDS_GRPC_AAI_PROPERTIES, Collections.emptyMap());
        assertNull(properties.convertToAaiProperties(operation)
                        .get(GrpcOperationPnfProperties.AAI_PNF_PREFIX + "dataA"));

        operation.setProperty(OperationProperties.OPT_CDS_GRPC_AAI_PROPERTIES,
                Map.of("dataC", "valueC"));
        assertEquals("valueC",
                properties.convertToAaiProperties(operation)
                        .get("dataC"));
    }
}