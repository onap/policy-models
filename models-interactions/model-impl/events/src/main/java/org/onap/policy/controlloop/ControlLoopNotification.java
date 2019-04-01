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
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public abstract class ControlLoopNotification implements Serializable {

    private static final long serialVersionUID = 7538596984567127915L;

    private String closedLoopControlName;
    private String version = "1.0.2";
    private UUID requestId;
    private String closedLoopEventClient;
    private ControlLoopTargetType targetType;
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

    public ControlLoopNotification() {

    }

    /**
     * Construct an instance.
     *
     * @param event the event
     */
    public ControlLoopNotification(ControlLoopEvent event) {
        if (event == null) {
            return;
        }

        this.setClosedLoopControlName(event.getClosedLoopControlName());
        this.setRequestId(event.getRequestId());
        this.setClosedLoopEventClient(event.getClosedLoopEventClient());
        this.setTargetType(event.getTargetType());
        this.setTarget(event.getTarget());
    }

    public String getClosedLoopControlName() {
        return closedLoopControlName;
    }

    public void setClosedLoopControlName(String closedLoopControlName) {
        this.closedLoopControlName = closedLoopControlName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public UUID getRequestId() {
        return requestId;
    }

    public void setRequestId(UUID requestId) {
        this.requestId = requestId;
    }

    public String getClosedLoopEventClient() {
        return closedLoopEventClient;
    }

    public void setClosedLoopEventClient(String closedLoopEventClient) {
        this.closedLoopEventClient = closedLoopEventClient;
    }

    public ControlLoopTargetType getTargetType() {
        return targetType;
    }

    public void setTargetType(ControlLoopTargetType targetType) {
        this.targetType = targetType;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getPolicyScope() {
        return policyScope;
    }

    public void setPolicyScope(String policyScope) {
        this.policyScope = policyScope;
    }

    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public String getPolicyVersion() {
        return policyVersion;
    }

    public void setPolicyVersion(String policyVersion) {
        this.policyVersion = policyVersion;
    }

    public ControlLoopNotificationType getNotification() {
        return notification;
    }

    public void setNotification(ControlLoopNotificationType notification) {
        this.notification = notification;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ZonedDateTime getNotificationTime() {
        return notificationTime;
    }

    public void setNotificationTime(ZonedDateTime notificationTime) {
        this.notificationTime = notificationTime;
    }

    public Integer getOpsClTimer() {
        return opsClTimer;
    }

    public void setOpsClTimer(Integer opsClTimer) {
        this.opsClTimer = opsClTimer;
    }

    public List<ControlLoopOperation> getHistory() {
        return history;
    }

    public void setHistory(List<ControlLoopOperation> history) {
        this.history = history;
    }
}
