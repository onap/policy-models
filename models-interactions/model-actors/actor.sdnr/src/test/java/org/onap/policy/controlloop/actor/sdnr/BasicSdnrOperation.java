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

package org.onap.policy.controlloop.actor.sdnr;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;
import org.onap.policy.common.utils.coder.StandardCoderObject;
import org.onap.policy.controlloop.actor.test.BasicBidirectionalTopicOperation;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.Util;
import org.onap.policy.controlloop.actorserviceprovider.impl.OperationMaker;
import org.onap.policy.controlloop.actorserviceprovider.parameters.BidirectionalTopicConfig;
import org.onap.policy.controlloop.policy.PolicyResult;
import org.onap.policy.sdnr.PciResponse;
import org.onap.policy.sdnr.PciResponseWrapper;
import org.onap.policy.sdnr.Status;
import org.onap.policy.sdnr.util.StatusCodeEnum;
import org.powermock.reflect.Whitebox;

public abstract class BasicSdnrOperation extends BasicBidirectionalTopicOperation {

    protected PciResponseWrapper response;

    /**
     * Constructs the object using a default actor and operation name.
     */
    public BasicSdnrOperation() {
        super();
    }

    /**
     * Constructs the object.
     *
     * @param actor actor name
     * @param operation operation name
     */
    public BasicSdnrOperation(String actor, String operation) {
        super(actor, operation);
    }

    /**
     * Initializes mocks and sets up.
     */
    public void setUp() throws Exception {
        super.setUpBasic();

        response = new PciResponseWrapper();

        PciResponse body = new PciResponse();
        Status status = new Status();
        status.setCode(100);
        status.setValue(StatusCodeEnum.SUCCESS.toString());
        body.setStatus(status);
        response.setBody(body);
    }

    /**
     * Runs the operation and verifies that the response is successful.
     *
     * @param operation operation to run
     */
    protected void verifyOperation(SdnrOperation operation)
                    throws InterruptedException, ExecutionException, TimeoutException {

        CompletableFuture<OperationOutcome> future2 = operation.start();
        executor.runAll(100);
        assertFalse(future2.isDone());

        verify(forwarder).register(any(), listenerCaptor.capture());
        provideResponse(listenerCaptor.getValue(), StatusCodeEnum.SUCCESS.toString());

        executor.runAll(100);
        assertTrue(future2.isDone());

        outcome = future2.get();
        assertEquals(PolicyResult.SUCCESS, outcome.getResult());
    }

    /**
     * Verifies that an exception is thrown if a field is missing from the enrichment
     * data.
     *
     * @param fieldName name of the field to be removed from the enrichment data
     * @param expectedText text expected in the exception message
     */
    protected void verifyMissing(String fieldName, String expectedText,
                    OperationMaker<BidirectionalTopicConfig, SdnrOperation> maker) {

        makeContext();
        enrichment.remove(fieldName);

        SdnrOperation oper = maker.apply(params, config);

        assertThatIllegalArgumentException().isThrownBy(() -> Whitebox.invokeMethod(oper, "makeRequest", 1))
                        .withMessageContaining("missing").withMessageContaining(expectedText);
    }

    @Override
    protected void makeContext() {
        super.makeContext();

        //Target target = new Target();
        //target.setResourceID(RESOURCE_ID);

        //params = params.toBuilder().target(target).build();
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
