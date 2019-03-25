/*-
 * ============LICENSE_START=======================================================
 * so
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
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
 * ============LICENSE_END=========================================================
 */

package org.onap.policy.so;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class SoModelInfo implements Serializable {

    private static final long serialVersionUID = -3283942659786236032L;

    @SerializedName("modelType")
    private String modelType;

    @SerializedName("modelInvariantId")
    private String modelInvariantId;

    @SerializedName("modelVersionId")
    private String modelVersionId;

    @SerializedName("modelName")
    private String modelName;

    @SerializedName("modelVersion")
    private String modelVersion;

    @SerializedName("modelCustomizationName")
    private String modelCustomizationName;

    @SerializedName("modelCustomizationId")
    private String modelCustomizationId;

    public SoModelInfo() {
      //required by author
    }

    public String getModelCustomizationId() {
        return modelCustomizationId;
    }

    public String getModelCustomizationName() {
        return modelCustomizationName;
    }

    public String getModelInvariantId() {
        return modelInvariantId;
    }

    public String getModelName() {
        return modelName;
    }

    public String getModelType() {
        return modelType;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public String getModelVersionId() {
        return modelVersionId;
    }

    public void setModelCustomizationId(String modelCustomizationId) {
        this.modelCustomizationId = modelCustomizationId;
    }

    public void setModelCustomizationName(String modelCustomizationName) {
        this.modelCustomizationName = modelCustomizationName;
    }

    public void setModelInvariantId(String modelInvariantId) {
        this.modelInvariantId = modelInvariantId;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public void setModelType(String modelType) {
        this.modelType = modelType;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    public void setModelVersionId(String modelVersionId) {
        this.modelVersionId = modelVersionId;
    }

}
