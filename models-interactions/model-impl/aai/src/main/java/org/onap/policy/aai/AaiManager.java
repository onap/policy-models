/*-
 * ============LICENSE_START=======================================================
 * aai
 * ================================================================================
 * Copyright (C) 2017-2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019-2020 Nordix Foundation.
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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.onap.policy.common.endpoints.event.comm.Topic.CommInfrastructure;
import org.onap.policy.common.endpoints.utils.NetLoggerUtil;
import org.onap.policy.common.endpoints.utils.NetLoggerUtil.EventType;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.rest.RestManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class handles communication towards and responses from A&AI for this module.
 */
@AllArgsConstructor
public final class AaiManager {

    // TODO remove this class

    /** The Constant logger. */
    private static final Logger logger = LoggerFactory.getLogger(AaiManager.class);

    private static final String APPLICATION_JSON = "application/json";

    private static final StandardCoder CODER = new StandardCoder();

    /** custom query and other AAI resource URLs. */
    private static final String CQ_URL = "/aai/v21/query?format=resource";
    private static final String TENANT_URL = "/aai/v21/search/nodes-query?"
                    + "search-node-type=vserver&filter=vserver-name:EQUALS:";
    private static final String PREFIX = "/aai/v21";
    private static final String PNF_URL = PREFIX + "/network/pnfs/pnf/";
    private static final String AAI_DEPTH_SUFFIX = "?depth=0";

    // The REST manager used for processing REST calls for this AAI manager
    private final RestManager restManager;

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
            var responseObj = new JSONObject(getResponse);
            JSONArray resultsArray;
            if (responseObj.has("result-data")) {
                resultsArray = (JSONArray) responseObj.get("result-data");
            } else {
                return null;
            }
            var resourceLink = resultsArray.getJSONObject(0).getString("resource-link");
            var start = resourceLink.replace(PREFIX, "");
            var query = "query/closed-loop";
            var payload = new JSONObject();
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

        String urlGet = url + TENANT_URL;

        var getResponse = getStringQuery(urlGet, username, password, requestId, vserver);
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

        logger.debug("RestManager.put before");
        String requestJson = getCustomQueryRequestPayload(url, username, password, requestId, vserver);
        NetLoggerUtil.log(EventType.OUT, CommInfrastructure.REST, url, requestJson);

        url = url + CQ_URL;

        Pair<Integer, String> httpDetails = this.restManager.put(url, username, password, headers, APPLICATION_JSON,
                        requestJson);
        logger.debug("RestManager.put after");

        if (httpDetails == null) {
            NetLoggerUtil.log(EventType.IN, CommInfrastructure.REST, url, "AAI POST Null Response");
            logger.debug("AAI POST Null Response to {}", url);
            return null;
        }

        int httpResponseCode = httpDetails.getLeft();

        NetLoggerUtil.log(EventType.IN, CommInfrastructure.REST, url, "Response code: " + httpResponseCode);
        NetLoggerUtil.getNetworkLogger().debug(httpDetails.getRight());

        logger.debug(url);
        logger.debug("{}", httpResponseCode);
        logger.debug(httpDetails.getRight());

        if (httpDetails.getRight() != null) {
            return new AaiCqResponse(httpDetails.getRight());
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

        var attemptsLeft = 3;

        while (attemptsLeft-- > 0) {
            NetLoggerUtil.getNetworkLogger().info("[OUT|{}|{}|]", CommInfrastructure.REST, urlGet);
            Pair<Integer, String> httpDetailsGet = restManager.get(urlGet, username, password, headers);
            if (httpDetailsGet == null) {
                NetLoggerUtil.log(EventType.IN, CommInfrastructure.REST, url, "AAI POST Null Response");
                logger.debug("AAI GET Null Response to {}", urlGet);
                return null;
            }

            int httpResponseCode = httpDetailsGet.getLeft();

            NetLoggerUtil.log(EventType.IN, CommInfrastructure.REST, url, "Response code: " + httpResponseCode);
            NetLoggerUtil.getNetworkLogger().debug(httpDetailsGet.getRight());

            logger.debug(urlGet);
            logger.debug("{}", httpResponseCode);
            logger.debug(httpDetailsGet.getRight());

            if (httpResponseCode == 200 && httpDetailsGet.getRight() != null) {
                return httpDetailsGet.getRight();
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
        headers.put("Accept", APPLICATION_JSON);

        return headers;
    }

    /**
     * Perform a GET request for a particular PNF by PNF ID towards A&AI.
     *
     * @param url the A&AI URL
     * @param username the user name for authentication
     * @param password the password for authentication
     * @param requestId the UUID of the request
     * @param pnfName the AAI unique identifier for PNF object
     * @return HashMap of PNF properties
     */
    public Map<String, String> getPnf(String url, String username, String password, UUID requestId, String pnfName) {
        String urlGet;
        try {
            urlGet = url + PNF_URL;
            pnfName = URLEncoder.encode(pnfName, StandardCharsets.UTF_8.toString()) + AAI_DEPTH_SUFFIX;
        } catch (UnsupportedEncodingException e) {
            logger.error("Failed to encode the pnfName: {} using UTF-8", pnfName, e);
            return null;
        }
        var responseGet = getStringQuery(urlGet, username, password, requestId, pnfName);
        if (responseGet == null) {
            logger.error("Null response from AAI for the url: {}.", urlGet);
            return null;
        }
        try {
            @SuppressWarnings("unchecked")
            Map<String, String> pnfParams = CODER.decode(responseGet, HashMap.class);
            // Map to AAI node.attribute notation
            return pnfParams.entrySet().stream()
                            .collect(Collectors.toMap(e -> "pnf." + e.getKey(), Map.Entry::getValue));
        } catch (CoderException e) {
            logger.error("Failed to fetch PNF from AAI", e);
            return null;
        }
    }
}
