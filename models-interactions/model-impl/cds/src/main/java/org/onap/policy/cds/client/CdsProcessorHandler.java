/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2019-2021 Bell Canada.
 * Modifications Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import java.util.concurrent.CountDownLatch;
<<<<<<< HEAD   (82579d Upgrade CDS dependency)
import org.onap.ccsdk.cds.controllerblueprints.common.api.ActionIdentifiers;
import org.onap.ccsdk.cds.controllerblueprints.processing.api.BlueprintProcessingServiceGrpc;
import org.onap.ccsdk.cds.controllerblueprints.processing.api.BlueprintProcessingServiceGrpc.BlueprintProcessingServiceStub;
=======
import lombok.AllArgsConstructor;
import org.onap.ccsdk.cds.controllerblueprints.processing.api.BluePrintProcessingServiceGrpc;
import org.onap.ccsdk.cds.controllerblueprints.processing.api.BluePrintProcessingServiceGrpc.BluePrintProcessingServiceStub;
>>>>>>> CHANGE (35dff8 Upgrade to cds 1.1.5-SNAPSHOT)
import org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceInput;
import org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceOutput;
import org.onap.policy.cds.api.CdsProcessorListener;
import org.onap.policy.common.endpoints.event.comm.Topic.CommInfrastructure;
import org.onap.policy.common.endpoints.utils.NetLoggerUtil;
import org.onap.policy.common.endpoints.utils.NetLoggerUtil.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CdsProcessorHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(CdsProcessorHandler.class);
    private static final String LOG_MSG = "[{}|{}|{}|]{}{}";

    private CdsProcessorListener listener;
    private String url;

    CdsProcessorHandler(final CdsProcessorListener listener, String url) {
        this.listener = listener;
        this.url = url;
    }

    CountDownLatch process(ExecutionServiceInput request, ManagedChannel channel) {
        final ActionIdentifiers header = request.getActionIdentifiers();
        LOGGER.info("Processing blueprint({}:{}) for action({})", header.getBlueprintVersion(),
            header.getBlueprintName(), header.getBlueprintVersion());

        final var finishLatch = new CountDownLatch(1);
        final BluePrintProcessingServiceStub asyncStub = BluePrintProcessingServiceGrpc.newStub(channel);
        final StreamObserver<ExecutionServiceOutput> responseObserver = new StreamObserver<>() {
            @Override
            public void onNext(ExecutionServiceOutput output) {
                NetLoggerUtil.log(EventType.IN, CommInfrastructure.REST, url, output.toString());
                listener.onMessage(output);
            }

            @Override
            public void onError(Throwable throwable) {
                LOGGER.info(LOG_MSG, EventType.IN, CommInfrastructure.REST, url, NetLoggerUtil.SYSTEM_LS,
                                throwable);
                listener.onError(throwable);
                finishLatch.countDown();
            }

            @Override
            public void onCompleted() {
                LOGGER.info("Completed blueprint({}:{}) for action({})", header.getBlueprintVersion(),
                    header.getBlueprintName(), header.getBlueprintVersion());
                finishLatch.countDown();
            }
        };

        final StreamObserver<ExecutionServiceInput> requestObserver = asyncStub.process(responseObserver);
        try {
            NetLoggerUtil.log(EventType.OUT, CommInfrastructure.REST, url, request.toString());

            // Send the message to CDS backend for processing
            requestObserver.onNext(request);
            // Mark the end of requests
            requestObserver.onCompleted();
        } catch (RuntimeException e) {
            requestObserver.onError(e);
        }
        return finishLatch;
    }
}
