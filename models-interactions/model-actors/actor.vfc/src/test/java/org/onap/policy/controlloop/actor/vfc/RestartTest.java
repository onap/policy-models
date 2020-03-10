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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.concurrent.CompletableFuture;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.vfc.VfcRequest;

public class RestartTest extends BasicVfcOperation {
    private Restart restartOper;

    /**
     * setup restart operation.
     */
    @Before
    public void setup() throws Exception {
        super.setUp();
        params.getContext().getEnrichment().put("service-instance.service-instance-id", "test-service-instance-id");
        params.getContext().getEnrichment().put("vserver.vserver-id", "test-vserver-id");
        params.getContext().getEnrichment().put("vserver.vserver-name", "test-vserver-name");
        restartOper = new Restart(params, config);
    }

    @Test
    public void testStartOperationAsync() {
        CompletableFuture<OperationOutcome> futureRes = restartOper.startOperationAsync(1, outcome);
        assertNotNull(futureRes);
        assertEquals(0, restartOper.getGetCount());
    }

    @Test
    public void testMakeRequest() {
        Pair<String, VfcRequest> resultPair = restartOper.makeRequest();
        assertNotNull(resultPair.getLeft());
        assertNotNull(resultPair.getRight());
    }
}
