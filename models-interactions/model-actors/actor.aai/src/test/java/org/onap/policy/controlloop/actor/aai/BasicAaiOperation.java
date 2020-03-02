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

package org.onap.policy.controlloop.actor.aai;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Map;
import javax.ws.rs.client.AsyncInvoker;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import org.mockito.Mock;
import org.onap.policy.controlloop.actor.test.BasicHttpOperation;

/**
 * Superclass for various operator tests.
 */
public abstract class BasicAaiOperation<Q> extends BasicHttpOperation<Q> {

    @Mock
    protected WebTarget webTarget;
    @Mock
    protected Builder webBuilder;
    @Mock
    protected AsyncInvoker webAsync;

    /**
     * Constructs the object using a default actor and operation name.
     */
    public BasicAaiOperation() {
        super();
    }

    /**
     * Constructs the object.
     *
     * @param actor actor name
     * @param operation operation name
     */
    public BasicAaiOperation(String actor, String operation) {
        super(actor, operation);
    }

    @Override
    public void setUpBasic() {
        super.setUpBasic();

        when(webBuilder.async()).thenReturn(webAsync);

        when(webTarget.request()).thenReturn(webBuilder);
        when(webTarget.path(any())).thenReturn(webTarget);
        when(webTarget.queryParam(any(), any())).thenReturn(webTarget);

        when(client.getWebTarget()).thenReturn(webTarget);
    }

    protected void verifyHeaders(Map<String, Object> headers) {
        assertEquals("POLICY", headers.get("X-FromAppId").toString());
        assertEquals(params.getRequestId().toString(), headers.get("X-TransactionId"));
    }
}
