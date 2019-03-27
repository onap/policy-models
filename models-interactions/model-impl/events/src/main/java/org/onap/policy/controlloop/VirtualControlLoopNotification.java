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

import com.google.gson.annotations.SerializedName;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class VirtualControlLoopNotification extends ControlLoopNotification {

    private static final long serialVersionUID = 5354756047932144017L;

    @SerializedName("AAI")
    private Map<String, String> aai = new HashMap<>();
    
    @SerializedName("closedLoopAlarmStart")
    private Instant closedLoopAlarmStart;
    
    @SerializedName("closedLoopAlarmEnd")
    private Instant closedLoopAlarmEnd;

    public VirtualControlLoopNotification() {}

    /**
     * Construct an instance.
     * 
     * @param event the event
     */
    public VirtualControlLoopNotification(VirtualControlLoopEvent event) {
        super(event);
        if (event == null) {
            return;
        }
        if (event.getAai() != null) {
            this.setAai(new HashMap<>(event.getAai()));
        }
        this.closedLoopAlarmStart = event.getClosedLoopAlarmStart();
        this.closedLoopAlarmEnd = event.getClosedLoopAlarmEnd();
    }

    public Map<String, String> getAai() {
        return aai;
    }

    public void setAai(Map<String, String> aai) {
        this.aai = aai;
    }

    public Instant getClosedLoopAlarmStart() {
        return closedLoopAlarmStart;
    }

    public void setClosedLoopAlarmStart(Instant closedLoopAlarmStart) {
        this.closedLoopAlarmStart = closedLoopAlarmStart;
    }

    public Instant getClosedLoopAlarmEnd() {
        return closedLoopAlarmEnd;
    }

    public void setClosedLoopAlarmEnd(Instant closedLoopAlarmEnd) {
        this.closedLoopAlarmEnd = closedLoopAlarmEnd;
    }
}
