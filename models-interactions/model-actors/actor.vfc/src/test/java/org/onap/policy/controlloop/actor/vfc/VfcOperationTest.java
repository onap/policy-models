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

package org.onap.policy.controlloop.actor.vfc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CompletableFuture;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.aai.domain.yang.CloudRegion;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.aai.domain.yang.ServiceInstance;
import org.onap.aai.domain.yang.Tenant;
import org.onap.policy.aai.AaiCqResponse;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.policy.PolicyResult;
import org.onap.policy.vfc.VfcHealActionVmInfo;
import org.onap.policy.vfc.VfcResponse;
import org.onap.policy.vfc.VfcResponseDescriptor;

public class VfcOperationTest extends BasicVfcOperation {

    private VfcOperation oper;

    /**
     * setUp.
     */
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        initConfig();

        oper = new VfcOperation(params, config) {};
    }

    @Test
    public void testConstructor_testGetWaitMsGet() {
        assertEquals(DEFAULT_ACTOR, oper.getActorName());
        assertEquals(DEFAULT_OPERATION, oper.getName());
        assertSame(config, oper.getConfig());
        assertEquals(1000 * WAIT_SEC_GETS, oper.getWaitMsGet());
    }

    @Test
    public void testStartPreprocessorAsync() {
        assertNotNull(oper.startPreprocessorAsync());
    }

    @Ignore
    @Test
    public void testPostProcess() throws Exception {
        // completed
        CompletableFuture<OperationOutcome> future2 = oper.postProcessResponse(outcome, PATH, rawResponse, response);
        assertTrue(future2.isDone());
        assertSame(outcome, future2.get());
        assertEquals(PolicyResult.SUCCESS, outcome.getResult());

        // failed
        response.getResponseDescriptor().setStatus("anything but finished");
        future2 = oper.postProcessResponse(outcome, PATH, rawResponse, response);
        assertTrue(future2.isDone());
        assertSame(outcome, future2.get());
        assertEquals(PolicyResult.FAILURE, outcome.getResult());
    }

    @Test
    public void testGetRequestState() {
        VfcResponse mockResponse = Mockito.mock(VfcResponse.class);
        Mockito.when(mockResponse.getResponseDescriptor()).thenReturn(null);
        assertNull(oper.getRequestState(mockResponse));

        VfcResponseDescriptor mockDescriptor = Mockito.mock(VfcResponseDescriptor.class);
        Mockito.when(mockResponse.getResponseDescriptor()).thenReturn(mockDescriptor);
        Mockito.when(mockDescriptor.getStatus()).thenReturn("COMPLETE"); // TODO use actual request state value
        assertNotNull(oper.getRequestState(mockResponse));
    }

    @Test
    public void testIsSuccess() {
        assertTrue(oper.isSuccess(rawResponse, response));
    }

    @Test
    public void testExtractionMethods() {
        GenericVnf mockVnf = Mockito.mock(GenericVnf.class);
        AaiCqResponse mockAaiCq = Mockito.mock(AaiCqResponse.class);
        VfcHealActionVmInfo mockVmInfo = Mockito.mock(VfcHealActionVmInfo.class);
        ServiceInstance mockServiceInstance = Mockito.mock(ServiceInstance.class);
        Tenant mockTenant = Mockito.mock(Tenant.class);
        CloudRegion mockCloudRegion = Mockito.mock(CloudRegion.class);

        Mockito.when(mockAaiCq.getGenericVnfByVnfName(Mockito.any(String.class))).thenReturn(mockVnf);
        Mockito.when(mockVmInfo.getVmname()).thenReturn("vserver-name-16102016-aai3255-data-11-1");
        Mockito.when(mockAaiCq.getServiceInstance()).thenReturn(mockServiceInstance);
        Mockito.when(mockAaiCq.getDefaultTenant()).thenReturn(mockTenant);
        Mockito.when(mockAaiCq.getDefaultCloudRegion()).thenReturn(mockCloudRegion);

        assertNotNull(oper.getVnfItem(mockAaiCq, mockVmInfo));
        assertNotNull(oper.getServiceInstance(mockAaiCq));
        assertNotNull(oper.getDefaultTenant(mockAaiCq));
        assertNotNull(oper.getDefaultCloudRegion(mockAaiCq));
    }

}
