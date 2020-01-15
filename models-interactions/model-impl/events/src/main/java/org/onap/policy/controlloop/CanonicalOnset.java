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

package org.onap.policy.controlloop;

import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * An ONSET event that determines equality with other ONSET events
 * with only non-time dependent values.
 */

@NoArgsConstructor
@ToString(callSuper = true)
public class CanonicalOnset extends Onset {
    private static final long serialVersionUID = 284865663873284818L;

    public CanonicalOnset(CanonicalOnset event) {
        super(event);
    }

    public CanonicalOnset(VirtualControlLoopEvent event) {
        super(event);
    }

    @Override
    public boolean equals(Object other) {
        // see hashcode method notes
        return EqualsBuilder.reflectionEquals(
            this, other, "requestId", "closedLoopAlarmStart", "closedLoopAlarmEnd");
    }

    @Override
    public int hashCode() {
        // The reflection based implementation has been chosen
        // for maintenance reasons, even though may incur in some
        // performance overhead.   The other possibility is to use
        // Objects.hash(..) but will require to spell out all fields
        // to be considered, which are many more than the exceptions,
        // in addition this class would need to be updated as new fields
        // are added.   Other option to consider in the future is to
        // restructure the class hierarchy.   Note that could not use
        // lombok annotations to exclude fields from superclasses.
        return
            HashCodeBuilder.reflectionHashCode(
                this, "requestId", "closedLoopAlarmStart", "closedLoopAlarmEnd");
    }

}
