/*-
 * ============LICENSE_START=======================================================
 * vfc
 * ================================================================================
 * Copyright (C) 2018-2020 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019-2020 Nordix Foundation.. All rights reserved.
 * Modifications Copyright (C) 2018-2019 AT&T Corporation. All rights reserved.
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

package org.onap.policy.vfc;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.rest.RestManager;
import org.onap.policy.vfc.VfcManager.VfcCallback;
import org.onap.policy.vfc.util.Serialization;

public class VfcManagerTest implements VfcCallback {

    private static final String SOME_URL = "http://somewhere.over.the.rainbow";

    private static final String DOROTHY = "Dorothy";

    private RestManager   mockedRestManager;

    private Pair<Integer, String> httpResponsePutOk;
    private Pair<Integer, String> httpResponseBadResponse;
    private Pair<Integer, String> httpResponseErr;

    private VfcRequest  request;
    private VfcResponse response;

    /**
     * Set up the mocked REST manager.
     */
    @Before
    public void setupMockedRest() {
        mockedRestManager   = mock(RestManager.class);

        httpResponsePutOk       = Pair.of(202, Serialization.gsonPretty.toJson(response));
        httpResponseBadResponse = Pair.of(202, Serialization.gsonPretty.toJson(null));
        httpResponseErr         = Pair.of(200, null);
    }

    /**
     * Create the request and response before.
     */
    @Before
    public void createRequestAndResponse() {
        VfcHealActionVmInfo actionInfo = new VfcHealActionVmInfo();
        actionInfo.setVmid("TheWizard");
        actionInfo.setVmname("The Wizard of Oz");

        VfcHealAdditionalParams additionalParams = new VfcHealAdditionalParams();
        additionalParams.setAction("Go Home");
        additionalParams.setActionInfo(actionInfo);

        VfcHealRequest healRequest = new VfcHealRequest();
        healRequest.setAdditionalParams(additionalParams);
        healRequest.setCause("WestWitch");
        healRequest.setVnfInstanceId("EmeraldCity");

        final UUID requestId = UUID.randomUUID();
        request = new VfcRequest();
        request.setHealRequest(healRequest);
        request.setNsInstanceId(DOROTHY);
        request.setRequestId(requestId);

        List<VfcResponseDescriptor> responseHistoryList = new ArrayList<>();;

        VfcResponseDescriptor responseDescriptor = new VfcResponseDescriptor();
        responseDescriptor.setErrorCode("1234");
        responseDescriptor.setProgress("Follow The Yellow Brick Road");
        responseDescriptor.setResponseHistoryList(responseHistoryList);
        responseDescriptor.setResponseId(UUID.randomUUID().toString());
        responseDescriptor.setStatus("finished");
        responseDescriptor.setStatusDescription("There's no place like home");

        response = new VfcResponse();
        response.setJobId("1234");
        response.setRequestId(request.getRequestId().toString());
        response.setResponseDescriptor(responseDescriptor);
    }

    @Test
    public void testVfcInitiation() {
        assertThatIllegalArgumentException().isThrownBy(() -> new VfcManager(null, null, null, null, null)).withMessage(
                        "the parameters \"cb\" and \"request\" on the VfcManager constructor may not be null");

        assertThatIllegalArgumentException().isThrownBy(() -> new VfcManager(this, null, null, null, null)).withMessage(
                        "the parameters \"cb\" and \"request\" on the VfcManager constructor may not be null");

        assertThatIllegalArgumentException().isThrownBy(() -> new VfcManager(this, request, null, null, null))
                        .withMessage("the \"url\" parameter on the VfcManager constructor may not be null");

        new VfcManager(this, request, SOME_URL, null, null);

        new VfcManager(this, request, SOME_URL, DOROTHY, "Toto");
    }

    @Test
    public void testVfcExecutionException() throws InterruptedException {
        VfcManager manager = new VfcManager(this, request, SOME_URL, DOROTHY, "Exception");
        manager.setRestManager(mockedRestManager);

        when(mockedRestManager.post(
            startsWith(SOME_URL),
            eq(DOROTHY),
            eq("Exception"),
            anyMap(),
            anyString(),
            anyString()))
            .thenThrow(new RuntimeException("OzException"));

        manager.run();

        verify(mockedRestManager).post(any(), any(), any(), any(), any(), any());
    }

    @Test
    public void testVfcExecutionNull() throws InterruptedException {
        VfcManager manager = new VfcManager(this, request, SOME_URL, DOROTHY, "Null");
        manager.setRestManager(mockedRestManager);

        when(mockedRestManager.post(startsWith(SOME_URL),
                eq(DOROTHY), eq("Null"), anyMap(), anyString(), anyString()))
                .thenReturn(null);

        manager.run();

        verify(mockedRestManager).post(any(), any(), any(), any(), any(), any());
    }

    @Test
    public void testVfcExecutionError0() throws InterruptedException {
        VfcManager manager = new VfcManager(this, request, SOME_URL, DOROTHY, "Error0");
        manager.setRestManager(mockedRestManager);

        when(mockedRestManager.post(startsWith(SOME_URL),
                eq(DOROTHY), eq("Error0"), anyMap(), anyString(), anyString()))
                .thenReturn(httpResponseErr);

        manager.run();

        verify(mockedRestManager).post(any(), any(), any(), any(), any(), any());
    }

    @Test
    public void testVfcExecutionBadResponse() throws InterruptedException {
        VfcManager manager = new VfcManager(this, request, SOME_URL, DOROTHY, "BadResponse");
        manager.setRestManager(mockedRestManager);

        when(mockedRestManager.post(startsWith(SOME_URL),
                eq(DOROTHY), eq("OK"), anyMap(), anyString(), anyString()))
                .thenReturn(httpResponseBadResponse);

        manager.run();

        verify(mockedRestManager).post(any(), any(), any(), any(), any(), any());
    }

    @Test
    public void testVfcExecutionOk() throws InterruptedException {
        VfcManager manager = new VfcManager(this, request, SOME_URL, DOROTHY, "Ok");
        manager.setRestManager(mockedRestManager);

        when(mockedRestManager.post(startsWith(SOME_URL),
                eq(DOROTHY), eq("OK"), anyMap(), anyString(), anyString()))
                .thenReturn(httpResponsePutOk);

        manager.run();

        verify(mockedRestManager).post(any(), any(), any(), any(), any(), any());
    }

    @Override
    public void onResponse(VfcResponse responseError) {
        //
        // Nothing needs to be done
        //
    }
}
