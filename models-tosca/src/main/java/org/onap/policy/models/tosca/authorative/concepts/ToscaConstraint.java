/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy Model
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019-2021 Nordix Foundation.
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

package org.onap.policy.models.tosca.authorative.concepts;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import lombok.Data;

/**
 * Class to represent TOSCA constraint matching input/output from/to client.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 */
@Data
public class ToscaConstraint {
    @SerializedName("valid_values")
    private List<String> validValues;

    private String equal;

    @SerializedName("greater_than")
    private String greaterThan;

    @SerializedName("greater_or_equal")
    private String greaterOrEqual;

    @SerializedName("less_than")
    private String lessThan;

    @SerializedName("less_or_equal")
    private String lessOrEqual;

    @SerializedName("in_range")
    private List<String> rangeValues;
}
