/*
 * ============LICENSE_START=======================================================
 * rest
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019 Nordix Foundation.
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

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.concurrent.Callable;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.awaitility.Duration;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.policy.rest.RestManager.Pair;

@Path("RestTest")
public class RestTest {

    private static final String BASE_URI = "http://localhost:32802/base/";
    private static final String NAME_PARAM = "Bob";
    private static final String AGE_PARAM = "10";
    private static final String PAYLOAD = "At last! ";
    private static final String RETURN_STRING = "Hello There ";
    private static final String GET_URI = BASE_URI + "RestTest/GetHello/" + NAME_PARAM + "?age=" + AGE_PARAM;
    private static final String PUT_URI = BASE_URI + "RestTest/PutHello/" + NAME_PARAM + "?age=" + AGE_PARAM;
    private static final String PUT_URI_BLANK = BASE_URI + "RestTest/PutBlank";
    private static final String POST_URI = BASE_URI + "RestTest/PostHello/" + NAME_PARAM + "?age=" + AGE_PARAM;
    private static final String POST_URI_BLANK = BASE_URI + "RestTest/PostBlank";
    private static final String EXPECT_STRING = RETURN_STRING + NAME_PARAM + " aged " + AGE_PARAM;

    private static HttpServer server;

    /**
     * Sets server endpoint for the tests.
     */
    @BeforeClass
    public static void setUp() throws Exception {
        server = GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), new ResourceConfig(RestTest.class));
        await().atMost(Duration.FIVE_SECONDS).until( new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return server.isStarted();
            }
        });
    }

    /**
     * Tear down server endpoint for the tests.
     *
     * @throws Exception if there is a problem
     */
    @AfterClass
    public static void tearDown() throws Exception {
        server.shutdownNow();
    }

    @Test(expected = NullPointerException.class)
    public void testGetUrlNull() {
        RestManager mgr = new RestManager();
        mgr.get(null, "user", null, null);
    }

    @Test(expected = NullPointerException.class)
    public void testPutUrlNull() {
        RestManager mgr = new RestManager();
        mgr.put(null, "user", null, null, MediaType.TEXT_PLAIN, PAYLOAD);
    }

    @Test(expected = NullPointerException.class)
    public void testPostUrlNull() {
        RestManager mgr = new RestManager();
        mgr.post(null, "user", null, null, MediaType.TEXT_PLAIN, PAYLOAD);
    }

    @Test
    public void testUsernameNull() {
        RestManager mgr = new RestManager();

        Pair<Integer, String> result = mgr.get(GET_URI, null, null, null);
        assertEquals((Integer)200, result.first);
        assertTrue(result.second != null);
        assertTrue(result.second.length() > 0);
        assertEquals("GOT: " + EXPECT_STRING, result.second);

        result = mgr.put(PUT_URI, null, null, null, MediaType.TEXT_PLAIN, PAYLOAD);
        assertEquals((Integer)200, result.first);
        assertTrue(result.second != null);
        assertTrue(result.second.length() > 0);
        assertEquals("PUT: " + PAYLOAD + EXPECT_STRING, result.second);

        result = mgr.put(PUT_URI_BLANK, null, null, null, MediaType.TEXT_PLAIN, PAYLOAD);
        assertEquals((Integer)200, result.first);
        assertTrue(result.second != null);
        assertTrue(result.second.length() > 0);
        assertEquals("PUT: " + PAYLOAD + RETURN_STRING, result.second);

        result = mgr.post(POST_URI, null, null, null, MediaType.TEXT_PLAIN, PAYLOAD);
        assertEquals((Integer)200, result.first);
        assertTrue(result.second != null);
        assertTrue(result.second.length() > 0);
        assertEquals("POST: " + PAYLOAD + EXPECT_STRING, result.second);

        result = mgr.post(POST_URI_BLANK, null, null, null, MediaType.TEXT_PLAIN, PAYLOAD);
        assertEquals((Integer)200, result.first);
        assertTrue(result.second != null);
        assertTrue(result.second.length() > 0);
        assertEquals("POST: " + PAYLOAD + RETURN_STRING, result.second);

    }

    @Test
    public void testUsernameEmpty() {
        RestManager mgr = new RestManager();

        Pair<Integer, String> result = mgr.get(GET_URI, "", null, null);
        assertEquals((Integer)200, result.first);
        assertTrue(result.second != null);
        assertTrue(result.second.length() > 0);
        assertEquals("GOT: " + EXPECT_STRING, result.second);

        result = mgr.put(PUT_URI, "", null, null, MediaType.TEXT_PLAIN, PAYLOAD);
        assertEquals((Integer)200, result.first);
        assertTrue(result.second != null);
        assertTrue(result.second.length() > 0);
        assertEquals("PUT: " + PAYLOAD + EXPECT_STRING, result.second);

        result = mgr.put(PUT_URI_BLANK, "", null, null, MediaType.TEXT_PLAIN, PAYLOAD);
        assertEquals((Integer)200, result.first);
        assertTrue(result.second != null);
        assertTrue(result.second.length() > 0);
        assertEquals("PUT: " + PAYLOAD + RETURN_STRING, result.second);

        result = mgr.post(POST_URI, "", null, null, MediaType.TEXT_PLAIN, PAYLOAD);
        assertEquals((Integer)200, result.first);
        assertTrue(result.second != null);
        assertTrue(result.second.length() > 0);
        assertEquals("POST: " + PAYLOAD + EXPECT_STRING, result.second);

        result = mgr.post(POST_URI_BLANK, "", null, null, MediaType.TEXT_PLAIN, PAYLOAD);
        assertEquals((Integer)200, result.first);
        assertTrue(result.second != null);
        assertTrue(result.second.length() > 0);
        assertEquals("POST: " + PAYLOAD + RETURN_STRING, result.second);
    }

    @Test
    public void testGoodUrl() {
        RestManager mgr = new RestManager();

        Pair<Integer, String> result = mgr.get(GET_URI, "user", null, null);
        assertEquals((Integer)200, result.first);
        assertTrue(result.second != null);
        assertTrue(result.second.length() > 0);
        assertEquals("GOT: " + EXPECT_STRING, result.second);

        result = mgr.put(PUT_URI, "user", null, null, MediaType.TEXT_PLAIN, PAYLOAD);
        assertEquals((Integer)200, result.first);
        assertTrue(result.second != null);
        assertTrue(result.second.length() > 0);
        assertEquals("PUT: " + PAYLOAD + EXPECT_STRING, result.second);

        result = mgr.put(PUT_URI_BLANK, "user", null, null, MediaType.TEXT_PLAIN, PAYLOAD);
        assertEquals((Integer)200, result.first);
        assertTrue(result.second != null);
        assertTrue(result.second.length() > 0);
        assertEquals("PUT: " + PAYLOAD + RETURN_STRING, result.second);

        result = mgr.post(POST_URI, "user", null, null, MediaType.TEXT_PLAIN, PAYLOAD);
        assertEquals((Integer)200, result.first);
        assertTrue(result.second != null);
        assertTrue(result.second.length() > 0);
        assertEquals("POST: " + PAYLOAD + EXPECT_STRING, result.second);

        result = mgr.post(POST_URI_BLANK, "user", null, null, MediaType.TEXT_PLAIN, PAYLOAD);
        assertEquals((Integer)200, result.first);
        assertTrue(result.second != null);
        assertTrue(result.second.length() > 0);
        assertEquals("POST: " + PAYLOAD + RETURN_STRING, result.second);
    }

    @Test
    public void testNoUrlParamUrl() {
        RestManager mgr = new RestManager();

        Pair<Integer, String> result = mgr.get(BASE_URI + "RestTest/GetHello/", null, null, null);
        assertEquals((Integer)404, result.first);
        assertTrue(result.second != null);
        assertTrue(result.second.length() == 0);

        result = mgr.put(BASE_URI + "RestTest/PutHello/", null, null, null, MediaType.TEXT_PLAIN, PAYLOAD);
        assertEquals((Integer)404, result.first);
        assertTrue(result.second != null);
        assertTrue(result.second.length() == 0);

        result = mgr.post(BASE_URI + "RestTest/PostHello/", null, null, null, MediaType.TEXT_PLAIN, PAYLOAD);
        assertEquals((Integer)404, result.first);
        assertTrue(result.second != null);
        assertTrue(result.second.length() == 0);
    }

    @Test
    public void testNoQueryParamUrl() {
        RestManager mgr = new RestManager();

        Pair<Integer, String> result = mgr.get(BASE_URI + "RestTest/GetHello/" + NAME_PARAM, null, null, null);
        assertEquals((Integer)200, result.first);
        assertTrue(result.second != null);
        assertTrue(result.second.length() > 0);
        assertEquals("GOT: " + RETURN_STRING + NAME_PARAM + " aged 90", result.second);

        result = mgr.put(BASE_URI + "RestTest/PutHello/" + NAME_PARAM, null, null, null, MediaType.TEXT_PLAIN, PAYLOAD);
        assertEquals((Integer)200, result.first);
        assertTrue(result.second != null);
        assertTrue(result.second.length() > 0);
        assertEquals("PUT: " + PAYLOAD + RETURN_STRING + NAME_PARAM + " aged 90", result.second);

        result = mgr.post(BASE_URI + "RestTest/PostHello/" + NAME_PARAM, null, null,
            null, MediaType.TEXT_PLAIN, PAYLOAD);
        assertEquals((Integer)200, result.first);
        assertTrue(result.second != null);
        assertTrue(result.second.length() > 0);
        assertEquals("POST: " + PAYLOAD + RETURN_STRING + NAME_PARAM + " aged 90", result.second);
    }

    @Test
    public void testBadUrl() {
        RestManager mgr = new RestManager();

        Pair<Integer, String> result = mgr.get(BASE_URI + "NonExistant/URL/", null, null, null);
        assertEquals((Integer)404, result.first);
        assertTrue(result.second != null);
        assertTrue(result.second.length() == 0);

        result = mgr.put(BASE_URI + "NonExistant/URL/", null, null, null, MediaType.TEXT_PLAIN, PAYLOAD);
        assertEquals((Integer)404, result.first);
        assertTrue(result.second != null);
        assertTrue(result.second.length() == 0);

        result = mgr.post(BASE_URI + "NonExistant/URL/", null, null, null, MediaType.TEXT_PLAIN, PAYLOAD);
        assertEquals((Integer)404, result.first);
        assertTrue(result.second != null);
        assertTrue(result.second.length() == 0);
    }

    @GET
    @Path("/GetHello/{name}")
    @Produces(MediaType.TEXT_PLAIN)
    public String getIt( @PathParam("name") String name, @DefaultValue("90") @QueryParam("age") String age) {
        return "GOT: " + RETURN_STRING + name + " aged " + age;
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
    public String putIt( String payload) {
        return "PUT: " + payload + RETURN_STRING;
    }

    @POST
    @Path("/PostHello/{name}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public String postBlank(
        String payload,
        @PathParam("name") String name,
        @DefaultValue("90") @QueryParam("age") String age) {

        return "POST: " + payload + RETURN_STRING + name + " aged " + age;
    }

    @POST
    @Path("/PostBlank")
    @Produces(MediaType.TEXT_PLAIN)
    public String postIt( String payload) {
        return "POST: " + payload + RETURN_STRING;
    }
}
