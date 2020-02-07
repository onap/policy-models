/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.controlloop.actorserviceprovider;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import javax.ws.rs.client.InvocationCallback;
import lombok.AccessLevel;
import lombok.Getter;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.controlloop.actorserviceprovider.pipeline.PipelineControllerFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for a <i>single</i> asynchronous response.
 *
 * @param <T> response type
 */
@Getter
public abstract class AsyncResponseHandler<T> implements InvocationCallback<T> {

    private static final Logger logger = LoggerFactory.getLogger(AsyncResponseHandler.class);

    @Getter(AccessLevel.NONE)
    private final PipelineControllerFuture<OperationOutcome> result = new PipelineControllerFuture<>();
    private final ControlLoopOperationParams params;
    private final OperationOutcome outcome;

    /**
     * Constructs the object.
     *
     * @param params operation parameters
     * @param outcome outcome to be populated based on the response
     */
    public AsyncResponseHandler(ControlLoopOperationParams params, OperationOutcome outcome) {
        this.params = params;
        this.outcome = outcome;
    }

    /**
     * Handles the given future, arranging to cancel it when the response is received.
     *
     * @param future future to be handled
     * @return a future to be used to cancel or wait for the response
     */
    public CompletableFuture<OperationOutcome> handle(Future<T> future) {
        result.add(future);
        return result;
    }

    /**
     * Invokes {@link #doComplete()} and then completes "this" with the returned value.
     */
    @Override
    public void completed(T rawResponse) {
        try {
            logger.trace("{}.{}: response completed for {}", params.getActor(), params.getOperation(),
                            params.getRequestId());
            result.complete(doComplete(rawResponse));

        } catch (RuntimeException e) {
            logger.trace("{}.{}: response handler threw an exception for {}", params.getActor(), params.getOperation(),
                            params.getRequestId());
            result.completeExceptionally(e);
        }
    }

    /**
     * Invokes {@link #doFailed()} and then completes "this" with the returned value.
     */
    @Override
    public void failed(Throwable throwable) {
        try {
            logger.trace("{}.{}: response failure for {}", params.getActor(), params.getOperation(),
                            params.getRequestId());
            result.complete(doFailed(throwable));

        } catch (RuntimeException e) {
            logger.trace("{}.{}: response failure handler threw an exception for {}", params.getActor(),
                            params.getOperation(), params.getRequestId());
            result.completeExceptionally(e);
        }
    }

    /**
     * Completes the processing of a response.
     *
     * @param rawResponse raw response that was received
     * @return the outcome
     */
    protected abstract OperationOutcome doComplete(T rawResponse);

    /**
     * Handles a response exception.
     *
     * @param thrown exception that was thrown
     * @return the outcome
     */
    protected abstract OperationOutcome doFailed(Throwable thrown);
}
