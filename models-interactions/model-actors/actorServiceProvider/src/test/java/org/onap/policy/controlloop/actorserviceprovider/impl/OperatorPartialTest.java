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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.Map;
import java.util.TreeMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onap.policy.controlloop.actorserviceprovider.Operation;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;

class OperatorPartialTest {
    private static final String ACTOR = "my-actor";
    private static final String OPERATION = "my-name";

    private OperatorPartial operator;

    /**
     * Initializes {@link #operator}.
     */
    @BeforeEach
    void setUp() {
        operator = new OperatorPartial(ACTOR, OPERATION) {
            @Override
            public Operation buildOperation(ControlLoopOperationParams params) {
                return null;
            }
        };
    }

    @Test
    void testOperatorPartial_testGetActorName_testGetName() {
        assertEquals(ACTOR, operator.getActorName());
        assertEquals(OPERATION, operator.getName());
        assertEquals(ACTOR + "." + OPERATION, operator.getFullName());
    }

    @Test
    void testDoStart() {
        operator.configure(null);

        operator = spy(operator);
        operator.start();

        verify(operator).doStart();
    }

    @Test
    void testDoStop() {
        operator.configure(null);
        operator.start();

        operator = spy(operator);
        operator.stop();

        verify(operator).doStop();
    }

    @Test
    void testDoShutdown() {
        operator.configure(null);
        operator.start();

        operator = spy(operator);
        operator.shutdown();

        verify(operator).doShutdown();
    }

    @Test
    void testDoConfigureMapOfStringObject() {
        operator = spy(operator);

        Map<String, Object> params = new TreeMap<>();
        operator.configure(params);

        verify(operator).doConfigure(params);
    }

    @Test
    void testGetBlockingExecutor() {
        assertNotNull(operator.getBlockingExecutor());
    }
}
