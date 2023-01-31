/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2023 Nordix Foundation.
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import javax.ws.rs.client.AsyncInvoker;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.stubbing.Answer;
import org.onap.policy.common.endpoints.http.client.HttpClient;
import org.onap.policy.common.endpoints.http.client.HttpClientFactory;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpConfig;

/**
 * Superclass for various HttpOperation tests.
 */
public class BasicHttpOperation extends BasicOperation {
    protected static final String MY_CLIENT = "my-client";
    protected static final String BASE_URI = "http://my-host:6969/base-uri/";
    protected static final String PATH = "my-path/";

    @Captor
    protected ArgumentCaptor<InvocationCallback<Response>> callbackCaptor;
    @Captor
    protected ArgumentCaptor<Entity<String>> requestCaptor;
    @Captor
    protected ArgumentCaptor<Map<String, Object>> headerCaptor;

    @Mock
    protected HttpConfig config;
    @Mock
    protected WebTarget webTarget;
    @Mock
    protected Builder webBuilder;
    @Mock
    protected AsyncInvoker webAsync;
    @Mock
    protected HttpClient client;
    @Mock
    protected HttpClientFactory factory;
    @Mock
    protected Response rawResponse;


    /**
     * Constructs the object using a default actor and operation name.
     */
    public BasicHttpOperation() {
        super();
    }

    /**
     * Constructs the object.
     *
     * @param actor actor name
     * @param operation operation name
     */
    public BasicHttpOperation(String actor, String operation) {
        super(actor, operation);
    }

    /**
     * Initializes mocks and sets up.
     */
    @Override
    public void setUpBasic() {
        super.setUpBasic();

        lenient().when(factory.get(MY_CLIENT)).thenReturn(client);

        lenient().when(rawResponse.getStatus()).thenReturn(200);

        lenient().when(webBuilder.async()).thenReturn(webAsync);
        lenient().when(webBuilder.accept(any(MediaType.class))).thenReturn(webBuilder);
        lenient().when(webBuilder.accept(any(String.class))).thenReturn(webBuilder);

        lenient().when(webTarget.request()).thenReturn(webBuilder);
        lenient().when(webTarget.path(any())).thenReturn(webTarget);
        lenient().when(webTarget.queryParam(any(), any())).thenReturn(webTarget);

        lenient().when(client.getWebTarget()).thenReturn(webTarget);

        lenient().when(client.getBaseUrl()).thenReturn(BASE_URI);

        initConfig();
    }

    /**
     * Initializes a configuration.
     */
    protected void initConfig() {
        lenient().when(config.getClient()).thenReturn(client);
        lenient().when(config.getPath()).thenReturn(PATH);
    }

    /**
     * Provides a response to an asynchronous HttpClient call.
     *
     * @param response response to be provided to the call
     * @return a function that provides the response to the call
     */
    protected Answer<CompletableFuture<Response>> provideResponse(Response response) {
        return provideResponse(response, 0);
    }

    /**
     * Provides a response to an asynchronous HttpClient call.
     *
     * @param response response to be provided to the call
     * @param index index of the callback within the arguments
     * @return a function that provides the response to the call
     */
    protected Answer<CompletableFuture<Response>> provideResponse(Response response, int index) {
        return args -> {
            InvocationCallback<Response> cb = args.getArgument(index);
            cb.completed(response);
            return CompletableFuture.completedFuture(response);
        };
    }
}
