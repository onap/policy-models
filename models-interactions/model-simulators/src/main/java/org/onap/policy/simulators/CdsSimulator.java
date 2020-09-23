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

import com.att.aft.dme2.internal.apache.commons.lang.StringUtils;
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

    private int  countOfEvents = 1;

    /**
     * Constructs the object, but does not start it.
     *
     * @param host host name of the server
     * @param port port of the server
     */
    public CdsSimulator(String host, int port) {
        this(host, port, 0, 0);
    }

    /**
     * Constructs the object, but does not start it.
     *
     * @param host host name of the server
     * @param port port of the server
     * @param countOfSuccesfulEvents number of successive successful events
     * @param responseTimer time for the request to be processed
     */
    public CdsSimulator(String host, int port, int countOfSuccesfulEvents, int responseTimer) {
        this.port = port;

        BluePrintProcessingServiceImplBase testCdsBlueprintServerImpl = new BluePrintProcessingServiceImplBase() {

            @Override
            public StreamObserver<ExecutionServiceInput> process(
                            final StreamObserver<ExecutionServiceOutput> responseObserver) {

                return new StreamObserver<ExecutionServiceInput>() {
                    final long requested = System.currentTimeMillis();

                    @Override
                    public void onNext(final ExecutionServiceInput executionServiceInput) {
                        try {
                            String responseString = getResponseString(executionServiceInput, countOfSuccesfulEvents);
                            Builder builder = ExecutionServiceOutput.newBuilder();
                            JsonFormat.parser().ignoringUnknownFields().merge(responseString, builder);
                            if (responseTimer > 0) {
                                long leftToSleep = requested + responseTimer - System.currentTimeMillis();
                                Thread.sleep(leftToSleep);
                            }
                            responseObserver.onNext(builder.build());
                            countOfEvents++;
                        } catch (InvalidProtocolBufferException e) {
                            throw new SimulatorRuntimeException("Cannot convert ExecutionServiceOutput output", e);

                        } catch (IOException e) {
                            throw new SimulatorRuntimeException("Cannot read ExecutionServiceOutput from file", e);
                        } catch (InterruptedException e) {
                            throw new SimulatorRuntimeException("Execution Interrupted", e);
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

    /**
     * Constructs the ResponseString on the basis of request.
     *
     * @param executionServiceInput service input
     * @param countOfSuccesfulEvents number of successive successful events
     * @return  responseString
     */
    public String getResponseString(ExecutionServiceInput executionServiceInput, int countOfSuccesfulEvents)
        throws IOException {
        String resourceName = "cds/DefaultResponseEvent.json";
        String responseString;
        if (!StringUtils.isBlank(executionServiceInput.getActionIdentifiers().getActionName())) {
            resourceName = "cds/" + executionServiceInput.getActionIdentifiers().getActionName()
                + ".json";
        }
        try {
            responseString = IOUtils.toString(getClass().getResource(resourceName), StandardCharsets.UTF_8);
        } catch (NullPointerException e) {
            responseString = IOUtils.toString(getClass().getResource("cds/DefaultResponseEvent.json"),
                StandardCharsets.UTF_8);
        }
        // generating the failure response by just changing the status message and status code
        if (countOfSuccesfulEvents > 0 && countOfEvents % countOfSuccesfulEvents == 0) {
            responseString = responseString.replace("success", "failure");
            responseString = responseString.replace("200", "500");
        }
        return responseString;
    }
}
