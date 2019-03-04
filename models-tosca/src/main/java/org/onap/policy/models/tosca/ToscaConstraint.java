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

package org.onap.policy.models.tosca;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Class to represent the Constraint of property in TOSCA definition.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 *
 */
@ToString
public class ToscaConstraint {

    @Getter
    @Setter
    @SerializedName("equal")
    private String equal;

    @Getter
    @Setter
    @SerializedName("greater_than")
    private String greaterThan;

    @Getter
    @Setter
    @SerializedName("greater_or_equal")
    private String greaterOrEqual;

    @Getter
    @Setter
    @SerializedName("less_than")
    private String lessThan;

    @Getter
    @Setter
    @SerializedName("less_or_equal")
    private String lessOrEqual;

    @Getter
    @Setter
    @SerializedName("in_range")
    private String inRange;

    @Getter
    @Setter
    @SerializedName("valid_values")
    private String validValues;

    @Getter
    @Setter
    @SerializedName("length")
    private String length;

    @Getter
    @Setter
    @SerializedName("min_length")
    private String minLength;

    @Getter
    @Setter
    @SerializedName("max_length")
    private String maxLength;

    @Getter
    @Setter
    @SerializedName("pattern")
    private String pattern;
}