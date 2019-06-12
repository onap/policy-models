/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2019 Bell Canada. All rights reserved.
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

package org.onap.policy.controlloop.actor.cds.beans;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import java.util.Map;
import org.onap.policy.controlloop.actor.cds.constants.CdsActorConstants;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"aai-properties", "config-deploy-properties"})
public class ConfigDeployRequest {

    @JsonProperty("config-deploy-properties")
    private Map<String, String> configDeployProperties;

    @JsonProperty("aai-properties")
    private Map<String, String> aaiProperties;

    public Map<String, String> getConfigDeployProperties() {
        return configDeployProperties;
    }

    public void setConfigDeployProperties(final Map<String, String> configDeployProperties) {
        this.configDeployProperties = configDeployProperties;
    }

    public Map<String, String> getAaiProperties() {
        return aaiProperties;
    }

    public void setAaiProperties(final Map<String, String> aaiProperties) {
        this.aaiProperties = aaiProperties;
    }

    @Override
    public String toString() {
        try {
            validateForCdsMandatoryParams();
            return "{\"config-assign-request\":" + new ObjectMapper().writeValueAsString(this) + '}';
        } catch (JsonProcessingException e) {
            return "";
        }
    }

    /**
     * Ensure that the policy payload is supplying the CDS blueprint mandatory parameters.
     */
    private void validateForCdsMandatoryParams() {
        Preconditions.checkState(configDeployProperties.containsKey(CdsActorConstants.KEY_CBA_NAME), "Missing CDS "
            + "blueprint name: {}", CdsActorConstants.KEY_CBA_NAME);
        Preconditions.checkState(configDeployProperties.containsKey(CdsActorConstants.KEY_CBA_VERSION), "Missing CDS "
            + "blueprint name: {}", CdsActorConstants.KEY_CBA_VERSION);
    }
}
