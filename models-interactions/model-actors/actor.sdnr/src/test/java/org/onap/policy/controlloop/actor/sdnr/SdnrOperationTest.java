/*-
 * ============LICENSE_START=======================================================
 * SdnrOperation
 * ================================================================================
 * Copyright (C) 2020-2022 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2023 Nordix Foundation.
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.policy.controlloop.actor.test.BasicBidirectionalTopicOperation;
import org.onap.policy.controlloop.actorserviceprovider.OperationProperties;
import org.onap.policy.controlloop.actorserviceprovider.OperationResult;
import org.onap.policy.controlloop.actorserviceprovider.impl.BidirectionalTopicOperation.Status;
import org.onap.policy.controlloop.actorserviceprovider.parameters.BidirectionalTopicConfig;
import org.onap.policy.controlloop.actorserviceprovider.parameters.BidirectionalTopicParams;
import org.onap.policy.sdnr.PciCommonHeader;
import org.onap.policy.sdnr.PciMessage;
import org.onap.policy.sdnr.PciRequest;
import org.onap.policy.sdnr.util.StatusCodeEnum;

@RunWith(MockitoJUnitRunner.class)
public class SdnrOperationTest extends BasicSdnrOperation {

    private SdnrOperation operation;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        BasicBidirectionalTopicOperation.initBeforeClass(MY_SINK, MY_SOURCE);
    }

    @AfterClass
    public static void tearDownAfterClass() {
        destroyAfterClass();
    }

    /**
     * Setup.
     */
    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        operation = new SdnrOperation(params, config);
        operation.setProperty(OperationProperties.EVENT_PAYLOAD, "my payload");
    }

    @After
    @Override
    public void tearDown() {
        super.tearDown();
    }

    @Test
    public void testSdnrOperation() {
        assertEquals(DEFAULT_ACTOR, operation.getActorName());
        assertEquals(DEFAULT_OPERATION, operation.getName());
    }

    @Test
    public void testGetPropertyNames() {
        assertThat(operation.getPropertyNames()).isEqualTo(List.of(OperationProperties.EVENT_PAYLOAD));
    }

    @Test
    public void testMakeRequest() {
        operation.generateSubRequestId(1);

        PciMessage request = operation.makeRequest(1);

        assertNotNull(request.getBody());
        assertEquals("1.0", request.getVersion());
        assertEquals("request", request.getType());
        assertEquals(DEFAULT_OPERATION.toLowerCase(), request.getRpcName());

        PciRequest input = request.getBody().getInput();
        assertNotNull(input);
        assertEquals(DEFAULT_OPERATION, input.getAction());

        PciCommonHeader header = input.getCommonHeader();
        assertNotNull(header);
        assertEquals(params.getRequestId(), header.getRequestId());
    }

    /**
     * Tests makeRequest() when a property is missing.
     */
    @Test
    public void testMakeRequestMissingProperty() {
        operation = new SdnrOperation(params, config);

        operation.generateSubRequestId(1);
        outcome.setSubRequestId(operation.getSubRequestId());

        assertThatIllegalStateException().isThrownBy(() -> operation.makeRequest(1))
                        .withMessageContaining("missing event payload");
    }

    @Test
    public void testGetExpectedKeyValues() {
        operation.generateSubRequestId(1);

        PciMessage request = operation.makeRequest(1);
        assertEquals(List.of(request.getBody().getInput().getCommonHeader().getSubRequestId()),
                        operation.getExpectedKeyValues(50, request));
    }

    /**
     * Tests "success" case with simulator.
     */
    @Test
    public void testSuccess() throws Exception {
        BidirectionalTopicParams opParams =
                        BidirectionalTopicParams.builder().sinkTopic(MY_SINK).sourceTopic(MY_SOURCE).build();
        config = new BidirectionalTopicConfig(blockingExecutor, opParams, topicMgr, SdnrOperation.SELECTOR_KEYS);

        params = params.toBuilder().retry(0).timeoutSec(5).executor(blockingExecutor).build();

        operation = new SdnrOperation(params, config);
        operation.setProperty(OperationProperties.EVENT_PAYLOAD, "my payload");

        outcome = operation.start().get();
        assertEquals(OperationResult.SUCCESS, outcome.getResult());
        assertTrue(outcome.getResponse() instanceof PciMessage);
    }

    @Test
    public void testDetmStatusStringResponse() {
        final org.onap.policy.sdnr.Status status = response.getBody().getOutput().getStatus();

        // null status
        response.getBody().getOutput().setStatus(null);
        assertEquals(Status.FAILURE, operation.detmStatus("", response));
        response.getBody().getOutput().setStatus(status);

        // invalid code
        status.setCode(-45);
        assertEquals(Status.FAILURE, operation.detmStatus("", response));


        status.setValue(StatusCodeEnum.ACCEPTED.toString());
        status.setCode(StatusCodeEnum.toValue(StatusCodeEnum.ACCEPTED));
        assertEquals(Status.STILL_WAITING, operation.detmStatus("", response));

        status.setValue(StatusCodeEnum.SUCCESS.toString());
        status.setCode(StatusCodeEnum.toValue(StatusCodeEnum.SUCCESS));
        assertEquals(Status.SUCCESS, operation.detmStatus("", response));

        status.setValue(StatusCodeEnum.REJECT.toString());
        status.setCode(StatusCodeEnum.toValue(StatusCodeEnum.REJECT));
        assertEquals(Status.FAILURE, operation.detmStatus("", response));

        status.setValue(StatusCodeEnum.REJECT.toString());
        status.setCode(313);
        assertEquals(Status.FAILURE, operation.detmStatus("", response));

        status.setValue(StatusCodeEnum.ERROR.toString());
        status.setCode(StatusCodeEnum.toValue(StatusCodeEnum.ERROR));
        assertEquals(Status.FAILURE, operation.detmStatus("", response));

        status.setValue(StatusCodeEnum.FAILURE.toString());
        status.setCode(450);
        assertEquals(Status.FAILURE, operation.detmStatus("", response));
    }

    @Test
    public void testSetOutcome() {
        // with a status value
        checkOutcome();
        assertEquals(StatusCodeEnum.SUCCESS.toString(), outcome.getMessage());

        // null status value
        response.getBody().getOutput().getStatus().setValue(null);
        checkOutcome();

        // null status
        response.getBody().getOutput().setStatus(null);
        checkOutcome();

        // null output
        response.getBody().setOutput(null);
        checkOutcome();

        // null body
        response.setBody(null);
        checkOutcome();
    }

    protected void checkOutcome() {
        assertSame(outcome, operation.setOutcome(outcome, OperationResult.SUCCESS, response));
        assertEquals(OperationResult.SUCCESS, outcome.getResult());
        assertNotNull(outcome.getMessage());
        assertSame(response, outcome.getResponse());
    }
}
