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

package org.onap.policy.controlloop.actor.test;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.core.Response;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.policy.common.endpoints.http.client.HttpClient;
import org.onap.policy.common.endpoints.http.client.HttpClientFactory;
import org.onap.policy.controlloop.VirtualControlLoopEvent;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.Util;
import org.onap.policy.controlloop.actorserviceprovider.controlloop.ControlLoopEventContext;
import org.onap.policy.controlloop.actorserviceprovider.impl.HttpOperator;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpParams;

/**
 * Superclass for various operator tests.
 *
 * @param <Q> request type
 */
public abstract class BasicOperator<Q> {
    protected static final UUID REQ_ID = UUID.randomUUID();
    protected static final String DEFAULT_ACTOR = "my-actor";
    protected static final String DEFAULT_OPERATION = "my-operation";
    protected static final String MY_CLIENT = "my-client";
    protected static final String BASE_URI = "/base-uri";
    protected static final String PATH = "my-path";
    protected static final String TARGET_ENTITY = "my-target";

    protected final String actor;
    protected final String operation;

    @Captor
    protected ArgumentCaptor<InvocationCallback<Response>> callbackCaptor;

    @Captor
    protected ArgumentCaptor<Entity<Q>> requestCaptor;

    @Mock
    protected HttpClient client;

    @Mock
    protected HttpClientFactory factory;

    @Mock
    protected Response rawResponse;

    protected CompletableFuture<Response> future;
    protected ControlLoopOperationParams params;
    protected Map<String, String> enrichment;
    protected VirtualControlLoopEvent event;
    protected ControlLoopEventContext context;
    protected OperationOutcome outcome;

    /**
     * Constructs the object using a default actor and operation name.
     */
    public BasicOperator() {
        this.actor = DEFAULT_ACTOR;
        this.operation = DEFAULT_OPERATION;
    }

    /**
     * Constructs the object.
     *
     * @param actor actor name
     * @param operation operation name
     */
    public BasicOperator(String actor, String operation) {
        this.actor = actor;
        this.operation = operation;
    }

    /**
     * Initializes mocks and sets up.
     */
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(factory.get(MY_CLIENT)).thenReturn(client);

        when(rawResponse.getStatus()).thenReturn(200);

        future = new CompletableFuture<>();
        when(client.getBaseUrl()).thenReturn(BASE_URI);

        makeContext();

        outcome = params.makeOutcome();
    }

    /**
     * Configures and starts the operator, arranging for it to return {@link #factory} as
     * its client factory.
     *
     * @param <T> operator type
     * @param operator operator to be configured
     * @return a spy on the operator
     */
    protected <T extends HttpOperator<R, S>, R, S> T configure(T operator) {
        operator = spy(operator);
        when(operator.getClientFactory()).thenReturn(factory);

        HttpParams config = HttpParams.builder().clientName(MY_CLIENT).path(PATH).build();
        Map<String, Object> mapParams = Util.translateToMap(operation, config);
        operator.configure(mapParams);
        operator.start();

        return operator;
    }

    /**
     * Reinitializes {@link #enrichment}, {@link #event}, {@link #context}, and
     * {@link #params}.
     */
    protected void makeContext() {
        enrichment = new TreeMap<>(makeEnrichment());

        event = new VirtualControlLoopEvent();
        event.setRequestId(REQ_ID);
        event.setAai(enrichment);

        context = new ControlLoopEventContext(event);

        params = ControlLoopOperationParams.builder().context(context).actor(actor).operation(operation)
                        .targetEntity(TARGET_ENTITY).build();
    }

    /**
     * Makes enrichment data.
     *
     * @return enrichment data
     */
    protected Map<String, String> makeEnrichment() {
        return new TreeMap<>();
    }
}
