/*
 * ============LICENSE_START=======================================================
 * TestSOActorServiceProvider
 * ================================================================================
 * Copyright (C) 2018 Ericsson. All rights reserved.
 * ================================================================================
 * Modifications Copyright (C) 2018-2019 AT&T. All rights reserved.
 * Modifications Copyright (C) 2019 Nordix Foundation.
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

package org.onap.policy.controlloop.actor.so;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.onap.policy.aai.AaiNqResponse;
import org.onap.policy.aai.AaiNqResponseWrapper;
import org.onap.policy.controlloop.ControlLoopOperation;
import org.onap.policy.controlloop.VirtualControlLoopEvent;
import org.onap.policy.controlloop.policy.Policy;
import org.onap.policy.so.SoOperationType;
import org.onap.policy.so.SoRequest;
import org.onap.policy.so.SoRequestParameters;
import org.onap.policy.so.util.Serialization;

public class SoActorServiceProviderTest {

    private static final String VF_MODULE_CREATE = "VF Module Create";
    private static final String VF_MODULE_DELETE = "VF Module Delete";

    @Test
    public void testConstructRequest() throws Exception {
        VirtualControlLoopEvent onset = new VirtualControlLoopEvent();
        final ControlLoopOperation operation = new ControlLoopOperation();
        final AaiNqResponseWrapper aaiNqResp = loadAaiResponse(onset, "aai/AaiNqResponse-Full.json");

        final UUID requestId = UUID.randomUUID();
        onset.setRequestId(requestId);

        Policy policy = new Policy();
        policy.setActor("Dorothy");
        policy.setRecipe("GoToOz");
        
        assertNull(new SoActorServiceProvider().constructRequest(onset, operation, policy, aaiNqResp));

        policy.setActor("SO");
        assertNull(new SoActorServiceProvider().constructRequest(onset, operation, policy, aaiNqResp));

        policy.setRecipe(VF_MODULE_CREATE);

        // empty policy payload
        SoRequest request = new SoActorServiceProvider().constructRequest(onset, operation, policy, aaiNqResp);
        assertNotNull(request);

        assertEquals("my_module_3", request.getRequestDetails().getRequestInfo().getInstanceName());
        assertEquals("policy", request.getRequestDetails().getRequestInfo().getRequestorId());
        assertEquals("RegionOne", request.getRequestDetails().getCloudConfiguration().getLcpCloudRegionId());

        // non-empty policy payload
        policy.setPayload(makePayload());
        request = new SoActorServiceProvider().constructRequest(onset, operation, policy, aaiNqResp);
        assertNotNull(request);
        assertEquals(true, request.getRequestDetails().getRequestParameters().isUsePreload());
        assertEquals("avalue", request.getRequestDetails().getRequestParameters().getUserParams().get(0).get("akey"));
        assertEquals(1, request.getRequestDetails().getConfigurationParameters().size());
        assertEquals("cvalue", request.getRequestDetails().getConfigurationParameters().get(0).get("ckey"));
        
        // payload with config, but no request params
        policy.setPayload(makePayload());
        policy.getPayload().remove(SoActorServiceProvider.REQ_PARAM_NM);
        request = new SoActorServiceProvider().constructRequest(onset, operation, policy, aaiNqResp);
        assertNotNull(request);
        assertNull(request.getRequestDetails().getRequestParameters());
        assertNotNull(request.getRequestDetails().getConfigurationParameters());
        
        // payload with request, but no config params
        policy.setPayload(makePayload());
        policy.getPayload().remove(SoActorServiceProvider.CONFIG_PARAM_NM);
        request = new SoActorServiceProvider().constructRequest(onset, operation, policy, aaiNqResp);
        assertNotNull(request);
        assertNotNull(request.getRequestDetails().getRequestParameters());
        assertNull(request.getRequestDetails().getConfigurationParameters());

        // null response
        assertNull(new SoActorServiceProvider().constructRequest(onset, operation, policy, null));

        // response has no base VF module
        assertNull(new SoActorServiceProvider().constructRequest(onset, operation, policy,
                        loadAaiResponse(onset, "aai/AaiNqResponse-NoBase.json")));

        // response has no non-base VF modules (other than the "dummy")
        assertNull(new SoActorServiceProvider().constructRequest(onset, operation, policy,
                        loadAaiResponse(onset, "aai/AaiNqResponse-NoNonBase.json")));

        policy.setRecipe(VF_MODULE_DELETE);
        SoRequest deleteRequest = new SoActorServiceProvider().constructRequest(onset, operation, policy, aaiNqResp);
        assertNotNull(deleteRequest);
        assertEquals(SoOperationType.DELETE_VF_MODULE, deleteRequest.getOperationType());

        /*
         * NOTE: The remaining tests must be done in order
         */

        policy.setRecipe(VF_MODULE_CREATE);

        // null tenant
        aaiNqResp.getAaiNqResponse().getInventoryResponseItems().get(0).getItems().getInventoryResponseItems()
                        .remove(1);
        assertNull(new SoActorServiceProvider().constructRequest(onset, operation, policy, aaiNqResp));

        // null service item
        aaiNqResp.getAaiNqResponse().getInventoryResponseItems().get(0).getItems().getInventoryResponseItems().get(0)
                        .setItems(null);
        assertNull(new SoActorServiceProvider().constructRequest(onset, operation, policy, aaiNqResp));

        // null response
        aaiNqResp.setAaiNqResponse(null);
        assertNull(new SoActorServiceProvider().constructRequest(onset, operation, policy, aaiNqResp));
    }

    @Test
    public void testSendRequest() {
        try {
            SoActorServiceProvider.sendRequest(UUID.randomUUID().toString(), null, null);
        } catch (Exception e) {
            fail("Test should not throw an exception");
        }
    }

    @Test
    public void testMethods() {
        SoActorServiceProvider sp = new SoActorServiceProvider();

        assertEquals("SO", sp.actor());
        assertEquals(2, sp.recipes().size());
        assertEquals(VF_MODULE_CREATE, sp.recipes().get(0));
        assertEquals(VF_MODULE_DELETE, sp.recipes().get(1));
        assertEquals(0, sp.recipePayloads(VF_MODULE_CREATE).size());
        assertEquals(0, sp.recipeTargets("unknown recipe").size());
        assertEquals(1, sp.recipeTargets(VF_MODULE_CREATE).size());
    }

    /**
     * Creates a policy payload containing request & configuration parameters.
     *
     * @return the payload
     */
    private Map<String, String> makePayload() {
        Map<String, String> payload = new TreeMap<>();

        payload.put(SoActorServiceProvider.REQ_PARAM_NM, makeReqParams());
        payload.put(SoActorServiceProvider.CONFIG_PARAM_NM, makeConfigParams());

        return payload;
    }

    /**
     * Creates request parameters.
     *
     * @return request parameters, encoded as JSON
     */
    private String makeReqParams() {
        SoRequestParameters params = new SoRequestParameters();

        params.setUsePreload(true);

        Map<String, String> map = new TreeMap<>();
        map.put("akey", "avalue");

        List<Map<String, String>> lst = new LinkedList<>();
        lst.add(map);

        params.setUserParams(lst);

        return Serialization.gsonPretty.toJson(params);
    }

    /**
     * Creates configuration parameters.
     *
     * @return configuration parameters, encoded as JSON
     */
    private String makeConfigParams() {
        Map<String, String> map = new TreeMap<>();
        map.put("ckey", "cvalue");

        List<Map<String, String>> lst = new LinkedList<>();
        lst.add(map);

        return Serialization.gsonPretty.toJson(lst);
    }

    /**
     * Reads an AAI vserver named-query response from a file.
     *
     * @param onset the ONSET event
     * @param fileName name of the file containing the JSON response
     * @return output from the AAI vserver named-query
     * @throws IOException if the file cannot be read
     */
    private AaiNqResponseWrapper loadAaiResponse(VirtualControlLoopEvent onset, String fileName) throws IOException {
        String resp = IOUtils.toString(getClass().getResource(fileName), StandardCharsets.UTF_8);
        AaiNqResponse aaiNqResponse = Serialization.gsonPretty.fromJson(resp, AaiNqResponse.class);

        return new AaiNqResponseWrapper(onset.getRequestId(), aaiNqResponse);
    }
}
