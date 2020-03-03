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

package org.onap.policy.controlloop.actor.vfc;

import static org.mockito.Mockito.when;

import org.mockito.Mock;
import org.onap.policy.controlloop.actor.test.BasicHttpOperation;
import org.onap.policy.vfc.VfcRequest;
import org.onap.policy.vfc.VfcResponse;

public abstract class BasicVfcOperation extends BasicHttpOperation<VfcRequest> {
    public static final String PATH_GET = "my-path-get/";
    public static final int MAX_GETS = 3;
    public static final int WAIT_SEC_GETS = 20;

    @Mock
    protected VfcConfig config;

    protected VfcResponse response;

    /**
     * Constructs the object using a default actor and operation name.
     */
    public BasicVfcOperation() {
        super();
    }

    /**
     * Constructs the object.
     *
     * @param actor actor name
     * @param operation operation name
     */
    public BasicVfcOperation(String actor, String operation) {
        super(actor, operation);
    }

    /**
     * Initializes mocks and sets up.
     */
    public void setUp() throws Exception {
        super.setUpBasic();

        response = new VfcResponse();

        // PLD

        when(rawResponse.getStatus()).thenReturn(200);
        when(rawResponse.readEntity(String.class)).thenReturn(coder.encode(response));

        initConfig();
    }

    @Override
    protected void initConfig() {
        super.initConfig();
        when(config.getClient()).thenReturn(client);
        when(config.getMaxGets()).thenReturn(MAX_GETS);
        when(config.getPathGet()).thenReturn(PATH_GET);
        when(config.getWaitSecGet()).thenReturn(WAIT_SEC_GETS);
    }

}
