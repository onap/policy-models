/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2020 Bell Canada. All rights reserved.
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

import java.util.concurrent.CompletableFuture;
import org.onap.ccsdk.cds.controllerblueprints.common.api.EventType;
import org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceOutput;
import org.onap.policy.cds.api.CdsProcessorListener;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.OperationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CDS Actor service-manager implementation.
 */
public class CdsActorServiceManager implements CdsProcessorListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(CdsActorServiceManager.class);

    private final CompletableFuture<OperationOutcome> future;

    private final OperationOutcome outcome;

    /**
     * Constructs the object.
     *
     * @param outcome the operation outcome to populate
     * @param future the future to complete
     */
    public CdsActorServiceManager(OperationOutcome outcome, CompletableFuture<OperationOutcome> future) {
        this.outcome = outcome;
        this.future = future;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public void onMessage(final ExecutionServiceOutput message) {
        LOGGER.info("Received notification from CDS: {}", message);
        EventType eventType = message.getStatus().getEventType();
        switch (eventType) {
            case EVENT_COMPONENT_PROCESSING:
                LOGGER.info("CDS is processing the message: {}", message);
                break;
            case EVENT_COMPONENT_EXECUTED:
                outcome.setResult(OperationResult.SUCCESS);
                outcome.setResponse(message);
                future.complete(outcome);
                break;
            default:
                outcome.setResult(OperationResult.FAILURE);
                outcome.setResponse(message);
                future.complete(outcome);
                break;
        }
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public void onError(final Throwable throwable) {
        future.completeExceptionally(throwable);
    }
}
