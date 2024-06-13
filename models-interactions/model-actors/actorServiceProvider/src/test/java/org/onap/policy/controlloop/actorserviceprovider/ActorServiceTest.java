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

package org.onap.policy.controlloop.actorserviceprovider;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onap.policy.common.parameters.ObjectValidationResult;
import org.onap.policy.common.parameters.ValidationStatus;
import org.onap.policy.controlloop.actorserviceprovider.impl.ActorImpl;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ActorParams;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ParameterValidationRuntimeException;
import org.onap.policy.controlloop.actorserviceprovider.spi.Actor;

public class ActorServiceTest {
    private static final String EXPECTED_EXCEPTION = "expected exception";
    private static final String ACTOR1 = "actor A";
    private static final String ACTOR2 = "actor B";
    private static final String ACTOR3 = "actor C";
    private static final String ACTOR4 = "actor D";

    private Actor actor1;
    private Actor actor2;
    private Actor actor3;
    private Actor actor4;

    private Map<String, Object> sub1;
    private Map<String, Object> sub2;
    private Map<String, Object> sub3;
    private Map<String, Object> sub4;
    private Map<String, Object> params;

    private ActorService service;


    /**
     * Initializes the fields, including a fully populated {@link #service}.
     */
    @BeforeEach
    public void setUp() {
        actor1 = spy(new ActorImpl(ACTOR1));
        actor2 = spy(new ActorImpl(ACTOR2));
        actor3 = spy(new ActorImpl(ACTOR3));
        actor4 = spy(new ActorImpl(ACTOR4));

        sub1 = Map.of("sub A", "value A", ActorParams.OPERATIONS_FIELD, Collections.emptyMap());
        sub2 = Map.of("sub B", "value B", ActorParams.OPERATIONS_FIELD, Collections.emptyMap());
        sub3 = Map.of("sub C", "value C", ActorParams.OPERATIONS_FIELD, Collections.emptyMap());
        sub4 = Map.of("sub D", "value D", ActorParams.OPERATIONS_FIELD, Collections.emptyMap());

        params = Map.of(ACTOR1, sub1, ACTOR2, sub2, ACTOR3, sub3, ACTOR4, sub4);

        service = makeService(actor1, actor2, actor3, actor4);
    }

    @Test
    public void testActorService_testBuildList() {
        /*
         * make a service where actors two and four have names that are duplicates of the
         * others
         */

        /*
         * actor0 has a higher sequence number than actor1, so it should be discarded,
         * even though it will be examined first
         */
        Actor actor0 = spy(new ActorImpl(ACTOR1) {
            @Override
            public int getSequenceNumber() {
                return 10000;
            }
        });

        actor2 = spy(new ActorImpl(ACTOR1));
        actor4 = spy(new ActorImpl(ACTOR3));

        service = makeService(actor0, actor1, actor2, actor3, actor4);

        assertEquals(2, service.getActorNames().size());

        assertSame(actor1, service.getActor(ACTOR1));
        assertSame(actor3, service.getActor(ACTOR3));
    }

    @Test
    public void testDoStart() {
        service.configure(params);

        setUpOp("testDoStart", actor -> when(actor.isConfigured()).thenReturn(false), Actor::start);

        /*
         * Start the service.
         */
        service.start();
        assertTrue(service.isAlive());

        Iterator<Actor> iter = service.getActors().iterator();
        verify(iter.next()).start();
        verify(iter.next(), never()).start();
        verify(iter.next()).start();
        verify(iter.next()).start();

        // no additional types of operations
        verify(actor1).configure(any());

        // no other types of operations
        verify(actor1, never()).stop();
        verify(actor1, never()).shutdown();
    }

    @Test
    public void testDoStop() {
        service.configure(params);
        service.start();

        setUpOp("testDoStop", Actor::stop, Actor::stop);

        /*
         * Stop the service.
         */
        service.stop();
        assertFalse(service.isAlive());

        Iterator<Actor> iter = service.getActors().iterator();
        verify(iter.next()).stop();
        verify(iter.next(), times(2)).stop();
        verify(iter.next()).stop();
        verify(iter.next()).stop();

        // no additional types of operations
        verify(actor1).configure(any());
        verify(actor1).start();

        // no other types of operation
        verify(actor1, never()).shutdown();
    }

    @Test
    public void testDoShutdown() {
        service.configure(params);
        service.start();

        setUpOp("testDoShutdown", Actor::shutdown, Actor::shutdown);

        /*
         * Shut down the service.
         */
        service.shutdown();
        assertFalse(service.isAlive());

        Iterator<Actor> iter = service.getActors().iterator();
        verify(iter.next()).shutdown();
        verify(iter.next(), times(2)).shutdown();
        verify(iter.next()).shutdown();
        verify(iter.next()).shutdown();

        // no additional types of operations
        verify(actor1).configure(any());
        verify(actor1).start();

        // no other types of operation
        verify(actor1, never()).stop();
    }

    /**
     * Applies an operation to the second actor, and then arranges for the third actor to
     * throw an exception when its operation is performed.
     *
     * @param testName test name
     * @param oper2 operation to apply to the second actor
     * @param oper3 operation to apply to the third actor
     */
    private void setUpOp(String testName, Consumer<Actor> oper2, Consumer<Actor> oper3) {
        Collection<Actor> actors = service.getActors();
        assertEquals(4, actors.size(), testName);

        Iterator<Actor> iter = actors.iterator();

        // leave the first alone
        iter.next();

        // apply oper2 to the second actor
        oper2.accept(iter.next());

        // throw an exception in the third
        oper3.accept(doThrow(new IllegalStateException(EXPECTED_EXCEPTION)).when(iter.next()));

        // leave the fourth alone
        iter.next();
    }

    @Test
    public void testGetActor() {
        assertSame(actor1, service.getActor(ACTOR1));
        assertSame(actor3, service.getActor(ACTOR3));

        assertThatIllegalArgumentException().isThrownBy(() -> service.getActor("unknown actor"));
    }

    @Test
    public void testGetActors() {
        // @formatter:off
        assertEquals("[actor A, actor B, actor C, actor D]",
                        service.getActors().stream()
                            .map(Actor::getName)
                            .sorted()
                            .collect(Collectors.toList())
                            .toString());
        // @formatter:on
    }

    @Test
    public void testGetActorNames() {
        // @formatter:off
        assertEquals("[actor A, actor B, actor C, actor D]",
                        service.getActorNames().stream()
                            .sorted()
                            .collect(Collectors.toList())
                            .toString());
        // @formatter:on
    }

    @Test
    public void testDoConfigure() {
        service.configure(params);
        assertTrue(service.isConfigured());

        verify(actor1).configure(sub1);
        verify(actor2).configure(sub2);
        verify(actor3).configure(sub3);
        verify(actor4).configure(sub4);

        // no other types of operations
        verify(actor1, never()).start();
        verify(actor1, never()).stop();
        verify(actor1, never()).shutdown();
    }

    /**
     * Tests doConfigure() where actors throw parameter validation and runtime exceptions.
     */
    @Test
    public void testDoConfigureExceptions() {
        makeValidException(actor1);
        makeRuntimeException(actor2);
        makeValidException(actor3);

        service.configure(params);
        assertTrue(service.isConfigured());
    }

    /**
     * Tests doConfigure(). Arranges for the following:
     * <ul>
     * <li>one actor is configured, but has parameters</li>
     * <li>another actor is configured, but has no parameters</li>
     * <li>another actor has no parameters and is not configured</li>
     * </ul>
     */
    @Test
    public void testDoConfigureConfigure() {
        // need mutable parameters
        params = new TreeMap<>(params);

        // configure one actor
        actor1.configure(sub1);

        // configure another and remove its parameters
        actor2.configure(sub2);
        params.remove(ACTOR2);

        // create a new, unconfigured actor
        ActorImpl actor5 = spy(new ActorImpl("UNCONFIGURED"));
        service = makeService(actor1, actor2, actor3, actor4, actor5);

        /*
         * Configure it.
         */
        service.configure(params);
        assertTrue(service.isConfigured());

        // this should have been configured again
        verify(actor1, times(2)).configure(sub1);

        // no parameters, so this should not have been configured again
        verify(actor2).configure(sub2);

        // these were only configured once
        verify(actor3).configure(sub3);
        verify(actor4).configure(sub4);

        // never configured
        verify(actor5, never()).configure(any());
        assertFalse(actor5.isConfigured());

        // start and verify that all are started except for the last
        service.start();
        verify(actor1).start();
        verify(actor2).start();
        verify(actor3).start();
        verify(actor4).start();
        verify(actor5, never()).start();
    }

    /**
     * Arranges for an actor to throw a validation exception when
     * {@link Actor#configure(Map)} is invoked.
     *
     * @param actor actor of interest
     */
    private void makeValidException(Actor actor) {
        ParameterValidationRuntimeException ex = new ParameterValidationRuntimeException(
                        new ObjectValidationResult(actor.getName(), null, ValidationStatus.INVALID, "null"));
        doThrow(ex).when(actor).configure(any());
    }

    /**
     * Arranges for an actor to throw a runtime exception when
     * {@link Actor#configure(Map)} is invoked.
     *
     * @param actor actor of interest
     */
    private void makeRuntimeException(Actor actor) {
        IllegalStateException ex = new IllegalStateException(EXPECTED_EXCEPTION);
        doThrow(ex).when(actor).configure(any());
    }

    @Test
    public void testLoadActors() {
        ActorService service = new ActorService();
        assertFalse(service.getActors().isEmpty());
        assertNotNull(service.getActor(DummyActor.class.getSimpleName()));
    }

    /**
     * Makes an actor service whose {@link ActorService#loadActors()} method returns the
     * given actors.
     *
     * @param actors actors to be returned
     * @return a new actor service
     */
    private ActorService makeService(Actor... actors) {
        return new ActorService() {
            @Override
            protected Iterable<Actor> loadActors() {
                return Arrays.asList(actors);
            }
        };
    }
}
