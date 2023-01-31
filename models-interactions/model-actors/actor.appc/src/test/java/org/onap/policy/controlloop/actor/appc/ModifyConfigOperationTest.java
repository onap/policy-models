/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.controlloop.actor.appc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.policy.appc.Request;
import org.onap.policy.appc.Response;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.controlloop.actorserviceprovider.OperationProperties;
import org.onap.policy.controlloop.actorserviceprovider.OperationResult;
import org.onap.policy.controlloop.actorserviceprovider.parameters.BidirectionalTopicConfig;
import org.onap.policy.controlloop.actorserviceprovider.parameters.BidirectionalTopicParams;

@RunWith(MockitoJUnitRunner.class)
public class ModifyConfigOperationTest extends BasicAppcOperation {

    private ModifyConfigOperation oper;


    public ModifyConfigOperationTest() {
        super(DEFAULT_ACTOR, ModifyConfigOperation.NAME);
    }

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        // use same topic name for both sides
        initBeforeClass(MY_SINK, MY_SINK);
    }

    @AfterClass
    public static void tearDownAfterClass() {
        destroyAfterClass();
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        oper = new ModifyConfigOperation(params, config);
    }

    @After
    @Override
    public void tearDown() {
        super.tearDown();
    }

    /**
     * Tests "success" case with simulator.
     */
    @Test
    public void testSuccess() throws Exception {
        BidirectionalTopicParams opParams =
                        BidirectionalTopicParams.builder().sinkTopic(MY_SINK).sourceTopic(MY_SINK).build();
        config = new BidirectionalTopicConfig(blockingExecutor, opParams, topicMgr, AppcOperation.SELECTOR_KEYS);

        params = params.toBuilder().retry(0).timeoutSec(5).executor(blockingExecutor).build();

        oper = new ModifyConfigOperation(params, config);

        oper.setProperty(OperationProperties.AAI_RESOURCE_VNF, genvnf);

        outcome = oper.start().get();
        assertEquals(OperationResult.SUCCESS, outcome.getResult());
        assertTrue(outcome.getResponse() instanceof Response);
    }

    @Test
    public void testConstructor() {
        assertEquals(DEFAULT_ACTOR, oper.getActorName());
        assertEquals(ModifyConfigOperation.NAME, oper.getName());
    }

    @Test
    public void testGetPropertyNames() {
        assertThat(oper.getPropertyNames()).isEqualTo(List.of(OperationProperties.AAI_RESOURCE_VNF));
    }

    @Test
    public void testMakeRequest() throws CoderException {
        oper.setProperty(OperationProperties.AAI_RESOURCE_VNF, genvnf);

        oper.generateSubRequestId(2);
        Request request = oper.makeRequest(2);
        assertNotNull(request);
        assertEquals(MY_VNF, request.getPayload().get(ModifyConfigOperation.VNF_ID_KEY));

        verifyRequest("modifyConfig.json", request, IGNORE_FIELDS);
    }
}
