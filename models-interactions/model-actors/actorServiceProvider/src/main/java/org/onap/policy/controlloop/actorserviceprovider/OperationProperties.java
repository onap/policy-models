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

    /**
     * A&AI VNF id for the target resource ID. Obtained as follows:
     * <ol>
     * <li>using the target resource ID, invoke the custom query
     * getGenericVnfByModelInvariantId() method to get the generic VNF</li>
     * <li>invoke the generic VNF getVnfId() method</li>
     * </ol>
     */
    public static final String AAI_RESOURCE_VNF = "AAI/resourceId/modelInvariantId/vnf";

    public static final String AAI_PNF = "AAI/pnf";

    /**
     * A&AI link to the vserver. Obtained as follows:
     * <ol>
     * <li>using the vserver name from the enrichment data, perform an A&AI tenant
     * query</li>
     * <li>get the "result-data" field from the tenant output</li>
     * <li>get the "resource-link" field from that</li>
     * <li>strip off the "/aai/v16" prefix</li>
     * </ol>
     */
    public static final String AAI_VSERVER_LINK = "AAI/vserver/link";

    /*
     * These are typically extracted from the event or from the event's enrichment data.
     */
    public static final String ENRICHMENT_BANDWIDTH = "enrichment/bandwidth";
    public static final String ENRICHMENT_BANDWIDTH_CHANGE_TIME = "enrichment/bandwidth-change-time";
    public static final String ENRICHMENT_GENERIC_VNF_ID = "enrichment/genericVnf/id";
    public static final String ENRICHMENT_NETWORK_ID = "enrichment/network/id";
    public static final String ENRICHMENT_SERVICE_ID = "enrichment/service/id";
    public static final String ENRICHMENT_SERVICE_INSTANCE_ID = "enrichment/service-instance.service-instance-id";
    public static final String ENRICHMENT_VNF_ID = "enrichment/vnfId";
    public static final String ENRICHMENT_VSERVER_ID = "enrichment/vserver/id";
    public static final String ENRICHMENT_VSERVER_NAME = "enrichment/vserver/name";

    public static final String EVENT_ADDITIONAL_PARAMS = "event/additionalParams";
    public static final String EVENT_PAYLOAD = "event/payload";

    /*
     * These are data computed and/or tracked by the invoker.
     */
    public static final String DATA_VF_COUNT = "data/vfCount";


    private OperationProperties() {
        super();
    }
}
