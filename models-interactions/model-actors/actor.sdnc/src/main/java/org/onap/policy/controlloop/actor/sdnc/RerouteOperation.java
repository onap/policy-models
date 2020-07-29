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

package org.onap.policy.controlloop.actor.sdnc;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.onap.policy.controlloop.actorserviceprovider.OperationProperties;
import org.onap.policy.controlloop.actorserviceprovider.controlloop.ControlLoopEventContext;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpConfig;
import org.onap.policy.sdnc.SdncHealNetworkInfo;
import org.onap.policy.sdnc.SdncHealRequest;
import org.onap.policy.sdnc.SdncHealRequestHeaderInfo;
import org.onap.policy.sdnc.SdncHealRequestInfo;
import org.onap.policy.sdnc.SdncHealServiceInfo;
import org.onap.policy.sdnc.SdncRequest;

public class RerouteOperation extends SdncOperation {
    public static final String NAME = "Reroute";

    // fields in the enrichment data
    public static final String SERVICE_ID_KEY = "service-instance.service-instance-id";
    public static final String NETWORK_ID_KEY = "network-information.network-id";

    // @formatter:off
    private static final List<String> PROPERTY_NAMES = List.of(
                            OperationProperties.ENRICHMENT_SERVICE_ID,
                            OperationProperties.ENRICHMENT_NETWORK_ID);
    // @formatter:on

    /**
     * Constructs the object.
     *
     * @param params operation parameters
     * @param config configuration for this operation
     */
    public RerouteOperation(ControlLoopOperationParams params, HttpConfig config) {
        super(params, config, PROPERTY_NAMES);
    }

    @Override
    protected SdncRequest makeRequest(int attempt) {
        ControlLoopEventContext context = params.getContext();

        String serviceInstance = context.getEnrichment().get(SERVICE_ID_KEY);
        if (StringUtils.isBlank(serviceInstance)) {
            throw new IllegalArgumentException("missing enrichment data, " + SERVICE_ID_KEY);
        }
        SdncHealServiceInfo serviceInfo = new SdncHealServiceInfo();
        serviceInfo.setServiceInstanceId(serviceInstance);

        String networkId = context.getEnrichment().get(NETWORK_ID_KEY);
        if (StringUtils.isBlank(networkId)) {
            throw new IllegalArgumentException("missing enrichment data, " + NETWORK_ID_KEY);
        }
        SdncHealNetworkInfo networkInfo = new SdncHealNetworkInfo();
        networkInfo.setNetworkId(networkId);

        SdncHealRequestInfo requestInfo = new SdncHealRequestInfo();
        requestInfo.setRequestAction("ReoptimizeSOTNInstance");

        SdncHealRequestHeaderInfo headerInfo = new SdncHealRequestHeaderInfo();
        headerInfo.setSvcAction("reoptimize");
        headerInfo.setSvcRequestId(getSubRequestId());

        SdncRequest request = new SdncRequest();
        request.setNsInstanceId(serviceInstance);
        request.setRequestId(params.getRequestId());
        request.setUrl("/" + getPath());

        SdncHealRequest healRequest = new SdncHealRequest();
        healRequest.setRequestHeaderInfo(headerInfo);
        healRequest.setNetworkInfo(networkInfo);
        healRequest.setRequestInfo(requestInfo);
        healRequest.setServiceInfo(serviceInfo);
        request.setHealRequest(healRequest);
        return request;
    }
}
