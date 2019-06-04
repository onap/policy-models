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

import org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceOutput;

/**
 * <p>
 * In order for the caller of {@link org.onap.policy.cds.client.CdsProcessorGrpcClient} to manage the callback to
 * handle the received messages appropriately, it needs to implement {@link CdsProcessorListener}.
 * </p>
 *
 * <p>Here is a sample implementation of a listener:
 * <pre>
 * new CdsProcessorListener {
 *
 *     &#64;Override
 *     public void onMessage(ExecutionServiceOutput message) {
 *         log.info("Received notification from CDS: {}", message);
 *     }
 *
 *     &#64;Override
 *     public void onError(Throwable throwable) {
 *         Status status = Status.fromThrowable(throwable);
 *         log.error("Failed processing blueprint {}", status, throwable);
 *     }
 * }
 * </pre>
 * </p>
 */
public interface CdsProcessorListener {

    void onMessage(ExecutionServiceOutput message);

    void onError(Throwable throwable);

}
