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
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * Class to represent TOSCA service template matching input/output from/to client.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ToscaServiceTemplate extends ToscaEntity {
    @SerializedName("tosca_definitions_version")
    private String toscaDefinitionsVersion;

    @SerializedName("data_types")
    private Map<String, ToscaDataType> dataTypes;

    @SerializedName("capability_types")
    private Map<String, ToscaCapabilityType> capabilityTypes;

    @SerializedName("node_types")
    private Map<String, ToscaNodeType> nodeTypes;

    @SerializedName("relationship_types")
    private Map<String, ToscaRelationshipType> relationshipTypes;

    @SerializedName("policy_types")
    private Map<String, ToscaPolicyType> policyTypes;

    @SerializedName("topology_template")
    private ToscaTopologyTemplate toscaTopologyTemplate;

    public Map<ToscaEntityKey, ToscaDataType> getDataTypesAsMap() {
        return ToscaEntity.getEntityMapAsMap(dataTypes);
    }

    public Map<ToscaEntityKey, ToscaPolicyType> getPolicyTypesAsMap() {
        return ToscaEntity.getEntityMapAsMap(policyTypes);
    }

    /**
     * Copy constructor.
     *
     * @param copyObject the obejct to copy from.
     */
    public ToscaServiceTemplate(@NonNull ToscaServiceTemplate copyObject) {
        super(copyObject);

        this.toscaDefinitionsVersion = copyObject.toscaDefinitionsVersion;

        // @formatter:off
        this.dataTypes         = (copyObject.dataTypes         != null
                ? new LinkedHashMap<>(copyObject.dataTypes)
                : null);
        this.capabilityTypes   = (copyObject.capabilityTypes   != null
                ? new LinkedHashMap<>(copyObject.capabilityTypes)
                : null);
        this.nodeTypes         = (copyObject.nodeTypes         != null
                ? new LinkedHashMap<>(copyObject.nodeTypes)
                : null);
        this.relationshipTypes = (copyObject.relationshipTypes != null
                ? new LinkedHashMap<>(copyObject.relationshipTypes)
                : null);
        this.policyTypes       = (copyObject.policyTypes       != null
                ? new LinkedHashMap<>(copyObject.policyTypes)
                : null);
        // @formatter:on

        this.toscaTopologyTemplate =
                (copyObject.toscaTopologyTemplate != null ? new ToscaTopologyTemplate(copyObject.toscaTopologyTemplate)
                        : null);
    }
}
