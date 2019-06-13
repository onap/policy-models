/*-
 * ============LICENSE_START=======================================================
 * aai
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

package org.onap.policy.aai;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.aai.util.Serialization;
import org.onap.policy.rest.RestManager;
import org.onap.policy.rest.RestManager.Pair;

public class AaiManagerTest {
    private static final String VSERVER_NAME = "vserverName";
    private static final String CQ_QUERY_URL = "http://testing.cq.query";
    private static final String WITCH = "Witch";
    private static final String DOROTHY = "Dorothy";
    private static final String SOME_URL = "http://somewhere.over.the.rainbow";
    private static final String ANOTHER_URL = "http://somewhere.under.the.rainbow";
    RestManager restManagerMock;
    UUID aaiNqRequestUuid = UUID.randomUUID();
    Pair<Integer, String> httpResponseOk;
    Pair<Integer, String> httpResponseErr0;
    Pair<Integer, String> httpResponseErr1;
    Pair<Integer, String> httpResponseWait;
    Pair<Integer, String> httpTenantResponseOk;
    Pair<Integer, String> httpCqResponseOk;

    private static final String TENANT_RESPONSE_SAMPLE =
            "src/test/resources/org/onap/policy/aai/AaiTenantResponse.json";



    /**
     * Set up test cases.
     *
     * @throws Exception if error occurs
     */
    @Before
    public void beforeTestAaiManager() throws Exception {
        restManagerMock = mock(RestManager.class);

        Map<String, String> expectedHeaders = new HashMap<>();
        expectedHeaders.put("X-FromAppId", "POLICY");
        expectedHeaders.put("X-TransactionId", aaiNqRequestUuid.toString());
        expectedHeaders.put("Accept", "application/json");

        String aaiCqResponse = new AaiCqResponseTest().getAaiCqResponse();
        String tenantResponse = this.getTenantQueryResponse();
        httpCqResponseOk = restManagerMock.new Pair<>(200, aaiCqResponse);
        httpTenantResponseOk = restManagerMock.new Pair<>(200, tenantResponse);
        AaiNqResponse aaiNqResponse = new AaiNqResponseTest().getAaiNqResponse();
        httpResponseOk = restManagerMock.new Pair<>(200, Serialization.gsonPretty.toJson(aaiNqResponse));
        httpResponseErr0 = restManagerMock.new Pair<>(200, null);
        httpResponseErr1 = restManagerMock.new Pair<>(200, "{");
        httpResponseWait = restManagerMock.new Pair<>(503, null);

    }


    @Test
    public void testAaiCqResponse() {
        AaiManager aaiManager = new AaiManager(restManagerMock);
        assertNotNull(aaiManager);

        UUID vserverNameRequestId = UUID.randomUUID();

        when(restManagerMock.put(startsWith(CQ_QUERY_URL), eq("Foo"), eq("Bar"), anyMap(), anyString(),
                anyString())).thenReturn(httpCqResponseOk);

        when(restManagerMock.get(startsWith(CQ_QUERY_URL), eq("Foo"), eq("Bar"), anyMap()))
                .thenReturn(httpTenantResponseOk);

        AaiCqResponse aaiCqResponse =
                aaiManager.getCustomQueryResponse(CQ_QUERY_URL, "Foo", "Bar", vserverNameRequestId, "Foo");
        assertNotNull(aaiCqResponse);

        when(restManagerMock.put(eq(""), eq("Foo"), eq("Bar"), anyMap(), anyString(), anyString()))
                .thenReturn(httpResponseErr0);

        when(restManagerMock.get(eq("/aai/v11/query?format=resource"), eq("Foo"), eq("Bar"), anyMap()))
                .thenReturn(httpResponseErr0);

        AaiCqResponse aaiCqResponseNull =
                aaiManager.getCustomQueryResponse("", "Foo", "Bar", vserverNameRequestId, "Foo");
        assertNull(aaiCqResponseNull);


        when(restManagerMock.put(eq("Error"), eq("Foo"), eq("Bar"), anyMap(), anyString(), anyString()))
                .thenReturn(httpResponseErr1);

        when(restManagerMock.get(eq("Error/aai/v11/query?format=resource"), eq("Foo"), eq("Bar"), anyMap()))
                .thenReturn(httpResponseErr1);

        AaiCqResponse aaiCqResponseErr =
                aaiManager.getCustomQueryResponse("Error", "Foo", "Bar", vserverNameRequestId, "Foo");
        assertNull(aaiCqResponseErr);
    }

    private String getTenantQueryResponse() throws IOException {
        String responseString = "";
        responseString = new String(Files.readAllBytes(new File(TENANT_RESPONSE_SAMPLE).toPath()));
        return responseString;
    }

    @Test
    public void testAaiManagerAaiNqRequest() {

        AaiManager aaiManager = new AaiManager(restManagerMock);
        assertNotNull(aaiManager);

        UUID aaiNqUuid = UUID.randomUUID();

        AaiNqQueryParameters aaiNqQueryParameters = new AaiNqQueryParameters();
        AaiNqNamedQuery aaiNqNamedQuery = new AaiNqNamedQuery();
        aaiNqNamedQuery.setNamedQueryUuid(aaiNqUuid);
        aaiNqQueryParameters.setNamedQuery(aaiNqNamedQuery);

        AaiNqRequest aaiNqRequest = new AaiNqRequest();
        aaiNqRequest.setQueryParameters(aaiNqQueryParameters);

        when(restManagerMock.post(startsWith(SOME_URL), eq(DOROTHY), eq("Gale"), anyMap(),
                anyString(), anyString())).thenReturn(httpResponseOk);

        AaiNqResponse aaiNqOkResponse = aaiManager.postQuery(SOME_URL, DOROTHY, "Gale",
                aaiNqRequest, aaiNqRequestUuid);
        assertNotNull(aaiNqOkResponse);

        when(restManagerMock.post(isNull(), eq(DOROTHY), anyString(), anyMap(), anyString(), anyString()))
                .thenReturn(null);

        AaiNqResponse aaiNqNullResponse = aaiManager.postQuery(null, DOROTHY, "Gale", null, aaiNqRequestUuid);
        assertNull(aaiNqNullResponse);

        when(restManagerMock.post(startsWith(SOME_URL), eq(WITCH), eq("West"), anyMap(),
                anyString(), anyString())).thenReturn(httpResponseErr0);

        AaiNqResponse aaiNqNotOkResponse0 = aaiManager.postQuery(SOME_URL, WITCH, "West",
                aaiNqRequest, aaiNqRequestUuid);
        assertNull(aaiNqNotOkResponse0);

        when(restManagerMock.post(startsWith(ANOTHER_URL), eq(WITCH), eq("West"), anyMap(),
                anyString(), anyString())).thenReturn(httpResponseErr1);

        AaiNqResponse aaiNqNotOkResponse1 = aaiManager.postQuery(ANOTHER_URL, WITCH, "West",
                aaiNqRequest, aaiNqRequestUuid);
        assertNull(aaiNqNotOkResponse1);
    }

    @Test
    public void testAaiManagerQueryByVserverName() {
        AaiManager aaiManager = new AaiManager(restManagerMock);
        assertNotNull(aaiManager);

        UUID vserverNameRequestId = UUID.randomUUID();

        when(restManagerMock.get(startsWith(SOME_URL), eq(DOROTHY), eq("Gale"), anyMap()))
                .thenReturn(httpResponseOk);

        AaiGetVserverResponse vserverResponse = aaiManager.getQueryByVserverName(SOME_URL,
                DOROTHY, "Gale", vserverNameRequestId, VSERVER_NAME);
        assertNotNull(vserverResponse);

        AaiGetVserverResponse vserverNullResponse =
                aaiManager.getQueryByVserverName(null, DOROTHY, "Gale", vserverNameRequestId, VSERVER_NAME);
        assertNull(vserverNullResponse);

        when(restManagerMock.get(startsWith(ANOTHER_URL), eq(WITCH), eq("West"), anyMap()))
                .thenReturn(httpResponseErr0);

        AaiGetVserverResponse vserverNotOkResponse0 = aaiManager.getQueryByVserverName(
                ANOTHER_URL, WITCH, "West", vserverNameRequestId, VSERVER_NAME);
        assertNull(vserverNotOkResponse0);
    }

    @Test
    public void testAaiManagerQueryByVnfId() {
        AaiManager aaiManager = new AaiManager(restManagerMock);
        assertNotNull(aaiManager);

        UUID vserverNameRequestId = UUID.randomUUID();

        when(restManagerMock.get(startsWith(SOME_URL), eq(DOROTHY), eq("Gale"), anyMap()))
                .thenReturn(httpResponseOk);

        AaiGetVnfResponse vnfResponse = aaiManager.getQueryByVnfId(SOME_URL, DOROTHY,
                "Gale", vserverNameRequestId, "vnfID");
        assertNotNull(vnfResponse);
    }

    @Test
    public void testAaiManagerQueryByVnfName() {
        AaiManager aaiManager = new AaiManager(restManagerMock);
        assertNotNull(aaiManager);

        UUID vserverNameRequestId = UUID.randomUUID();

        when(restManagerMock.get(startsWith(SOME_URL), eq(DOROTHY), eq("Gale"), anyMap()))
                .thenReturn(httpResponseOk);

        AaiGetVnfResponse vnfResponse = aaiManager.getQueryByVnfId(SOME_URL, DOROTHY,
                "Gale", vserverNameRequestId, "vnfName");
        assertNotNull(vnfResponse);
    }
}
