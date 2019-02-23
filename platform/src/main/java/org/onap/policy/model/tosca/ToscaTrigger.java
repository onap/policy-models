/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy Model
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.policy.model.tosca;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Class to represent the trigger of policy type in TOSCA definition
 *
 * @author Chenfei Gao (cgao@research.att.com)
 *
 */
@ToString
public class ToscaTrigger {

    @Getter
    @Setter
    @SerializedName("description")
    private String description;

    @Getter
    @Setter
    @SerializedName("event_type")
    private String eventType;

    @Getter
    @Setter
    @SerializedName("schedule")
    private ToscaTimeInterval schedule;

    @Getter
    @Setter
    @SerializedName("target_filter")
    private ToscaEventFilter targetFilter;

    @Getter
    @Setter
    @SerializedName("condition")
    private ToscaConstraint condition;

    @Getter
    @Setter
    @SerializedName("constraint")
    private ToscaConstraint constraint;

    @Getter
    @Setter
    @SerializedName("period")
    private String period;

    @Getter
    @Setter
    @SerializedName("evaluations")
    private int evaluations;

    @Getter
    @Setter
    @SerializedName("method")
    private String method;

    @Getter
    @Setter
    @SerializedName("action")
    private String action;
}