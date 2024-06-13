/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2022 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2023, 2024 Nordix Foundation.
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.policy.controlloop.actor.test.BasicBidirectionalTopicOperation;
import org.onap.policy.controlloop.actorserviceprovider.OperationProperties;
import org.onap.policy.controlloop.actorserviceprovider.OperationResult;
import org.onap.policy.sdnr.util.StatusCodeEnum;

@ExtendWith(MockitoExtension.class)
public class A1pOperationTest extends BasicA1pOperation {

    private A1pOperation operation;

    @BeforeAll
    public static void setUpBeforeClass() throws Exception {
        BasicBidirectionalTopicOperation.initBeforeClass(MY_SINK, MY_SOURCE);
    }

    @AfterAll
    public static void tearDownAfterClass() {
        destroyAfterClass();
    }

    /**
     * Setup.
     */
    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();

        operation = new A1pOperation(params, config);
        operation.setProperty(OperationProperties.EVENT_PAYLOAD, "my payload");
    }

    @AfterEach
    @Override
    public void tearDown() {
        super.tearDown();
    }

    @Test
    public void testA1pOperation() {
        assertEquals(DEFAULT_ACTOR, operation.getActorName());
        assertEquals(DEFAULT_OPERATION, operation.getName());
    }

    @Test
    public void testGetPropertyNames() {
        assertThat(operation.getPropertyNames()).isEqualTo(List.of(OperationProperties.EVENT_PAYLOAD));
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
