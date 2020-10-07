/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix Foundation.
 *  Modifications Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
 *  Modifications Copyright (C) 2020 Bell Canada. All rights reserved.
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.onap.ccsdk.cds.controllerblueprints.common.api.ActionIdentifiers;
import org.onap.ccsdk.cds.controllerblueprints.processing.api.BluePrintProcessingServiceGrpc.BluePrintProcessingServiceImplBase;
import org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceInput;
import org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceOutput;
import org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceOutput.Builder;
import org.onap.policy.common.utils.resources.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CdsSimulator {

    private static final Logger LOGGER = LoggerFactory.getLogger(CdsSimulator.class);

    @Getter
    private final int port;

    private final Server server;

    private final String resourceLocation;

    private AtomicInteger countOfEvents = new AtomicInteger(1);

    /**
     * Constructs the object, but does not start it.
     *
     * @param host host name of the server
     * @param port port of the server
     */
    public CdsSimulator(String host, int port) {
        this(host, port, "org/onap/policy/simulators/cds/", 0, 0);
    }

    /**
     * Constructs the object, but does not start it.
     *
     * @param host host name of the server
     * @param port port of the server
     * @param countOfSuccesfulEvents number of successive successful events
     * @param requestedResponseDelayMs time for the request to be processed
     */
    public CdsSimulator(String host, int port, String resourceLocation, int countOfSuccesfulEvents,
        long requestedResponseDelayMs) {
        this.port = port;
        this.resourceLocation = resourceLocation;

        BluePrintProcessingServiceImplBase testCdsBlueprintServerImpl = new BluePrintProcessingServiceImplBase() {

            @Override
            public StreamObserver<ExecutionServiceInput> process(
                            final StreamObserver<ExecutionServiceOutput> responseObserver) {

                return new StreamObserver<ExecutionServiceInput>() {

                    @Override
                    public void onNext(final ExecutionServiceInput executionServiceInput) {
                        LOGGER.info("Received request input to CDS: {}", executionServiceInput);
                        try {
                            String responseString = getResponseString(executionServiceInput, countOfSuccesfulEvents);
                            Builder builder = ExecutionServiceOutput.newBuilder();
                            JsonFormat.parser().ignoringUnknownFields().merge(responseString, builder);
                            TimeUnit.MILLISECONDS.sleep(requestedResponseDelayMs);
                            responseObserver.onNext(builder.build());
                        } catch (InvalidProtocolBufferException e) {
                            throw new SimulatorRuntimeException("Cannot convert ExecutionServiceOutput output", e);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
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
    public String getResponseString(ExecutionServiceInput executionServiceInput, int countOfSuccesfulEvents) {
        String resourceName = "DefaultResponseEvent";
        if (!StringUtils.isBlank(executionServiceInput.getActionIdentifiers().getActionName())) {
            ActionIdentifiers actionIdentifiers = executionServiceInput.getActionIdentifiers();
            resourceName = actionIdentifiers.getBlueprintName() + "-" + actionIdentifiers.getActionName();
        }
        if (countOfSuccesfulEvents > 0 && countOfEvents.getAndIncrement() % countOfSuccesfulEvents == 0) {
            // generating the failure response
            resourceName = resourceName + "-error.json";
        } else {
            resourceName = resourceName + ".json";
        }
        LOGGER.info("Fetching response from {}", resourceName);
        String responseString = ResourceUtils.getResourceAsString(resourceLocation + resourceName);
        if (responseString == null) {
            LOGGER.info("Expected response file {} not found in {}", resourceName, resourceLocation);
            responseString = ResourceUtils.getResourceAsString(resourceLocation
                + "DefaultResponseEvent.json");
        }
        LOGGER.debug("Returning response from CDS Simulator: {}", responseString);
        return responseString;
    }
}
