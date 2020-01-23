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

import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.onap.policy.controlloop.VirtualControlLoopEvent;
import org.onap.policy.sdnc.SdncHealNetworkInfo;
import org.onap.policy.sdnc.SdncHealRequest;
import org.onap.policy.sdnc.SdncHealRequestHeaderInfo;
import org.onap.policy.sdnc.SdncHealRequestInfo;
import org.onap.policy.sdnc.SdncHealServiceInfo;
import org.onap.policy.sdnc.SdncRequest;

public class RerouteManager extends SdncOperationManager {
    public static final String NAME = "Reroute";

    /**
     * Constructs the object.
     *
     * @param actorName name of the actor with which this manager is associated
     */
    public RerouteManager(String actorName) {
        super(actorName, NAME);
    }

    @Override
    protected SdncRequest constructRequest(VirtualControlLoopEvent onset) {
        String serviceInstance = onset.getAai().get("service-instance.service-instance-id");
        if (StringUtils.isBlank(serviceInstance)) {
            throw new IllegalArgumentException("missing enrichment data, service-instance-id");
        }
        SdncHealServiceInfo serviceInfo = new SdncHealServiceInfo();
        serviceInfo.setServiceInstanceId(serviceInstance);

        String networkId = onset.getAai().get("network-information.network-id");
        if (StringUtils.isBlank(networkId)) {
            throw new IllegalArgumentException("missing enrichment data, network-id");
        }
        SdncHealNetworkInfo networkInfo = new SdncHealNetworkInfo();
        networkInfo.setNetworkId(networkId);

        SdncHealRequestInfo requestInfo = new SdncHealRequestInfo();
        requestInfo.setRequestAction("ReoptimizeSOTNInstance");

        SdncHealRequestHeaderInfo headerInfo = new SdncHealRequestHeaderInfo();
        headerInfo.setSvcAction("reoptimize");
        headerInfo.setSvcRequestId(UUID.randomUUID().toString());

        SdncRequest request = new SdncRequest();
        request.setNsInstanceId(serviceInstance);
        request.setRequestId(onset.getRequestId());
        request.setUrl("/GENERIC-RESOURCE-API:network-topology-operation");

        SdncHealRequest healRequest = new SdncHealRequest();
        healRequest.setRequestHeaderInfo(headerInfo);
        healRequest.setNetworkInfo(networkInfo);
        healRequest.setRequestInfo(requestInfo);
        healRequest.setServiceInfo(serviceInfo);
        request.setHealRequest(healRequest);
        return request;
    }
}
