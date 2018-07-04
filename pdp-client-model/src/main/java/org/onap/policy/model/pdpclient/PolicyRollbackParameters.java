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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * PolicyRollbackParameters.
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2018-06-05T10:23:28.596Z")
public class PolicyRollbackParameters {
    @SerializedName("policyIdentity")
    private PolicyIdentity policyIdentity = null;

    @SerializedName("policyUri")
    private String policyUri = null;

    @SerializedName("policyMetadata")
    private Map<String, String> policyMetadata = null;

    public PolicyRollbackParameters policyIdentity(PolicyIdentity policyIdentity) {
        this.policyIdentity = policyIdentity;
        return this;
    }

    /**
     * Get policyIdentity.
     * 
     * @return policyIdentity
     **/
    @ApiModelProperty(value = "")
    public PolicyIdentity getPolicyIdentity() {
        return policyIdentity;
    }

    public void setPolicyIdentity(PolicyIdentity policyIdentity) {
        this.policyIdentity = policyIdentity;
    }

    public PolicyRollbackParameters policyUri(String policyUri) {
        this.policyUri = policyUri;
        return this;
    }

    /**
     * Get policyUri.
     * 
     * @return policyUri
     **/
    @ApiModelProperty(value = "")
    public String getPolicyUri() {
        return policyUri;
    }

    public void setPolicyUri(String policyUri) {
        this.policyUri = policyUri;
    }

    public PolicyRollbackParameters policyMetadata(Map<String, String> policyMetadata) {
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
    public PolicyRollbackParameters putPolicyMetadataItem(String key, String policyMetadataItem) {
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
        PolicyRollbackParameters policyRollbackParameters = (PolicyRollbackParameters) obj;
        return Objects.equals(this.policyIdentity, policyRollbackParameters.policyIdentity)
                && Objects.equals(this.policyUri, policyRollbackParameters.policyUri)
                && Objects.equals(this.policyMetadata, policyRollbackParameters.policyMetadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(policyIdentity, policyUri, policyMetadata);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class PolicyRollbackParameters {\n");

        sb.append("    policyIdentity: ").append(toIndentedString(policyIdentity)).append("\n");
        sb.append("    policyUri: ").append(toIndentedString(policyUri)).append("\n");
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

