/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.controlloop.actor.sdnc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.mockito.Mockito;
import org.onap.policy.common.endpoints.event.comm.bus.internal.BusTopicParams;
import org.onap.policy.common.endpoints.http.client.HttpClientFactoryInstance;
import org.onap.policy.common.endpoints.http.server.HttpServletServerFactoryInstance;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.controlloop.actor.test.BasicHttpOperation;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.OperationResult;
import org.onap.policy.sdnc.SdncRequest;
import org.onap.policy.sdnc.SdncResponse;
import org.onap.policy.sdnc.SdncResponseOutput;
import org.onap.policy.simulators.Util;

/**
 * Superclass for various operator tests.
 */
public abstract class BasicSdncOperation extends BasicHttpOperation {
    /**
     * Fields to be ignored when comparing requests with JSON.
     */
    protected static final String[] IGNORE_FIELDS = {"svc-request-id"};

    protected SdncResponse response;

    /**
     * Constructs the object using a default actor and operation name.
     */
    public BasicSdncOperation() {
        super();
    }

    /**
     * Constructs the object.
     *
     * @param actor actor name
     * @param operation operation name
     */
    public BasicSdncOperation(String actor, String operation) {
        super(actor, operation);
    }

    /**
     * Starts the simulator.
     */
    protected static void initBeforeClass() throws Exception {
        var testServer = Util.buildSdncSim();
        assertNotNull(testServer);

        BusTopicParams clientParams = BusTopicParams.builder().clientName(MY_CLIENT).basePath("restconf/operations/")
                        .hostname("localhost").managed(true).port(Util.SDNCSIM_SERVER_PORT).build();
        HttpClientFactoryInstance.getClientFactory().build(clientParams);
    }

    protected static void destroyAfterClass() {
        HttpClientFactoryInstance.getClientFactory().destroy();
        HttpServletServerFactoryInstance.getServerFactory().destroy();
    }

    /**
     * Initializes mocks and sets up.
     */
    public void setUp() throws Exception {
        super.setUpBasic();

        response = new SdncResponse();

        SdncResponseOutput output = new SdncResponseOutput();
        response.setResponseOutput(output);
        output.setResponseCode("200");

        Mockito.lenient().when(rawResponse.readEntity(String.class)).thenReturn(new StandardCoder().encode(response));
    }

    /**
     * Runs the operation and verifies that the response is successful.
     *
     * @param operation operation to run
     * @return the request that was posted
     */
    protected SdncRequest verifyOperation(SdncOperation operation)
                    throws InterruptedException, ExecutionException, TimeoutException, CoderException {

        CompletableFuture<OperationOutcome> future2 = operation.start();
        executor.runAll(100);
        assertFalse(future2.isDone());

        verify(client).post(callbackCaptor.capture(), any(), requestCaptor.capture(), any());
        callbackCaptor.getValue().completed(rawResponse);

        executor.runAll(100);
        assertTrue(future2.isDone());

        outcome = future2.get();
        assertEquals(OperationResult.SUCCESS, outcome.getResult());
        assertTrue(outcome.getResponse() instanceof SdncResponse);

        assertNotNull(outcome.getSubRequestId());

        String reqText = requestCaptor.getValue().getEntity();

        return coder.decode(reqText, SdncRequest.class);
    }
}
