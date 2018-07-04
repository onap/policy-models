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
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Policy.
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2018-06-05T10:23:28.596Z")
public class Policy {
    @SerializedName("policyId")
    private String policyId = null;

    @SerializedName("policyName")
    private String policyName = null;

    @SerializedName("policyVersion")
    private String policyVersion = null;

    @SerializedName("policyFiles")
    private List<File> policyFiles = null;

    @SerializedName("policyMetadata")
    private Map<String, String> policyMetadata = null;

    public Policy policyId(String policyId) {
        this.policyId = policyId;
        return this;
    }

    /**
     * Get policyId.
     * 
     * @return policyId
     **/
    @ApiModelProperty(value = "")
    public String getPolicyId() {
        return policyId;
    }

    public void setPolicyId(String policyId) {
        this.policyId = policyId;
    }

    public Policy policyName(String policyName) {
        this.policyName = policyName;
        return this;
    }

    /**
     * Get policyName.
     * 
     * @return policyName
     **/
    @ApiModelProperty(value = "")
    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public Policy policyVersion(String policyVersion) {
        this.policyVersion = policyVersion;
        return this;
    }

    /**
     * Get policyVersion.
     * 
     * @return policyVersion
     **/
    @ApiModelProperty(value = "")
    public String getPolicyVersion() {
        return policyVersion;
    }

    public void setPolicyVersion(String policyVersion) {
        this.policyVersion = policyVersion;
    }

    public Policy policyFiles(List<File> policyFiles) {
        this.policyFiles = policyFiles;
        return this;
    }

    /**
     * Add a policy files item.
     * 
     * @param policyFilesItem the policyFilesItem to add
     * @return this Policy
     */
    public Policy addPolicyFilesItem(File policyFilesItem) {
        if (this.policyFiles == null) {
            this.policyFiles = new ArrayList<File>();
        }
        this.policyFiles.add(policyFilesItem);
        return this;
    }

    /**
     * Get policyFiles.
     * 
     * @return policyFiles
     **/
    @ApiModelProperty(value = "")
    public List<File> getPolicyFiles() {
        return policyFiles;
    }

    public void setPolicyFiles(List<File> policyFiles) {
        this.policyFiles = policyFiles;
    }

    public Policy policyMetadata(Map<String, String> policyMetadata) {
        this.policyMetadata = policyMetadata;
        return this;
    }

    /**
     * Put a policyMetadataItem.
     * 
     * @param key the key
     * @param policyMetadataItem the policyMetadataItem
     * @return this Policy
     */
    public Policy putPolicyMetadataItem(String key, String policyMetadataItem) {
        if (this.policyMetadata == null) {
            this.policyMetadata = new HashMap<String, String>();
        }
        this.policyMetadata.put(key, policyMetadataItem);
        return this;
    }

    /**
     * Get policyMetadata.
     * 
     * @return policyMetadata
     **/
    @ApiModelProperty(value = "")
    public Map<String, String> getPolicyMetadata() {
        return policyMetadata;
    }

    public void setPolicyMetadata(Map<String, String> policyMetadata) {
        this.policyMetadata = policyMetadata;
    }


    @Override
    public boolean equals(java.lang.Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Policy policy = (Policy) obj;
        return Objects.equals(this.policyId, policy.policyId) && Objects.equals(this.policyName, policy.policyName)
                && Objects.equals(this.policyVersion, policy.policyVersion)
                && Objects.equals(this.policyFiles, policy.policyFiles)
                && Objects.equals(this.policyMetadata, policy.policyMetadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(policyId, policyName, policyVersion, policyFiles, policyMetadata);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Policy {\n");

        sb.append("    policyId: ").append(toIndentedString(policyId)).append("\n");
        sb.append("    policyName: ").append(toIndentedString(policyName)).append("\n");
        sb.append("    policyVersion: ").append(toIndentedString(policyVersion)).append("\n");
        sb.append("    policyFiles: ").append(toIndentedString(policyFiles)).append("\n");
        sb.append("    policyMetadata: ").append(toIndentedString(policyMetadata)).append("\n");
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

