/*
 * ============LICENSE_START=======================================================
 * rest
 * ================================================================================
 * Copyright (C) 2017-2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019-2020, 2023-2025 Nordix Foundation.
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
 *
 *  SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.policy.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.onap.policy.common.endpoints.http.server.HttpServletServer;
import org.onap.policy.common.endpoints.http.server.HttpServletServerFactoryInstance;
import org.onap.policy.common.utils.network.NetworkUtil;

@Path("RestTest")
public class RestTest {   // NOSONAR - class needs to be public for Jersey to work
    private static final String MERGE_PATCH_PLUS_JSON = "application/merge-patch+json";

    private static final String NAME_PARAM = "Bob";
    private static final String AGE_PARAM = "10";
    private static final String PAYLOAD = "At last! ";
    private static final String RETURN_STRING = "Hello There ";
    private static final String EXPECT_STRING = RETURN_STRING + NAME_PARAM + " aged " + AGE_PARAM;

    private static final String LOCALHOST = "localhost";
    private static final String BASE = "base";

    private static int port;
    private static String baseUri;
    private static String getUri;
    private static String deleteUri;
    private static String putUri;
    private static String putUriBlank;
    private static String postUri;
    private static String postUriBlank;
    private static String patchUri;
    private static String patchUriBlank;

    private static HttpServletServer server;

    /**
     * Sets server endpoint for the tests.
     */
    @BeforeAll
    static void setUp() throws Exception {

        port = NetworkUtil.allocPort();
        baseUri = "http://" + LOCALHOST + ":" + port + "/" + BASE + "/";
        getUri = baseUri + "RestTest/GetHello/" + NAME_PARAM + "?age=" + AGE_PARAM;
        deleteUri = baseUri + "RestTest/DeleteHello/" + NAME_PARAM + "?age=" + AGE_PARAM;
        putUri = baseUri + "RestTest/PutHello/" + NAME_PARAM + "?age=" + AGE_PARAM;
        putUriBlank = baseUri + "RestTest/PutBlank";
        postUri = baseUri + "RestTest/PostHello/" + NAME_PARAM + "?age=" + AGE_PARAM;
        postUriBlank = baseUri + "RestTest/PostBlank";
        patchUri = baseUri + "RestTest/PatchHello/" + NAME_PARAM + "?age=" + AGE_PARAM;
        patchUriBlank = baseUri + "RestTest/PatchBlank";

        server = HttpServletServerFactoryInstance.getServerFactory()
            .build("RestTest", LOCALHOST, port, "/" + BASE, false, true);
        server.addServletClass("/*", RestTest.class.getName());
        server.waitedStart(5000);

    }

    /**
     * Tear down server endpoint for the tests.
     *
     */
    @AfterAll
    static void tearDown() {
        HttpServletServerFactoryInstance.getServerFactory().destroy();
    }

    @Test
    void testGetUrlNull() {
        RestManager mgr = new RestManager();
        assertThrows(NullPointerException.class, () -> {
            mgr.get(null, "user", null, null);
        });
    }   

    @Test
    void testPutUrlNull() {
        RestManager mgr = new RestManager();
        assertThrows(NullPointerException.class, () -> {
            mgr.put(null, "user", null, null, MediaType.TEXT_PLAIN, PAYLOAD);
        });
    }

    @Test
    void testPostUrlNull() {
        RestManager mgr = new RestManager();
        assertThrows(NullPointerException.class, () -> {
            mgr.post(null, "user", null, null, MediaType.TEXT_PLAIN, PAYLOAD);
        });
    }

    @Test
    void testDeleteUrlNull() {
        RestManager mgr = new RestManager();
        assertThrows(NullPointerException.class, () -> {
            mgr.delete(null, "user", null, null, null, null);
        });
    }

    @Test
    void testPatchUrlNull() {
        RestManager mgr = new RestManager();
        assertThrows(NullPointerException.class, () -> {
            mgr.patch(null, "user", null, null, PAYLOAD);
        });
    }

    @Test
    void testUsernameNull() {
        RestManager mgr = new RestManager();

        Pair<Integer, String> result = mgr.get(getUri, null, null, null);
        checkResult(result, "GOT: " + EXPECT_STRING);

        result = mgr.delete(deleteUri, null, null, null, null, null);
        checkResult(result, "DELETE: " + EXPECT_STRING);

        result = mgr.delete(deleteUri, null, null, null);
        checkResult(result, "DELETE: " + EXPECT_STRING);

        result = mgr.put(putUri, null, null, null, MediaType.TEXT_PLAIN, PAYLOAD);
        checkResult(result, "PUT: " + PAYLOAD + EXPECT_STRING);

        result = mgr.put(putUriBlank, null, null, null, MediaType.TEXT_PLAIN, PAYLOAD);
        checkResult(result, "PUT: " + PAYLOAD + RETURN_STRING);

        result = mgr.post(postUri, null, null, null, MediaType.TEXT_PLAIN, PAYLOAD);
        checkResult(result, "POST: " + PAYLOAD + EXPECT_STRING);

        result = mgr.post(postUriBlank, null, null, null, MediaType.TEXT_PLAIN, PAYLOAD);
        checkResult(result, "POST: " + PAYLOAD + RETURN_STRING);

        result = mgr.patch(patchUri, null, null, null, PAYLOAD);
        checkResult(result, "PATCH: " + PAYLOAD + EXPECT_STRING);

        result = mgr.patch(patchUriBlank, null, null, null, PAYLOAD);
        checkResult(result, "PATCH: " + PAYLOAD + RETURN_STRING);

    }

    private void checkResult(Pair<Integer, String> result, String expectedText) {
        assertEquals((Integer) 200, result.getLeft());
        assertNotNull(result.getRight());
        assertFalse(result.getRight().isEmpty());
        assertEquals(expectedText, result.getRight());
    }

    @Test
    void testUsernameEmpty() {
        RestManager mgr = new RestManager();

        Pair<Integer, String> result = mgr.get(getUri, "", null, null);
        checkResult(result, "GOT: " + EXPECT_STRING);

        result = mgr.delete(deleteUri, "", null, null, null, null);
        checkResult(result, "DELETE: " + EXPECT_STRING);

        result = mgr.put(putUri, "", null, null, MediaType.TEXT_PLAIN, PAYLOAD);
        checkResult(result, "PUT: " + PAYLOAD + EXPECT_STRING);

        result = mgr.put(putUriBlank, "", null, null, MediaType.TEXT_PLAIN, PAYLOAD);
        checkResult(result, "PUT: " + PAYLOAD + RETURN_STRING);

        result = mgr.post(postUri, "", null, null, MediaType.TEXT_PLAIN, PAYLOAD);
        checkResult(result, "POST: " + PAYLOAD + EXPECT_STRING);

        result = mgr.post(postUriBlank, "", null, null, MediaType.TEXT_PLAIN, PAYLOAD);
        checkResult(result, "POST: " + PAYLOAD + RETURN_STRING);

        result = mgr.patch(patchUri, "", null, null, PAYLOAD);
        checkResult(result, "PATCH: " + PAYLOAD + EXPECT_STRING);

        result = mgr.patch(patchUriBlank, "", null, null, PAYLOAD);
        checkResult(result, "PATCH: " + PAYLOAD + RETURN_STRING);

    }

    @Test
    void testGoodUrl() {
        RestManager mgr = new RestManager();

        Pair<Integer, String> result = mgr.get(getUri, "user", null, null);
        checkResult(result, "GOT: " + EXPECT_STRING);

        result = mgr.delete(deleteUri, "user", null, null, null, null);
        checkResult(result, "DELETE: " + EXPECT_STRING);

        result = mgr.put(putUri, "user", null, null, MediaType.TEXT_PLAIN, PAYLOAD);
        checkResult(result, "PUT: " + PAYLOAD + EXPECT_STRING);

        result = mgr.put(putUriBlank, "user", null, null, MediaType.TEXT_PLAIN, PAYLOAD);
        checkResult(result, "PUT: " + PAYLOAD + RETURN_STRING);

        result = mgr.post(postUri, "user", null, null, MediaType.TEXT_PLAIN, PAYLOAD);
        checkResult(result, "POST: " + PAYLOAD + EXPECT_STRING);

        result = mgr.post(postUriBlank, "user", null, null, MediaType.TEXT_PLAIN, PAYLOAD);
        checkResult(result, "POST: " + PAYLOAD + RETURN_STRING);

        result = mgr.patch(patchUri, "user", null, null, PAYLOAD);
        checkResult(result, "PATCH: " + PAYLOAD + EXPECT_STRING);

        result = mgr.patch(patchUriBlank, "user", null, null, PAYLOAD);
        checkResult(result, "PATCH: " + PAYLOAD + RETURN_STRING);

    }

    @Test
    void testNoUrlParamUrl() {
        RestManager mgr = new RestManager();

        Pair<Integer, String> result = mgr.get(baseUri + "RestTest/GetHello/", null, null, null);

        assertEquals((Integer) 404, result.getLeft());

        result = mgr.delete(baseUri + "RestTest/DeleteHello/", null, null, null, null, null);
        assertEquals((Integer) 404, result.getLeft());

        result = mgr.put(baseUri + "RestTest/PutHello/", null, null, null, MediaType.TEXT_PLAIN, PAYLOAD);
        assertEquals((Integer) 404, result.getLeft());

        result = mgr.post(baseUri + "RestTest/PostHello/", null, null, null, MediaType.TEXT_PLAIN, PAYLOAD);
        assertEquals((Integer) 404, result.getLeft());

        result = mgr.patch(baseUri + "RestTest/PatchHello/", null, null, null, PAYLOAD);
        assertEquals((Integer) 404, result.getLeft());

    }

    @Test
    void testNoQueryParamUrl() {
        RestManager mgr = new RestManager();

        Pair<Integer, String> result = mgr.get(baseUri + "RestTest/GetHello/" + NAME_PARAM, null, null, null);
        checkResult(result, "GOT: " + RETURN_STRING + NAME_PARAM + " aged 90");

        result = mgr.delete(baseUri + "RestTest/DeleteHello/" + NAME_PARAM, null, null, null, null, null);
        checkResult(result, "DELETE: " + RETURN_STRING + NAME_PARAM + " aged 90");

        result = mgr.put(baseUri + "RestTest/PutHello/" + NAME_PARAM, null, null, null, MediaType.TEXT_PLAIN, PAYLOAD);
        checkResult(result, "PUT: " + PAYLOAD + RETURN_STRING + NAME_PARAM + " aged 90");

        result = mgr.post(baseUri + "RestTest/PostHello/" + NAME_PARAM, null, null,
            null, MediaType.TEXT_PLAIN, PAYLOAD);
        checkResult(result, "POST: " + PAYLOAD + RETURN_STRING + NAME_PARAM + " aged 90");

        result = mgr.patch(baseUri + "RestTest/PatchHello/" + NAME_PARAM, null, null,
            null, PAYLOAD);
        checkResult(result, "PATCH: " + PAYLOAD + RETURN_STRING + NAME_PARAM + " aged 90");

    }

    @Test
    void testBadUrl() {
        RestManager mgr = new RestManager();

        Pair<Integer, String> result = mgr.get(baseUri + "NonExistant/URL/", null, null, null);

        assertEquals((Integer) 404, result.getLeft());

        result = mgr.delete(baseUri + "NonExistant/URL/", null, null, null, null, null);
        assertEquals((Integer) 404, result.getLeft());

        result = mgr.put(baseUri + "NonExistant/URL/", null, null, null, MediaType.TEXT_PLAIN, PAYLOAD);
        assertEquals((Integer) 404, result.getLeft());

        result = mgr.post(baseUri + "NonExistant/URL/", null, null, null, MediaType.TEXT_PLAIN, PAYLOAD);
        assertEquals((Integer) 404, result.getLeft());

        result = mgr.patch(baseUri + "NonExistant/URL/", null, null, null, PAYLOAD);
        assertEquals((Integer) 404, result.getLeft());

    }

    @Test
    void testWrongUrl() {
        RestManager mgr = new RestManager();

        Pair<Integer, String> result = mgr.get(deleteUri, null, null, null);

        assertEquals((Integer) 405, result.getLeft());

        result = mgr.delete(getUri, null, null, null, null, null);
        assertEquals((Integer) 405, result.getLeft());

        result = mgr.delete(getUri, null, null, null);
        assertEquals((Integer) 405, result.getLeft());

        result = mgr.put(getUri, null, null, null, MediaType.TEXT_PLAIN, PAYLOAD);
        assertEquals((Integer) 405, result.getLeft());

        result = mgr.post(getUri, null, null, null, MediaType.TEXT_PLAIN, PAYLOAD);
        assertEquals((Integer) 405, result.getLeft());

        result = mgr.patch(getUri, null, null, null, PAYLOAD);
        assertEquals((Integer) 405, result.getLeft());

    }

    @Test
    void testBody() {
        RestManager mgr = new RestManager();

        Pair<Integer, String> result = mgr.delete(getUri, null, null, null, null, "");
        assertEquals((Integer) 405, result.getLeft());

        Map<String, String> hm = new HashMap<String, String>();
        hm.put("header", "header");
        result = mgr.delete(getUri, null, null, hm, null, "");
        assertEquals((Integer) 405, result.getLeft());

        result = mgr.delete(deleteUri, "user", null, null, MediaType.TEXT_PLAIN, "body");
        checkResult(result, "DELETE: " + EXPECT_STRING);

        result = mgr.put(deleteUri, "user", null, null, "content", "body");
        assertEquals((Integer) 400, result.getLeft());
    }

    @GET
    @Path("/GetHello/{name}")
    @Produces(MediaType.TEXT_PLAIN)
    public String getIt(@PathParam("name") String name, @DefaultValue("90") @QueryParam("age") String age) {
        return "GOT: " + RETURN_STRING + name + " aged " + age;
    }

    @DELETE
    @Path("/DeleteHello/{name}")
    @Produces(MediaType.TEXT_PLAIN)
    public String deleteIt(@PathParam("name") String name, @DefaultValue("90") @QueryParam("age") String age) {
        return "DELETE: " + RETURN_STRING + name + " aged " + age;
    }

    @PUT
    @Path("/PutHello/{name}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public String putBlank(
        String payload,
        @PathParam("name") String name,
        @DefaultValue("90") @QueryParam("age") String age) {

        return "PUT: " + payload + RETURN_STRING + name + " aged " + age;
    }

    @PUT
    @Path("/PutBlank")
    @Produces(MediaType.TEXT_PLAIN)
    public String putIt(String payload) {
        return "PUT: " + payload + RETURN_STRING;
    }

    @POST
    @Path("/PostHello/{name}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public String postIt(
        String payload,
        @PathParam("name") String name,
        @DefaultValue("90") @QueryParam("age") String age) {

        return "POST: " + payload + RETURN_STRING + name + " aged " + age;
    }

    @POST
    @Path("/PostBlank")
    @Produces(MediaType.TEXT_PLAIN)
    public String postBlank(String payload) {
        return "POST: " + payload + RETURN_STRING;
    }

    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @HttpMethod("PATCH")
    @Documented
    public static @interface Patch {
    }

    @Patch
    @Path("/PatchHello/{name}")
    @Consumes(MERGE_PATCH_PLUS_JSON)
    @Produces(MERGE_PATCH_PLUS_JSON)
    public String patchIt(
        String payload,
        @PathParam("name") String name,
        @DefaultValue("90") @QueryParam("age") String age) {

        return "PATCH: " + payload + RETURN_STRING + name + " aged " + age;
    }

    @Patch
    @Path("/PatchBlank")
    @Produces(MERGE_PATCH_PLUS_JSON)
    public String patchBlank(String payload) {
        return "PATCH: " + payload + RETURN_STRING;
    }
}
