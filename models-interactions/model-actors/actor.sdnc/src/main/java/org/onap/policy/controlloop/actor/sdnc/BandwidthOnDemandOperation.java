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
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpConfig;
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

public class BandwidthOnDemandOperation extends SdncOperation {

    public static final String NAME = "BandwidthOnDemand";

    // fields in the enrichment data
    public static final String SERVICE_ID_KEY = "service-instance.service-instance-id";
    public static final String BANDWIDTH = "bandwidth";
    public static final String BANDWIDTH_CHANGE_TIME = "bandwidth-change-time";
    public static final String VNF_ID = "vnfId";

    // @formatter:off
    private static final List<String> PROPERTY_NAMES = List.of(
                            OperationProperties.ENRICHMENT_SERVICE_ID,
                            OperationProperties.ENRICHMENT_BANDWIDTH,
                            OperationProperties.ENRICHMENT_BANDWIDTH_CHANGE_TIME,
                            OperationProperties.ENRICHMENT_VNF_ID);
    // @formatter:on

    /**
     * Constructs the object.
     *
     * @param params operation parameters
     * @param config configuration for this operation
     */
    public BandwidthOnDemandOperation(ControlLoopOperationParams params, HttpConfig config) {
        super(params, config, PROPERTY_NAMES);
    }

    @Override
    protected SdncRequest makeRequest(int attempt) {
        String serviceInstance = getProperty(OperationProperties.ENRICHMENT_SERVICE_ID);
        if (StringUtils.isBlank(serviceInstance)) {
            throw new IllegalArgumentException("missing enrichment data, " + SERVICE_ID_KEY);
        }

        SdncHealVfModuleParameter bandwidth = new SdncHealVfModuleParameter();
        bandwidth.setName(BANDWIDTH);
        bandwidth.setValue(getProperty(OperationProperties.ENRICHMENT_BANDWIDTH));

        SdncHealVfModuleParameter timeStamp = new SdncHealVfModuleParameter();
        timeStamp.setName(BANDWIDTH_CHANGE_TIME);
        timeStamp.setValue(getProperty(OperationProperties.ENRICHMENT_BANDWIDTH_CHANGE_TIME));

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
        headerInfo.setSvcRequestId(getSubRequestId());

        SdncRequest request = new SdncRequest();
        request.setNsInstanceId(serviceInstance);
        request.setRequestId(params.getRequestId());
        request.setUrl("/" + getPath());

        SdncHealVnfInfo vnfInfo = new SdncHealVnfInfo();
        vnfInfo.setVnfId(getProperty(OperationProperties.ENRICHMENT_VNF_ID));

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
