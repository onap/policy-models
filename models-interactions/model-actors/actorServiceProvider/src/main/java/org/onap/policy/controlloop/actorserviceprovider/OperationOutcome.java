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

import java.time.Instant;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.onap.policy.controlloop.ControlLoopOperation;
import org.onap.policy.controlloop.ControlLoopResponse;
import org.onap.policy.controlloop.policy.PolicyResult;

/**
 * Outcome from an operation. Objects of this type are passed from one stage to the next.
 */
@Data
@NoArgsConstructor
public class OperationOutcome {
    private String actor;
    private String operation;
    private String target;
    private Instant start;
    private Instant end;
    private String subRequestId;
    private PolicyResult result = PolicyResult.SUCCESS;
    private String message;
    private boolean finalOutcome;
    private Object response;
    private ControlLoopResponse controlLoopResponse;

    /**
     * Copy constructor.
     *
     * @param source source object from which to copy
     */
    public OperationOutcome(OperationOutcome source) {
        this.actor = source.actor;
        this.operation = source.operation;
        this.target = source.target;
        this.start = source.start;
        this.end = source.end;
        this.subRequestId = source.subRequestId;
        this.result = source.result;
        this.message = source.message;
        this.finalOutcome = source.finalOutcome;
        this.response = source.response;
        this.controlLoopResponse = source.controlLoopResponse;
    }

    /**
     * Creates a {@link ControlLoopOperation}, populating all fields with the values from
     * this object. Sets the outcome field to the string representation of this object's
     * outcome.
     *
     * @return
     */
    public ControlLoopOperation toControlLoopOperation() {
        ControlLoopOperation clo = new ControlLoopOperation();

        clo.setActor(actor);
        clo.setOperation(operation);
        clo.setTarget(target);
        clo.setStart(start);
        clo.setEnd(end);
        clo.setSubRequestId(subRequestId);
        clo.setOutcome(result.toString());
        clo.setMessage(message);

        return clo;
    }

    @SuppressWarnings("unchecked")
    public <T> T getResponse() {
        return (T) response;
    }

    /**
     * Determines if this outcome is for the given actor and operation.
     *
     * @param actor actor name
     * @param operation operation name
     * @return {@code true} if this outcome is for the given actor and operation
     */
    public boolean isFor(@NonNull String actor, @NonNull String operation) {
        // do the operation check first, as it's most likely to be unique
        return (operation.equals(this.operation) && actor.equals(this.actor));
    }

    /**
     * Determines if an outcome is for the given actor and operation.
     *
     * @param outcome outcome to be examined, or {@code null}
     * @param actor actor name
     * @param operation operation name
     * @return {@code true} if this outcome is for the given actor and operation,
     *         {@code false} it is {@code null} or not for the actor/operation
     */
    public static boolean isFor(OperationOutcome outcome, String actor, String operation) {
        return (outcome != null && outcome.isFor(actor, operation));
    }

    /**
     * Sets the result.
     *
     * @param result new result
     */
    public void setResult(@NonNull PolicyResult result) {
        this.result = result;
    }
}
