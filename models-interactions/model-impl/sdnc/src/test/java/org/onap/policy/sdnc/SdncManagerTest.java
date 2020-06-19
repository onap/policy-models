/*-
 * ============LICENSE_START=======================================================
 * sdnc
 * ================================================================================
 * Copyright (C) 2018 Huawei. All rights reserved.
 * ================================================================================
 * Modifications Copyright (C) 2019-2020 AT&T Intellectual Property. All rights reserved
 * Modifications Copyright (C) 2019-2020 Nordix Foundation.
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

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.utils.resources.Pair;
import org.onap.policy.rest.RestManager;
import org.onap.policy.sdnc.SdncManager.SdncCallback;
import org.onap.policy.sdnc.util.Serialization;

public class SdncManagerTest implements SdncCallback {
    private static final String SOMEWHERE_OVER_THE_RAINBOW = "http://somewhere.over.the.rainbow";

    private static final String DOROTHY = "Dorothy";

    private RestManager   mockedRestManager;

    private Pair<Integer, String> httpResponsePutOk;
    private Pair<Integer, String> httpResponseBadResponse;
    private Pair<Integer, String> httpResponseErr;

    private SdncRequest  request;
    private SdncResponse response;

    /**
     * Set up the mocked REST manager.
     */
    @Before
    public void setupMockedRest() {
        mockedRestManager   = mock(RestManager.class);

        httpResponsePutOk       = new Pair<>(202, Serialization.gsonPretty.toJson(response));
        httpResponseBadResponse = new Pair<>(202, Serialization.gsonPretty.toJson(null));
        httpResponseErr         = new Pair<>(200, null);
    }

    /**
     * Create the request and response before.
     */
    @Before
    public void createRequestAndResponse() {
        SdncHealServiceInfo serviceInfo = new SdncHealServiceInfo();
        serviceInfo.setServiceInstanceId("E-City");

        SdncHealRequestHeaderInfo additionalParams = new SdncHealRequestHeaderInfo();
        additionalParams.setSvcAction("Go Home");
        additionalParams.setSvcRequestId("My Request");

        SdncHealRequest healRequest = new SdncHealRequest();
        healRequest.setRequestHeaderInfo(additionalParams);
        healRequest.setServiceInfo(serviceInfo);

        UUID requestId = UUID.randomUUID();
        request = new SdncRequest();
        request.setRequestId(requestId);
        request.setHealRequest(healRequest);
        request.setNsInstanceId(DOROTHY);

        SdncResponseOutput responseDescriptor = new SdncResponseOutput();
        responseDescriptor.setSvcRequestId("1234");
        responseDescriptor.setResponseCode("200");
        responseDescriptor.setAckFinalIndicator("final-indicator-00");

        response = new SdncResponse();
        response.setRequestId(request.getRequestId().toString());
        response.setResponseOutput(responseDescriptor);
    }

    @Test
    public void testSdncInitiation() {

        assertThatIllegalArgumentException().isThrownBy(() -> new SdncManager(null, null, null, null, null))
            .withMessage("the parameters \"callback\" and \"request\" on the SdncManager constructor may not be null");

        assertThatIllegalArgumentException().isThrownBy(() -> new SdncManager(this, null, null, null, null))
            .withMessage("the parameters \"callback\" and \"request\" on the SdncManager constructor may not be null");

        assertThatIllegalArgumentException().isThrownBy(() -> new SdncManager(this, request, null, null, null))
            .withMessage("the \"url\" parameter on the SdncManager constructor may not be null");

        new SdncManager(this, request, SOMEWHERE_OVER_THE_RAINBOW, DOROTHY, "Toto");
    }

    @Test
    public void testSdncExecutionException() throws InterruptedException {
        SdncManager manager = new SdncManager(this, request, SOMEWHERE_OVER_THE_RAINBOW, DOROTHY, "Exception");
        manager.setRestManager(mockedRestManager);

        when(mockedRestManager.post(startsWith(SOMEWHERE_OVER_THE_RAINBOW), eq(DOROTHY), eq("Exception"), anyMap(),
                        anyString(), anyString())).thenThrow(new RuntimeException("OzException"));

        Thread managerThread = new Thread(manager);
        managerThread.start();

        managerThread.join(1000);

        verify(mockedRestManager).post(any(), any(), any(), any(), any(), any());
    }

    @Test
    public void testSdncExecutionNull() throws InterruptedException {
        SdncManager manager = new SdncManager(this, request, SOMEWHERE_OVER_THE_RAINBOW, DOROTHY, "Null");
        manager.setRestManager(mockedRestManager);

        when(mockedRestManager.post(startsWith(SOMEWHERE_OVER_THE_RAINBOW), eq(DOROTHY), eq("Null"), anyMap(),
                        anyString(), anyString())).thenReturn(null);

        manager.run();

        verify(mockedRestManager).post(any(), any(), any(), any(), any(), any());
    }


    @Test
    public void testSdncExecutionError0() throws InterruptedException {
        SdncManager manager = new SdncManager(this, request, SOMEWHERE_OVER_THE_RAINBOW, DOROTHY, "Error0");
        manager.setRestManager(mockedRestManager);

        when(mockedRestManager.post(startsWith(SOMEWHERE_OVER_THE_RAINBOW), eq(DOROTHY), eq("Error0"), anyMap(),
                        anyString(), anyString())).thenReturn(httpResponseErr);

        manager.run();

        verify(mockedRestManager).post(any(), any(), any(), any(), any(), any());
    }

    @Test
    public void testSdncExecutionBadResponse() throws InterruptedException {
        SdncManager manager = new SdncManager(this, request, SOMEWHERE_OVER_THE_RAINBOW, DOROTHY, "BadResponse");
        manager.setRestManager(mockedRestManager);

        when(mockedRestManager.post(startsWith(SOMEWHERE_OVER_THE_RAINBOW), eq(DOROTHY), eq("OK"), anyMap(),
                        anyString(), anyString())).thenReturn(httpResponseBadResponse);

        manager.run();

        verify(mockedRestManager).post(any(), any(), any(), any(), any(), any());
    }

    @Test
    public void testSdncExecutionOk() throws InterruptedException {
        SdncManager manager = new SdncManager(this, request, SOMEWHERE_OVER_THE_RAINBOW, DOROTHY, "OOK");
        manager.setRestManager(mockedRestManager);

        when(mockedRestManager.post(startsWith(SOMEWHERE_OVER_THE_RAINBOW), eq(DOROTHY), eq("OK"), anyMap(),
                        anyString(), anyString())).thenReturn(httpResponsePutOk);

        manager.run();

        verify(mockedRestManager).post(any(), any(), any(), any(), any(), any());
    }

    @Override
    public void onCallback(SdncResponse response) {
        //
        // Nothing really to do
        //
    }
}
