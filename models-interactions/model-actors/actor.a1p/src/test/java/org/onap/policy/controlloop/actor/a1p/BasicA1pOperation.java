/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2022 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.controlloop.actor.a1p;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import org.onap.policy.common.endpoints.event.comm.TopicSink;
import org.onap.policy.common.endpoints.event.comm.TopicSource;
import org.onap.policy.common.utils.coder.StandardCoderObject;
import org.onap.policy.controlloop.actor.test.BasicBidirectionalTopicOperation;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.OperationResult;
import org.onap.policy.controlloop.actorserviceprovider.Util;
import org.onap.policy.sdnr.PciBody;
import org.onap.policy.sdnr.PciMessage;
import org.onap.policy.sdnr.PciResponse;
import org.onap.policy.sdnr.Status;
import org.onap.policy.sdnr.util.StatusCodeEnum;
import org.onap.policy.simulators.SdnrTopicServer;
import org.onap.policy.simulators.TopicServer;

public abstract class BasicA1pOperation extends BasicBidirectionalTopicOperation<PciMessage> {

    protected PciMessage response;

    /**
     * Constructs the object using a default actor and operation name.
     */
    public BasicA1pOperation() {
        super();
    }

    /**
     * Constructs the object.
     *
     * @param actor actor name
     * @param operation operation name
     */
    public BasicA1pOperation(String actor, String operation) {
        super(actor, operation);
    }

    /**
     * Initializes mocks and sets up.
     */
    void setUp() throws Exception {
        super.setUpBasic();

        response = new PciMessage();

        PciBody body = new PciBody();
        response.setBody(body);

        PciResponse output = new PciResponse();
        body.setOutput(output);

        Status status = new Status();
        output.setStatus(status);
        status.setCode(100);
        status.setValue(StatusCodeEnum.SUCCESS.toString());
    }

    void tearDown() {
        super.tearDownBasic();
    }

    @Override
    protected TopicServer<PciMessage> makeServer(TopicSink sink, TopicSource source) {
        return new SdnrTopicServer(sink, source);
    }

    /**
     * Runs the operation and verifies that the response is successful.
     *
     * @param operation operation to run
     */
    protected void verifyOperation(A1pOperation operation)
                    throws InterruptedException, ExecutionException {

        CompletableFuture<OperationOutcome> future2 = operation.start();
        executor.runAll(100);
        assertFalse(future2.isDone());

        verify(forwarder).register(any(), listenerCaptor.capture());
        provideResponse(listenerCaptor.getValue(), StatusCodeEnum.SUCCESS.toString());

        executor.runAll(100);
        assertTrue(future2.isDone());

        outcome = future2.get();
        assertEquals(OperationResult.SUCCESS, outcome.getResult());
    }

    /**
     * Provides a response to the listener.
     *
     * @param listener listener to which to provide the response
     * @param code response code
     * @param description response description
     */
    protected void provideResponse(BiConsumer<String, StandardCoderObject> listener, int code, String description) {
        PciResponse response = new PciResponse();

        Status status = new Status();
        response.setStatus(status);
        status.setCode(code);

        provideResponse(listener, Util.translate("", response, String.class));
    }
}
