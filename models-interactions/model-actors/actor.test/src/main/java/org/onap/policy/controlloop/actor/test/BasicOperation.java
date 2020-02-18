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

import static org.junit.Assert.assertEquals;

import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import javax.ws.rs.core.Response;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.policy.common.utils.coder.Coder;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.resources.ResourceUtils;
import org.onap.policy.common.utils.time.PseudoExecutor;
import org.onap.policy.controlloop.VirtualControlLoopEvent;
import org.onap.policy.controlloop.actorserviceprovider.ActorService;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.controlloop.ControlLoopEventContext;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;

/**
 * Superclass for various Operation tests.
 */
public class BasicOperation {
    protected static final Coder coder = new StandardCoder();
    protected static final UUID REQ_ID = UUID.randomUUID();
    protected static final String DEFAULT_ACTOR = "default-actor";
    protected static final String DEFAULT_OPERATION = "default-operation";
    protected static final String TARGET_ENTITY = "my-target";

    protected final String actorName;
    protected final String operationName;

    @Mock
    protected ActorService service;

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
    public BasicOperation() {
        this.actorName = DEFAULT_ACTOR;
        this.operationName = DEFAULT_OPERATION;
    }

    /**
     * Constructs the object.
     *
     * @param actor actor name
     * @param operation operation name
     */
    public BasicOperation(String actor, String operation) {
        this.actorName = actor;
        this.operationName = operation;
    }

    /**
     * Initializes mocks and sets up.
     */
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        future = new CompletableFuture<>();

        executor = new PseudoExecutor();

        makeContext();

        outcome = params.makeOutcome();
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
                        .actor(actorName).operation(operationName).targetEntity(TARGET_ENTITY).payload(makePayload())
                        .build();
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
     * Makes payload data.
     *
     * @return payload data
     */
    protected Map<String, String> makePayload() {
        return null;
    }

    /**
     * Pretty-prints a request and verifies that the result matches the expected JSON.
     *
     * @param <R> request type
     * @param expectedJsonFile name of the file containing the expected JSON
     * @param request request to verify
     * @param ignore names of fields to be ignored, because they change with each request
     * @throws CoderException if the request cannot be pretty-printed
     */
    protected <R> void verifyRequest(String expectedJsonFile, R request, String... ignore) throws CoderException {
        String json = coder.encode(request, true);
        String expected = ResourceUtils.getResourceAsString(expectedJsonFile);

        // strip various items, because they change for each request
        for (String stripper : ignore) {
            stripper += "[^,]*";
            json = json.replaceAll(stripper, "");
            expected = expected.replaceAll(stripper, "");
        }

        json = json.trim();
        expected = expected.trim();

        assertEquals(expected, json);
    }
}
