/*
 * ============LICENSE_START=======================================================
 * Copyright (C) 2018 Huawei. All rights reserved.
 * ================================================================================
 * Modifications Copyright (C) 2019 AT&T Intellectual Property. All rights reserved
 * Modifications Copyright (C) 2019 Nordix Foundation.
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

package org.onap.policy.sdnc;

import com.google.gson.JsonSyntaxException;

import java.util.HashMap;
import java.util.Map;

import org.onap.policy.common.endpoints.event.comm.Topic.CommInfrastructure;
import org.onap.policy.common.endpoints.utils.NetLoggerUtil;
import org.onap.policy.common.endpoints.utils.NetLoggerUtil.EventType;
import org.onap.policy.rest.RestManager;
import org.onap.policy.rest.RestManager.Pair;
import org.onap.policy.sdnc.util.Serialization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SdncManager implements Runnable {

    private String sdncUrlBase;
    private String username;
    private String password;
    private SdncRequest sdncRequest;
    private SdncCallback callback;
    private static final Logger logger = LoggerFactory.getLogger(SdncManager.class);

    // The REST manager used for processing REST calls for this Sdnc manager
    private RestManager restManager;
    
    public interface SdncCallback {
    	public void onCallback(SdncResponse response);
    }

    /**
     * Constructor.
     *
     * @param wm Drools working memory
     * @param request request
     */
    public SdncManager(SdncCallback cb, SdncRequest request, String url,
    		String user, String password) {
        if (callback == null || request == null) {
            throw new IllegalArgumentException(
                  "the parameters \"callback\" and \"request\" on the SdncManager constructor may not be null"
            );
        }
        this.callback = cb;
        this.sdncRequest = request;
        this.sdncUrlBase = url;
        this.username = user;
        this.password = password;

        restManager = new RestManager();
    }

    /**
     * Set the parameters.
     *
     * @param baseUrl base URL
     * @param name username
     * @param pwd password
     */
    public void setSdncParams(String baseUrl, String name, String pwd) {
        sdncUrlBase = baseUrl;
        username = name;
        password = pwd;
    }

    @Override
    public void run() {
        Map<String, String> headers = new HashMap<>();
        Pair<Integer, String> httpDetails;

        SdncResponse responseError = new SdncResponse();
        SdncResponseOutput responseOutput = new SdncResponseOutput();
        responseOutput.setResponseCode("404");
        responseError.setResponseOutput(responseOutput);

        headers.put("Accept", "application/json");
        String sdncUrl = sdncUrlBase + sdncRequest.getUrl();

        try {
            String sdncRequestJson = Serialization.gsonPretty.toJson(sdncRequest);
            NetLoggerUtil.log(EventType.OUT, CommInfrastructure.REST, sdncUrl, sdncRequestJson);
            logger.info("[OUT|{}|{}|]{}{}", CommInfrastructure.REST, sdncUrl, NetLoggerUtil.SYSTEM_LS, sdncRequestJson);

            httpDetails = restManager.post(sdncUrl, username, password, headers, "application/json",
                                           sdncRequestJson);
        } catch (Exception e) {
            logger.info(e.getMessage(), e);
            this.callback.onCallback(responseError);
            return;
        }

        if (httpDetails == null) {
            this.callback.onCallback(responseError);
            return;
        }

        try {
            SdncResponse response = Serialization.gsonPretty.fromJson(httpDetails.second, SdncResponse.class);
            NetLoggerUtil.log(EventType.IN, CommInfrastructure.REST, sdncUrl, httpDetails.second);
            logger.info("[IN|{}|{}|]{}{}", "Sdnc", sdncUrl, NetLoggerUtil.SYSTEM_LS, httpDetails.second);
            String body = Serialization.gsonPretty.toJson(response);
            logger.info("Response to Sdnc Heal post:");
            logger.info(body);
            response.setRequestId(sdncRequest.getRequestId().toString());

            if (!response.getResponseOutput().getResponseCode().equals("200")) {
                logger.info(
                    "Sdnc Heal Restcall failed with http error code {} {}", httpDetails.first, httpDetails.second
                );
            }

            this.callback.onCallback(response);
        } catch (JsonSyntaxException e) {
            logger.info("Failed to deserialize into SdncResponse {}", e.getLocalizedMessage(), e);
        } catch (Exception e) {
            logger.info("Unknown error deserializing into SdncResponse {}", e.getLocalizedMessage(), e);
        }
    }

    /**
     * Protected setter for rest manager to allow mocked rest manager to be used for testing.
     * @param restManager the test REST manager
     */
    protected void setRestManager(final RestManager restManager) {
        this.restManager = restManager;
    }
}
