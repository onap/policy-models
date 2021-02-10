/*-
 * ============LICENSE_START=======================================================
 * controlloop
 * ================================================================================
 * Copyright (C) 2017-2019, 2021 AT&T Intellectual Property. All rights reserved.
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
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public abstract class ControlLoopNotification implements Serializable {

    private static final long serialVersionUID = 7538596984567127915L;

    private String closedLoopControlName;
    private String version = "1.0.2";
    private UUID requestId;
    private String closedLoopEventClient;
    private String targetType;
    private String target;
    private String from;
    private String policyScope;
    private String policyName;
    private String policyVersion;
    private ControlLoopNotificationType notification;
    private String message;
    private ZonedDateTime notificationTime = ZonedDateTime.now(ZoneOffset.UTC);
    private Integer opsClTimer;
    private List<ControlLoopOperation> history = new LinkedList<>();

    /**
     * Construct an instance.
     *
     * @param event the event
     */
    protected ControlLoopNotification(ControlLoopEvent event) {
        if (event == null) {
            return;
        }

        this.setClosedLoopControlName(event.getClosedLoopControlName());
        this.setRequestId(event.getRequestId());
        this.setClosedLoopEventClient(event.getClosedLoopEventClient());
        this.setTargetType(event.getTargetType());
        this.setTarget(event.getTarget());
    }
}
