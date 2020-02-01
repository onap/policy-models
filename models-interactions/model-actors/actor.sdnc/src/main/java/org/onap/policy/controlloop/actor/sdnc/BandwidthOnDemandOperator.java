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
import org.onap.policy.sdnc.SdncHealRequest;
import org.onap.policy.sdnc.SdncHealRequestHeaderInfo;
import org.onap.policy.sdnc.SdncHealRequestInfo;
import org.onap.policy.sdnc.SdncHealServiceInfo;
import org.onap.policy.sdnc.SdncHealVfModuleInfo;
import org.onap.policy.sdnc.SdncHealVfModuleParameter;
import org.onap.policy.sdnc.SdncHealVfModuleParametersInfo;
import org.onap.policy.sdnc.SdncHealVfModuleRequestInput;
import org.onap.policy.sdnc.SdncHealVnfInfo;
import org.onap.policy.sdnc.SdncRequest;

public class BandwidthOnDemandOperator extends SdncOperator {
    public static final String NAME = "BandwidthOnDemand";

    /**
     * Constructs the object.
     *
     * @param actorName name of the actor with which this operator is associated
     */
    public BandwidthOnDemandOperator(String actorName) {
        super(actorName, NAME);
    }

    @Override
    protected SdncRequest constructRequest(VirtualControlLoopEvent onset) {
        String serviceInstance = onset.getAai().get("service-instance.service-instance-id");
        if (StringUtils.isBlank(serviceInstance)) {
            throw new IllegalArgumentException("missing enrichment data, service-instance-id");
        }

        SdncHealVfModuleParameter bandwidth = new SdncHealVfModuleParameter();
        bandwidth.setName("bandwidth");
        bandwidth.setValue(onset.getAai().get("bandwidth"));

        SdncHealVfModuleParameter timeStamp = new SdncHealVfModuleParameter();
        timeStamp.setName("bandwidth-change-time");
        timeStamp.setValue(onset.getAai().get("bandwidth-change-time"));

        SdncHealVfModuleParametersInfo vfParametersInfo = new SdncHealVfModuleParametersInfo();
        vfParametersInfo.addParameters(bandwidth);
        vfParametersInfo.addParameters(timeStamp);

        SdncHealVfModuleRequestInput vfRequestInfo = new SdncHealVfModuleRequestInput();
        vfRequestInfo.setVfModuleParametersInfo(vfParametersInfo);

        SdncHealServiceInfo serviceInfo = new SdncHealServiceInfo();
        serviceInfo.setServiceInstanceId(serviceInstance);

        SdncHealRequestInfo requestInfo = new SdncHealRequestInfo();
        requestInfo.setRequestAction("SdwanBandwidthChange");

        SdncHealRequestHeaderInfo headerInfo = new SdncHealRequestHeaderInfo();
        headerInfo.setSvcAction("update");
        headerInfo.setSvcRequestId(UUID.randomUUID().toString());

        SdncRequest request = new SdncRequest();
        request.setNsInstanceId(serviceInstance);
        request.setRequestId(onset.getRequestId());
        request.setUrl("/GENERIC-RESOURCE-API:vf-module-topology-operation");

        SdncHealVnfInfo vnfInfo = new SdncHealVnfInfo();
        vnfInfo.setVnfId(onset.getAai().get("vnfId"));

        SdncHealVfModuleInfo vfModuleInfo = new SdncHealVfModuleInfo();
        vfModuleInfo.setVfModuleId("");

        SdncHealRequest healRequest = new SdncHealRequest();
        healRequest.setVnfInfo(vnfInfo);
        healRequest.setVfModuleInfo(vfModuleInfo);
        healRequest.setRequestHeaderInfo(headerInfo);
        healRequest.setVfModuleRequestInput(vfRequestInfo);
        healRequest.setRequestInfo(requestInfo);
        healRequest.setServiceInfo(serviceInfo);
        request.setHealRequest(healRequest);
        return request;
    }
}
