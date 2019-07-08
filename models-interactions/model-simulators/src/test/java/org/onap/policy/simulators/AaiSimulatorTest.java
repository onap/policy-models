/*-
 * ============LICENSE_START=======================================================
 * simulators
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.simulators;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.policy.aai.AaiCqResponse;
import org.onap.policy.aai.AaiGetVnfResponse;
import org.onap.policy.aai.AaiManager;
import org.onap.policy.aai.AaiNqInstanceFilters;
import org.onap.policy.aai.AaiNqNamedQuery;
import org.onap.policy.aai.AaiNqQueryParameters;
import org.onap.policy.aai.AaiNqRequest;
import org.onap.policy.aai.AaiNqResponse;
import org.onap.policy.common.endpoints.http.server.HttpServletServerFactoryInstance;
import org.onap.policy.rest.RestManager;

public class AaiSimulatorTest {

    /**
     * Set up test class.
     */
    @BeforeClass
    public static void setUpSimulator() {
        try {
            Util.buildAaiSim();
        } catch (final Exception e) {
            fail(e.getMessage());
        }
    }

    @AfterClass
    public static void tearDownSimulator() {
        HttpServletServerFactoryInstance.getServerFactory().destroy();
    }

    @Test
    public void testGet() {
        final AaiGetVnfResponse response = new AaiManager(new RestManager()).getQueryByVnfId(
                "http://localhost:6666/aai/v11/network/generic-vnfs/generic-vnf/", "testUser", "testPass",
                UUID.randomUUID(), "5e49ca06-2972-4532-9ed4-6d071588d792");
        assertNotNull(response);
        assertNotNull(response.getRelationshipList());
    }

    @Test
    public void testCqGet() {
        final AaiCqResponse response = new AaiManager(new RestManager()).getCustomQueryResponse("http://localhost:6666",
                "testUser", "testPass", UUID.randomUUID(), "vfw-vm-0201-2");
        assertNotNull(response);
        assertEquals(response.getVserver().getVserverName(), "vfw-vm-0201-2");
    }

    @Test
    public void testPost() {
        // check vserver named query
        final AaiNqRequest request = new AaiNqRequest();
        final AaiNqQueryParameters tempQueryParameters = new AaiNqQueryParameters();
        final AaiNqNamedQuery tempNamedQuery = new AaiNqNamedQuery();
        tempNamedQuery.setNamedQueryUuid(UUID.fromString("4ff56a54-9e3f-46b7-a337-07a1d3c6b469"));
        tempQueryParameters.setNamedQuery(tempNamedQuery);
        request.setQueryParameters(tempQueryParameters);
        Map<String, String> tempInnerMap = new HashMap<>();
        tempInnerMap.put("vserver-name", "vserver-name-16102016-aai3255-data-11-1");
        Map<String, Map<String, String>> tempOuterMap = new HashMap<>();
        tempOuterMap.put("vserver", tempInnerMap);
        List<Map<String, Map<String, String>>> tempInstanceFilter = new LinkedList<>();
        tempInstanceFilter.add(tempOuterMap);
        AaiNqInstanceFilters tempInstanceFilters = new AaiNqInstanceFilters();
        tempInstanceFilters.setInstanceFilter(tempInstanceFilter);
        request.setInstanceFilters(tempInstanceFilters);

        AaiNqResponse response = new AaiManager(new RestManager()).postQuery("http://localhost:6666", "testUser",
                "testPass", request, UUID.randomUUID());
        assertNotNull(response);
        assertNotNull(response.getInventoryResponseItems());

        // check error response for vserver query
        tempInnerMap.put("vserver-name", "error");

        response = new AaiManager(new RestManager()).postQuery("http://localhost:6666", "testUser", "testPass", request,
                UUID.randomUUID());
        assertNotNull(response);
        assertNotNull(response.getRequestError());
        assertTrue(response.getRequestError().getServiceExcept().getVariables()[2].contains("vserver"));

        // check generic-vnf named query
        tempNamedQuery.setNamedQueryUuid(UUID.fromString("a93ac487-409c-4e8c-9e5f-334ae8f99087"));
        tempQueryParameters.setNamedQuery(tempNamedQuery);
        request.setQueryParameters(tempQueryParameters);
        tempInnerMap = new HashMap<>();
        tempInnerMap.put("vnf-id", "de7cc3ab-0212-47df-9e64-da1c79234deb");
        tempOuterMap = new HashMap<>();
        tempOuterMap.put("generic-vnf", tempInnerMap);
        tempInstanceFilter = new LinkedList<>();
        tempInstanceFilter.add(tempOuterMap);
        tempInstanceFilters = new AaiNqInstanceFilters();
        tempInstanceFilters.setInstanceFilter(tempInstanceFilter);
        request.setInstanceFilters(tempInstanceFilters);

        response = new AaiManager(new RestManager()).postQuery("http://localhost:6666", "testUser", "testPass", request,
                UUID.randomUUID());
        assertNotNull(response);
        assertNotNull(response.getInventoryResponseItems());
        assertNull(response.getRequestError());

        // check error response for generic-vnf query
        tempInnerMap.put("vnf-id", "error");

        response = new AaiManager(new RestManager()).postQuery("http://localhost:6666", "testUser", "testPass", request,
                UUID.randomUUID());
        assertNotNull(response);
        assertNotNull(response.getRequestError());
        assertTrue(response.getRequestError().getServiceExcept().getVariables()[2].contains("generic-vnf"));
    }
}
