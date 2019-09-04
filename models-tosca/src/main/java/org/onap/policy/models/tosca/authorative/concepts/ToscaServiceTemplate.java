/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy Model
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.policy.models.tosca.authorative.concepts;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Class to represent TOSCA service template matching input/output from/to client.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ToscaServiceTemplate extends ToscaEntity {

    @ApiModelProperty(name = "tosca_definitions_version")
    @SerializedName("tosca_definitions_version")
    private String toscaDefinitionsVersion;

    @ApiModelProperty(name = "topology_template")
    @SerializedName("topology_template")
    private ToscaTopologyTemplate toscaTopologyTemplate;

    @ApiModelProperty(name = "policy_types")
    @SerializedName("policy_types")
    private Map<String, ToscaPolicyType> policyTypes;

    @ApiModelProperty(name = "data_types")
    @SerializedName("data_types")
    private Map<String, ToscaDataType> dataTypes;

    public Map<ToscaEntityKey, ToscaPolicyType> getPolicyTypesAsMap() {
        return ToscaEntity.getEntityMapAsMap(policyTypes);
    }

    public Map<ToscaEntityKey, ToscaDataType> getDataTypesAsMap() {
        return ToscaEntity.getEntityMapAsMap(dataTypes);
    }
}