/*-
 * ============LICENSE_START=======================================================
 * controlloop
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019 Nordix Foundation.
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

package org.onap.policy.controlloop;

import java.io.Serializable;
import java.time.Instant;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ControlLoopOperation implements Serializable {

    private static final long serialVersionUID = 8662706581293017099L;

    private String actor;
    private String operation;
    private String target;
    private Instant start = Instant.now();
    private Instant end;
    private String subRequestId;
    private String outcome;
    private String message;

    /**
     * Construct an instance from an existing instance.
     *
     * @param op the existing instance
     */
    public ControlLoopOperation(ControlLoopOperation op) {
        if (op == null) {
            return;
        }

        this.actor = op.actor;
        this.operation = op.operation;
        this.target = op.target;
        this.start = op.start;
        this.end = op.end;
        this.subRequestId = op.subRequestId;
        this.outcome = op.outcome;
        this.message = op.message;
    }

    public String toMessage() {
        return "actor=" + actor + ",operation=" + operation + ",target=" + target + ",subRequestId=" + subRequestId;
    }

    public String toHistory() {
        return "actor=" + actor + ",operation=" + operation + ",target=" + target + ",start=" + start + ",end=" + end
                + ",subRequestId=" + subRequestId + ",outcome=" + outcome + ",message=" + message;
    }

    @Override
    public String toString() {
        return "ControlLoopOperation [actor=" + actor + ", operation=" + operation + ", target=" + target + ", start="
                + start + ", end=" + end + ", subRequestId=" + subRequestId + ", outcome=" + outcome + ", message="
                + message + "]";
    }
}
