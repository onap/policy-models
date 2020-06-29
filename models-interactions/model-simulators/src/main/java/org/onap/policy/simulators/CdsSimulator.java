/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix Foundation.
 *  Modifications Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import io.grpc.Server;
import io.grpc.netty.NettyServerBuilder;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import lombok.Getter;
import org.apache.commons.io.IOUtils;
import org.onap.ccsdk.cds.controllerblueprints.processing.api.BluePrintProcessingServiceGrpc.BluePrintProcessingServiceImplBase;
import org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceInput;
import org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceOutput;
import org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceOutput.Builder;

public class CdsSimulator {
    @Getter
    private final int port;

    private final Server server;

    /**
     * Constructs the object, but does not start it.
     *
     * @param host host name of the server
     * @param port port of the server
     */
    public CdsSimulator(String host, int port) {
        this.port = port;

        BluePrintProcessingServiceImplBase testCdsBlueprintServerImpl = new BluePrintProcessingServiceImplBase() {

            @Override
            public StreamObserver<ExecutionServiceInput> process(
                            final StreamObserver<ExecutionServiceOutput> responseObserver) {

                return new StreamObserver<ExecutionServiceInput>() {

                    @Override
                    public void onNext(final ExecutionServiceInput executionServiceInput) {
                        try {
                            String responseString = IOUtils.toString(
                                            getClass().getResource("cds/CreateSubscriptionResponseEvent.json"),
                                            StandardCharsets.UTF_8);
                            Builder builder = ExecutionServiceOutput.newBuilder();
                            JsonFormat.parser().ignoringUnknownFields().merge(responseString, builder);
                            responseObserver.onNext(builder.build());

                        } catch (InvalidProtocolBufferException e) {
                            throw new SimulatorRuntimeException("Cannot convert ExecutionServiceOutput output", e);

                        } catch (IOException e) {
                            throw new SimulatorRuntimeException("Cannot read ExecutionServiceOutput from file", e);
                        }
                    }

                    @Override
                    public void onError(final Throwable throwable) {
                        responseObserver.onError(throwable);
                    }

                    @Override
                    public void onCompleted() {
                        responseObserver.onCompleted();
                    }
                };
            }
        };

        server = NettyServerBuilder.forAddress(new InetSocketAddress(host, port)).addService(testCdsBlueprintServerImpl)
                        .build();
    }

    public void start() throws IOException {
        server.start();
    }

    public void stop() {
        server.shutdown();
    }
}
