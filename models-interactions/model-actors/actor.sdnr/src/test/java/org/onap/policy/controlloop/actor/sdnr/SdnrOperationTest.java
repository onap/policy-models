/*-
 * ============LICENSE_START=======================================================
 * SdnrOperation
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import java.util.Arrays;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.controlloop.actorserviceprovider.impl.BidirectionalTopicOperation.Status;
import org.onap.policy.controlloop.policy.PolicyResult;
import org.onap.policy.sdnr.PciCommonHeader;
import org.onap.policy.sdnr.PciRequestWrapper;
import org.onap.policy.sdnr.util.StatusCodeEnum;

public class SdnrOperationTest extends BasicSdnrOperation {

    private SdnrOperation operation;

    /**
     * Setup.
     */
    @Before
    public void setUp() throws Exception {
        super.setUp();

        operation = new SdnrOperation(params, config) {};
    }

    @Test
    public void testSdnrOperation() {
        assertEquals(DEFAULT_ACTOR, operation.getActorName());
        assertEquals(DEFAULT_OPERATION, operation.getName());
    }

    @Test
    public void testMakeRequest() {
        Pair<String, PciRequestWrapper> result = operation.makeRequest(1);
        assertNotNull(result.getLeft());

        PciRequestWrapper request = result.getRight();

        assertNotNull(request.getBody());
        assertEquals("1.0", request.getVersion());
        assertEquals("request", request.getType());

        PciCommonHeader header = request.getBody().getCommonHeader();
        assertNotNull(header);
        assertEquals(params.getRequestId(), header.getRequestId());
    }

    @Test
    public void testGetExpectedKeyValues() {
        PciRequestWrapper request = operation.makeRequest(1).getRight();
        assertEquals(Arrays.asList(request.getBody().getCommonHeader().getSubRequestId()),
                operation.getExpectedKeyValues(50, request));

    }

    @Test
    public void testDetmStatusStringResponse() {
        final org.onap.policy.sdnr.Status status = response.getBody().getStatus();

        // null status
        response.getBody().setStatus(null);
        assertThatIllegalArgumentException().isThrownBy(() -> operation.detmStatus("", response))
                        .withMessage("SDNR response is missing the response status");
        response.getBody().setStatus(status);

        // invalid code
        status.setCode(-45);
        assertThatIllegalArgumentException().isThrownBy(() -> operation.detmStatus("", response))
                        .withMessage("unknown SDNR response status code: -45");


        status.setValue(StatusCodeEnum.ACCEPTED.toString());
        status.setCode(StatusCodeEnum.toValue(StatusCodeEnum.ACCEPTED));
        assertEquals(Status.STILL_WAITING, operation.detmStatus("", response));

        status.setValue(StatusCodeEnum.SUCCESS.toString());
        status.setCode(StatusCodeEnum.toValue(StatusCodeEnum.SUCCESS));
        assertEquals(Status.SUCCESS, operation.detmStatus("", response));

        status.setValue(StatusCodeEnum.REJECT.toString());
        status.setCode(StatusCodeEnum.toValue(StatusCodeEnum.REJECT));
        assertThatIllegalArgumentException().isThrownBy(() -> operation.detmStatus("", response))
            .withMessage("SDNR request was not accepted, code=" + StatusCodeEnum.REJECT.toString());

        status.setValue(StatusCodeEnum.REJECT.toString());
        status.setCode(313);
        assertThatIllegalArgumentException().isThrownBy(() -> operation.detmStatus("", response))
            .withMessage("SDNR request was not accepted, code=" + StatusCodeEnum.REJECT.toString());

        status.setValue(StatusCodeEnum.ERROR.toString());
        status.setCode(StatusCodeEnum.toValue(StatusCodeEnum.ERROR));
        assertThatIllegalArgumentException().isThrownBy(() -> operation.detmStatus("", response))
            .withMessage("SDNR request was not accepted, code=" + StatusCodeEnum.ERROR.toString());

        status.setValue(StatusCodeEnum.FAILURE.toString());
        status.setCode(450);
        assertEquals(Status.FAILURE, operation.detmStatus("", response));
    }

    @Test
    public void testSetOutcome() {
        final org.onap.policy.sdnr.Status status = response.getBody().getStatus();

        // null status
        response.getBody().setStatus(null);
        assertSame(outcome, operation.setOutcome(outcome, PolicyResult.SUCCESS, response));
        assertEquals(PolicyResult.SUCCESS, outcome.getResult());
        assertNotNull(outcome.getMessage());
        response.getBody().setStatus(status);

    }
}
