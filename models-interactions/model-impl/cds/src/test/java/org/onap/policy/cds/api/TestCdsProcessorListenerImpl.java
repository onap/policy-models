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

package org.onap.policy.cds.api;

import io.grpc.Status;
import org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used as a helper for the gRPC client unit test.
 */
public class TestCdsProcessorListenerImpl implements CdsProcessorListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestCdsProcessorListenerImpl.class);

    /**
     * Used to verify/inspect message received from server.
     */
    @Override
    public void onMessage(final ExecutionServiceOutput message) {
        LOGGER.info("Received notification from CDS: {}", message);
    }

    /**
     * Used to verify/inspect error received from server.
     */
    @Override
    public void onError(final Throwable throwable) {
        Status status = Status.fromThrowable(throwable);
        LOGGER.error("Failed processing blueprint {}", status, throwable);
    }
}
