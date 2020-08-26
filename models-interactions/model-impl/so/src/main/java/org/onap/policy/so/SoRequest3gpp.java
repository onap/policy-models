/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020 Wipro Limited.
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

import java.io.Serializable;
import java.util.Map;

public class SoRequest3gpp implements Serializable {

    private static final long serialVersionUID = -3283942659786236032L;

    private String name;
    private String serviceInstanceID;
    private String globalSubscriberId;
    private String subscriptionServiceType;
    private String networkType;
    private Map<String, Object> additionalProperties;

    public SoRequest3gpp() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getServiceInstanceID() {
        return serviceInstanceID;
    }

    public void setServiceInstanceID(String serviceInstanceID) {
        this.serviceInstanceID = serviceInstanceID;
    }

    public String getGlobalSubscriberId() {
        return globalSubscriberId;
    }

    public void setGlobalSubscriberId(String globalSubscriberId) {
        this.globalSubscriberId = globalSubscriberId;
    }

    public String getSubscriptionServiceType() {
        return subscriptionServiceType;
    }

    public void setSubscriptionServiceType(String subscriptionServiceType) {
        this.subscriptionServiceType = subscriptionServiceType;
    }

    public String getNetworkType() {
        return networkType;
    }

    public void setNetworkType(String networkType) {
        this.networkType = networkType;
    }

    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }

    public void setAdditionalProperties(Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }
}
