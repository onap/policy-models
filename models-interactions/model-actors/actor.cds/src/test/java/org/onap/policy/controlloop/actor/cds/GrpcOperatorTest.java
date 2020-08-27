/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2020 Bell Canada. All rights reserved.
 * Modifications Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.controlloop.actor.cds;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;

import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.onap.policy.cds.properties.CdsServerProperties;
import org.onap.policy.controlloop.VirtualControlLoopEvent;
import org.onap.policy.controlloop.actor.cds.constants.CdsActorConstants;
import org.onap.policy.controlloop.actorserviceprovider.Operation;
import org.onap.policy.controlloop.actorserviceprovider.TargetType;
import org.onap.policy.controlloop.actorserviceprovider.Util;
import org.onap.policy.controlloop.actorserviceprovider.controlloop.ControlLoopEventContext;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ParameterValidationRuntimeException;

public class GrpcOperatorTest {

    GrpcOperator operation;
    Map<String, Object> paramMap;
    Map<String, Object> invalidParamMap;

    /**
     * Initializes fields, including {@link #operation}.
     */
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        operation = new GrpcOperator(CdsActorConstants.CDS_ACTOR, GrpcOperation.NAME, GrpcOperation::new);

        CdsServerProperties props = new CdsServerProperties();
        props.setHost("grpcHost");
        props.setPort(1234);
        props.setUsername("grpcUsername");
        props.setPassword("grpcPassword");
        props.setTimeout(30);

        paramMap = Util.translateToMap(GrpcOperation.NAME, props);
        props.setHost(null);
        invalidParamMap = Util.translateToMap(GrpcOperation.NAME, props);
    }

    @Test
    public void testGrpcOperator() {
        assertEquals(CdsActorConstants.CDS_ACTOR, operation.getActorName());
        assertEquals(GrpcOperation.NAME, operation.getName());
        assertEquals(CdsActorConstants.CDS_ACTOR + "." + GrpcOperation.NAME, operation.getFullName());
    }


    @Test
    public void testDoConfigure() {

        operation.doConfigure(paramMap);
        assertEquals(30000, operation.getCurrentConfig().getTimeoutMs());

        // use invalidParamsMap
        assertThatExceptionOfType(ParameterValidationRuntimeException.class)
                        .isThrownBy(() -> operation.makeConfiguration(invalidParamMap));
    }

    @Test
    public void testBuildOperation() {
        VirtualControlLoopEvent event = new VirtualControlLoopEvent();
        ControlLoopEventContext context = new ControlLoopEventContext(event);
        ControlLoopOperationParams params = ControlLoopOperationParams.builder().actor(CdsActorConstants.CDS_ACTOR)
                        .operation(GrpcOperation.NAME).context(context).targetType(TargetType.VM).build();

        // not configured yet
        assertThatIllegalStateException().isThrownBy(() -> operation.buildOperation(params));

        operation.configure(paramMap);

        // not running yet
        assertThatIllegalStateException().isThrownBy(() -> operation.buildOperation(params));

        operation.start();
        Operation operation1 = operation.buildOperation(params);
        assertEquals(GrpcOperation.NAME, operation1.getName());

        // with no operation-maker
        GrpcOperator oper2 = new GrpcOperator(CdsActorConstants.CDS_ACTOR, GrpcOperation.NAME);
        assertThatThrownBy(() -> oper2.buildOperation(params)).isInstanceOf(UnsupportedOperationException.class);
    }
}
