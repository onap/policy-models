/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2019 Bell Canada.
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

import com.google.common.base.Preconditions;
import io.grpc.ManagedChannel;
import io.grpc.internal.DnsNameResolverProvider;
import io.grpc.internal.PickFirstLoadBalancerProvider;
import io.grpc.netty.NettyChannelBuilder;
import java.util.concurrent.CountDownLatch;
import org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceInput;
import org.onap.policy.cds.api.CdsProcessorListener;
import org.onap.policy.cds.properties.CdsServerProperties;
import org.onap.policy.common.parameters.GroupValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * The CDS processor client uses gRPC for communication between Policy and CDS. This communication is configured to use
 * a streaming approach, which means the client sends an event to which the server can reply with multiple
 * sub-responses, until full completion of the processing.
 * </p>
 * The client is implemented as a singleton in order to use the same channel for all requests.
 */
public class CdsProcessorGrpcClient implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(CdsProcessorGrpcClient.class);

    private static CdsProcessorGrpcClient instance = null;

    private ManagedChannel channel;
    private CdsProcessorHandler handler;

    private CdsProcessorGrpcClient(final CdsProcessorListener listener, CdsServerProperties props) {
        final GroupValidationResult validationResult = props.validate();
        Preconditions.checkState(validationResult.getStatus().isValid(), "Error validating CDS server "
            + "properties: " + validationResult.getResult());

        this.channel = NettyChannelBuilder.forAddress(props.getHost(), props.getPort())
            .nameResolverFactory(new DnsNameResolverProvider())
            .loadBalancerFactory(new PickFirstLoadBalancerProvider())
            .intercept(new BasicAuthClientHeaderInterceptor(props)).usePlaintext().build();
        this.handler = new CdsProcessorHandler(listener);
        LOGGER.info("CdsProcessorListener started");
    }

    private CdsProcessorGrpcClient(final ManagedChannel channel, final CdsProcessorHandler handler) {
        this.channel = channel;
        this.handler = handler;
    }

    /**
     * Indicate if the client instance is initialized.
     * @return true if the instance is initialized and false otherwise.
     */
    public static boolean isInstanceInitialized() {
        return instance != null;
    }

    /**
     * Initialize the unique client instance.
     * @param listener the client listener.
     * @param props the client properties.
     * @throws IllegalArgumentException if the instance is already initialized.
     */
    public static void initInstance(final CdsProcessorListener listener, CdsServerProperties props) {
        synchronized (CdsProcessorGrpcClient.class) {
            if (instance != null) {
                throw new IllegalArgumentException("Instance is already initialized.");
            }
            instance = new CdsProcessorGrpcClient(listener, props);
        }
    }

    static void initInstance(final ManagedChannel channel, final CdsProcessorHandler handler) {
        synchronized (CdsProcessorGrpcClient.class) {
            if (instance != null) {
                throw new IllegalArgumentException("Instance is already initialized.");
            }
            instance = new CdsProcessorGrpcClient(channel, handler);
        }
    }

    static void cleanInstance() {
        synchronized (CdsProcessorGrpcClient.class) {
            instance = null;
        }
    }

    /**
     * Get the unique client instance.
     * @return the client.
     * @throws IllegalArgumentException if the instance is not initialized yet.
     */
    public static CdsProcessorGrpcClient getInstance() {
        if (instance == null) {
            throw new IllegalArgumentException("Instance is not initialized.");
        }
        return instance;
    }

    /**
     * Sends a request to the CDS backend micro-service.
     *
     * <p>The caller will be returned a CountDownLatch that can be used to define how long the processing can wait. The
     * CountDownLatch is initiated with just 1 count. When the client receives an #onCompleted callback, the counter
     * will decrement.</p>
     *
     * <p>It is the user responsibility to close the client.</p>
     *
     * @param input request to send
     * @return CountDownLatch instance that can be use to #await for completeness of processing
     */
    public CountDownLatch sendRequest(ExecutionServiceInput input) {
        return handler.process(input, channel);
    }

    @Override
    public void close() {
        if (channel != null) {
            channel.shutdown();
        }
        LOGGER.info("CdsProcessorListener stopped");
    }
}
