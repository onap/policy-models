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

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;

/**
 * PolicyFile.
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2018-06-05T10:23:28.596Z")
public class PolicyFile {
    @SerializedName("fileType")
    private String fileType = null;

    @SerializedName("fileContents")
    private String fileContents = null;

    public PolicyFile fileType(String fileType) {
        this.fileType = fileType;
        return this;
    }

    /**
     * Get fileType.
     * 
     * @return fileType
     **/
    @ApiModelProperty(value = "")
    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public PolicyFile fileContents(String fileContents) {
        this.fileContents = fileContents;
        return this;
    }

    /**
     * Get fileContents.
     * 
     * @return fileContents
     **/
    @ApiModelProperty(value = "")
    public String getFileContents() {
        return fileContents;
    }

    public void setFileContents(String fileContents) {
        this.fileContents = fileContents;
    }


    @Override
    public boolean equals(java.lang.Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        PolicyFile policyFile = (PolicyFile) obj;
        return Objects.equals(this.fileType, policyFile.fileType)
                && Objects.equals(this.fileContents, policyFile.fileContents);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileType, fileContents);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class PolicyFile {\n");

        sb.append("    fileType: ").append(toIndentedString(fileType)).append("\n");
        sb.append("    fileContents: ").append(toIndentedString(fileContents)).append("\n");
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

