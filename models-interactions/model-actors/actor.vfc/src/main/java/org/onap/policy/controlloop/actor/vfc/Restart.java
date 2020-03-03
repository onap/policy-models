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

package org.onap.policy.controlloop.actor.vfc;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import org.apache.commons.lang3.tuple.Pair;
import org.onap.policy.aai.AaiCqResponse;
import org.onap.policy.controlloop.VirtualControlLoopEvent;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpConfig;
import org.onap.policy.vfc.VfcHealActionVmInfo;
import org.onap.policy.vfc.VfcHealAdditionalParams;
import org.onap.policy.vfc.VfcHealRequest;
import org.onap.policy.vfc.VfcRequest;

public class Restart extends VfcOperation {
    public static final String NAME = "VF Module Create";

    // TODO Verify GENERIC_VNF_ID
    private static final String GENERIC_VNF_ID = "generic-vnf.vnf-id";

    public Restart(ControlLoopOperationParams params, HttpConfig config) {
        super(params, config);
    }

    /**
     * Ensures that A&AI customer query has been performed, and then runs the guard query.
     */
    @Override
    protected CompletableFuture<OperationOutcome> startPreprocessorAsync() {
        // PLD start A&AI? Guard?
        VirtualControlLoopEvent onset = params.getContext().getEvent();
        if (onset.getAai() == null) {
            onset.setAai(new HashMap<String, String>());
        }
        onset.getAai().put("vserver.vserver-name", "vserver-name-16102016-aai3255-data-11-1");
        onset.getAai().put("vserver.vserver-id", "vserver-id-16102016-aai3255-data-11-1");
        onset.getAai().put("generic-vnf.vnf-id", "vnf-id-16102016-aai3255-data-11-1");
        onset.getAai().put("service-instance.service-instance-id", "service-instance-id-16102016-aai3255-data-11-1");
        onset.getAai().put("vserver.is-closed-loop-disabled", "false");
        onset.getAai().put("vserver.prov-status", "ACTIVE");
        return super.startPreprocessorAsync();
    }

    @Override
    protected CompletableFuture<OperationOutcome> startOperationAsync(int attempt, OperationOutcome outcome) {

        // starting a whole new attempt - reset the count
        resetGetCount();

        // PLD TODO

        return null;
    }

    /**
     * Makes a request.
     *
     * @return a pair containing the request URL and the new request
     */
    protected Pair<String, VfcRequest> makeRequest() {

        VfcRequest request = new VfcRequest();
        VirtualControlLoopEvent onset = params.getContext().getEvent();
        String serviceInstance = onset.getAai().get("service-instance.service-instance-id");
        AaiCqResponse aaiCqResponse = params.getContext().getProperty(AaiCqResponse.CONTEXT_KEY);

        if (serviceInstance == null || serviceInstance.isEmpty()) {
            if (aaiCqResponse == null) {
                return null;
            }
            serviceInstance = aaiCqResponse.getServiceInstance().getServiceInstanceId();

            if (serviceInstance == null) {
                return null;
            }
        }

        request.setNsInstanceId(serviceInstance);
        request.setRequestId(onset.getRequestId());
        request.setHealRequest(new VfcHealRequest());
        request.getHealRequest().setVnfInstanceId(onset.getAai().get(GENERIC_VNF_ID));
        // TODO request.getHealRequest().setCause(operation.getMessage());
        request.getHealRequest().setAdditionalParams(new VfcHealAdditionalParams());

        if (getName().toLowerCase().equalsIgnoreCase(GENERIC_VNF_ID)) {
            request.getHealRequest().getAdditionalParams().setAction("restartvm");
            request.getHealRequest().getAdditionalParams().setActionInfo(new VfcHealActionVmInfo());
            request.getHealRequest().getAdditionalParams().getActionInfo()
                    .setVmid(onset.getAai().get("vserver.vserver-id"));
            request.getHealRequest().getAdditionalParams().getActionInfo()
                    .setVmname(onset.getAai().get("vserver.vserver-name"));

        }
        else {
            return null;
        }


        // TODO
        String requestUrl = makeUrl();
        return Pair.of(requestUrl, request);
    }
}
