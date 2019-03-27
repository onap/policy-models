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

    public ControlLoopOperation() {

    }

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

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public Instant getStart() {
        return start;
    }

    public void setStart(Instant start) {
        this.start = start;
    }

    public Instant getEnd() {
        return end;
    }

    public void setEnd(Instant end) {
        this.end = end;
    }

    public String getSubRequestId() {
        return subRequestId;
    }

    public void setSubRequestId(String subRequestId) {
        this.subRequestId = subRequestId;
    }

    public String getOutcome() {
        return outcome;
    }

    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "ControlLoopOperation [actor=" + actor + ", operation=" + operation + ", target=" + target + ", start="
                + start + ", end=" + end + ", subRequestId=" + subRequestId + ", outcome=" + outcome + ", message="
                + message + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((actor == null) ? 0 : actor.hashCode());
        result = prime * result + ((end == null) ? 0 : end.hashCode());
        result = prime * result + ((message == null) ? 0 : message.hashCode());
        result = prime * result + ((operation == null) ? 0 : operation.hashCode());
        result = prime * result + ((outcome == null) ? 0 : outcome.hashCode());
        result = prime * result + ((start == null) ? 0 : start.hashCode());
        result = prime * result + ((subRequestId == null) ? 0 : subRequestId.hashCode());
        result = prime * result + ((target == null) ? 0 : target.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ControlLoopOperation other = (ControlLoopOperation) obj;
        if (actor != null ? !actor.equals(other.actor) : other.actor != null) {
            return false;
        }
        if (end != null ? !end.equals(other.end) : other.end != null) {
            return false;
        }
        if (message != null ? !message.equals(other.message) : other.message != null) {
            return false;
        }
        if (operation != null ? !operation.equals(other.operation) : other.operation != null) {
            return false;
        }
        if (outcome != null ? !outcome.equals(other.outcome) : other.outcome != null) {
            return false;
        }
        if (start != null ? !start.equals(other.start) : other.start != null) {
            return false;
        }
        if (subRequestId != null ? !subRequestId.equals(other.subRequestId) : other.subRequestId != null) {
            return false;
        }
        if (target != null ? !target.equals(other.target) : other.target != null) {
            return false;
        }
        return true;
    }

}
