/*-
 * ============LICENSE_START=======================================================
 * aai manager new
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.aai;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONObject;
import org.onap.policy.common.endpoints.event.comm.Topic.CommInfrastructure;
import org.onap.policy.common.endpoints.utils.NetLoggerUtil;
import org.onap.policy.common.endpoints.utils.NetLoggerUtil.EventType;
import org.onap.policy.rest.RestManager;
import org.onap.policy.rest.RestManager.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class handles communication towards and responses from A&AI for this module.
 */
public final class AaiManagerNew {

    /** The Constant logger. */
    private static final Logger logger = LoggerFactory.getLogger(AaiManager.class);

    /** The rest manager. */
    // The REST manager used for processing REST calls for this AAI manager
    private final RestManager restManagerNew;

    /**
     * Constructor, create the AAI manager with the specified REST manager.
     *
     * @param restManager the rest manager to use for REST calls
     */
    public AaiManagerNew(final RestManager restManagerNew) {
        this.restManagerNew = restManagerNew;
    }

    /**
     * Creates the custom query payload from a tenant query response.
     *
     * @param getResponse response from the tenant query
     * @return String Payload
     */
    private String createCustomQueryPayload(String getResponse) {

        if (getResponse == null) {
            return null;
        } else {
            JSONObject responseObj = new JSONObject(getResponse);
            JSONArray resultsArray = new JSONArray();
            if (responseObj.has("result-data")) {
                resultsArray = (JSONArray) responseObj.get("result-data");
            } else {
                return null;
            }
            String resourceLink = resultsArray.getJSONObject(0).getString("resource-link");
            String start = resourceLink.replace("/aai/v11", "");
            String query = "query/closed-loop";
            JSONObject payload = new JSONObject();
            payload.put("start", start);
            payload.put("query", query);
            return payload.toString();

        }
    }


    /**
     * This method is used to get the information for custom query.
     *
     * @param url url of the get method
     * @param username Aai username
     * @param password Aai password
     * @param requestId request ID
     * @param vserver Id of the vserver
     * @return String
     */
    private String getCustomQueryRequestPayload(String url, String username, String password, UUID requestId,
            String vserver) {

        String urlGet = url + "/aai/v11/search/nodes-query?search-node-type=vserver&filter=vserver-name:";
        String getResponse = getStringQuery(urlGet, username, password, requestId, vserver);
        return createCustomQueryPayload(getResponse);
    }



    /**
     * Calls Aai and returns a custom query response for a vserver.
     *
     * @param url Aai url
     * @param username Aai Username
     * @param password Aai Password
     * @param requestId request ID
     * @param vserver Vserver
     * @return AaiCqResponse response from Aai for custom query
     */
    public AaiCqResponse getCustomQueryResponse(String url, String username, String password, UUID requestId,
            String vserver) {

        final Map<String, String> headers = createHeaders(requestId);

        url = url + "/aai/v11/query?format=resource";

        logger.debug("RestManager.put before");
        String requestJson = getCustomQueryRequestPayload(url, username, password, requestId, vserver);
        NetLoggerUtil.log(EventType.OUT, CommInfrastructure.REST, url, requestJson);
        Pair<Integer, String> httpDetails =
                this.restManagerNew.put(url, username, password, headers, "application/json", requestJson);
        logger.debug("RestManager.put after");

        if (httpDetails == null) {
            logger.info("AAI POST Null Response to {}", url);
            return null;
        }

        int httpResponseCode = httpDetails.first;

        logger.info(url);
        logger.info("{}", httpResponseCode);
        logger.info(httpDetails.second);

        if (httpDetails.second != null) {
            String resp = httpDetails.second;
            return new AaiCqResponse(resp);
        }
        return null;
    }



    /**
     * Returns the string response of a get query.
     *
     * @param url Aai URL
     * @param username Aai Username
     * @param password Aai Password
     * @param requestId AaiRequestId
     * @param key Aai Key
     * @return String returns the string from the get query
     */
    private String getStringQuery(final String url, final String username, final String password, final UUID requestId,
            final String key) {

        Map<String, String> headers = createHeaders(requestId);

        String urlGet = url + key;

        int attemptsLeft = 3;

        while (attemptsLeft-- > 0) {
            NetLoggerUtil.getNetworkLogger().info("[OUT|{}|{}|]", CommInfrastructure.REST, urlGet);
            Pair<Integer, String> httpDetailsGet = restManagerNew.get(urlGet, username, password, headers);
            if (httpDetailsGet == null) {
                logger.info("AAI GET Null Response to {}", urlGet);
                return null;
            }

            int httpResponseCode = httpDetailsGet.first;

            logger.info(urlGet);
            logger.info("{}", httpResponseCode);
            logger.info(httpDetailsGet.second);

            if (httpResponseCode == 200) {
                String responseGet = httpDetailsGet.second;
                if (responseGet != null) {
                    return responseGet;
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

        }

        return null;
    }

    /**
     * Create the headers for the HTTP request.
     *
     * @param requestId the request ID to insert in the headers
     * @return the HTTP headers
     */
    private Map<String, String> createHeaders(final UUID requestId) {
        Map<String, String> headers = new HashMap<>();

        headers.put("X-FromAppId", "POLICY");
        headers.put("X-TransactionId", requestId.toString());
        headers.put("Accept", "application/json");

        return headers;
    }

}

