/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2017-2018 Intel Corp. All rights reserved.
 * Modifications Copyright (C) 2018-2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.controlloop.actor.vfc;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.List;
import org.onap.policy.aai.AaiCqResponse;
import org.onap.policy.controlloop.ControlLoopOperation;
import org.onap.policy.controlloop.VirtualControlLoopEvent;
import org.onap.policy.controlloop.actorserviceprovider.spi.Actor;
import org.onap.policy.controlloop.policy.Policy;
import org.onap.policy.vfc.VfcHealActionVmInfo;
import org.onap.policy.vfc.VfcHealAdditionalParams;
import org.onap.policy.vfc.VfcHealRequest;
import org.onap.policy.vfc.VfcRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VfcActorServiceProviderNew implements Actor {
    private static final Logger logger = LoggerFactory.getLogger(VfcActorServiceProviderNew.class);

    // Strings for VFC Actor
    private static final String VFC_ACTOR = "VFC";

    // Strings for targets
    private static final String TARGET_VM = "VM";

    // Strings for recipes
    private static final String RECIPE_RESTART = "Restart";

    private static final ImmutableList<String> recipes = ImmutableList.of(RECIPE_RESTART);
    private static final ImmutableMap<String, List<String>> targets =
            new ImmutableMap.Builder<String, List<String>>().put(RECIPE_RESTART, ImmutableList.of(TARGET_VM)).build();

    @Override
    public String actor() {
        return VFC_ACTOR;
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
     * This method constructs the VFC request.
     *
     * @param onset onset object
     * @param operation operation object
     * @param policy policy object
     * @param aaiCqResponse response from aai custom query
     * @return VfcRequest
     */
    public static VfcRequest constructRequest(VirtualControlLoopEvent onset, ControlLoopOperation operation,
            Policy policy, AaiCqResponse aaiCqResponse) {

        // Construct an VFC request
        VfcRequest request = new VfcRequest();
        String serviceInstance = onset.getAai().get("service-instance.service-instance-id");
        if (serviceInstance == null || "".equals(serviceInstance)) {
            // get service isntance from AaiCqResponse
            if (aaiCqResponse == null) {
                return null;
            }
            serviceInstance = aaiCqResponse.getServiceInstance().getServiceInstanceId();
            // If the serviceInstanceId returned is null then return null
            if (serviceInstance == null) {
                return null;
            }

        }
        request.setNsInstanceId(serviceInstance);
        request.setRequestId(onset.getRequestId());
        request.setHealRequest(new VfcHealRequest());
        request.getHealRequest().setVnfInstanceId(onset.getAai().get("generic-vnf.vnf-id"));
        request.getHealRequest().setCause(operation.getMessage());
        request.getHealRequest().setAdditionalParams(new VfcHealAdditionalParams());

        if (policy.getRecipe().toLowerCase().equalsIgnoreCase(RECIPE_RESTART)) {
            request.getHealRequest().getAdditionalParams().setAction("restartvm");
            request.getHealRequest().getAdditionalParams().setActionInfo(new VfcHealActionVmInfo());
            request.getHealRequest().getAdditionalParams().getActionInfo()
                    .setVmid(onset.getAai().get("vserver.vserver-id"));
            request.getHealRequest().getAdditionalParams().getActionInfo()
                    .setVmname(onset.getAai().get("vserver.vserver-name"));
        } else {
            return null;
        }
        return request;
    }
}

