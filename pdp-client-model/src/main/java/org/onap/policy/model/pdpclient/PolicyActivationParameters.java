/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2018 Ericsson. All rights reserved.
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

package org.onap.policy.model.pdpclient;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.swagger.annotations.ApiModelProperty;
import java.io.IOException;
import java.util.Objects;

/**
 * PolicyActivationParameters.
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2018-06-05T10:23:28.596Z")
public class PolicyActivationParameters {
    @SerializedName("policy")
    private PolicyIdentity policy = null;

    /**
     * Gets or Sets activiationMode.
     */
    @JsonAdapter(ActiviationModeEnum.Adapter.class)
    public enum ActiviationModeEnum {
        LIVE("live"),

        SAFE("safe");

        private String value;

        ActiviationModeEnum(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }

        /**
         * Convert from a String value to a ActiviationModeEnum.
         * 
         * @param text the String value
         * @return the corresponding ActiviationModeEnum
         */
        public static ActiviationModeEnum fromValue(String text) {
            for (ActiviationModeEnum b : ActiviationModeEnum.values()) {
                if (String.valueOf(b.value).equals(text)) {
                    return b;
                }
            }
            return null;
        }

        public static class Adapter extends TypeAdapter<ActiviationModeEnum> {
            @Override
            public void write(final JsonWriter jsonWriter, final ActiviationModeEnum enumeration) throws IOException {
                jsonWriter.value(enumeration.getValue());
            }

            @Override
            public ActiviationModeEnum read(final JsonReader jsonReader) throws IOException {
                String value = jsonReader.nextString();
                return ActiviationModeEnum.fromValue(String.valueOf(value));
            }
        }
    }

    @SerializedName("activiationMode")
    private ActiviationModeEnum activiationMode = null;

    public PolicyActivationParameters policy(PolicyIdentity policy) {
        this.policy = policy;
        return this;
    }

    /**
     * Get policy.
     * 
     * @return policy
     **/
    @ApiModelProperty(value = "")
    public PolicyIdentity getPolicy() {
        return policy;
    }

    public void setPolicy(PolicyIdentity policy) {
        this.policy = policy;
    }

    public PolicyActivationParameters activiationMode(ActiviationModeEnum activiationMode) {
        this.activiationMode = activiationMode;
        return this;
    }

    /**
     * Get activiationMode.
     * 
     * @return activiationMode
     **/
    @ApiModelProperty(value = "")
    public ActiviationModeEnum getActiviationMode() {
        return activiationMode;
    }

    public void setActiviationMode(ActiviationModeEnum activiationMode) {
        this.activiationMode = activiationMode;
    }


    @Override
    public boolean equals(java.lang.Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        PolicyActivationParameters policyActivationParameters = (PolicyActivationParameters) obj;
        return Objects.equals(this.policy, policyActivationParameters.policy)
                && Objects.equals(this.activiationMode, policyActivationParameters.activiationMode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(policy, activiationMode);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class PolicyActivationParameters {\n");

        sb.append("    policy: ").append(toIndentedString(policy)).append("\n");
        sb.append("    activiationMode: ").append(toIndentedString(activiationMode)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces (except the first
     * line).
     */
    private String toIndentedString(java.lang.Object obj) {
        if (obj == null) {
            return "null";
        }
        return obj.toString().replace("\n", "\n    ");
    }

}

