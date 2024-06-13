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

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onap.policy.common.parameters.ObjectValidationResult;
import org.onap.policy.common.parameters.ValidationStatus;
import org.onap.policy.controlloop.actorserviceprovider.Operation;
import org.onap.policy.controlloop.actorserviceprovider.Operator;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ActorParams;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ParameterValidationRuntimeException;

class ActorImplTest {
    private static final String EXPECTED_EXCEPTION = "expected exception";
    private static final String ACTOR_NAME = "my-actor";
    private static final String OPER1 = "add";
    private static final String OPER2 = "subtract";
    private static final String OPER3 = "multiply";
    private static final String OPER4 = "divide";

    private MyOper oper1;
    private MyOper oper2;
    private MyOper oper3;
    private MyOper oper4;

    private Map<String, Object> sub1;
    private Map<String, Object> sub2;
    private Map<String, Object> sub3;
    private Map<String, Object> sub4;
    private Map<String, Object> params;

    private ActorImpl actor;


    /**
     * Initializes the fields, including a fully populated {@link #actor}.
     */
    @BeforeEach
   void setUp() {
        oper1 = spy(new MyOper(OPER1));
        oper2 = spy(new MyOper(OPER2));
        oper3 = spy(new MyOper(OPER3));
        oper4 = spy(new MyOper(OPER4));

        sub1 = Map.of("sub A", "value A");
        sub2 = Map.of("sub B", "value B");
        sub3 = Map.of("sub C", "value C");
        sub4 = Map.of("sub D", "value D");

        params = Map.of(ActorParams.OPERATIONS_FIELD, Map.of(OPER1, sub1, OPER2, sub2, OPER3, sub3, OPER4, sub4));

        actor = makeActor(oper1, oper2, oper3, oper4);
    }

    @Test
   void testActorImpl_testGetName() {
        assertEquals(ACTOR_NAME, actor.getName());
        assertEquals(4, actor.getOperationNames().size());
        assertEquals(0, actor.getSequenceNumber());
    }

    @Test
   void testDoStart() {
        actor.configure(params);
        assertEquals(4, actor.getOperationNames().size());

        /*
         * arrange for second operator to be unconfigured and the third operator to throw
         * an exception
         */
        Iterator<Operator> iter = actor.getOperators().iterator();
        iter.next();
        when(iter.next().isConfigured()).thenReturn(false);
        when(iter.next().start()).thenThrow(new IllegalStateException(EXPECTED_EXCEPTION));

        /*
         * Start the actor.
         */
        actor.start();
        assertTrue(actor.isAlive());

        iter = actor.getOperators().iterator();
        verify(iter.next()).start();
        // this one isn't configured, so shouldn't attempt to start it
        verify(iter.next(), never()).start();
        // this one threw an exception
        iter.next();
        verify(iter.next()).start();

        // no other types of operations
        verify(oper1, never()).stop();
        verify(oper1, never()).shutdown();
    }

    @Test
   void testDoStop() {
        actor.configure(params);
        actor.start();
        assertEquals(4, actor.getOperationNames().size());

        // arrange for second operator to throw an exception
        Iterator<Operator> iter = actor.getOperators().iterator();
        iter.next();
        when(iter.next().stop()).thenThrow(new IllegalStateException(EXPECTED_EXCEPTION));

        /*
         * Stop the actor.
         */
        actor.stop();
        assertFalse(actor.isAlive());

        iter = actor.getOperators().iterator();
        verify(iter.next()).stop();
        // this one threw an exception
        iter.next();
        verify(iter.next()).stop();
        verify(iter.next()).stop();

        // no additional types of operations
        verify(oper1).configure(any());
        verify(oper1).start();

        // no other types of operation
        verify(oper1, never()).shutdown();
    }

    @Test
   void testDoShutdown() {
        actor.configure(params);
        actor.start();
        assertEquals(4, actor.getOperationNames().size());

        // arrange for second operator to throw an exception
        Iterator<Operator> iter = actor.getOperators().iterator();
        iter.next();
        doThrow(new IllegalStateException(EXPECTED_EXCEPTION)).when(iter.next()).shutdown();

        /*
         * Stop the actor.
         */
        actor.shutdown();
        assertFalse(actor.isAlive());

        iter = actor.getOperators().iterator();
        verify(iter.next()).shutdown();
        // this one threw an exception
        iter.next();
        verify(iter.next()).shutdown();
        verify(iter.next()).shutdown();

        // no additional types of operations
        verify(oper1).configure(any());
        verify(oper1).start();

        // no other types of operation
        verify(oper1, never()).stop();
    }

    @Test
    void testAddOperator() {
        // cannot add operators if already configured
        actor.configure(params);
        assertThatIllegalStateException().isThrownBy(() -> actor.addOperator(oper1));

        /*
         * make an actor where operators two and four have names that are duplicates of
         * the others
         */
        oper2 = spy(new MyOper(OPER1));
        oper4 = spy(new MyOper(OPER3));

        actor = makeActor(oper1, oper2, oper3, oper4);

        assertEquals(2, actor.getOperationNames().size());

        assertSame(oper1, actor.getOperator(OPER1));
        assertSame(oper3, actor.getOperator(OPER3));
    }

    @Test
    void testGetOperator() {
        assertSame(oper1, actor.getOperator(OPER1));
        assertSame(oper3, actor.getOperator(OPER3));

        assertThatIllegalArgumentException().isThrownBy(() -> actor.getOperator("unknown name"));
    }

    @Test
    void testGetOperators() {
        // @formatter:off
        assertEquals("[add, divide, multiply, subtract]",
                        actor.getOperators().stream()
                            .map(Operator::getName)
                            .sorted()
                            .collect(Collectors.toList())
                            .toString());
        // @formatter:on
    }

    @Test
    void testGetOperationNames() {
        // @formatter:off
        assertEquals("[add, divide, multiply, subtract]",
                        actor.getOperationNames().stream()
                            .sorted()
                            .collect(Collectors.toList())
                            .toString());
        // @formatter:on
    }

    @Test
    void testDoConfigure() {
        actor.configure(params);
        assertTrue(actor.isConfigured());

        verify(oper1).configure(sub1);
        verify(oper2).configure(sub2);
        verify(oper3).configure(sub3);
        verify(oper4).configure(sub4);

        // no other types of operations
        verify(oper1, never()).start();
        verify(oper1, never()).stop();
        verify(oper1, never()).shutdown();
    }

    /**
     * Tests doConfigure() where operators throw parameter validation and runtime
     * exceptions.
     */
    @Test
    void testDoConfigureExceptions() {
        makeValidException(oper1);
        makeRuntimeException(oper2);
        makeValidException(oper3);

        actor.configure(params);
        assertTrue(actor.isConfigured());
    }

    /**
     * Tests doConfigure(). Arranges for the following:
     * <ul>
     * <li>one operator is configured, but has parameters</li>
     * <li>another operator is configured, but has no parameters</li>
     * <li>another operator has no parameters and is not configured</li>
     * </ul>
     */
    @Test
    void testDoConfigureConfigure() {
        // configure one operator
        oper1.configure(sub1);

        // configure another and remove its parameters
        oper2.configure(sub2);
        params = Map.of(ActorParams.OPERATIONS_FIELD, Map.of(OPER1, sub1, OPER3, sub3, OPER4, sub4));

        // create a new, unconfigured actor
        Operator oper5 = spy(new MyOper("UNCONFIGURED"));
        actor = makeActor(oper1, oper2, oper3, oper4, oper5);

        /*
         * Configure it.
         */
        actor.configure(params);
        assertTrue(actor.isConfigured());

        // this should have been configured again
        verify(oper1, times(2)).configure(sub1);

        // no parameters, so this should not have been configured again
        verify(oper2).configure(sub2);

        // these were only configured once
        verify(oper3).configure(sub3);
        verify(oper4).configure(sub4);

        // never configured
        verify(oper5, never()).configure(any());
        assertFalse(oper5.isConfigured());

        // start and verify that all are started except for the last
        actor.start();
        verify(oper1).start();
        verify(oper2).start();
        verify(oper3).start();
        verify(oper4).start();
        verify(oper5, never()).start();
    }

    /**
     * Arranges for an operator to throw a validation exception when
     * {@link Operator#configure(Map)} is invoked.
     *
     * @param oper operator of interest
     */
    private void makeValidException(Operator oper) {
        ParameterValidationRuntimeException ex = new ParameterValidationRuntimeException(
                        new ObjectValidationResult(actor.getName(), null, ValidationStatus.INVALID, "null"));
        doThrow(ex).when(oper).configure(any());
    }

    /**
     * Arranges for an operator to throw a runtime exception when
     * {@link Operator#configure(Map)} is invoked.
     *
     * @param oper operator of interest
     */
    private void makeRuntimeException(Operator oper) {
        IllegalStateException ex = new IllegalStateException(EXPECTED_EXCEPTION);
        doThrow(ex).when(oper).configure(any());
    }

    @Test
    void testMakeOperatorParameters() {
        actor.configure(params);

        // each operator should have received its own parameters
        verify(oper1).configure(sub1);
        verify(oper2).configure(sub2);
        verify(oper3).configure(sub3);
        verify(oper4).configure(sub4);
    }

    /**
     * Makes an actor with the given operators.
     *
     * @param operators associated operators
     * @return a new actor
     */
    private ActorImpl makeActor(Operator... operators) {
        ActorImpl actor = new ActorImpl(ACTOR_NAME);

        for (Operator oper : operators) {
            actor.addOperator(oper);
        }

        return actor;
    }

    private static class MyOper extends OperatorPartial {

        public MyOper(String name) {
            super(ACTOR_NAME, name);
        }

        @Override
        public Operation buildOperation(ControlLoopOperationParams params) {
            return null;
        }
    }
}
