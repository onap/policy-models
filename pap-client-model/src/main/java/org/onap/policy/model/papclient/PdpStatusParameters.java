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

package org.onap.policy.model.papclient;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.swagger.annotations.ApiModelProperty;
import java.io.IOException;
import java.util.Objects;

/**
 * PdpStatusParameters.
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2018-06-13T16:07:10.810Z")
public class PdpStatusParameters {
    @SerializedName("pdpName")
    private String pdpName = null;

    @SerializedName("pdpVersion")
    private String pdpVersion = null;

    /**
     * Gets or Sets pdpState.
     */
    @JsonAdapter(PdpStateEnum.Adapter.class)
    public enum PdpStateEnum {
        PASSIVE("passive"),

        ACTIVE("active"),

        SAFE("safe"),

        TEST("test");

        private String value;

        PdpStateEnum(String value) {
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
         * Convert from String value to PdpStateEnum.
         * 
         * @param text the String value
         * @return the corresponding PdpStateEnum
         */
        public static PdpStateEnum fromValue(String text) {
            for (PdpStateEnum b : PdpStateEnum.values()) {
                if (String.valueOf(b.value).equals(text)) {
                    return b;
                }
            }
            return null;
        }

        public static class Adapter extends TypeAdapter<PdpStateEnum> {
            @Override
            public void write(final JsonWriter jsonWriter, final PdpStateEnum enumeration) throws IOException {
                jsonWriter.value(enumeration.getValue());
            }

            @Override
            public PdpStateEnum read(final JsonReader jsonReader) throws IOException {
                String value = jsonReader.nextString();
                return PdpStateEnum.fromValue(String.valueOf(value));
            }
        }
    }

    @SerializedName("pdpState")
    private PdpStateEnum pdpState = null;

    @SerializedName("pdpType")
    private String pdpType = null;

    @SerializedName("pdpGroupName")
    private String pdpGroupName = null;

    @SerializedName("pdpEndpoint")
    private String pdpEndpoint = null;

    public PdpStatusParameters pdpName(String pdpName) {
        this.pdpName = pdpName;
        return this;
    }

    /**
     * Get pdpName.
     * 
     * @return pdpName
     **/
    @ApiModelProperty(value = "")
    public String getPdpName() {
        return pdpName;
    }

    public void setPdpName(String pdpName) {
        this.pdpName = pdpName;
    }

    public PdpStatusParameters pdpVersion(String pdpVersion) {
        this.pdpVersion = pdpVersion;
        return this;
    }

    /**
     * Get pdpName.
     * 
     * @return pdpName
     **/
    @ApiModelProperty(value = "")
    public String getPdpVersion() {
        return pdpVersion;
    }

    public void setPdpVersion(String pdpVersion) {
        this.pdpVersion = pdpVersion;
    }

    public PdpStatusParameters pdpState(PdpStateEnum pdpState) {
        this.pdpState = pdpState;
        return this;
    }

    /**
     * Get pdpState.
     * 
     * @return pdpState
     **/
    @ApiModelProperty(value = "")
    public PdpStateEnum getPdpState() {
        return pdpState;
    }

    public void setPdpState(PdpStateEnum pdpState) {
        this.pdpState = pdpState;
    }

    public PdpStatusParameters pdpType(String pdpType) {
        this.pdpType = pdpType;
        return this;
    }

    /**
     * Get pdpType.
     * 
     * @return pdpType
     **/
    @ApiModelProperty(value = "")
    public String getPdpType() {
        return pdpType;
    }

    public void setPdpType(String pdpType) {
        this.pdpType = pdpType;
    }

    public PdpStatusParameters pdpGroupName(String pdpGroupName) {
        this.pdpGroupName = pdpGroupName;
        return this;
    }

    /**
     * Get pdpGroupName.
     * 
     * @return pdpGroupName
     **/
    @ApiModelProperty(value = "")
    public String getPdpGroupName() {
        return pdpGroupName;
    }

    public void setPdpGroupName(String pdpGroupName) {
        this.pdpGroupName = pdpGroupName;
    }

    public PdpStatusParameters pdpEndpoint(String pdpEndpoint) {
        this.pdpEndpoint = pdpEndpoint;
        return this;
    }

    /**
     * Get pdpEndpoint.
     * 
     * @return pdpEndpoint
     **/
    @ApiModelProperty(value = "")
    public String getPdpEndpoint() {
        return pdpEndpoint;
    }

    public void setPdpEndpoint(String pdpEndpoint) {
        this.pdpEndpoint = pdpEndpoint;
    }


    @Override
    public boolean equals(java.lang.Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        PdpStatusParameters pdpStatusParameters = (PdpStatusParameters) obj;
        return Objects.equals(this.pdpName, pdpStatusParameters.pdpName)
                && Objects.equals(this.pdpState, pdpStatusParameters.pdpState)
                && Objects.equals(this.pdpType, pdpStatusParameters.pdpType)
                && Objects.equals(this.pdpGroupName, pdpStatusParameters.pdpGroupName)
                && Objects.equals(this.pdpEndpoint, pdpStatusParameters.pdpEndpoint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pdpName, pdpState, pdpType, pdpGroupName, pdpEndpoint);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class PdpStatusParameters {\n");

        sb.append("    pdpName: ").append(toIndentedString(pdpName)).append("\n");
        sb.append("    pdpState: ").append(toIndentedString(pdpState)).append("\n");
        sb.append("    pdpType: ").append(toIndentedString(pdpType)).append("\n");
        sb.append("    pdpGroupName: ").append(toIndentedString(pdpGroupName)).append("\n");
        sb.append("    pdpEndpoint: ").append(toIndentedString(pdpEndpoint)).append("\n");
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

