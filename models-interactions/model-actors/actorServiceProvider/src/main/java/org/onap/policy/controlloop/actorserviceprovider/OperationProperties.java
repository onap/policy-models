/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020-2021 AT&T Intellectual Property. All rights reserved.
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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Names of properties needed by the Actors defined within this repo. Note: this is not
 * exhaustive, as additional property names may be returned by company-defined Actors.
 * <p/>
 * Note: any time a property is added, applications using the actors must be updated to
 * provide the property's value when requested.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OperationProperties {

    /**
     * A&AI Default Cloud Region. Obtained as follows:
     * <ol>
     * <li>invoke the custom query getDefaultCloudRegion() method</li>
     * </ol>
     */
    public static final String AAI_DEFAULT_CLOUD_REGION = "AAI/defaultCloudRegion";

    /**
     * A&AI Default Tenant. Obtained as follows:
     * <ol>
     * <li>invoke the custom query getDefaultTenant() method</li>
     * </ol>
     */
    public static final String AAI_DEFAULT_TENANT = "AAI/defaultTenant";

    /**
     * A&AI PNF. Obtained as follows:
     * <ol>
     * <li>using the target entity, invoke AaiGetPnfOperation</li>
     * </ol>
     */
    public static final String AAI_PNF = "AAI/pnf";

    /**
     * A&AI [Generic] VNF for the target resource ID. Obtained as follows:
     * <ol>
     * <li>using the target resource ID, invoke the custom query
     * getGenericVnfByModelInvariantId() method to get the generic VNF</li>
     * </ol>
     */
    public static final String AAI_RESOURCE_VNF = "AAI/resourceId/vnf";

    /**
     * A&AI Service instance. Obtained as follows:
     * <ol>
     * <li>invoke the custom query getServiceInstance() method</li>
     * </ol>
     */
    public static final String AAI_SERVICE = "AAI/service";

    /**
     * A&AI Service model. Obtained as follows:
     * <ol>
     * <li>invoke the custom query getServiceInstance() method</li>
     * <li>using the service instance, invoke the getModelVersionId() method</li>
     * </ol>
     */
    public static final String AAI_SERVICE_MODEL = "AAI/service/model";

    /**
     * A&AI Target Entity. This is a String that can typically be found in the enrichment
     * data, depending on the Target type. Sometimes, however, it must be retrieved via an
     * A&AI query.
     */
    public static final String AAI_TARGET_ENTITY = "AAI/targetEntity";

    /**
     * A&AI VNF. Obtained as follows:
     * <ol>
     * <li>using the target model invariant ID, invoke the custom query
     * getGenericVnfByModelInvariantId() method to get the VNF</li>
     * <li>using the VNF item, invoke the getModelVersionId() method to get the
     * version</li>
     * </ol>
     */
    public static final String AAI_VNF = "AAI/vnf";

    /**
     * A&AI VNF Model. Obtained as follows:
     * <ol>
     * <li>using the target model invariant ID, invoke the custom query
     * getGenericVnfByModelInvariantId() method to get the VNF</li>
     * <li>using the VNF item, invoke the getModelVersionId() method to get the
     * version</li>
     * <li>using the version, invoke the custom query getModelVerByVersionId() method</li>
     * </ol>
     */
    public static final String AAI_VNF_MODEL = "AAI/vnf/model";

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

    /**
     * Optional A&AI properties (Map-String-String) for CDS GRPC. If an application
     * provides this, it will be used instead of constructing the map from the other
     * properties.
     */
    public static final String OPT_CDS_GRPC_AAI_PROPERTIES = "cds/grpc/aai/properties";

    /*
     * These are typically extracted from the event or from the event's enrichment data.
     *
     * NOTE: all of the values must be of the form "enrichment/{enrichment-field-name}".
     */
    public static final String ENRICHMENT_BANDWIDTH = "enrichment/bandwidth";
    public static final String ENRICHMENT_BANDWIDTH_CHANGE_TIME = "enrichment/bandwidth-change-time";
    public static final String ENRICHMENT_GENERIC_VNF_ID = "enrichment/generic-vnf.vnf-id";
    public static final String ENRICHMENT_NETWORK_ID = "enrichment/network-information.network-id";
    public static final String ENRICHMENT_SERVICE_ID = "enrichment/service-instance.service-instance-id";
    public static final String ENRICHMENT_VNF_ID = "enrichment/vnfId";
    public static final String ENRICHMENT_VSERVER_ID = "enrichment/vserver.vserver-id";
    public static final String ENRICHMENT_VSERVER_NAME = "enrichment/vserver.vserver-name";

    public static final String EVENT_ADDITIONAL_PARAMS = "event/additionalParams";
    public static final String EVENT_PAYLOAD = "event/payload";

    /*
     * These are data computed and/or tracked by the invoker.
     */

    /**
     * An Integer containing the count.
     */
    public static final String DATA_VF_COUNT = "data/vfCount";
}
