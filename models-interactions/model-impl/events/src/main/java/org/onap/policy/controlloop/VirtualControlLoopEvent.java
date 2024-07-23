/*-
 * ============LICENSE_START=======================================================
 * controlloop
 * ================================================================================
 * Copyright (C) 2017-2020 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019, 2024 Nordix Foundation.
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
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@ToString(callSuper = true)
public class VirtualControlLoopEvent extends ControlLoopEvent {

    private static final long serialVersionUID = -5752405682246066226L;

    @SerializedName("AAI")
    private Map<String, String> aai = new HashMap<>();
    protected String payload;
    protected Instant closedLoopAlarmStart;
    protected Instant closedLoopAlarmEnd;

    /**
     * Construct an instance from an existing instance.
     *
     * @param event the existing instance
     */
    public VirtualControlLoopEvent(VirtualControlLoopEvent event) {
        super(event);
        if (event == null) {
            return;
        }
        if (event.aai != null) {
            this.aai = new HashMap<>(event.aai);
        }
        this.payload = event.payload;
        this.closedLoopAlarmStart = event.closedLoopAlarmStart;
        this.closedLoopAlarmEnd = event.closedLoopAlarmEnd;
    }
}
