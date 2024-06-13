/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020-2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2020 Wipro Limited.
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

package org.onap.policy.controlloop.actor.so;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import jakarta.ws.rs.client.InvocationCallback;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.aai.domain.yang.CloudRegion;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.aai.domain.yang.ServiceInstance;
import org.onap.aai.domain.yang.Tenant;
import org.onap.policy.common.endpoints.http.client.HttpClientFactoryInstance;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.OperationProperties;
import org.onap.policy.controlloop.actorserviceprovider.OperationResult;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpPollingConfig;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpPollingParams;
import org.onap.policy.so.SoRequest;
import org.onap.policy.so.SoResponse;

@ExtendWith(MockitoExtension.class)
class VfModuleDeleteTest extends BasicSoOperation {
    private static final String EXPECTED_EXCEPTION = "expected exception";
    private static final String SVC_INSTANCE_ID = "my-service-instance-id";
    private static final String VNF_ID = "my-vnf-id";

    @Mock
    private java.net.http.HttpClient javaClient;
    @Mock
    private HttpResponse<String> javaResp;
    @Mock
    private InvocationCallback<Response> callback;

    private CompletableFuture<HttpResponse<String>> javaFuture;
    private VfModuleDelete oper;

    VfModuleDeleteTest() {
        super(DEFAULT_ACTOR, VfModuleDelete.NAME);
    }

    @BeforeAll
    static void setUpBeforeClass() throws Exception {
        initBeforeClass();
    }

    @AfterAll
    static void tearDownAfterClass() {
        destroyAfterClass();
    }

    /**
     * Sets up.
     */
    @Override
    @BeforeEach
    void setUp() throws Exception {
        super.setUp();

        initHostPort();

        configureResponse(coder.encode(response));

        oper = new MyOperation(params, config);

        loadProperties();
    }

    /**
     * Tests "success" case with simulator.
     */
    @Test
    void testSuccess() throws Exception {
        HttpPollingParams opParams = HttpPollingParams.builder().clientName(MY_CLIENT).path("serviceInstances/v7")
            .pollPath("orchestrationRequests/v5/").maxPolls(2).build();
        config = new HttpPollingConfig(blockingExecutor, opParams, HttpClientFactoryInstance.getClientFactory());

        params = params.toBuilder().retry(0).timeoutSec(5).executor(blockingExecutor).build();

        oper = new VfModuleDelete(params, config);

        loadProperties();

        // run the operation
        outcome = oper.start().get();
        assertEquals(OperationResult.SUCCESS, outcome.getResult());
        assertInstanceOf(SoResponse.class, outcome.getResponse());

        int count = oper.getProperty(OperationProperties.DATA_VF_COUNT);
        assertEquals(VF_COUNT - 1, count);
    }

    @Test
    void testConstructor() {
        assertEquals(DEFAULT_ACTOR, oper.getActorName());
        assertEquals(VfModuleDelete.NAME, oper.getName());
        assertTrue(oper.isUsePolling());

        // verify that target validation is done
        params = params.toBuilder().targetType(null).build();
        assertThatIllegalArgumentException().isThrownBy(() -> new MyOperation(params, config))
            .withMessageContaining("Target information");
    }

    @Test
    void testGetPropertyNames() {
        // @formatter:off
        assertThat(oper.getPropertyNames()).isEqualTo(
            List.of(
                OperationProperties.AAI_SERVICE,
                OperationProperties.AAI_VNF,
                OperationProperties.AAI_DEFAULT_CLOUD_REGION,
                OperationProperties.AAI_DEFAULT_TENANT,
                OperationProperties.DATA_VF_COUNT));
        // @formatter:on
    }

    @Test
    void testStartOperationAsync_testSuccessfulCompletion() throws Exception {
        // use a real executor
        params = params.toBuilder().executor(ForkJoinPool.commonPool()).build();

        oper = new MyOperation(params, config) {
            @Override
            public long getPollWaitMs() {
                return 1;
            }
        };

        loadProperties();

        final int origCount = 30;
        oper.setVfCount(origCount);

        CompletableFuture<OperationOutcome> future2 = oper.start();

        outcome = future2.get(5, TimeUnit.SECONDS);
        assertEquals(OperationResult.SUCCESS, outcome.getResult());

        SoResponse resp = outcome.getResponse();
        assertNotNull(resp);
        assertEquals(REQ_ID.toString(), resp.getRequestReferences().getRequestId());

        assertEquals(origCount - 1, oper.getVfCount());
    }

    /**
     * Tests startOperationAsync() when polling is required.
     */
    @Test
    void testStartOperationAsyncWithPolling() throws Exception {

        // indicate that the response was incomplete
        configureResponse(coder.encode(response).replace("COMPLETE", "incomplete"));

        lenient().when(rawResponse.getStatus()).thenReturn(500, 500, 500, 200, 200);
        lenient().when(client.get(any(), any(), any())).thenAnswer(provideResponse(rawResponse));

        // use a real executor
        params = params.toBuilder().executor(ForkJoinPool.commonPool()).build();

        oper = new MyOperation(params, config) {
            @Override
            public long getPollWaitMs() {
                return 1;
            }
        };

        loadProperties();

        CompletableFuture<OperationOutcome> future2 = oper.start();

        outcome = future2.get(5, TimeUnit.SECONDS);
        assertEquals(OperationResult.SUCCESS, outcome.getResult());
    }

    @Test
    void testMakeRequest() throws CoderException {
        Pair<String, SoRequest> pair = oper.makeRequest();

        assertEquals("/my-service-instance-id/vnfs/my-vnf-id/vfModules/null", pair.getLeft());

        verifyRequest("VfModuleDelete.json", pair.getRight());
    }

    @Test
    void testDelete() throws Exception {
        SoRequest req = new SoRequest();
        req.setRequestId(REQ_ID);

        Map<String, Object> headers = Map.of("key-A", "value-A");

        String reqText = oper.prettyPrint(req);

        final CompletableFuture<Response> delFuture =
            oper.delete("my-uri", headers, MediaType.APPLICATION_JSON, reqText, callback);

        ArgumentCaptor<HttpRequest> reqCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(javaClient).sendAsync(reqCaptor.capture(), any());

        HttpRequest req2 = reqCaptor.getValue();
        assertEquals("http://my-host:6969/my-uri", req2.uri().toString());
        assertEquals("DELETE", req2.method());

        HttpHeaders headers2 = req2.headers();
        assertEquals("value-A", headers2.firstValue("key-A").orElse("missing-key"));
        assertEquals(MediaType.APPLICATION_JSON, headers2.firstValue("Content-type").orElse("missing-key"));

        assertTrue(delFuture.isDone());
        Response resp = delFuture.get();

        verify(callback).completed(resp);

        assertEquals(200, resp.getStatus());

        SoResponse resp2 = resp.readEntity(SoResponse.class);
        assertEquals(SoOperation.COMPLETE, resp2.getRequest().getRequestStatus().getRequestState());
    }

    /**
     * Tests delete() when an exception is thrown in the future.
     */
    @Test
    @SuppressWarnings("unchecked")
    void testDeleteException() throws Exception {
        Throwable thrown = new IllegalStateException(EXPECTED_EXCEPTION);

        // need a new future, with an exception
        javaFuture = CompletableFuture.failedFuture(thrown);
        lenient().when(javaClient.sendAsync(any(), any(BodyHandlers.ofString().getClass()))).thenReturn(javaFuture);

        SoRequest req = new SoRequest();
        req.setRequestId(REQ_ID);

        String reqText = oper.prettyPrint(req);

        CompletableFuture<Response> delFuture =
            oper.delete("/my-uri", Map.of(), MediaType.APPLICATION_JSON, reqText, callback);

        assertTrue(delFuture.isCompletedExceptionally());

        ArgumentCaptor<Throwable> thrownCaptor = ArgumentCaptor.forClass(Throwable.class);
        verify(callback).failed(thrownCaptor.capture());
        assertSame(thrown, thrownCaptor.getValue().getCause());
    }

    /**
     * Tests addAuthHeader() when there is a username, but no password.
     */
    @Test
    void testAddAuthHeader() {
        Builder builder = mock(Builder.class);
        lenient().when(client.getUserName()).thenReturn("the-user");
        lenient().when(client.getPassword()).thenReturn("the-password");
        oper.addAuthHeader(builder);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);

        verify(builder).header(keyCaptor.capture(), valueCaptor.capture());

        assertEquals("Authorization", keyCaptor.getValue());

        String encoded = Base64.getEncoder().encodeToString("the-user:the-password".getBytes(StandardCharsets.UTF_8));
        assertEquals("Basic " + encoded, valueCaptor.getValue());
    }

    /**
     * Tests addAuthHeader() when there is no username.
     */
    @Test
    void testAddAuthHeaderNoUser() {
        Builder builder = mock(Builder.class);
        lenient().when(client.getPassword()).thenReturn("world");
        oper.addAuthHeader(builder);
        verify(builder, never()).header(any(), any());

        // repeat with empty username
        lenient().when(client.getUserName()).thenReturn("");
        oper.addAuthHeader(builder);
        verify(builder, never()).header(any(), any());
    }

    /**
     * Tests addAuthHeader() when there is a username, but no password.
     */
    @Test
    void testAddAuthHeaderUserOnly() {
        Builder builder = mock(Builder.class);
        lenient().when(client.getUserName()).thenReturn("my-user");
        oper.addAuthHeader(builder);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);

        verify(builder).header(keyCaptor.capture(), valueCaptor.capture());

        assertEquals("Authorization", keyCaptor.getValue());

        String encoded = Base64.getEncoder().encodeToString("my-user:".getBytes(StandardCharsets.UTF_8));
        assertEquals("Basic " + encoded, valueCaptor.getValue());
    }

    /**
     * Tests makeRequest() when a property is missing.
     */
    @Test
    void testMakeRequestMissingProperty() throws Exception {
        loadProperties();

        ServiceInstance instance = new ServiceInstance();
        oper.setProperty(OperationProperties.AAI_SERVICE, instance);

        assertThatIllegalArgumentException().isThrownBy(() -> oper.makeRequest())
            .withMessageContaining("missing service instance ID");
    }

    @Test
    void testMakeHttpClient() {
        // must use a real operation to invoke this method
        assertNotNull(new MyOperation(params, config).makeHttpClient());
    }

    private void initHostPort() {
        lenient().when(client.getBaseUrl()).thenReturn("http://my-host:6969/");
    }

    @SuppressWarnings("unchecked")
    private void configureResponse(String responseText) throws CoderException {
        // indicate that the response was completed
        lenient().when(javaResp.statusCode()).thenReturn(200);
        lenient().when(javaResp.body()).thenReturn(responseText);

        javaFuture = CompletableFuture.completedFuture(javaResp);
        lenient().when(javaClient.sendAsync(any(), any(BodyHandlers.ofString().getClass()))).thenReturn(javaFuture);
    }

    private class MyOperation extends VfModuleDelete {

        MyOperation(ControlLoopOperationParams params, HttpPollingConfig config) {
            super(params, config);
        }

        @Override
        protected java.net.http.HttpClient makeHttpClient() {
            return javaClient;
        }
    }

    private void loadProperties() {
        // set the properties
        ServiceInstance instance = new ServiceInstance();
        instance.setServiceInstanceId(SVC_INSTANCE_ID);
        oper.setProperty(OperationProperties.AAI_SERVICE, instance);

        GenericVnf vnf = new GenericVnf();
        vnf.setVnfId(VNF_ID);
        oper.setProperty(OperationProperties.AAI_VNF, vnf);

        CloudRegion cloudRegion = new CloudRegion();
        cloudRegion.setCloudRegionId("my-cloud-id");
        oper.setProperty(OperationProperties.AAI_DEFAULT_CLOUD_REGION, cloudRegion);

        Tenant tenant = new Tenant();
        tenant.setTenantId("my-tenant-id");
        oper.setProperty(OperationProperties.AAI_DEFAULT_TENANT, tenant);

        oper.setProperty(OperationProperties.DATA_VF_COUNT, VF_COUNT);
    }
}
