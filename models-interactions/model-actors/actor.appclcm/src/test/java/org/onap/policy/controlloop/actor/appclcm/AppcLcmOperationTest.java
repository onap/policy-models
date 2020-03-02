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
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.policy.appclcm.AppcLcmBody;
import org.onap.policy.appclcm.AppcLcmCommonHeader;
import org.onap.policy.appclcm.AppcLcmDmaapWrapper;
import org.onap.policy.appclcm.AppcLcmInput;
import org.onap.policy.appclcm.AppcLcmOutput;
import org.onap.policy.appclcm.AppcLcmResponseStatus;
import org.onap.policy.controlloop.VirtualControlLoopEvent;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.controlloop.ControlLoopEventContext;
import org.onap.policy.controlloop.actorserviceprovider.impl.BidirectionalTopicOperation.Status;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.controlloop.policy.PolicyResult;
import org.powermock.reflect.Whitebox;

public class AppcLcmOperationTest {
    private static AppcLcmInput mockInput;
    private static AppcLcmOutput mockOutput;
    private static AppcLcmBody mockBody;
    private static AppcLcmDmaapWrapper mockInputWrapper;
    private static AppcLcmDmaapWrapper mockOutputWrapper;
    private static OperationOutcome mockOperationOutcome;
    private static AppcLcmOperation operation;
    private static AppcLcmResponseStatus mockResponseStatus;
    private static AppcLcmCommonHeader mockCommonHeader;
    private static ControlLoopOperationParams mockParams;
    private static ControlLoopEventContext mockContext;
    private static VirtualControlLoopEvent mockEvent;

    /**
     * Setup mocks for testing.
     */
    @BeforeClass
    public static void setup() {
        mockInput = Mockito.mock(AppcLcmInput.class);
        mockOutput = Mockito.mock(AppcLcmOutput.class);
        mockBody = Mockito.mock(AppcLcmBody.class);
        mockContext = Mockito.mock(ControlLoopEventContext.class);
        mockEvent = Mockito.mock(VirtualControlLoopEvent.class);
        mockInputWrapper = Mockito.mock(AppcLcmDmaapWrapper.class);
        mockOutputWrapper = Mockito.mock(AppcLcmDmaapWrapper.class);
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

    @Ignore
    @Test
    public void testMakeRequest() {
        UUID randomId = UUID.randomUUID();
        Mockito.doCallRealMethod().when(operation).makeRequest(1, "sampleTargetVnf");
        Mockito.when(mockParams.getRequestId()).thenReturn(randomId);
        Mockito.when(mockParams.getPayload()).thenReturn(null);
        Mockito.when(mockParams.getContext()).thenReturn(mockContext);
        Mockito.when(mockParams.getOperation()).thenReturn("Config-Modify");
        Mockito.when(mockContext.getEvent()).thenReturn(mockEvent);
        Mockito.when(mockEvent.getRequestId()).thenReturn(randomId);
        Whitebox.setInternalState(operation, "params", mockParams);
        assertNotNull(operation.makeRequest(1, "sampleTargetVnf"));
        Mockito.verify(mockParams, atLeast(1)).getRequestId();
        Mockito.verify(mockParams, atLeast(1)).getPayload();
        Mockito.verify(mockParams, atLeast(1)).getContext();
        Mockito.verify(mockContext, atLeast(1)).getEvent();
        Mockito.verify(mockEvent, atLeast(1)).getRequestId();
    }

    @Test
    public void testGetExpectedKeyValues() {
        Mockito.doCallRealMethod().when(operation).getExpectedKeyValues(1, mockInputWrapper);
        Mockito.when(mockInputWrapper.getBody()).thenReturn(mockBody);
        Mockito.when(mockBody.getInput()).thenReturn(mockInput);
        Mockito.when(mockInput.getCommonHeader()).thenReturn(mockCommonHeader);
        Mockito.when(mockCommonHeader.getSubRequestId()).thenReturn("sampleSubRequestId");

        List<String> retList = operation.getExpectedKeyValues(1, mockInputWrapper);
        assertNotNull(retList);
        assertEquals(1, retList.size());

        Mockito.verify(mockInputWrapper, atLeast(1)).getBody();
        Mockito.verify(mockBody, atLeast(1)).getInput();
        Mockito.verify(mockInput, atLeast(1)).getCommonHeader();
        Mockito.verify(mockCommonHeader, atLeast(1)).getSubRequestId();
    }

    @Test
    public void testDetmStatus() {
        Mockito.doCallRealMethod().when(operation).detmStatus("testResponse", mockOutputWrapper);
        Mockito.when(mockOutputWrapper.getBody()).thenReturn(mockBody);
        Mockito.when(mockBody.getOutput()).thenReturn(mockOutput);
        Mockito.when(mockOutput.getStatus()).thenReturn(mockResponseStatus);
        Mockito.when(mockResponseStatus.getCode()).thenReturn(100);
        Status retStatus = operation.detmStatus("testResponse", mockOutputWrapper);
        assertEquals(Status.STILL_WAITING, retStatus);

        Mockito.when(mockResponseStatus.getCode()).thenReturn(400);
        retStatus = operation.detmStatus("testResponse", mockOutputWrapper);
        assertEquals(Status.SUCCESS, retStatus);

        Mockito.when(mockResponseStatus.getCode()).thenReturn(450);
        retStatus = operation.detmStatus("testResponse", mockOutputWrapper);
        assertEquals(Status.FAILURE, retStatus);

        Mockito.when(mockOutput.getStatus()).thenReturn(null);
        assertThatIllegalArgumentException().isThrownBy(() -> operation.detmStatus("testResponse", mockOutputWrapper));

        Mockito.when(mockResponseStatus.getCode()).thenReturn(200);
        assertThatIllegalArgumentException().isThrownBy(() -> operation.detmStatus("testResponse", mockOutputWrapper));

        Mockito.verify(mockOutputWrapper, atLeast(1)).getBody();
        Mockito.verify(mockBody, atLeast(1)).getOutput();
        Mockito.verify(mockOutput, atLeast(1)).getStatus();
        Mockito.verify(mockResponseStatus, atLeast(1)).getCode();
    }

    @Test
    public void testSetOutcome() {
        Mockito.doCallRealMethod().when(operation).setOutcome(mockOperationOutcome, PolicyResult.SUCCESS,
                mockOutputWrapper);
        Mockito.doCallRealMethod().when(operation).setOutcome(mockOperationOutcome, PolicyResult.FAILURE,
                mockOutputWrapper);

        Mockito.doCallRealMethod().when(mockOperationOutcome).setResult(any(PolicyResult.class));
        Mockito.doCallRealMethod().when(mockOperationOutcome).setMessage(any(String.class));
        Mockito.doCallRealMethod().when(mockOperationOutcome).getResult();
        Mockito.doCallRealMethod().when(mockOperationOutcome).getMessage();

        Mockito.when(mockOutputWrapper.getBody()).thenReturn(mockBody);
        Mockito.when(mockBody.getOutput()).thenReturn(mockOutput);
        Mockito.when(mockOutput.getStatus()).thenReturn(mockResponseStatus);
        Mockito.when(mockResponseStatus.getMessage()).thenReturn(null);

        OperationOutcome result = operation.setOutcome(mockOperationOutcome, PolicyResult.SUCCESS, mockOutputWrapper);
        assertNull(result);
        Mockito.verify(operation).setOutcome(mockOperationOutcome, PolicyResult.SUCCESS, mockOutputWrapper);

        Mockito.when(mockOutput.getStatus()).thenReturn(mockResponseStatus);
        Mockito.when(mockResponseStatus.getMessage()).thenReturn("sampleMessage");
        result = operation.setOutcome(mockOperationOutcome, PolicyResult.FAILURE, mockOutputWrapper);
        assertEquals(PolicyResult.FAILURE, result.getResult());
        assertNotNull(result.getMessage());

        Mockito.verify(mockOutputWrapper, atLeast(1)).getBody();
        Mockito.verify(mockBody, atLeast(1)).getOutput();
        Mockito.verify(mockOutput, atLeast(1)).getStatus();
        Mockito.verify(mockResponseStatus, atLeast(1)).getMessage();
        Mockito.verify(operation, atLeast(1)).setOutcome(mockOperationOutcome, PolicyResult.SUCCESS, mockOutputWrapper);
        Mockito.verify(operation, atLeast(1)).setOutcome(mockOperationOutcome, PolicyResult.FAILURE, mockOutputWrapper);
    }

}
