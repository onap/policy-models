/*-
 * ============LICENSE_START=======================================================
 * simulators
 * ================================================================================
 * Copyright (C) 2017-2020 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019-2024 Nordix Foundation.
 * Modifications Copyright (C) 2020 Wipro Limited.
 * Modifications Copyright (C) 2022 CTC, Inc. and others.
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.HashMap;
import java.util.UUID;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.onap.policy.common.endpoints.http.server.HttpServletServerFactoryInstance;
import org.onap.policy.rest.RestManager;
import org.onap.policy.so.SoCloudConfiguration;
import org.onap.policy.so.SoModelInfo;
import org.onap.policy.so.SoRelatedInstance;
import org.onap.policy.so.SoRelatedInstanceListElement;
import org.onap.policy.so.SoRequest;
import org.onap.policy.so.SoRequest3gpp;
import org.onap.policy.so.SoRequestCll;
import org.onap.policy.so.SoRequestDetails;
import org.onap.policy.so.SoRequestInfo;
import org.onap.policy.so.SoRequestParameters;
import org.onap.policy.so.util.Serialization;

class SoSimulatorTest {

    /**
     * Set up test class.
     */
    @BeforeAll
    static void setUpSimulator() {
        try {
            var testServer = Util.buildSoSim();
            assertNotNull(testServer);
        } catch (final Exception e) {
            fail(e.getMessage());
        }
    }

    @AfterAll
    static void tearDownSimulator() {
        HttpServletServerFactoryInstance.getServerFactory().destroy();
        SoSimulatorJaxRs.setRequirePolling(false);
    }

    /**
     * Create dummy SO request for TestResponse() junit.
     */
    private SoRequest createTestRequest() {

        // Construct SO Request
        final SoRequest request = new SoRequest();
        request.setRequestId(UUID.randomUUID());
        request.setRequestDetails(new SoRequestDetails());
        request.getRequestDetails().setModelInfo(new SoModelInfo());
        request.getRequestDetails().setCloudConfiguration(new SoCloudConfiguration());
        request.getRequestDetails().setRequestInfo(new SoRequestInfo());
        request.getRequestDetails().setRequestParameters(new SoRequestParameters());
        request.getRequestDetails().getRequestParameters().setUserParams(null);
        //
        // cloudConfiguration
        //
        request.getRequestDetails().getCloudConfiguration().setLcpCloudRegionId("DFW");
        request.getRequestDetails().getCloudConfiguration().setTenantId("1015548");
        //
        // modelInfo
        //
        request.getRequestDetails().getModelInfo().setModelType("vfModule");
        request.getRequestDetails().getModelInfo().setModelInvariantId("f32568ec-2f1c-458a-864b-0593d53d141a");
        request.getRequestDetails().getModelInfo().setModelVersionId("69615025-879d-4f0d-afe3-b7d1a7eeed1f");
        request.getRequestDetails().getModelInfo().setModelName("C15ce9e1E9144c8fB8bb..dnsscaling..module-1");
        request.getRequestDetails().getModelInfo().setModelVersion("1.0");
        //
        // requestInfo
        //
        request.getRequestDetails().getRequestInfo()
                        .setInstanceName("vDNS_Ete_Named90e1ab3-dcd5-4877-9edb-eadfc84e32c8");
        request.getRequestDetails().getRequestInfo().setSource("POLICY");
        request.getRequestDetails().getRequestInfo().setSuppressRollback(false);
        request.getRequestDetails().getRequestInfo().setRequestorId("policy");
        //
        // relatedInstanceList
        //
        final SoRelatedInstanceListElement relatedInstanceListElement1 = new SoRelatedInstanceListElement();
        final SoRelatedInstanceListElement relatedInstanceListElement2 = new SoRelatedInstanceListElement();
        relatedInstanceListElement1.setRelatedInstance(new SoRelatedInstance());
        relatedInstanceListElement2.setRelatedInstance(new SoRelatedInstance());
        //
        relatedInstanceListElement1.getRelatedInstance().setInstanceId("cf8426a6-0b53-4e3d-bfa6-4b2f4d5913a5");
        relatedInstanceListElement1.getRelatedInstance().setModelInfo(new SoModelInfo());
        relatedInstanceListElement1.getRelatedInstance().getModelInfo().setModelType("service");
        relatedInstanceListElement1.getRelatedInstance().getModelInfo()
                        .setModelInvariantId("4fcbc1c0-7793-46d8-8aa1-fa1c2ed9ec7b");
        relatedInstanceListElement1.getRelatedInstance().getModelInfo()
                        .setModelVersionId("5c996219-b2e2-4c76-9b43-7e8672a33c1d");
        relatedInstanceListElement1.getRelatedInstance().getModelInfo().setModelName("8330e932-2a23-4943-8606");
        relatedInstanceListElement1.getRelatedInstance().getModelInfo().setModelVersion("1.0");
        //
        relatedInstanceListElement2.getRelatedInstance().setInstanceId("594e2fe0-48b8-41ff-82e2-3d4bab69b192");
        relatedInstanceListElement2.getRelatedInstance().setModelInfo(new SoModelInfo());
        relatedInstanceListElement2.getRelatedInstance().getModelInfo().setModelType("vnf");
        relatedInstanceListElement2.getRelatedInstance().getModelInfo()
                        .setModelInvariantId("033a32ed-aa65-4764-a736-36f2942f1aa0");
        relatedInstanceListElement2.getRelatedInstance().getModelInfo()
                        .setModelVersionId("d4d072dc-4e21-4a03-9524-628985819a8e");
        relatedInstanceListElement2.getRelatedInstance().getModelInfo().setModelName("c15ce9e1-e914-4c8f-b8bb");
        relatedInstanceListElement2.getRelatedInstance().getModelInfo().setModelVersion("1");
        relatedInstanceListElement2.getRelatedInstance().getModelInfo()
                        .setModelCustomizationName("c15ce9e1-e914-4c8f-b8bb 1");
        //
        request.getRequestDetails().getRelatedInstanceList().add(relatedInstanceListElement1);
        request.getRequestDetails().getRelatedInstanceList().add(relatedInstanceListElement2);

        return request;
    }

    @Test
    void testPost() {
        SoSimulatorJaxRs.setRequirePolling(false);
        String request = Serialization.gsonPretty.toJson(this.createTestRequest());
        Pair<Integer, String> httpDetails = new RestManager().post(
                        "http://localhost:6667/serviceInstantiation/v7/serviceInstances/12345/vnfs/12345/vfModules/scaleOut",
                        "username",
                        "password", new HashMap<>(), "application/json", request);
        assertNotNull(httpDetails);
        assertThat(httpDetails.getRight()).contains("\"COMPLETE\"").contains("requestSelfLink");

        /*
         * Repeat, but set the flag indicating that the request should yield incomplete.
         */
        SoSimulatorJaxRs.setRequirePolling(true);

        request = Serialization.gsonPretty.toJson(this.createTestRequest());
        httpDetails = new RestManager().post(
                        "http://localhost:6667/serviceInstantiation/v7/serviceInstances/12345/vnfs/12345/vfModules/scaleOut",
                        "username",
                        "password", new HashMap<>(), "application/json", request);
        assertNotNull(httpDetails);
        assertThat(httpDetails.getRight()).contains("requestSelfLink").doesNotContain("\"COMPLETE\"");

        // now poll for the response
        String uri = extractUri(httpDetails.getRight());
        httpDetails = new RestManager().get(
                        "http://localhost:6667/orchestrationRequests/v5/" + uri,
                        "username",
                        "password", new HashMap<>());
        assertNotNull(httpDetails);
        assertThat(httpDetails.getRight()).contains("\"IN_PROGRESS\"").doesNotContain("requestSelfLink");

        // poll again
        httpDetails = new RestManager().get(
                        "http://localhost:6667/orchestrationRequests/v5/" + uri,
                        "username",
                        "password", new HashMap<>());
        assertNotNull(httpDetails);
        assertThat(httpDetails.getRight()).contains("\"COMPLETE\"").doesNotContain("requestSelfLink");
    }

    @Test
    void testDelete() {
        SoSimulatorJaxRs.setRequirePolling(false);
        String request = Serialization.gsonPretty.toJson(this.createTestRequest());
        Pair<Integer, String> httpDetails = new RestManager().delete(
                        "http://localhost:6667/serviceInstances/v7/12345/vnfs/12345/vfModules/12345",
                        "username",
                        "password", new HashMap<>(), "application/json", request);
        assertNotNull(httpDetails);
        assertThat(httpDetails.getRight()).contains("\"COMPLETE\"").contains("requestSelfLink");

        /*
         * Repeat, but set the flag indicating that the request should yield incomplete.
         */
        SoSimulatorJaxRs.setRequirePolling(true);

        request = Serialization.gsonPretty.toJson(this.createTestRequest());
        httpDetails = new RestManager().delete(
                        "http://localhost:6667/serviceInstances/v7/12345/vnfs/12345/vfModules/12345",
                        "username",
                        "password", new HashMap<>(), "application/json", request);
        assertNotNull(httpDetails);
        assertThat(httpDetails.getRight()).contains("requestSelfLink").doesNotContain("\"COMPLETE\"");

        // now poll for the response
        String uri = extractUri(httpDetails.getRight());
        httpDetails = new RestManager().get(
                        "http://localhost:6667/orchestrationRequests/v5/" + uri,
                        "username",
                        "password", new HashMap<>());
        assertNotNull(httpDetails);
        assertThat(httpDetails.getRight()).contains("\"IN_PROGRESS\"").doesNotContain("requestSelfLink");

        // poll again
        httpDetails = new RestManager().get(
                        "http://localhost:6667/orchestrationRequests/v5/" + uri,
                        "username",
                        "password", new HashMap<>());
        assertNotNull(httpDetails);
        assertThat(httpDetails.getRight()).contains("\"COMPLETE\"").doesNotContain("requestSelfLink");
    }

    @Test
    void testModifyNssi() {
        SoSimulatorJaxRs.setRequirePolling(false);
        String request = Serialization.gsonPretty.toJson(this.createNssiRequest());
        Pair<Integer, String> httpDetails = new RestManager().put(
                "http://localhost:6667/3gppservices/v7/modify",
                "username",
                "password", new HashMap<>(), "application/json", request);
        assertNotNull(httpDetails);
        assertEquals(200, httpDetails.getLeft().intValue());
        assertThat(httpDetails.getRight()).contains("jobId").contains("status");
    }

    private SoRequest3gpp createNssiRequest() {
        SoRequest3gpp request = new SoRequest3gpp();

        request.setName("URLLC_core1");
        request.setServiceInstanceId("ff67a209-dc69-4a1a-b89a-c1f55c2a8842");
        request.setGlobalSubscriberId("5G Customer");
        request.setSubscriptionServiceType("5G");
        request.setNetworkType("an");
        request.setAdditionalProperties(new HashMap<String, Object>());

        return request;
    }

    @Test
    void testModifyCll() {
        SoSimulatorJaxRs.setRequirePolling(false);
        String request = Serialization.gsonPretty.toJson(this.createCllRequest());
        Pair<Integer, String> httpDetails = new RestManager().put(
                "http://localhost:6667/infra/serviceIntent/v1/modify",
                "username",
                "password", new HashMap<>(), "application/json", request);
        assertNotNull(httpDetails);
        assertEquals(200, httpDetails.getLeft().intValue());
        assertThat(httpDetails.getRight()).contains("jobId").contains("status");
    }

    private SoRequestCll createCllRequest() {
        SoRequestCll request = new SoRequestCll();

        request.setName("cloud-leased-line-101");
        request.setServiceInstanceId("cll-instance-01");
        request.setGlobalSubscriberId("IBNCustomer");
        request.setSubscriptionServiceType("ibn");
        request.setServiceType("CLL");
        request.setAdditionalProperties(new HashMap<String, Object>());

        return request;
    }

    private String extractUri(String response) {
        final String prefix = "\"requestId\": \"";

        int start = response.indexOf(prefix);
        assertTrue(start >= 0);

        start += prefix.length();

        int end = response.indexOf('"', start);
        assertTrue(end >= 0);

        return response.substring(start, end);
    }
}
