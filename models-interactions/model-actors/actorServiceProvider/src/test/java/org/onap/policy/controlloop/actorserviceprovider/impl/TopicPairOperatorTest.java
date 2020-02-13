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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.policy.controlloop.actorserviceprovider.Operation;
import org.onap.policy.controlloop.actorserviceprovider.Util;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ParameterValidationRuntimeException;
import org.onap.policy.controlloop.actorserviceprovider.parameters.TopicPairParams;
import org.onap.policy.controlloop.actorserviceprovider.topic.Forwarder;
import org.onap.policy.controlloop.actorserviceprovider.topic.SelectorKey;
import org.onap.policy.controlloop.actorserviceprovider.topic.TopicPair;
import org.onap.policy.controlloop.actorserviceprovider.topic.TopicPairManager;

public class TopicPairOperatorTest {
    private static final String ACTOR = "my-actor";
    private static final String OPERATION = "my-operation";
    private static final String MY_SOURCE = "my-source";
    private static final String MY_TARGET = "my-target";
    private static final int TIMEOUT_SEC = 10;

    @Mock
    private TopicPairManager mgr;
    @Mock
    private TopicPair pair;
    @Mock
    private Forwarder forwarder;
    @Mock
    private TopicPairOperation<String, Integer> operation;

    private List<SelectorKey> keys;
    private TopicPairParams params;
    private MyOperator oper;

    /**
     * Sets up.
     */
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        keys = List.of(new SelectorKey(""));

        when(mgr.getTopicPair(MY_SOURCE, MY_TARGET)).thenReturn(pair);
        when(pair.addForwarder(keys)).thenReturn(forwarder);

        oper = new MyOperator(keys);

        params = TopicPairParams.builder().source(MY_SOURCE).target(MY_TARGET).timeoutSec(TIMEOUT_SEC).build();
        oper.configure(Util.translateToMap(OPERATION, params));
        oper.start();
    }

    @Test
    public void testTopicPairOperator_testGetParams_testGetTopicPair_testGetForwarder() {
        assertEquals(ACTOR, oper.getActorName());
        assertEquals(OPERATION, oper.getName());
        assertEquals(params, oper.getParams());
        assertSame(pair, oper.getTopicPair());
        assertSame(forwarder, oper.getForwarder());
    }

    @Test
    public void testDoConfigure() {
        oper.stop();

        // invalid parameters
        params.setSource(null);
        assertThatThrownBy(() -> oper.configure(Util.translateToMap(OPERATION, params)))
                        .isInstanceOf(ParameterValidationRuntimeException.class);
    }

    @Test
    public void testMakeOperator() {
        AtomicReference<ControlLoopOperationParams> paramsRef = new AtomicReference<>();
        AtomicReference<TopicPairOperator> operRef = new AtomicReference<>();

        // @formatter:off
        BiFunction<ControlLoopOperationParams, TopicPairOperator, TopicPairOperation<String, Integer>> maker =
            (params, operator) -> {
                paramsRef.set(params);
                operRef.set(operator);
                return operation;
            };
        // @formatter:on

        TopicPairOperator oper2 = TopicPairOperator.makeOperator(ACTOR, OPERATION, mgr, maker, new SelectorKey(""));

        assertEquals(ACTOR, oper2.getActorName());
        assertEquals(OPERATION, oper2.getName());

        ControlLoopOperationParams params2 = ControlLoopOperationParams.builder().build();

        assertSame(operation, oper2.buildOperation(params2));
        assertSame(params2, paramsRef.get());
        assertSame(oper2, operRef.get());
    }


    private class MyOperator extends TopicPairOperator {
        public MyOperator(List<SelectorKey> selectorKeys) {
            super(ACTOR, OPERATION, mgr, selectorKeys);
        }

        @Override
        public Operation buildOperation(ControlLoopOperationParams params) {
            return null;
        }
    }
}
