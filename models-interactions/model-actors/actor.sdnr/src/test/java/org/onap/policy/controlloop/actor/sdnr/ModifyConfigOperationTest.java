/*--
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

package org.onap.policy.controlloop.actor.sdnr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.controlloop.actor.test.BasicBidirectionalTopicOperation;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.controlloop.ControlLoopEventContext;
import org.onap.policy.controlloop.actorserviceprovider.parameters.BidirectionalTopicConfig;
import org.onap.policy.controlloop.actorserviceprovider.parameters.BidirectionalTopicParams;
import org.onap.policy.controlloop.policy.PolicyResult;
import org.onap.policy.sdnr.PciMessage;

public class ModifyConfigOperationTest extends BasicSdnrOperation {

    private ModifyConfigOperation oper;

    public ModifyConfigOperationTest() {
        super(DEFAULT_ACTOR, ModifyConfigOperation.NAME);
    }

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        BasicBidirectionalTopicOperation.initBeforeClass(MY_SINK, MY_SOURCE);
    }

    @AfterClass
    public static void tearDownAfterClass() {
        destroyAfterClass();
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        oper = new ModifyConfigOperation(params, config);
    }

    @After
    @Override
    public void tearDown() {
        super.tearDown();
    }


    /**
     * Tests "success" case with simulator.
     */
    @Test
    public void testSuccess() throws Exception {
        BidirectionalTopicParams opParams =
                        BidirectionalTopicParams.builder().sinkTopic(MY_SINK).sourceTopic(MY_SOURCE).build();
        config = new BidirectionalTopicConfig(blockingExecutor, opParams, topicMgr, SdnrOperation.SELECTOR_KEYS);

        params = params.toBuilder().retry(0).timeoutSec(5).executor(blockingExecutor).build();

        oper = new ModifyConfigOperation(params, config) {
            @Override
            protected CompletableFuture<OperationOutcome> startGuardAsync() {
                return null;
            }
        };

        outcome = oper.start().get();
        assertEquals(PolicyResult.SUCCESS, outcome.getResult());
    }

    @Test
    public void testConstructor() {
        assertEquals(DEFAULT_ACTOR, oper.getActorName());
        assertEquals(ModifyConfigOperation.NAME, oper.getName());
    }

    @Test
    public void testStartPreprocessorAsync() throws Exception {
        final CompletableFuture<OperationOutcome> future2 = new CompletableFuture<>();
        context = mock(ControlLoopEventContext.class);
        when(context.getEvent()).thenReturn(event);
        params = params.toBuilder().context(context).build();

        AtomicBoolean guardStarted = new AtomicBoolean();

        oper = new ModifyConfigOperation(params, config) {
            @Override
            protected CompletableFuture<OperationOutcome> startGuardAsync() {
                guardStarted.set(true);
                return super.startGuardAsync();
            }
        };
        CompletableFuture<OperationOutcome> future3 = oper.startPreprocessorAsync();

        assertNotNull(future3);
        assertFalse(future.isDone());
        assertTrue(guardStarted.get());

        future2.complete(params.makeOutcome());
        assertTrue(executor.runAll(100));
        assertTrue(future3.isDone());
        assertEquals(PolicyResult.SUCCESS, future3.get().getResult());
    }

    @Test
    public void testMakeRequest() throws CoderException {
        Pair<String, PciMessage> result = oper.makeRequest(1);
        assertNotNull(result.getLeft());
        assertNotNull(result.getRight());
    }
}
