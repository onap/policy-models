/*-
 * ============LICENSE_START=======================================================
 * sdnc
 * ================================================================================
 * Copyright (C) 2018 Huawei. All rights reserved.
 * ================================================================================
 * Modifications Copyright (C) 2019 AT&T Intellectual Property. All rights reserved
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

package org.onap.policy.sdnc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.endsWith;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.policy.rest.RestManager;
import org.onap.policy.rest.RestManager.Pair;
import org.onap.policy.sdnc.SdncManager.SdncCallback;
import org.onap.policy.sdnc.util.Serialization;

public class SdncManagerTest implements SdncCallback {
    private RestManager   mockedRestManager;

    private Pair<Integer, String> httpResponsePutOk;
    private Pair<Integer, String> httpResponseGetOk;
    private Pair<Integer, String> httpResponseBadResponse;
    private Pair<Integer, String> httpResponseErr;

    private SdncRequest  request;
    private SdncResponse response;

    @BeforeClass
    public static void beforeTestSdncManager() {
    }

    /**
     * Set up the mocked REST manager.
     */
    @Before
    public void setupMockedRest() {
        mockedRestManager   = mock(RestManager.class);

        httpResponsePutOk       = mockedRestManager.new Pair<>(202, Serialization.gsonPretty.toJson(response));
        httpResponseGetOk       = mockedRestManager.new Pair<>(200, Serialization.gsonPretty.toJson(response));
        httpResponseBadResponse = mockedRestManager.new Pair<>(202, Serialization.gsonPretty.toJson(null));
        httpResponseErr         = mockedRestManager.new Pair<>(200, null);
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
        request.setNsInstanceId("Dorothy");

        SdncResponseOutput responseDescriptor = new SdncResponseOutput();
        responseDescriptor.setSvcRequestId("1234");
        responseDescriptor.setResponseCode("200");
        responseDescriptor.setAckFinalIndicator("final-indicator-00");

        response = new SdncResponse();
        response.setRequestId(request.getRequestId().toString());
        response.setResponseOutput(responseDescriptor);
    }

    @Test
    public void testSdncInitiation() throws InterruptedException {
        try {
            new SdncManager(null, null, null, null, null);
            fail("test should throw an exception here");
        }
        catch (IllegalArgumentException e) {
            assertEquals(
                "the parameters \"callback\" and \"request\" on the SdncManager constructor may not be null",
                e.getMessage()
            );
        }

        try {
            new SdncManager(this, null, null, null, null);
            fail("test should throw an exception here");
        }
        catch (IllegalArgumentException e) {
            assertEquals(
                "the parameters \"callback\" and \"request\" on the SdncManager constructor may not be null",
                e.getMessage()
            );
        }

        try {
            new SdncManager(this, request, null, null, null);
            fail("test should throw an exception here");
        }
        catch (IllegalArgumentException e) {
            assertEquals(
                "the \"url\" parameter on the SdncManager constructor may not be null",
                e.getMessage()
            );
        }

        new SdncManager(this, request, "http://somewhere.over.the.rainbow", "Dorothy", "Toto");
    }

    @Test
    public void testSdncExecutionException() throws InterruptedException {
        SdncManager manager = new SdncManager(this, request, "http://somewhere.over.the.rainbow", "Dorothy", "Exception");
        manager.setRestManager(mockedRestManager);

        Thread managerThread = new Thread(manager);
        managerThread.start();

        when(mockedRestManager.post(startsWith("http://somewhere.over.the.rainbow"), eq("Dorothy"), eq("Exception"), anyMap(), anyString(), anyString()))
            .thenThrow(new RuntimeException("OzException"));


        managerThread.join(100);
    }

    @Test
    public void testSdncExecutionNull() throws InterruptedException {
        SdncManager manager = new SdncManager(this, request, "http://somewhere.over.the.rainbow", "Dorothy", "Null");
        manager.setRestManager(mockedRestManager);

        Thread managerThread = new Thread(manager);
        managerThread.start();

        when(mockedRestManager.post(startsWith("http://somewhere.over.the.rainbow"), eq("Dorothy"), eq("Null"), anyMap(), anyString(), anyString()))
            .thenReturn(null);

        managerThread.join(100);
    }


    @Test
    public void testSdncExecutionError0() throws InterruptedException {
        SdncManager manager = new SdncManager(this, request, "http://somewhere.over.the.rainbow", "Dorothy", "Error0");
        manager.setRestManager(mockedRestManager);

        Thread managerThread = new Thread(manager);
        managerThread.start();

        when(mockedRestManager.post(startsWith("http://somewhere.over.the.rainbow"), eq("Dorothy"), eq("Error0"), anyMap(), anyString(), anyString()))
            .thenReturn(httpResponseErr);

        managerThread.join(100);
    }

    @Test
    public void testSdncExecutionBadResponse() throws InterruptedException {
        SdncManager manager = new SdncManager(this, request, "http://somewhere.over.the.rainbow", "Dorothy", "BadResponse");
        manager.setRestManager(mockedRestManager);

        Thread managerThread = new Thread(manager);
        managerThread.start();

        when(mockedRestManager.post(startsWith("http://somewhere.over.the.rainbow"), eq("Dorothy"), eq("OK"), anyMap(), anyString(), anyString()))
            .thenReturn(httpResponseBadResponse);

        managerThread.join(100);
    }

    @Test
    public void testSdncExecutionOk() throws InterruptedException {
        SdncManager manager = new SdncManager(this, request, "http://somewhere.over.the.rainbow", "Dorothy", "OOK");
        manager.setRestManager(mockedRestManager);

        Thread managerThread = new Thread(manager);
        managerThread.start();

        when(mockedRestManager.post(startsWith("http://somewhere.over.the.rainbow"), eq("Dorothy"), eq("OK"), anyMap(), anyString(), anyString()))
            .thenReturn(httpResponsePutOk);

        when(mockedRestManager.get(endsWith("1234"), eq("Dorothy"), eq("OK"), anyMap()))
            .thenReturn(httpResponseGetOk);


        managerThread.join(100);
    }

    @Override
    public void onCallback(SdncResponse response) {
        //
        // Nothing really to do
        //
    }
}
