/*-
 * ============LICENSE_START=======================================================
 * aai
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

package org.onap.policy.aai;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class AaiNqInventoryResponseItem implements Serializable {

    private static final long serialVersionUID = 7142072567154675183L;

    @SerializedName("model-name")
    private String modelName;

    @SerializedName("vf-module")
    private AaiNqVfModule vfModule;

    @SerializedName("service-instance")
    private AaiNqServiceInstance serviceInstance;

    @SerializedName("vserver")
    private AaiNqVServer vserver;

    @SerializedName("tenant")
    private AaiNqTenant tenant;

    @SerializedName("cloud-region")
    private AaiNqCloudRegion cloudRegion;

    @SerializedName("generic-vnf")
    private AaiNqGenericVnf genericVnf;

    @SerializedName("extra-properties")
    private AaiNqExtraProperties extraProperties;

    @SerializedName("inventory-response-items")
    private AaiNqInventoryResponseItems items;

    public String getModelName() {
        return modelName;
    }

    public AaiNqVfModule getVfModule() {
        return vfModule;
    }

    public AaiNqServiceInstance getServiceInstance() {
        return serviceInstance;
    }

    public AaiNqVServer getVserver() {
        return vserver;
    }

    public AaiNqTenant getTenant() {
        return tenant;
    }

    public AaiNqCloudRegion getCloudRegion() {
        return cloudRegion;
    }

    public AaiNqGenericVnf getGenericVnf() {
        return genericVnf;
    }

    public AaiNqExtraProperties getExtraProperties() {
        return extraProperties;
    }

    public AaiNqInventoryResponseItems getItems() {
        return items;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public void setVfModule(AaiNqVfModule vfModule) {
        this.vfModule = vfModule;
    }

    public void setServiceInstance(AaiNqServiceInstance serviceInstance) {
        this.serviceInstance = serviceInstance;
    }

    public void setVserver(AaiNqVServer vserver) {
        this.vserver = vserver;
    }

    public void setTenant(AaiNqTenant tenant) {
        this.tenant = tenant;
    }

    public void setCloudRegion(AaiNqCloudRegion cloudRegion) {
        this.cloudRegion = cloudRegion;
    }

    public void setGenericVnf(AaiNqGenericVnf genericVnf) {
        this.genericVnf = genericVnf;
    }

    public void setExtraProperties(AaiNqExtraProperties extraProperties) {
        this.extraProperties = extraProperties;
    }

    public void setItems(AaiNqInventoryResponseItems items) {
        this.items = items;
    }
}
