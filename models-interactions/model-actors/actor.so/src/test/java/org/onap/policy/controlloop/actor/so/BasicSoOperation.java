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

package org.onap.policy.controlloop.actor.so;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.mockito.Mock;
import org.onap.policy.aai.AaiCqResponse;
import org.onap.policy.common.endpoints.event.comm.bus.internal.BusTopicParams;
import org.onap.policy.common.endpoints.http.client.HttpClientFactoryInstance;
import org.onap.policy.common.endpoints.http.server.HttpServletServerFactoryInstance;
import org.onap.policy.common.gson.GsonMessageBodyHandler;
import org.onap.policy.controlloop.actor.test.BasicHttpOperation;
import org.onap.policy.controlloop.actorserviceprovider.Util;
import org.onap.policy.controlloop.policy.Target;
import org.onap.policy.simulators.SoSimulatorJaxRs;
import org.onap.policy.so.SoRequest;
import org.onap.policy.so.SoRequestParameters;
import org.onap.policy.so.SoRequestReferences;
import org.onap.policy.so.SoRequestStatus;
import org.onap.policy.so.SoResponse;

/**
 * Superclass for various operator tests.
 */
public abstract class BasicSoOperation extends BasicHttpOperation<SoRequest> {
    protected static final String[] IGNORE_FIELDS = {"RequestID", "subRequestID", "seconds", "nanos"};

    public static final String MODEL_CUSTOM_ID = "my-model-customization-id";
    public static final String MODEL_INVAR_ID = "my-model-invariant-id";
    public static final String MODEL_NAME = "my-model-name";
    public static final String MODEL_VERSION = "my-model-version";
    public static final String MODEL_VERS_ID = "my-model-version-id";
    public static final String SUBSCRIPTION_SVC_TYPE = "my-subscription-service-type";
    public static final String MY_PATH = "my-path";
    public static final String PATH_GET = "my-path-get/";
    public static final int MAX_GETS = 3;
    public static final int WAIT_SEC_GETS = 20;
    public static final Integer VF_COUNT = 10;

    @Mock
    protected SoConfig config;

    protected Target target;
    protected SoResponse response;

    /**
     * Constructs the object using a default actor and operation name.
     */
    public BasicSoOperation() {
        super();
    }

    /**
     * Constructs the object.
     *
     * @param actor actor name
     * @param operation operation name
     */
    public BasicSoOperation(String actor, String operation) {
        super(actor, operation);
    }

    /**
     * Starts the simulator.
     */
    protected static void initBeforeClass() throws Exception {
        org.onap.policy.simulators.Util.buildSoSim();

        BusTopicParams clientParams = BusTopicParams.builder().clientName(MY_CLIENT).basePath("").hostname("localhost")
                        .managed(true).port(org.onap.policy.simulators.Util.SOSIM_SERVER_PORT)
                        .serializationProvider(GsonMessageBodyHandler.class.getName()).build();
        HttpClientFactoryInstance.getClientFactory().build(clientParams);

        SoSimulatorJaxRs.setYieldIncomplete(true);
    }

    protected static void destroyAfterClass() {
        SoSimulatorJaxRs.setYieldIncomplete(false);
        HttpClientFactoryInstance.getClientFactory().destroy();
        HttpServletServerFactoryInstance.getServerFactory().destroy();
    }

    /**
     * Initializes mocks and sets up.
     */
    public void setUp() throws Exception {
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

        when(rawResponse.getStatus()).thenReturn(200);
        when(rawResponse.readEntity(String.class)).thenReturn(coder.encode(response));

        initConfig();
    }

    @Override
    protected void initConfig() {
        super.initConfig();
        when(config.getClient()).thenReturn(client);
        when(config.getPath()).thenReturn(MY_PATH);
        when(config.getMaxGets()).thenReturn(MAX_GETS);
        when(config.getPathGet()).thenReturn(PATH_GET);
        when(config.getWaitSecGet()).thenReturn(WAIT_SEC_GETS);
    }

    @Override
    protected void makeContext() {
        super.makeContext();

        target = new Target();
        target.setModelCustomizationId(MODEL_CUSTOM_ID);
        target.setModelInvariantId(MODEL_INVAR_ID);
        target.setModelName(MODEL_NAME);
        target.setModelVersion(MODEL_VERSION);
        target.setModelVersionId(MODEL_VERS_ID);

        params = params.toBuilder().target(target).build();
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

    protected AaiCqResponse makeCqResponse() {
        when(cqResponse.getVfModuleCount(any(), any(), any())).thenReturn(VF_COUNT);
        return cqResponse;
    }
}
