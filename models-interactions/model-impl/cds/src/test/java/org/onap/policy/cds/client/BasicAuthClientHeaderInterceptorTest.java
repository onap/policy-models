/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2017 - 2019 Bell Canada.
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

import static org.junit.Assert.assertEquals;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import io.grpc.ClientInterceptors;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.Metadata.Key;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.ServerInterceptors;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.GrpcCleanupRule;
import java.io.IOException;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.onap.ccsdk.cds.controllerblueprints.processing.api.BluePrintProcessingServiceGrpc;
import org.onap.ccsdk.cds.controllerblueprints.processing.api.BluePrintProcessingServiceGrpc.BluePrintProcessingServiceImplBase;
import org.onap.ccsdk.cds.controllerblueprints.processing.api.BluePrintProcessingServiceGrpc.BluePrintProcessingServiceStub;
import org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceInput;
import org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceOutput;
import org.onap.policy.cds.properties.CdsServerProperties;

@RunWith(JUnit4.class)
public class BasicAuthClientHeaderInterceptorTest {

    // Generate a unique in-process server name.
    private static final String SERVER_NAME = InProcessServerBuilder.generateName();
    private static final String CREDS = "test";

    // Manages automatic graceful shutdown for the registered server and client channels at the end of test.
    @Rule
    public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

    private final ServerInterceptor mockCdsGrpcServerInterceptor = mock(ServerInterceptor.class, delegatesTo(
        new ServerInterceptor() {
            @Override
            public <ReqT, RespT> Listener<ReqT> interceptCall(
                ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
                return next.startCall(call, headers);
            }
        }));
    private final CdsServerProperties props = new CdsServerProperties();
    private final AtomicReference<StreamObserver<ExecutionServiceOutput>> responseObserverRef = new AtomicReference<>();

    private ManagedChannel channel;

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
        props.setUsername(CREDS);
        props.setPassword(CREDS);
        props.setTimeout(60);

        // Implement the test gRPC server
        BluePrintProcessingServiceImplBase testCdsBlueprintServerImpl = new BluePrintProcessingServiceImplBase() {
            @Override
            public StreamObserver<ExecutionServiceInput> process(
                StreamObserver<ExecutionServiceOutput> responseObserver) {
                return super.process(responseObserver);
            }
        };

        // Create a server, add service, start, and register for automatic graceful shutdown.
        grpcCleanup.register(InProcessServerBuilder.forName(SERVER_NAME).directExecutor()
            .addService(ServerInterceptors.intercept(testCdsBlueprintServerImpl, mockCdsGrpcServerInterceptor)).build()
            .start());

        // Create a client channel and register for automatic graceful shutdown
        channel = grpcCleanup.register(InProcessChannelBuilder.forName(SERVER_NAME).directExecutor().build());
    }

    @Test
    public void testIfBasicAuthHeaderIsDeliveredToCdsServer() {
        BluePrintProcessingServiceStub bpProcessingSvcStub = BluePrintProcessingServiceGrpc
            .newStub(ClientInterceptors.intercept(channel, new BasicAuthClientHeaderInterceptor(props)));
        ArgumentCaptor<Metadata> metadataCaptor = ArgumentCaptor.forClass(Metadata.class);
        bpProcessingSvcStub.process(new StreamObserver<ExecutionServiceOutput>() {
            @Override
            public void onNext(final ExecutionServiceOutput executionServiceOutput) {
                // Test purpose only
            }

            @Override
            public void onError(final Throwable throwable) {
                // Test purpose only
            }

            @Override
            public void onCompleted() {
                // Test purpose only
            }
        });
        verify(mockCdsGrpcServerInterceptor).interceptCall(ArgumentMatchers.any(), metadataCaptor.capture(),
            ArgumentMatchers.any());

        Key<String> authHeader = Key
            .of(BasicAuthClientHeaderInterceptor.BASIC_AUTH_HEADER_KEY, Metadata.ASCII_STRING_MARSHALLER);
        String expectedBaseAuth = Base64.getEncoder().encodeToString(String.format("%s:%s", CREDS, CREDS).getBytes());
        assertEquals(expectedBaseAuth, metadataCaptor.getValue().get(authHeader));
    }
}


