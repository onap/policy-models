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

import io.grpc.Status;
import java.util.concurrent.atomic.AtomicReference;
import org.onap.ccsdk.cds.controllerblueprints.common.api.EventType;
import org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceOutput;
import org.onap.policy.cds.api.CdsProcessorListener;
import org.onap.policy.controlloop.actor.cds.constants.CdsActorConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CDS Actor service-manager implementation.
 */
public class CdsActorServiceManager implements CdsProcessorListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(CdsActorServiceManager.class);

    private final AtomicReference<String> cdsStatus;

    public CdsActorServiceManager(AtomicReference<String> cdsStatus) {
        this.cdsStatus = cdsStatus;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public void onMessage(final ExecutionServiceOutput message) {
        LOGGER.info("Received notification from CDS: {}", message);
        EventType eventType = message.getStatus().getEventType();
        switch (eventType) {
            case EVENT_COMPONENT_FAILURE:
                cdsStatus.compareAndSet(null, CdsActorConstants.FAILED);
                break;
            case EVENT_COMPONENT_PROCESSING:
                cdsStatus.compareAndSet(null, CdsActorConstants.PROCESSING);
                break;
            case EVENT_COMPONENT_EXECUTED:
                cdsStatus.compareAndSet(null, CdsActorConstants.SUCCESS);
                break;
            default:
                cdsStatus.compareAndSet(null, CdsActorConstants.FAILED);
                break;
        }
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public void onError(final Throwable throwable) {
        Status status = Status.fromThrowable(throwable);
        cdsStatus.compareAndSet(null, CdsActorConstants.ERROR);
        LOGGER.error("Failed processing blueprint {} {}", status, throwable);
    }
}
