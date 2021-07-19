/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2019 Bell Canada.
 * Modifications Copyright (C) 2019-2020 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.cds.client;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.GrpcCleanupRule;
import io.grpc.util.MutableHandlerRegistry;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.onap.ccsdk.cds.controllerblueprints.common.api.ActionIdentifiers;
import org.onap.ccsdk.cds.controllerblueprints.processing.api.BlueprintProcessingServiceGrpc.BlueprintProcessingServiceImplBase;
import org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceInput;
import org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceOutput;
import org.onap.policy.cds.api.CdsProcessorListener;
import org.onap.policy.cds.api.TestCdsProcessorListenerImpl;
import org.onap.policy.cds.properties.CdsServerProperties;

public class CdsProcessorGrpcClientTest {

    // Generate a unique in-process server name.
    private static final String SERVER_NAME = InProcessServerBuilder.generateName();

    // Manages automatic graceful shutdown for the registered server and client channels at the end of test.
    @Rule
    public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

    private final CdsProcessorListener listener = spy(new TestCdsProcessorListenerImpl());
    private final CdsServerProperties props = new CdsServerProperties();
    private final MutableHandlerRegistry serviceRegistry = new MutableHandlerRegistry();
    private final AtomicReference<StreamObserver<ExecutionServiceOutput>> responseObserverRef = new AtomicReference<>();
    private final List<String> messagesDelivered = new ArrayList<>();
    private final CountDownLatch allRequestsDelivered = new CountDownLatch(1);

    private CdsProcessorGrpcClient client;

    /**
     * Setup the test.
     *
     * @throws IOException on failure to register the test grpc server for graceful shutdown
     */
    @Before
    public void setUp() throws IOException {
        // Setup the CDS properties
        props.setHost(SERVER_NAME);
        props.setPort(2000);
        props.setUsername("testUser");
        props.setPassword("testPassword");
        props.setTimeout(60);

        // Create a server, add service, start, and register for automatic graceful shutdown.
        grpcCleanup.register(InProcessServerBuilder.forName(SERVER_NAME)
            .fallbackHandlerRegistry(serviceRegistry).directExecutor().build().start());

        // Create a client channel and register for automatic graceful shutdown
        ManagedChannel channel = grpcCleanup
            .register(InProcessChannelBuilder.forName(SERVER_NAME).directExecutor().build());

        // Create an instance of the gRPC client
        client = new CdsProcessorGrpcClient(channel, new CdsProcessorHandler(listener, "gRPC://localhost:1234/"));

        // Implement the test gRPC server
        BlueprintProcessingServiceImplBase testCdsBlueprintServerImpl = new BlueprintProcessingServiceImplBase() {
            @Override
            public StreamObserver<ExecutionServiceInput> process(
                final StreamObserver<ExecutionServiceOutput> responseObserver) {
                responseObserverRef.set(responseObserver);

                return new StreamObserver<ExecutionServiceInput>() {
                    @Override
                    public void onNext(final ExecutionServiceInput executionServiceInput) {
                        messagesDelivered.add(executionServiceInput.getActionIdentifiers().getActionName());
                    }

                    @Override
                    public void onError(final Throwable throwable) {
                        // Test method
                    }

                    @Override
                    public void onCompleted() {
                        allRequestsDelivered.countDown();
                    }
                };
            }
        };
        serviceRegistry.addService(testCdsBlueprintServerImpl);
    }

    @After
    public void tearDown() {
        client.close();
    }

    @Test
    public void testCdsProcessorGrpcClientConstructor() {
        assertThatCode(() -> new CdsProcessorGrpcClient(listener, props).close()).doesNotThrowAnyException();
    }

    @Test(expected = IllegalStateException.class)
    public void testCdsProcessorGrpcClientConstructorFailure() {
        props.setHost(null);
        new CdsProcessorGrpcClient(listener, props).close();
    }

    @Test
    public void testSendRequestFail() throws InterruptedException {
        // Setup
        ExecutionServiceInput testReq = ExecutionServiceInput.newBuilder()
            .setActionIdentifiers(ActionIdentifiers.newBuilder().setActionName("policy-to-cds").build())
            .build();

        // Act
        CountDownLatch finishLatch = client.sendRequest(testReq);
        responseObserverRef.get().onError(new Throwable("failed to send testReq."));

        verify(listener).onError(any(Throwable.class));
        assertTrue(finishLatch.await(0, TimeUnit.SECONDS));
    }

    @Test
    public void testSendRequestSuccess() throws InterruptedException {
        // Setup request
        ExecutionServiceInput testReq1 = ExecutionServiceInput.newBuilder()
            .setActionIdentifiers(ActionIdentifiers.newBuilder().setActionName("policy-to-cds-req1").build()).build();

        // Act
        final CountDownLatch finishLatch = client.sendRequest(testReq1);

        // Assert that request message was sent and delivered once to the server
        assertTrue(allRequestsDelivered.await(1, TimeUnit.SECONDS));
        assertEquals(Collections.singletonList("policy-to-cds-req1"), messagesDelivered);

        // Setup the server to send out two simple response messages and verify that the client receives them.
        ExecutionServiceOutput testResp1 = ExecutionServiceOutput.newBuilder()
            .setActionIdentifiers(ActionIdentifiers.newBuilder().setActionName("policy-to-cds-resp1").build()).build();
        ExecutionServiceOutput testResp2 = ExecutionServiceOutput.newBuilder()
            .setActionIdentifiers(ActionIdentifiers.newBuilder().setActionName("policy-to-cds-resp2").build()).build();
        responseObserverRef.get().onNext(testResp1);
        verify(listener).onMessage(testResp1);
        responseObserverRef.get().onNext(testResp2);
        verify(listener).onMessage(testResp2);

        // let server complete.
        responseObserverRef.get().onCompleted();
        assertTrue(finishLatch.await(0, TimeUnit.SECONDS));
    }
}
