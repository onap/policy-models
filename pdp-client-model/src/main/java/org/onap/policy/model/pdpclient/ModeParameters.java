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
 * ModeParameters.
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2018-06-05T10:23:28.596Z")
public class ModeParameters {
    /**
     * Gets or Sets mode.
     */
    @JsonAdapter(ModeEnum.Adapter.class)
    public enum ModeEnum {
        ACTIVE("active"),

        SAFE("safe"),

        TEST("test");

        private String value;

        ModeEnum(String value) {
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
         * Convert from String value to ModeEnum.
         * 
         * @param text the String value
         * @return the corresponding ModeEnum
         */
        public static ModeEnum fromValue(String text) {
            for (ModeEnum b : ModeEnum.values()) {
                if (String.valueOf(b.value).equals(text)) {
                    return b;
                }
            }
            return null;
        }

        public static class Adapter extends TypeAdapter<ModeEnum> {
            @Override
            public void write(final JsonWriter jsonWriter, final ModeEnum enumeration) throws IOException {
                jsonWriter.value(enumeration.getValue());
            }

            @Override
            public ModeEnum read(final JsonReader jsonReader) throws IOException {
                String value = jsonReader.nextString();
                return ModeEnum.fromValue(String.valueOf(value));
            }
        }
    }

    @SerializedName("mode")
    private ModeEnum mode = null;

    public ModeParameters mode(ModeEnum mode) {
        this.mode = mode;
        return this;
    }

    /**
     * Get mode.
     * 
     * @return mode
     **/
    @ApiModelProperty(required = true, value = "")
    public ModeEnum getMode() {
        return mode;
    }

    public void setMode(ModeEnum mode) {
        this.mode = mode;
    }


    @Override
    public boolean equals(java.lang.Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ModeParameters modeParameters = (ModeParameters) obj;
        return Objects.equals(this.mode, modeParameters.mode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mode);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ModeParameters {\n");

        sb.append("    mode: ").append(toIndentedString(mode)).append("\n");
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

