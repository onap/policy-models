/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2024 Nordix Foundation
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

package org.onap.policy.controlloop.actor.appc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.policy.appc.Request;
import org.onap.policy.appc.Response;
import org.onap.policy.appc.ResponseCode;
import org.onap.policy.appc.ResponseStatus;
import org.onap.policy.common.message.bus.event.TopicSink;
import org.onap.policy.common.message.bus.event.TopicSource;
import org.onap.policy.common.utils.coder.StandardCoderInstantAsMillis;
import org.onap.policy.common.utils.coder.StandardCoderObject;
import org.onap.policy.controlloop.actor.test.BasicBidirectionalTopicOperation;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.OperationResult;
import org.onap.policy.controlloop.actorserviceprovider.Util;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.simulators.AppcLegacyTopicServer;
import org.onap.policy.simulators.TopicServer;

/**
 * Superclass for various operator tests.
 */
public abstract class BasicAppcOperation extends BasicBidirectionalTopicOperation<Request> {
    protected static final String[] IGNORE_FIELDS = {"RequestID", "subRequestID", "TimeStamp"};
    protected static final String MY_DESCRIPTION = "my-description";
    protected static final String MY_VNF = "my-vnf";
    protected static final String KEY1 = "my-key-A";
    protected static final String KEY2 = "my-key-B";
    protected static final String KEY3 = "my-key-C";
    protected static final String VALUE1 = "{\"input\":\"hello\"}";
    protected static final String VALUE2 = "{\"output\":\"world\"}";
    protected static final String RESOURCE_ID = "my-resource";

    protected Response response;
    protected GenericVnf genvnf;

    /**
     * Constructs the object using a default actor and operation name.
     */
    public BasicAppcOperation() {
        this.coder = new StandardCoderInstantAsMillis();
    }

    /**
     * Constructs the object.
     *
     * @param actor actor name
     * @param operation operation name
     */
    public BasicAppcOperation(String actor, String operation) {
        super(actor, operation);
        this.coder = new StandardCoderInstantAsMillis();
    }

    /**
     * Initializes mocks and sets up.
     */
    void setUp() {
        super.setUpBasic();

        response = new Response();

        ResponseStatus status = new ResponseStatus();
        response.setStatus(status);
        status.setCode(ResponseCode.SUCCESS.getValue());
        status.setDescription(MY_DESCRIPTION);

        genvnf = new GenericVnf();
        genvnf.setVnfId(MY_VNF);
    }

    void tearDown() {
        super.tearDownBasic();
    }

    @Override
    protected TopicServer<Request> makeServer(TopicSink sink, TopicSource source) {
        return new AppcLegacyTopicServer(sink, source);
    }

    /**
     * Runs the operation and verifies that the response is successful.
     *
     * @param operation operation to run
     */
    protected void verifyOperation(AppcOperation operation)
                    throws InterruptedException, ExecutionException {

        CompletableFuture<OperationOutcome> future2 = operation.start();
        executor.runAll(100);
        assertFalse(future2.isDone());

        verify(forwarder).register(any(), listenerCaptor.capture());
        provideResponse(listenerCaptor.getValue(), ResponseCode.SUCCESS.getValue(), MY_DESCRIPTION);

        executor.runAll(100);
        assertTrue(future2.isDone());

        outcome = future2.get();
        assertEquals(OperationResult.SUCCESS, outcome.getResult());
        assertEquals(MY_DESCRIPTION, outcome.getMessage());
    }

    @Override
    protected void makeContext() {
        super.makeContext();

        Map<String, String> entities = Map.of(ControlLoopOperationParams.PARAMS_ENTITY_RESOURCEID, RESOURCE_ID);

        params = params.toBuilder().targetEntityIds(entities).build();
    }

    /**
     * Provides a response to the listener.
     *
     * @param listener listener to which to provide the response
     * @param code response code
     * @param description response description
     */
    protected void provideResponse(BiConsumer<String, StandardCoderObject> listener, int code, String description) {
        Response response = new Response();

        ResponseStatus status = new ResponseStatus();
        response.setStatus(status);
        status.setCode(code);
        status.setDescription(description);

        provideResponse(listener, Util.translate("", response, String.class));
    }

    @Override
    protected Map<String, Object> makePayload() {
        return Map.of(KEY1, VALUE1, KEY2, VALUE2);
    }
}
