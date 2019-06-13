/*-
 * ============LICENSE_START=======================================================
 * policy-yaml
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

package org.onap.policy.controlloop.policy.guard;

import java.util.LinkedList;
import java.util.List;

public class ControlLoopGuard {

    private Guard guard;

    private List<GuardPolicy> guards;

    public ControlLoopGuard() {
        //DO Nothing Empty Constructor
    }

    public ControlLoopGuard(ControlLoopGuard clGuard) {
        this.guard = new Guard();
        this.guards = new LinkedList<>(clGuard.guards);
    }

    public Guard getGuard() {
        return guard;
    }

    public void setGuard(Guard guard) {
        this.guard = guard;
    }

    public List<GuardPolicy> getGuards() {
        return guards;
    }

    public void setGuards(List<GuardPolicy> guards) {
        this.guards = guards;
    }

    @Override
    public String toString() {
        return "Guard [guard=" + guard + ", GuardPolicies=" + guards + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((guard == null) ? 0 : guard.hashCode());
        result = prime * result + ((guards == null) ? 0 : guards.hashCode());
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
        ControlLoopGuard other = (ControlLoopGuard) obj;
        if (guard == null) {
            if (other.guard != null) {
                return false;
            }
        } else if (!guard.equals(other.guard)) {
            return false;
        }
        if (guards == null) {
            if (other.guards != null) {
                return false;
            }
        } else if (!guards.equals(other.guards)) {
            return false;
        }
        return true;
    }


}
