/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2020-2021 Bell Canada. All rights reserved.
 * Modifications Copyright (C) 2023-2025 Nordix Foundation.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.policy.simulators;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.protobuf.util.JsonFormat;
import io.grpc.ManagedChannel;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.onap.ccsdk.cds.controllerblueprints.processing.api.BluePrintProcessingServiceGrpc;
import org.onap.ccsdk.cds.controllerblueprints.processing.api.BluePrintProcessingServiceGrpc.BluePrintProcessingServiceStub;
import org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceInput;
import org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceInput.Builder;
import org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceOutput;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.network.NetworkUtil;
import org.onap.policy.common.utils.resources.ResourceUtils;

class CdsSimulatorTest {
    private static final StandardCoder coder = new StandardCoder();

    private CdsSimulator sim;

    /**
     * Starts the simulator, allocating a unique port for each test so we don't have to
     * wait for the prior server to shut down.
     */
    @BeforeEach
    void setUp() throws Exception {
        int port = NetworkUtil.allocPort();
        sim = new CdsSimulator(Util.LOCALHOST, port);
        sim.start();
        assertTrue(NetworkUtil.isTcpPortOpen(Util.LOCALHOST, port, 200, 250L));
    }

    @AfterEach
    void tearDown() {
        sim.stop();
    }

    @Test
    @Order(1)
    void test() throws Exception {
        String requestStr = IOUtils.toString(
            Objects.requireNonNull(getClass().getResource("cds/cds.request.json")),
            StandardCharsets.UTF_8);
        Builder builder = ExecutionServiceInput.newBuilder();
        JsonFormat.parser().ignoringUnknownFields().merge(requestStr, builder);
        ExecutionServiceInput request = builder.build();
        ManagedChannel channel = NettyChannelBuilder.forAddress(Util.LOCALHOST, sim.getPort()).usePlaintext().build();

        try {
            final CompletableFuture<ExecutionServiceOutput> future = new CompletableFuture<>();
            final CountDownLatch completed = new CountDownLatch(1);

            BluePrintProcessingServiceStub asyncStub = BluePrintProcessingServiceGrpc.newStub(channel);

            StreamObserver<ExecutionServiceOutput> responseObserver = new StreamObserver<>() {
                @Override
                public void onNext(ExecutionServiceOutput output) {
                    future.complete(output);
                }

                @Override
                public void onError(Throwable throwable) {
                    future.completeExceptionally(throwable);
                }

                @Override
                public void onCompleted() {
                    completed.countDown();
                }
            };

            StreamObserver<ExecutionServiceInput> requestObserver = asyncStub.process(responseObserver);
            try {
                // publish the message
                requestObserver.onNext(request);

                // indicate that the request is done
                requestObserver.onCompleted();

            } catch (RuntimeException e) {
                requestObserver.onError(e);
            }

            // wait for it to complete
            assertTrue(completed.await(5, TimeUnit.SECONDS));

            ExecutionServiceOutput result = future.get();
            assertEquals(200, result.getStatus().getCode());

        } finally {
            channel.shutdown();
        }
    }

    @Test
    @Order(2)
    void testGetResponse() throws IOException, CoderException {
        CdsSimulator cdsSimulator = new CdsSimulator(Util.LOCALHOST, sim.getPort());
        String reqstr = ResourceUtils.getResourceAsString(
            "org/onap/policy/simulators/cds/cds.request.json");
        String responseqstr = ResourceUtils.getResourceAsString(
            "org/onap/policy/simulators/cds/pm_control-create-subscription.json");
        ExecutionServiceInput request = coder.decode(reqstr, ExecutionServiceInput.class);
        org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceOutput.Builder esoBuilder =
            ExecutionServiceOutput.newBuilder();
        JsonFormat.parser().ignoringUnknownFields().merge(responseqstr, esoBuilder);
        assertEquals(esoBuilder.toString(), cdsSimulator.getResponse(request, 0).toString());
    }
}
