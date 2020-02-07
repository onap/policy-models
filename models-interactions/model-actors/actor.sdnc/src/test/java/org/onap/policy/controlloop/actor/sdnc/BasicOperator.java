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

package org.onap.policy.controlloop.actor.sdnc;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import org.onap.policy.controlloop.VirtualControlLoopEvent;
import org.onap.policy.controlloop.actorserviceprovider.controlloop.ControlLoopEventContext;

/**
 * Superclass for various operator tests.
 */
public abstract class BasicOperator {
    protected static final UUID REQ_ID = UUID.randomUUID();
    protected static final String ACTOR = "my-actor";

    protected Map<String, String> enrichment;
    protected VirtualControlLoopEvent event;
    protected ControlLoopEventContext context;

    /**
     * Verifies that an exception is thrown if a field is missing from the enrichment
     * data.
     *
     * @param oper operator to construct the request
     * @param fieldName name of the field to be removed from the enrichment data
     * @param expectedText text expected in the exception message
     */
    protected void verifyMissing(SdncOperator oper, String fieldName, String expectedText) {
        makeContext();
        enrichment.remove(fieldName);

        assertThatIllegalArgumentException().isThrownBy(() -> oper.constructRequest(context))
                        .withMessageContaining("missing").withMessageContaining(expectedText);
    }

    protected void makeContext() {
        // need a mutable map, so make a copy
        enrichment = new TreeMap<>(makeEnrichment());

        event = new VirtualControlLoopEvent();
        event.setRequestId(REQ_ID);
        event.setAai(enrichment);

        context = new ControlLoopEventContext(event);
    }

    protected abstract Map<String, String> makeEnrichment();
}
