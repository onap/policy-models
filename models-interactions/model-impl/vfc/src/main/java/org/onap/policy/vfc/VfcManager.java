/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2017-2019 Intel Corp. All rights reserved.
 * Modifications Copyright (C) 2019 Nordix Foundation.
 * Modifications Copyright (C) 2018-2019 AT&T Corporation. All rights reserved.
 * Modifications Copyright (C) 2019 Samsung Electronics Co., Ltd.
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

package org.onap.policy.vfc;

import com.google.gson.JsonSyntaxException;

import java.util.HashMap;
import java.util.Map;

import org.onap.policy.common.endpoints.event.comm.Topic.CommInfrastructure;
import org.onap.policy.common.endpoints.utils.NetLoggerUtil;
import org.onap.policy.common.endpoints.utils.NetLoggerUtil.EventType;
import org.onap.policy.rest.RestManager;
import org.onap.policy.rest.RestManager.Pair;
import org.onap.policy.vfc.util.Serialization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class VfcManager implements Runnable {

    private String vfcUrlBase;
    private String username;
    private String password;
    private VfcRequest vfcRequest;
    private VfcCallback callback;
    private static final Logger logger = LoggerFactory.getLogger(VfcManager.class);

    // The REST manager used for processing REST calls for this VFC manager
    private RestManager restManager;
    
    public interface VfcCallback {

		void onResponse(VfcResponse responseError);
    }

    /**
     * Constructor.
     *
     * @param wm Drools working memory
     * @param request request
     */
    public VfcManager(VfcCallback cb, VfcRequest request, String url, String user, String pwd) {
        if (cb == null || request == null) {
            throw new IllegalArgumentException(
                    "the parameters \"cb\" and \"request\" on the VfcManager constructor may not be null");
        }
        callback = cb;
        vfcRequest = request;
        vfcUrlBase = url;
        username = user;
        password = pwd;

        restManager = new RestManager();
    }

    /**
     * Set the parameters.
     *
     * @param baseUrl base URL
     * @param name username
     * @param pwd password
     */
    public void setVfcParams(String baseUrl, String name, String pwd) {
        vfcUrlBase = baseUrl + "/api/nslcm/v1";
        username = name;
        password = pwd;
    }

    @Override
    public void run() {
        Map<String, String> headers = new HashMap<>();
        Pair<Integer, String> httpDetails;

        VfcResponse responseError = new VfcResponse();
        responseError.setResponseDescriptor(new VfcResponseDescriptor());
        responseError.getResponseDescriptor().setStatus("error");

        headers.put("Accept", "application/json");
        String vfcUrl = vfcUrlBase + "/ns/" + vfcRequest.getNsInstanceId() + "/heal";
        try {
            String vfcRequestJson = Serialization.gsonPretty.toJson(vfcRequest);
            NetLoggerUtil.log(EventType.OUT, CommInfrastructure.REST, vfcUrl, vfcRequestJson);

            httpDetails = restManager.post(vfcUrl, username, password, headers, "application/json", vfcRequestJson);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            this.callback.onResponse(responseError);
            return;
        }

        if (httpDetails == null) {
            this.callback.onResponse(responseError);
            return;
        }

        if (httpDetails.first != 202) {
            logger.warn("VFC Heal Restcall failed");
            return;
        }

        try {
            VfcResponse response = Serialization.gsonPretty.fromJson(httpDetails.second, VfcResponse.class);
            NetLoggerUtil.log(EventType.IN, CommInfrastructure.REST, vfcUrl, httpDetails.second);
            String body = Serialization.gsonPretty.toJson(response);
            logger.debug("Response to VFC Heal post:");
            logger.debug(body);

            String jobId = response.getJobId();
            int attemptsLeft = 20;

            String urlGet = vfcUrlBase + "/jobs/" + jobId;
            VfcResponse responseGet = null;

            while (attemptsLeft-- > 0) {
                NetLoggerUtil.getNetworkLogger().info("[OUT|{}|{}|]", "VFC", urlGet);
                Pair<Integer, String> httpDetailsGet = restManager.get(urlGet, username, password, headers);
                responseGet = Serialization.gsonPretty.fromJson(httpDetailsGet.second, VfcResponse.class);
                NetLoggerUtil.log(EventType.IN, CommInfrastructure.REST, vfcUrl, httpDetailsGet.second);
                responseGet.setRequestId(vfcRequest.getRequestId().toString());
                body = Serialization.gsonPretty.toJson(responseGet);
                logger.debug("Response to VFC Heal get:");
                logger.debug(body);

                String responseStatus = responseGet.getResponseDescriptor().getStatus();
                if (httpDetailsGet.first == 200
                        && ("finished".equalsIgnoreCase(responseStatus) || "error".equalsIgnoreCase(responseStatus))) {
                    logger.debug("VFC Heal Status {}", responseGet.getResponseDescriptor().getStatus());
                    this.callback.onResponse(responseGet);
                    break;
                }
                Thread.sleep(20000);
            }
            if ((attemptsLeft <= 0) && (responseGet != null) && (responseGet.getResponseDescriptor() != null)
                    && (responseGet.getResponseDescriptor().getStatus() != null)
                    && (!responseGet.getResponseDescriptor().getStatus().isEmpty())) {
                logger.debug("VFC timeout. Status: ({})", responseGet.getResponseDescriptor().getStatus());
                this.callback.onResponse(responseGet);
            }
        } catch (JsonSyntaxException e) {
            logger.error("Failed to deserialize into VfcResponse {}", e.getLocalizedMessage(), e);
        } catch (InterruptedException e) {
            logger.error("Interrupted exception: {}", e.getLocalizedMessage(), e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.error("Unknown error deserializing into VfcResponse {}", e.getLocalizedMessage(), e);
        }
    }

    /**
     * Protected setter for rest manager to allow mocked rest manager to be used for testing.
     *
     * @param restManager the test REST manager
     */
    protected void setRestManager(final RestManager restManager) {
        this.restManager = restManager;
    }
}
