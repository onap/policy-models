/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy Model
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2020-2021 Nordix Foundation.
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * Class to represent TOSCA topology template matching input/output from/to client.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 */
@Data
@NoArgsConstructor
public class ToscaTopologyTemplate {
    private String description;

    private Map<String, ToscaParameter> inputs;

    @SerializedName("node_templates")
    private Map<String, ToscaNodeTemplate> nodeTemplates;

    private List<Map<String, ToscaPolicy>> policies;

    public Map<ToscaEntityKey, ToscaPolicy> getPoliciesAsMap() {
        return ToscaEntity.getEntityListMapAsMap(policies);
    }

    /**
     * Copy constructor.
     *
     * @param copyObject the obejct to copy from.
     */
    public ToscaTopologyTemplate(@NonNull ToscaTopologyTemplate copyObject) {
        this.description = copyObject.description;

        // @formatter:off
        this.inputs        = (copyObject.inputs        != null ? new LinkedHashMap<>(copyObject.inputs)        : null);
        this.nodeTemplates = (copyObject.nodeTemplates != null ? new LinkedHashMap<>(copyObject.nodeTemplates) : null);
        this.policies      = (copyObject.policies      != null ? new ArrayList<>(copyObject.policies)          : null);
        // @formatter:on
    }
}
