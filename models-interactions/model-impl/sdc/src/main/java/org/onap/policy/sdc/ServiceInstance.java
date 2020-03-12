/*-
 * ============LICENSE_START=======================================================
 * sdc
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019-2020 Nordix Foundation.
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

package org.onap.policy.sdc;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.UUID;

import lombok.Data;

@Data
public class ServiceInstance implements Serializable {

    private static final long serialVersionUID = 6285260780966679625L;

    @SerializedName("personaModelUUID")
    private UUID personaModelUuid;

    @SerializedName("serviceUUID")
    private UUID serviceUuid;

    @SerializedName("serviceInstanceUUID")
    private UUID serviceInstanceUuid;

    @SerializedName("widgetModelUUID")
    private UUID widgetModelUuid;

    private String widgetModelVersion;
    private String serviceName;
    private String serviceInstanceName;

    public ServiceInstance() {
        // Empty Constructor
    }

    /**
     * Constructor.
     *
     * @param instance copy object
     */
    public ServiceInstance(ServiceInstance instance) {
        if (instance == null) {
            return;
        }
        this.personaModelUuid = instance.personaModelUuid;
        this.serviceUuid = instance.serviceUuid;
        this.serviceInstanceUuid = instance.serviceInstanceUuid;
        this.widgetModelUuid = instance.widgetModelUuid;
        this.widgetModelVersion = instance.widgetModelVersion;
        this.serviceName = instance.serviceName;
        this.serviceInstanceName = instance.serviceInstanceName;
    }
}
