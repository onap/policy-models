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

import lombok.ToString;

/**
 * An ABATED event.
 */
@ToString(callSuper = true)
public class Abated extends VirtualControlLoopEvent {
    private static final long serialVersionUID = -90742191326653587L;

    /**
     * No arguments constructor.
     */
    public Abated() {
        super();
        setClosedLoopEventStatus(ControlLoopEventStatus.ABATED);
    }

    /**
     * Constructor from a VirtualControlLoop event.
     */
    public Abated(VirtualControlLoopEvent event) {
        super(event);
        setClosedLoopEventStatus(ControlLoopEventStatus.ABATED);
    }

    @Override
    public void setClosedLoopEventStatus(ControlLoopEventStatus status) {
        if (status != ControlLoopEventStatus.ABATED) {
            throw new IllegalArgumentException("Not an ABATED event status");
        }

        super.setClosedLoopEventStatus(status);
    }
}
