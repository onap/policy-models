/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020-2021 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.controlloop.actorserviceprovider.impl;

import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.policy.controlloop.actorserviceprovider.Util;
import org.onap.policy.controlloop.actorserviceprovider.parameters.BidirectionalTopicConfig;
import org.onap.policy.controlloop.actorserviceprovider.parameters.BidirectionalTopicParams;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ParameterValidationRuntimeException;
import org.onap.policy.controlloop.actorserviceprovider.topic.BidirectionalTopicHandler;
import org.onap.policy.controlloop.actorserviceprovider.topic.BidirectionalTopicManager;
import org.onap.policy.controlloop.actorserviceprovider.topic.Forwarder;
import org.onap.policy.controlloop.actorserviceprovider.topic.SelectorKey;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
class BidirectionalTopicOperatorTest {
    private static final String ACTOR = "my-actor";
    private static final String OPERATION = "my-operation";
    private static final String MY_SOURCE = "my-source";
    private static final String MY_SINK = "my-target";
    private static final int TIMEOUT_SEC = 10;

    @Mock
    private BidirectionalTopicManager mgr;
    @Mock
    private BidirectionalTopicHandler handler;
    @Mock
    private Forwarder forwarder;
    @Mock
    private BidirectionalTopicOperation<String, Integer> operation;

    private List<SelectorKey> keys;
    private BidirectionalTopicParams params;
    private MyOperator oper;

    /**
     * Sets up.
     */
    @BeforeEach
    void setUp() {
        keys = List.of(new SelectorKey(""));

        Mockito.lenient().when(mgr.getTopicHandler(MY_SINK, MY_SOURCE)).thenReturn(handler);
        Mockito.lenient().when(handler.addForwarder(keys)).thenReturn(forwarder);

        oper = new MyOperator(keys);

        params = BidirectionalTopicParams.builder().sourceTopic(MY_SOURCE).sinkTopic(MY_SINK).timeoutSec(TIMEOUT_SEC)
                        .build();
        oper.configure(Util.translateToMap(OPERATION, params));
        oper.start();
    }

    @Test
    void testConstructor_testGetParams_testGetTopicHandler_testGetForwarder() {
        assertEquals(ACTOR, oper.getActorName());
        assertEquals(OPERATION, oper.getName());
        assertNotNull(oper.getCurrentConfig());
    }

    @Test
    void testDoConfigure() {
        oper.stop();

        // invalid parameters
        params.setSourceTopic(null);
        Map<String, Object> map = Util.translateToMap(OPERATION, params);
        assertThatThrownBy(() -> oper.configure(map))
                        .isInstanceOf(ParameterValidationRuntimeException.class);
    }

    @Test
    void testBuildOperator() {
        AtomicReference<ControlLoopOperationParams> paramsRef = new AtomicReference<>();
        AtomicReference<BidirectionalTopicConfig> configRef = new AtomicReference<>();

        // @formatter:off
        OperationMaker<BidirectionalTopicConfig, BidirectionalTopicOperation<?, ?>> maker =
            (params, config) -> {
                paramsRef.set(params);
                configRef.set(config);
                return operation;
            };
        // @formatter:on

        BidirectionalTopicOperator oper2 =
                        new BidirectionalTopicOperator(ACTOR, OPERATION, mgr, maker, new SelectorKey(""));

        assertEquals(ACTOR, oper2.getActorName());
        assertEquals(OPERATION, oper2.getName());

        ControlLoopOperationParams params2 = ControlLoopOperationParams.builder().build();

        // configure and start it
        params = BidirectionalTopicParams.builder().sourceTopic(MY_SOURCE).sinkTopic(MY_SINK).timeoutSec(TIMEOUT_SEC)
                        .build();
        oper2.configure(Util.translateToMap(OPERATION, params));

        // not running yet
        assertThatIllegalStateException().isThrownBy(() -> oper2.buildOperation(params2));

        oper2.start();

        assertSame(operation, oper2.buildOperation(params2));
        assertSame(params2, paramsRef.get());
        assertSame(oper2.getCurrentConfig(), configRef.get());

        // with no operation-maker
        BidirectionalTopicOperator oper3 =
                        new BidirectionalTopicOperator(ACTOR, OPERATION, mgr, Arrays.asList(new SelectorKey("")));
        assertThatThrownBy(() -> oper3.buildOperation(params2)).isInstanceOf(UnsupportedOperationException.class);
    }


    private class MyOperator extends BidirectionalTopicOperator {
        MyOperator(List<SelectorKey> selectorKeys) {
            super(ACTOR, OPERATION, mgr, selectorKeys);
        }

        @Override
        public BidirectionalTopicOperation<?, ?> buildOperation(ControlLoopOperationParams params) {
            return null;
        }
    }
}
