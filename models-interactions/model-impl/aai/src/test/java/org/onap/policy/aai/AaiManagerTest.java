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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.onap.policy.aai.util.Serialization;
import org.onap.policy.rest.RestManager;
import org.onap.policy.rest.RestManager.Pair;

public class AaiManagerTest {
    RestManager restManagerMock;
    UUID aaiNqRequestUuid = UUID.randomUUID();
    Pair<Integer, String> httpResponseOk;
    Pair<Integer, String> httpResponseErr0;
    Pair<Integer, String> httpResponseErr1;
    Pair<Integer, String> httpResponseWait;

    /**
     * Set up test cases.
     */
    @Before
    public void beforeTestAaiManager() {
        restManagerMock = mock(RestManager.class);

        Map<String, String> expectedHeaders = new HashMap<>();
        expectedHeaders.put("X-FromAppId", "POLICY");
        expectedHeaders.put("X-TransactionId", aaiNqRequestUuid.toString());
        expectedHeaders.put("Accept", "application/json");

        AaiNqResponse aaiNqResponse = new AaiNqResponseTest().getAaiNqResponse();
        httpResponseOk = restManagerMock.new Pair<>(200, Serialization.gsonPretty.toJson(aaiNqResponse));
        httpResponseErr0 = restManagerMock.new Pair<>(200, null);
        httpResponseErr1 = restManagerMock.new Pair<>(200, "{");
        httpResponseWait = restManagerMock.new Pair<>(503, null);
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

        when(restManagerMock.post(startsWith("http://somewhere.over.the.rainbow"), eq("Dorothy"), eq("Gale"), anyMap(),
                anyString(), anyString())).thenReturn(httpResponseOk);

        AaiNqResponse aaiNqOkResponse = aaiManager.postQuery("http://somewhere.over.the.rainbow", "Dorothy", "Gale",
                aaiNqRequest, aaiNqRequestUuid);
        assertNotNull(aaiNqOkResponse);

        when(restManagerMock.post(isNull(), eq("Dorothy"), anyString(), anyMap(), anyString(), anyString()))
                .thenReturn(null);

        AaiNqResponse aaiNqNullResponse = aaiManager.postQuery(null, "Dorothy", "Gale", null, aaiNqRequestUuid);
        assertNull(aaiNqNullResponse);

        when(restManagerMock.post(startsWith("http://somewhere.over.the.rainbow"), eq("Witch"), eq("West"), anyMap(),
                anyString(), anyString())).thenReturn(httpResponseErr0);

        AaiNqResponse aaiNqNotOkResponse0 = aaiManager.postQuery("http://somewhere.over.the.rainbow", "Witch", "West",
                aaiNqRequest, aaiNqRequestUuid);
        assertNull(aaiNqNotOkResponse0);

        when(restManagerMock.post(startsWith("http://somewhere.under.the.rainbow"), eq("Witch"), eq("West"), anyMap(),
                anyString(), anyString())).thenReturn(httpResponseErr1);

        AaiNqResponse aaiNqNotOkResponse1 = aaiManager.postQuery("http://somewhere.under.the.rainbow", "Witch", "West",
                aaiNqRequest, aaiNqRequestUuid);
        assertNull(aaiNqNotOkResponse1);
    }

    @Test
    public void testAaiManagerQueryByVserverName() {
        AaiManager aaiManager = new AaiManager(restManagerMock);
        assertNotNull(aaiManager);

        UUID vserverNameRequestId = UUID.randomUUID();

        when(restManagerMock.get(startsWith("http://somewhere.over.the.rainbow"), eq("Dorothy"), eq("Gale"), anyMap()))
                .thenReturn(httpResponseOk);

        AaiGetVserverResponse vserverResponse = aaiManager.getQueryByVserverName("http://somewhere.over.the.rainbow",
                "Dorothy", "Gale", vserverNameRequestId, "vserverName");
        assertNotNull(vserverResponse);

        AaiGetVserverResponse vserverNullResponse =
                aaiManager.getQueryByVserverName(null, "Dorothy", "Gale", vserverNameRequestId, "vserverName");
        assertNull(vserverNullResponse);

        when(restManagerMock.get(startsWith("http://somewhere.under.the.rainbow"), eq("Witch"), eq("West"), anyMap()))
                .thenReturn(httpResponseErr0);

        AaiGetVserverResponse vserverNotOkResponse0 = aaiManager.getQueryByVserverName(
                "http://somewhere.under.the.rainbow", "Witch", "West", vserverNameRequestId, "vserverName");
        assertNull(vserverNotOkResponse0);
    }

    @Test
    public void testAaiManagerQueryByVnfId() {
        AaiManager aaiManager = new AaiManager(restManagerMock);
        assertNotNull(aaiManager);

        UUID vserverNameRequestId = UUID.randomUUID();

        when(restManagerMock.get(startsWith("http://somewhere.over.the.rainbow"), eq("Dorothy"), eq("Gale"), anyMap()))
                .thenReturn(httpResponseOk);

        AaiGetVnfResponse vnfResponse = aaiManager.getQueryByVnfId("http://somewhere.over.the.rainbow", "Dorothy",
                "Gale", vserverNameRequestId, "vnfID");
        assertNotNull(vnfResponse);
    }

    @Test
    public void testAaiManagerQueryByVnfName() {
        AaiManager aaiManager = new AaiManager(restManagerMock);
        assertNotNull(aaiManager);

        UUID vserverNameRequestId = UUID.randomUUID();

        when(restManagerMock.get(startsWith("http://somewhere.over.the.rainbow"), eq("Dorothy"), eq("Gale"), anyMap()))
                .thenReturn(httpResponseOk);

        AaiGetVnfResponse vnfResponse = aaiManager.getQueryByVnfId("http://somewhere.over.the.rainbow", "Dorothy",
                "Gale", vserverNameRequestId, "vnfName");
        assertNotNull(vnfResponse);
    }
}
