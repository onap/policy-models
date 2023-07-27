/*
 * ============LICENSE_START=======================================================
 * rest
 * ================================================================================
 * Copyright (C) 2017-2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019-2020, 2023 Nordix Foundation.
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

package org.onap.policy.rest;

import jakarta.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestManager {
    private static final Logger logger = LoggerFactory.getLogger(RestManager.class);

    // Constants for string literals
    private static final String CONTENT_TYPE = "Content-Type";

    /**
     * Perform REST PUT.
     *
     * @param url the url
     * @param username the user
     * @param password the password
     * @param headers any headers
     * @param contentType what the content type is
     * @param body body to send
     * @return the response status code and the body
     */
    public Pair<Integer, String> put(String url, String username, String password, Map<String, String> headers,
            String contentType, String body) {
        var put = new HttpPut(url);
        addHeaders(put, username, password, headers);
        put.addHeader(CONTENT_TYPE, contentType);
        try {
            var input = new StringEntity(body);
            input.setContentType(contentType);
            put.setEntity(input);
        } catch (Exception e) {
            logger.error("put threw: ", e);
            return null;
        }
        return sendRequest(put);
    }

    /**
     * Perform REST Post.
     *
     * @param url the url
     * @param username the user
     * @param password the password
     * @param headers any headers
     * @param contentType what the content type is
     * @param body body to send
     * @return the response status code and the body
     */
    public Pair<Integer, String> post(String url, String username, String password, Map<String, String> headers,
            String contentType, String body) {
        var post = new HttpPost(url);
        addHeaders(post, username, password, headers);
        post.addHeader(CONTENT_TYPE, contentType);
        try {
            var input = new StringEntity(body);
            input.setContentType(contentType);
            post.setEntity(input);
        } catch (Exception e) {
            logger.error("post threw: ", e);
            return null;
        }
        return sendRequest(post);
    }

    /**
     * Do a REST get.
     *
     * @param url URL
     * @param username user
     * @param password password
     * @param headers any headers to add
     * @return a Pair for the response status and the body
     */
    public Pair<Integer, String> get(String url, String username, String password, Map<String, String> headers) {
        var get = new HttpGet(url);
        addHeaders(get, username, password, headers);
        return sendRequest(get);
    }

    /**
     * Perform REST Delete. <br/>
     * <i>Note: Many REST endpoints will return a 400 error for delete requests with a non-empty body</i>
     *
     * @param url the url
     * @param username the user
     * @param password the password
     * @param headers any headers
     * @param contentType what the content type is
     * @param body body (optional) to send
     * @return the response status code and the body
     */
    public Pair<Integer, String> delete(String url, String username, String password, Map<String, String> headers,
            String contentType, String body) {
        var delete = new HttpDeleteWithBody(url);
        addHeaders(delete, username, password, headers);
        if (body != null && !body.isEmpty()) {
            delete.addHeader(CONTENT_TYPE, contentType);
            try {
                var input = new StringEntity(body);
                input.setContentType(contentType);
                delete.setEntity(input);
            } catch (Exception e) {
                logger.error("delete threw: ", e);
                return null;
            }
        }
        return sendRequest(delete);
    }

    /**
     * Perform REST Delete.
     *
     * @param url the url
     * @param username the user
     * @param password the password
     * @param headers any headers
     * @return the response status code and the body
     */
    public Pair<Integer, String> delete(String url, String username, String password, Map<String, String> headers) {
        var delete = new HttpDelete(url);
        addHeaders(delete, username, password, headers);
        return sendRequest(delete);
    }

    /**
     * Perform REST Patch.
     *
     * @param url the url
     * @param username the user
     * @param password the password
     * @param headers any headers
     * @param body body to send
     * @return the response status code and the body
     */
    public Pair<Integer, String> patch(String url, String username, String password, Map<String, String> headers,
            String body) {
        var contentType = "application/merge-patch+json";
        var patch = new HttpPatch(url);
        addHeaders(patch, username, password, headers);
        patch.addHeader(CONTENT_TYPE, contentType);
        try {
            var input = new StringEntity(body);
            input.setContentType(contentType);
            patch.setEntity(input);
        } catch (Exception e) {
            logger.error("patch threw: ", e);
            return null;
        }
        return sendRequest(patch);
    }

    /**
     * Send REST request.
     *
     * @param request http request to send
     * @return the response status code and the body
     */
    private Pair<Integer, String> sendRequest(HttpRequestBase request) {
        if (logger.isDebugEnabled()) {
            logger.debug("***** sendRequest to url {}:", request.getURI());
        }

        try (CloseableHttpClient client =
                HttpClientBuilder.create().setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE).build()) {
            HttpResponse response = client.execute(request);
            if (response != null) {
                var returnBody = EntityUtils.toString(response.getEntity(), "UTF-8");
                logger.debug("HTTP Response Status Code: {}", response.getStatusLine().getStatusCode());
                logger.debug("HTTP Response Body:");
                logger.debug(returnBody);

                return Pair.of(response.getStatusLine().getStatusCode(), returnBody);
            } else {
                logger.error("Response from {} is null", request.getURI());
                return null;
            }
        } catch (Exception e) {
            logger.error("Request failed to {}", request.getURI(), e);
            return null;
        }
    }

    /**
     * Add header to the request.
     *
     * @param request http request to send
     * @param username the user
     * @param password the password
     * @param headers any headers
     */
    private void addHeaders(HttpRequestBase request, String username, String password, Map<String, String> headers) {
        String authHeader = makeAuthHeader(username, password);
        if (headers != null) {
            for (Entry<String, String> entry : headers.entrySet()) {
                request.addHeader(entry.getKey(), headers.get(entry.getKey()));
            }
        }
        if (authHeader != null) {
            request.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
        }
    }

    private String makeAuthHeader(String username, String password) {
        if (username == null || username.isEmpty()) {
            return null;
        }

        String auth = username + ":" + (password == null ? "" : password);
        return "Basic " + DatatypeConverter.printBase64Binary(auth.getBytes(StandardCharsets.ISO_8859_1));
    }
}
