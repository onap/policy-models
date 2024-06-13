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

package org.onap.policy.controlloop.actor.so;

import static org.mockito.Mockito.lenient;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.mockito.Mock;
import org.onap.policy.common.endpoints.event.comm.bus.internal.BusTopicParams;
import org.onap.policy.common.endpoints.http.client.HttpClientFactoryInstance;
import org.onap.policy.common.endpoints.http.server.HttpServletServerFactoryInstance;
import org.onap.policy.controlloop.actor.test.BasicHttpOperation;
import org.onap.policy.controlloop.actorserviceprovider.TargetType;
import org.onap.policy.controlloop.actorserviceprovider.Util;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpPollingConfig;
import org.onap.policy.simulators.SoSimulatorJaxRs;
import org.onap.policy.so.SoRequest;
import org.onap.policy.so.SoRequestParameters;
import org.onap.policy.so.SoRequestReferences;
import org.onap.policy.so.SoRequestStatus;
import org.onap.policy.so.SoResponse;

/**
 * Superclass for various operator tests.
 */
abstract class BasicSoOperation extends BasicHttpOperation {
    protected static final String[] IGNORE_FIELDS = {"RequestID", "subRequestID", "seconds", "nanos"};

    static final String MODEL_CUSTOM_ID = "my-model-customization-id";
    static final String MODEL_INVAR_ID = "my-model-invariant-id";
    static final String MODEL_NAME = "my-model-name";
    static final String MODEL_VERSION = "my-model-version";
    static final String MODEL_VERS_ID = "my-model-version-id";
    static final String SUBSCRIPTION_SVC_TYPE = "my-subscription-service-type";
    static final String MY_PATH = "my-path";
    static final String POLL_PATH = "my-poll-path/";
    static final int MAX_POLLS = 3;
    static final int POLL_WAIT_SEC = 20;
    static final Integer VF_COUNT = 10;

    @Mock
    protected HttpPollingConfig config;

    protected TargetType targetType;
    protected Map<String, String> targetEntities = new HashMap<>();
    protected SoResponse response;

    /**
     * Constructs the object using a default actor and operation name.
     */
    BasicSoOperation() {
        super();
    }

    /**
     * Constructs the object.
     *
     * @param actor actor name
     * @param operation operation name
     */
    BasicSoOperation(String actor, String operation) {
        super(actor, operation);
    }

    /**
     * Starts the simulator.
     */
    protected static void initBeforeClass() throws Exception {
        org.onap.policy.simulators.Util.buildSoSim();

        BusTopicParams clientParams = BusTopicParams.builder().clientName(MY_CLIENT).basePath("").hostname("localhost")
            .managed(true).port(org.onap.policy.simulators.Util.SOSIM_SERVER_PORT)
            .build();
        HttpClientFactoryInstance.getClientFactory().build(clientParams);

        SoSimulatorJaxRs.setRequirePolling(true);
    }

    protected static void destroyAfterClass() {
        SoSimulatorJaxRs.setRequirePolling(false);
        HttpClientFactoryInstance.getClientFactory().destroy();
        HttpServletServerFactoryInstance.getServerFactory().destroy();
    }

    /**
     * Initializes mocks and sets up.
     */
    void setUp() throws Exception {
        super.setUpBasic();

        response = new SoResponse();

        SoRequest request = new SoRequest();
        response.setRequest(request);

        SoRequestStatus status = new SoRequestStatus();
        request.setRequestStatus(status);
        status.setRequestState(SoOperation.COMPLETE);

        SoRequestReferences ref = new SoRequestReferences();
        response.setRequestReferences(ref);
        ref.setRequestId(REQ_ID.toString());

        lenient().when(rawResponse.getStatus()).thenReturn(200);
        lenient().when(rawResponse.readEntity(String.class)).thenReturn(coder.encode(response));

        initConfig();
    }

    @Override
    protected void initConfig() {
        super.initConfig();
        lenient().when(config.getClient()).thenReturn(client);
        lenient().when(config.getPath()).thenReturn(MY_PATH);
        lenient().when(config.getMaxPolls()).thenReturn(MAX_POLLS);
        lenient().when(config.getPollPath()).thenReturn(POLL_PATH);
        lenient().when(config.getPollWaitSec()).thenReturn(POLL_WAIT_SEC);
    }

    @Override
    protected void makeContext() {
        super.makeContext();

        targetType = TargetType.VNF;

        targetEntities = new HashMap<>();
        targetEntities.put(ControlLoopOperationParams.PARAMS_ENTITY_MODEL_CUSTOMIZATION_ID, MODEL_CUSTOM_ID);
        targetEntities.put(ControlLoopOperationParams.PARAMS_ENTITY_MODEL_INVARIANT_ID, MODEL_INVAR_ID);
        targetEntities.put(ControlLoopOperationParams.PARAMS_ENTITY_MODEL_NAME, MODEL_NAME);
        targetEntities.put(ControlLoopOperationParams.PARAMS_ENTITY_MODEL_VERSION, MODEL_VERSION);
        targetEntities.put(ControlLoopOperationParams.PARAMS_ENTITY_MODEL_VERSION_ID, MODEL_VERS_ID);

        params = params.toBuilder().targetType(targetType).targetEntityIds(targetEntities).build();
    }

    @Override
    protected Map<String, Object> makePayload() {
        Map<String, Object> payload = new HashMap<>();

        // request parameters
        SoRequestParameters reqParams = new SoRequestParameters();
        reqParams.setSubscriptionServiceType(SUBSCRIPTION_SVC_TYPE);
        payload.put(SoOperation.REQ_PARAM_NM, Util.translate("", reqParams, String.class));

        // config parameters
        List<Map<String, String>> config = new LinkedList<>();
        config.add(Collections.emptyMap());
        payload.put(SoOperation.CONFIG_PARAM_NM, Util.translate("", config, String.class));

        return payload;
    }
}
