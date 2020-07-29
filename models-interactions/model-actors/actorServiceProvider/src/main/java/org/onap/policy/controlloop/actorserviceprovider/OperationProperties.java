/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.controlloop.actorserviceprovider;

/**
 * Names of properties needed by the Actors defined within this repo. Note: this is not
 * exhaustive, as additional property names may be returned by company-defined Actors.
 */
public class OperationProperties {
    public static final String AAI_MODEL_CLOUD_REGION = "AAI/modelInvariantId/cloudRegion";
    public static final String AAI_MODEL_INVARIANT_GENERIC_VNF = "AAI/modelInvariantId/genericVnf";
    public static final String AAI_MODEL_SERVICE = "AAI/modelInvariantId/service";
    public static final String AAI_MODEL_TENANT = "AAI/modelInvariantId/tenant";
    public static final String AAI_MODEL_VNF = "AAI/modelInvariantId/vnf";
    public static final String AAI_RESOURCE_SERVICE_INSTANCE = "AAI/resourceId/serviceInstanceId";
    public static final String AAI_RESOURCE_VNF = "AAI/resourceId/modelInvariantId/vnf";
    public static final String AAI_PNF = "AAI/pnf";
    public static final String AAI_VSERVER_LINK = "AAI/vserver/link";

    /*
     * These are typically extracted from the event or from the event's enrichment data.
     */
    public static final String ADDITIONAL_EVENT_PARAMS = "event/additionalParams";
    public static final String BANDWIDTH = "bandwidth";
    public static final String BANDWIDTH_CHANGE_TIME = "bandwidthChangeTime";
    public static final String EVENT_PAYLOAD = "event/payload";
    public static final String GENERIC_VNF_ID = "genericVnfId";
    public static final String NETWORK_ID = "networkId";
    public static final String SERVICE_ID = "serviceId";
    public static final String SERVICE_INSTANCE_ID = "serviceInstanceId";
    public static final String VNF_ID = "vnfId";
    public static final String VSERVER_ID = "vserverId";
    public static final String VSERVER_NAME = "vserverName";

    public static final String VF_COUNT = "vfCount";


    private OperationProperties() {
        super();
    }
}
