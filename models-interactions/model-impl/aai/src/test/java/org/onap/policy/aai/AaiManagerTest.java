/*-
 * ============LICENSE_START=======================================================
 * aai
 * ================================================================================
 * Copyright (C) 2017-2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019-2024 Nordix Foundation.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onap.policy.rest.RestManager;

public class AaiManagerTest {
    private static final String CQ_QUERY_URL = "http://testing.cq.query";
    private static final String DOROTHY = "Dorothy";
    private static final String SOME_URL = "http://somewhere.over.the.rainbow";
    private static final String TENANT_RESPONSE_SAMPLE =
            "src/test/resources/org/onap/policy/aai/AaiTenantResponse.json";

    private RestManager restManagerMock;
    private Pair<Integer, String> httpResponseErr0;
    private Pair<Integer, String> httpResponseErr1;
    private Pair<Integer, String> httpTenantResponseOk;
    private Pair<Integer, String> httpCqResponseOk;

    /**
     * Set up test cases.
     *
     * @throws Exception if error occurs
     */
    @BeforeEach
    public void beforeTestAaiManager() throws Exception {
        restManagerMock = mock(RestManager.class);

        String aaiCqResponse = new AaiCqResponseTest().getAaiCqResponse();
        String tenantResponse = this.getTenantQueryResponse();
        httpCqResponseOk = Pair.of(200, aaiCqResponse);
        httpTenantResponseOk = Pair.of(200, tenantResponse);
        httpResponseErr0 = Pair.of(200, null);
        httpResponseErr1 = Pair.of(200, "{");

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

        AaiCqResponse aaiCqResponseNull =
                aaiManager.getCustomQueryResponse("", "Foo", "Bar", vserverNameRequestId, "Foo");
        assertNull(aaiCqResponseNull);


        when(restManagerMock.put(eq("Error"), eq("Foo"), eq("Bar"), anyMap(), anyString(), anyString()))
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
    public void testAaiManagerGetPnf() {
        AaiManager aaiManager = new AaiManager(restManagerMock);
        assertNotNull(aaiManager);
        String pnfName = "test-pnf";
        String pnfResponse = "{\"pnf-name\":" + pnfName
                                     + ",\"pnf-id\":\"123456\",\"in-maint\":false,\"ipaddress-v4-oam\":\"1.1.1.1\"}";

        Pair<Integer, String> pnfHttpResponse = Pair.of(200, pnfResponse);
        when(restManagerMock.get(contains(pnfName), eq(DOROTHY), eq("Gale"), anyMap()))
                .thenReturn(pnfHttpResponse);

        Map<String, String> pnfParams = aaiManager.getPnf(SOME_URL, DOROTHY, "Gale", UUID.randomUUID(), pnfName);
        assertNotNull(pnfParams);
        assertEquals(pnfName, pnfParams.get("pnf.pnf-name"));
    }
}
