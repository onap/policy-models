/*-
 * ============LICENSE_START=======================================================
 * AppcLcmOperation
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

package org.onap.policy.controlloop.actor.appclcm;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;

import java.util.List;
import java.util.UUID;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.policy.appclcm.AppcLcmCommonHeader;
import org.onap.policy.appclcm.AppcLcmInput;
import org.onap.policy.appclcm.AppcLcmOutput;
import org.onap.policy.appclcm.AppcLcmResponseStatus;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.impl.BidirectionalTopicOperation.Status;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.controlloop.policy.PolicyResult;
import org.powermock.reflect.Whitebox;

public class AppcLcmOperationTest {
    private static AppcLcmInput mockInput;
    private static AppcLcmOutput mockOutput;
    private static OperationOutcome mockOperationOutcome;
    private static AppcLcmOperation operation;
    private static AppcLcmResponseStatus mockResponseStatus;
    private static AppcLcmCommonHeader mockCommonHeader;
    private static ControlLoopOperationParams mockParams;

    /**
     * Setup mocks for testing.
     */
    @BeforeClass
    public static void setup() {
        mockInput = Mockito.mock(AppcLcmInput.class);
        mockOutput = Mockito.mock(AppcLcmOutput.class);
        mockOperationOutcome = Mockito.mock(OperationOutcome.class);
        mockResponseStatus = Mockito.mock(AppcLcmResponseStatus.class);
        mockCommonHeader = Mockito.mock(AppcLcmCommonHeader.class);
        mockParams = Mockito.mock(ControlLoopOperationParams.class);
        operation = Mockito.mock(AppcLcmOperation.class);
    }

    @Test
    public void testStartPreprocessorAsync() {
        Mockito.doCallRealMethod().when(operation).startPreprocessorAsync();
        assertNull(operation.startPreprocessorAsync());
    }

    @Test
    public void testMakeRequest() {
        UUID randomId = UUID.randomUUID();
        Mockito.doCallRealMethod().when(operation).makeRequest(1, "sampleTargetVnf");
        Mockito.when(mockParams.getRequestId()).thenReturn(randomId);
        Mockito.when(mockParams.getPayload()).thenReturn(null);
        Whitebox.setInternalState(operation, "params", mockParams);
        assertNotNull(operation.makeRequest(1, "sampleTargetVnf"));
    }

    @Test
    public void testGetExpectedKeyValues() {
        Mockito.doCallRealMethod().when(operation).getExpectedKeyValues(1, mockInput);
        Mockito.when(mockInput.getCommonHeader()).thenReturn(mockCommonHeader);
        Mockito.when(mockCommonHeader.getSubRequestId()).thenReturn("sampleSubRequestId");

        List<String> retList = operation.getExpectedKeyValues(1, mockInput);
        assertNotNull(retList);
        assertEquals(1, retList.size());
    }

    @Test
    public void testDetmStatus() {
        Mockito.doCallRealMethod().when(operation).detmStatus("testResponse", mockOutput);
        Mockito.when(mockOutput.getStatus()).thenReturn(mockResponseStatus);
        Mockito.when(mockOutput.getPayload()).thenReturn("testPayload");
        Mockito.when(mockResponseStatus.getCode()).thenReturn(100);
        Status retStatus = operation.detmStatus("testResponse", mockOutput);
        assertEquals(Status.STILL_WAITING, retStatus);

        Mockito.when(mockResponseStatus.getCode()).thenReturn(400);
        retStatus = operation.detmStatus("testResponse", mockOutput);
        assertEquals(Status.SUCCESS, retStatus);

        Mockito.when(mockResponseStatus.getCode()).thenReturn(450);
        retStatus = operation.detmStatus("testResponse", mockOutput);
        assertEquals(Status.FAILURE, retStatus);

        Mockito.when(mockOutput.getStatus()).thenReturn(null);

        assertThatIllegalArgumentException().isThrownBy(() -> operation.detmStatus("testResponse", mockOutput));

        Mockito.when(mockResponseStatus.getCode()).thenReturn(200);
        assertThatIllegalArgumentException().isThrownBy(() -> operation.detmStatus("testResponse", mockOutput));
    }

    @Test
    public void testSetOutcome() {
        Mockito.doCallRealMethod().when(operation).setOutcome(mockOperationOutcome, PolicyResult.SUCCESS, mockOutput);
        Mockito.doCallRealMethod().when(operation).setOutcome(mockOperationOutcome, PolicyResult.FAILURE, mockOutput);

        Mockito.doCallRealMethod().when(mockOperationOutcome).setResult(any(PolicyResult.class));
        Mockito.doCallRealMethod().when(mockOperationOutcome).setMessage(any(String.class));
        Mockito.doCallRealMethod().when(mockOperationOutcome).getResult();
        Mockito.doCallRealMethod().when(mockOperationOutcome).getMessage();

        OperationOutcome result = null;

        Mockito.when(mockOutput.getStatus()).thenReturn(mockResponseStatus);
        Mockito.when(mockResponseStatus.getMessage()).thenReturn(null);
        result = operation.setOutcome(mockOperationOutcome, PolicyResult.SUCCESS, mockOutput);
        assertNull(result);
        Mockito.verify(mockOutput, atLeast(1)).getStatus();
        Mockito.verify(mockResponseStatus, atLeast(1)).getMessage();
        Mockito.verify(operation).setOutcome(mockOperationOutcome, PolicyResult.SUCCESS, mockOutput);

        Mockito.doCallRealMethod().when(operation).setOutcome(mockOperationOutcome, PolicyResult.FAILURE, mockOutput);
        Mockito.when(mockOutput.getStatus()).thenReturn(mockResponseStatus);
        Mockito.when(mockResponseStatus.getMessage()).thenReturn("sampleMessage");
        result = operation.setOutcome(mockOperationOutcome, PolicyResult.FAILURE, mockOutput);
        assertEquals(PolicyResult.FAILURE, result.getResult());
        assertNotNull(result.getMessage());
        Mockito.verify(mockOutput, atLeast(1)).getStatus();
        Mockito.verify(mockResponseStatus, atLeast(1)).getMessage();
        Mockito.verify(operation, atLeast(1)).setOutcome(mockOperationOutcome, PolicyResult.FAILURE, mockOutput);
    }

}
