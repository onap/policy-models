/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2020 Bell Canada. All rights reserved.
 * Modifications Copyright (C) 2020-2021 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.controlloop.actor.cds;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.ccsdk.cds.controllerblueprints.common.api.EventType;
import org.onap.ccsdk.cds.controllerblueprints.common.api.Status;
import org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceOutput;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.OperationResult;

@ExtendWith(MockitoExtension.class)
 class GrpcActorServiceManagerTest {

    CdsActorServiceManager manager;
    CompletableFuture<OperationOutcome> future;
    ExecutionServiceOutput output;

    /**
     * Sets up the fields.
     */
    @BeforeEach
     void setUp() throws Exception {
        future = new CompletableFuture<>();
        manager = new CdsActorServiceManager(new OperationOutcome(), future);
    }

    @Test
     void testOnMessageSuccess() throws InterruptedException, ExecutionException, TimeoutException {

        Status status = Status.newBuilder().setEventType(EventType.EVENT_COMPONENT_EXECUTED).build();
        output = ExecutionServiceOutput.newBuilder().setStatus(status).build();
        manager.onMessage(output);
        OperationOutcome outcome = future.get(2, TimeUnit.SECONDS);
        assertEquals(OperationResult.SUCCESS, outcome.getResult());
        assertSame(output, outcome.getResponse());
    }

    @Test
     void testOnMessageProcessing() throws InterruptedException, ExecutionException, TimeoutException {

        Status status = Status.newBuilder().setEventType(EventType.EVENT_COMPONENT_PROCESSING).build();
        output = ExecutionServiceOutput.newBuilder().setStatus(status).build();
        manager.onMessage(output);
        assertThatThrownBy(() -> future.get(200, TimeUnit.MILLISECONDS)).isInstanceOf(TimeoutException.class);
        assertFalse(future.isDone());
    }

    @Test
     void testOnMessageFailure() throws InterruptedException, ExecutionException, TimeoutException {

        Status status = Status.newBuilder().setEventType(EventType.EVENT_COMPONENT_FAILURE).build();
        output = ExecutionServiceOutput.newBuilder().setStatus(status).build();
        manager.onMessage(output);
        OperationOutcome outcome = future.get(2, TimeUnit.SECONDS);
        assertEquals(OperationResult.FAILURE, outcome.getResult());
        assertSame(output, outcome.getResponse());
    }

    @Test
     void testOnError() throws InterruptedException, ExecutionException, TimeoutException {

        Exception exception = new Exception("something failed");
        manager.onError(exception);
        assertTrue(future.isCompletedExceptionally());
    }
}
