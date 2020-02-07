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

    /**
     * Sets the result.
     *
     * @param result new result
     */
    public void setResult(@NonNull PolicyResult result) {
        this.result = result;
    }
}
