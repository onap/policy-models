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
import org.mockito.stubbing.Answer;
import org.onap.policy.common.endpoints.http.client.HttpClient;
import org.onap.policy.common.endpoints.http.client.HttpClientFactory;
import org.onap.policy.common.utils.time.PseudoExecutor;
import org.onap.policy.controlloop.VirtualControlLoopEvent;
import org.onap.policy.controlloop.actorserviceprovider.ActorService;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.controlloop.ControlLoopEventContext;
import org.onap.policy.controlloop.actorserviceprovider.impl.HttpOperator;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;

/**
 * Superclass for various operator tests.
 *
 * @param <Q> request type
 */
public class BasicHttpOperation<Q> {
    protected static final UUID REQ_ID = UUID.randomUUID();
    protected static final String DEFAULT_ACTOR = "default-actor";
    protected static final String DEFAULT_OPERATION = "default-operation";
    protected static final String MY_CLIENT = "my-client";
    protected static final String BASE_URI = "/base-uri";
    protected static final String PATH = "/my-path";
    protected static final String TARGET_ENTITY = "my-target";

    protected final String actorName;
    protected final String operationName;

    @Captor
    protected ArgumentCaptor<InvocationCallback<Response>> callbackCaptor;

    @Captor
    protected ArgumentCaptor<Entity<Q>> requestCaptor;

    @Captor
    protected ArgumentCaptor<Map<String, Object>> headerCaptor;

    @Mock
    protected ActorService service;

    @Mock
    protected HttpClient client;

    @Mock
    protected HttpClientFactory factory;

    @Mock
    protected Response rawResponse;

    @Mock
    protected HttpOperator operator;

    protected CompletableFuture<Response> future;
    protected ControlLoopOperationParams params;
    protected Map<String, String> enrichment;
    protected VirtualControlLoopEvent event;
    protected ControlLoopEventContext context;
    protected OperationOutcome outcome;
    protected PseudoExecutor executor;

    /**
     * Constructs the object using a default actor and operation name.
     */
    public BasicHttpOperation() {
        this.actorName = DEFAULT_ACTOR;
        this.operationName = DEFAULT_OPERATION;
    }

    /**
     * Constructs the object.
     *
     * @param actor actor name
     * @param operation operation name
     */
    public BasicHttpOperation(String actor, String operation) {
        this.actorName = actor;
        this.operationName = operation;
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

        executor = new PseudoExecutor();

        makeContext();

        outcome = params.makeOutcome();

        initOperator();
    }

    /**
     * Reinitializes {@link #enrichment}, {@link #event}, {@link #context}, and
     * {@link #params}.
     * <p/>
     * Note: {@link #params} is configured to use {@link #executor}.
     */
    protected void makeContext() {
        enrichment = new TreeMap<>(makeEnrichment());

        event = new VirtualControlLoopEvent();
        event.setRequestId(REQ_ID);
        event.setAai(enrichment);

        context = new ControlLoopEventContext(event);

        params = ControlLoopOperationParams.builder().executor(executor).context(context).actorService(service)
                        .actor(actorName).operation(operationName).targetEntity(TARGET_ENTITY).build();
    }

    /**
     * Initializes an operator so that it is "alive" and has the given names.
     */
    protected void initOperator() {
        when(operator.isAlive()).thenReturn(true);
        when(operator.getFullName()).thenReturn(actorName + "." + operationName);
        when(operator.getActorName()).thenReturn(actorName);
        when(operator.getName()).thenReturn(operationName);
        when(operator.getClient()).thenReturn(client);
        when(operator.getPath()).thenReturn(PATH);
    }

    /**
     * Makes enrichment data.
     *
     * @return enrichment data
     */
    protected Map<String, String> makeEnrichment() {
        return new TreeMap<>();
    }

    /**
     * Provides a response to an asynchronous HttpClient call.
     *
     * @param response response to be provided to the call
     * @return a function that provides the response to the call
     */
    protected Answer<CompletableFuture<Response>> provideResponse(Response response) {
        return args -> {
            InvocationCallback<Response> cb = args.getArgument(0);
            cb.completed(response);
            return CompletableFuture.completedFuture(response);
        };
    }
}
