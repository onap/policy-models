/*-
 * ============LICENSE_START=======================================================
 * SdncActorServiceProvider
 * ================================================================================
 * Copyright (C) 2018-2019 Huawei Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019 Nordix Foundation.
 * Modifications Copyright (C) 2019 AT&T Intellectual Property.
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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.onap.policy.controlloop.ControlLoopOperation;
import org.onap.policy.controlloop.VirtualControlLoopEvent;
import org.onap.policy.controlloop.actorserviceprovider.spi.Actor;
import org.onap.policy.controlloop.policy.Policy;
import org.onap.policy.sdnc.SdncHealNetworkInfo;
import org.onap.policy.sdnc.SdncHealRequest;
import org.onap.policy.sdnc.SdncHealRequestHeaderInfo;
import org.onap.policy.sdnc.SdncHealRequestInfo;
import org.onap.policy.sdnc.SdncHealServiceInfo;
import org.onap.policy.sdnc.SdncHealVfModuleParameter;
import org.onap.policy.sdnc.SdncHealVfModuleParametersInfo;
import org.onap.policy.sdnc.SdncHealVfModuleRequestInput;
import org.onap.policy.sdnc.SdncHealVnfInfo;
import org.onap.policy.sdnc.SdncRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SdncActorServiceProvider implements Actor {
    private static final Logger logger = LoggerFactory.getLogger(SdncActorServiceProvider.class);

    // Strings for Sdnc Actor
    private static final String SDNC_ACTOR = "SDNC";

    // Strings for targets
    private static final String TARGET_VM = "VM";

    // Strings for recipes
    private static final String RECIPE_REROUTE = "Reroute";

    // Strings for recipes
    private static final String RECIPE_BW_ON_DEMAND = "BandwidthOnDemand";

    private static final ImmutableList<String> recipes = ImmutableList.of(RECIPE_REROUTE);
    private static final ImmutableMap<String, List<String>> targets =
            new ImmutableMap.Builder<String, List<String>>().put(RECIPE_REROUTE, ImmutableList.of(TARGET_VM)).build();

    @Override
    public String actor() {
        return SDNC_ACTOR;
    }

    @Override
    public List<String> recipes() {
        return ImmutableList.copyOf(recipes);
    }

    @Override
    public List<String> recipeTargets(String recipe) {
        return ImmutableList.copyOf(targets.getOrDefault(recipe, Collections.emptyList()));
    }

    @Override
    public List<String> recipePayloads(String recipe) {
        return Collections.emptyList();
    }

    /**
     * Construct a request.
     *
     * @param onset the onset event
     * @param operation the control loop operation
     * @param policy the policy
     * @return the constructed request
     */
    public SdncRequest constructRequest(VirtualControlLoopEvent onset, ControlLoopOperation operation,
            Policy policy) {
        switch (policy.getRecipe()) {
            case RECIPE_REROUTE:
                return constructReOptimizeRequest(onset);
            case RECIPE_BW_ON_DEMAND:
                logger.info("Construct request for receipe {}" , RECIPE_BW_ON_DEMAND);
                return constructBwOnDemandRequest(onset);
            default:
                logger.info("Unsupported recipe {} " + policy.getRecipe());
                return null;
        }
    }

    private SdncRequest constructBwOnDemandRequest(VirtualControlLoopEvent onset) {
        // Construct an Sdnc request
        String serviceInstance = onset.getAai().get("service-instance.service-instance-id");
        if (serviceInstance == null || serviceInstance.isEmpty()) {
            // This indicates that AAI Enrichment needs to be done by event producer.
            return null;
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
        request.setUrl("/GENERIC-RESOURCE-API:vnf-topology-operation");

        SdncHealVnfInfo vnfInfo = new SdncHealVnfInfo();
        vnfInfo.setVnfId(onset.getAai().get("vnfId"));

        SdncHealRequest healRequest = new SdncHealRequest();
        healRequest.setVnfInfo(vnfInfo);
        healRequest.setRequestHeaderInfo(headerInfo);
        healRequest.setVfModuleRequestInput(vfRequestInfo);
        healRequest.setRequestInfo(requestInfo);
        healRequest.setServiceInfo(serviceInfo);
        request.setHealRequest(healRequest);
        return request;
    }

    private SdncRequest constructReOptimizeRequest(VirtualControlLoopEvent onset) {
        // Construct an Sdnc request
        String serviceInstance = onset.getAai().get("service-instance.service-instance-id");
        if (serviceInstance == null || serviceInstance.isEmpty()) {
            // This indicates that AAI Enrichment needs to be done by event producer.
            return null;
        }
        SdncHealServiceInfo serviceInfo = new SdncHealServiceInfo();
        serviceInfo.setServiceInstanceId(serviceInstance);

        String networkId = onset.getAai().get("network-information.network-id");
        if (networkId == null || networkId.isEmpty()) {
            // This indicates that AAI Enrichment needs to be done by event producer.
            return null;
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
