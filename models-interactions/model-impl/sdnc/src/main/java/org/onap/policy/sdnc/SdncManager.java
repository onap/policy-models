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

import org.drools.core.WorkingMemory;
import org.onap.policy.common.endpoints.event.comm.Topic.CommInfrastructure;
import org.onap.policy.common.endpoints.utils.NetLoggerUtil;
import org.onap.policy.common.endpoints.utils.NetLoggerUtil.EventType;
import org.onap.policy.drools.system.PolicyEngine;
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
    private WorkingMemory workingMem;
    private static final Logger logger = LoggerFactory.getLogger(SdncManager.class);

    // The REST manager used for processing REST calls for this Sdnc manager
    private RestManager restManager;

    /**
     * Constructor.
     *
     * @param wm Drools working memory
     * @param request request
     */
    public SdncManager(WorkingMemory wm, SdncRequest request) {
        if (wm == null || request == null) {
            throw new IllegalArgumentException(
                  "the parameters \"wm\" and \"request\" on the SdncManager constructor may not be null"
            );
        }
        workingMem = wm;
        sdncRequest = request;

        restManager = new RestManager();

        setSdncParams(getPeManagerEnvProperty("sdnc.url"), getPeManagerEnvProperty("sdnc.username"),
            getPeManagerEnvProperty("sdnc.password"));
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
            workingMem.insert(responseError);
            return;
        }

        if (httpDetails == null) {
            workingMem.insert(responseError);
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

            workingMem.insert(response);
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

    /**
     * This method reads and validates environmental properties coming from the policy engine. Null properties cause
     * an {@link IllegalArgumentException} runtime exception to be thrown
     * @param  enginePropertyName name of the parameter to retrieve
     * @return the property value
     */

    private String getPeManagerEnvProperty(String enginePropertyName) {
        String enginePropertyValue = PolicyEngine.manager.getEnvironmentProperty(enginePropertyName);
        if (enginePropertyValue == null) {
            throw new IllegalArgumentException(
                "The value of policy engine manager environment property \""
                   + enginePropertyName + "\" may not be null"
            );
        }
        return enginePropertyValue;
    }
}
