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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.onap.policy.cds.properties.CdsServerProperties;
import org.onap.policy.controlloop.VirtualControlLoopEvent;
import org.onap.policy.controlloop.actor.cds.constants.CdsActorConstants;
import org.onap.policy.controlloop.actorserviceprovider.Operation;
import org.onap.policy.controlloop.actorserviceprovider.OperationProperties;
import org.onap.policy.controlloop.actorserviceprovider.Util;
import org.onap.policy.controlloop.actorserviceprovider.controlloop.ControlLoopEventContext;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ParameterValidationRuntimeException;
import org.onap.policy.controlloop.policy.Target;
import org.onap.policy.controlloop.policy.TargetType;

public class GrpcOperatorTest {

    private GrpcOperator operation;
    private Map<String, Object> paramMap;
    private Map<String, Object> invalidParamMap;

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
    public void testGetPropertyNames() {

        Target target = new Target();

        ControlLoopOperationParams params = ControlLoopOperationParams.builder().target(target).build();

        /*
         * check VNF case
         */
        target.setType(TargetType.VNF);

        // @formatter:off
        assertThat(operation.getPropertyNames(params)).isEqualTo(
                        List.of(
                            OperationProperties.AAI_RESOURCE_VNF,
                            OperationProperties.AAI_SERVICE,
                            OperationProperties.EVENT_ADDITIONAL_PARAMS,
                            OperationProperties.OPT_CDS_GRPC_AAI_PROPERTIES));
        // @formatter:on

        /*
         * check PNF case
         */
        target.setType(TargetType.PNF);

        // @formatter:off
        assertThat(operation.getPropertyNames(params)).isEqualTo(
                        List.of(
                            OperationProperties.AAI_PNF,
                            OperationProperties.EVENT_ADDITIONAL_PARAMS,
                            OperationProperties.OPT_CDS_GRPC_AAI_PROPERTIES));
        // @formatter:on
    }

    @Test
    public void testBuildOperation() {
        VirtualControlLoopEvent event = new VirtualControlLoopEvent();
        ControlLoopEventContext context = new ControlLoopEventContext(event);
        Target target = new Target();
        target.setType(TargetType.VM);
        ControlLoopOperationParams params = ControlLoopOperationParams.builder().actor(CdsActorConstants.CDS_ACTOR)
                        .operation(GrpcOperation.NAME).context(context).target(target).build();

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
