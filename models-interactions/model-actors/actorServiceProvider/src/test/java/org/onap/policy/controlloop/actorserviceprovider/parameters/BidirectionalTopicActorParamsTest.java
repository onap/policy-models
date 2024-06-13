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

package org.onap.policy.controlloop.actorserviceprovider.parameters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onap.policy.common.parameters.ValidationResult;
import org.onap.policy.controlloop.actorserviceprovider.Util;

public class BidirectionalTopicActorParamsTest {
    private static final String CONTAINER = "my-container";

    private static final String DFLT_SOURCE = "default-source";
    private static final String DFLT_SINK = "default-target";
    private static final int DFLT_TIMEOUT = 10;

    private static final String OPER1_NAME = "oper A";
    private static final String OPER1_SOURCE = "source A";
    private static final String OPER1_SINK = "target A";
    private static final int OPER1_TIMEOUT = 20;

    // oper2 uses some default values
    private static final String OPER2_NAME = "oper B";
    private static final String OPER2_SOURCE = "source B";

    // oper3 uses default values for everything
    private static final String OPER3_NAME = "oper C";

    private Map<String, Map<String, Object>> operMap;
    private BidirectionalTopicActorParams params;


    /**
     * Sets up.
     */
    @BeforeEach
    public void setUp() {
        BidirectionalTopicParams oper1 = BidirectionalTopicParams.builder().sourceTopic(OPER1_SOURCE)
                        .sinkTopic(OPER1_SINK).timeoutSec(OPER1_TIMEOUT).build();

        Map<String, Object> oper1Map = Util.translateToMap(OPER1_NAME, oper1);
        Map<String, Object> oper2Map = Map.of("source", OPER2_SOURCE);
        Map<String, Object> oper3Map = Collections.emptyMap();
        operMap = Map.of(OPER1_NAME, oper1Map, OPER2_NAME, oper2Map, OPER3_NAME, oper3Map);

        params = makeBidirectionalTopicActorParams();
    }

    @Test
    public void testValidate() {
        assertTrue(params.validate(CONTAINER).isValid());

        // only a few fields are required
        BidirectionalTopicActorParams sparse =
                        Util.translate(CONTAINER, Map.of(ActorParams.OPERATIONS_FIELD, operMap, "timeoutSec", 1),
                                        BidirectionalTopicActorParams.class);
        assertTrue(sparse.validate(CONTAINER).isValid());

        testValidateField(ActorParams.OPERATIONS_FIELD, "null", params2 -> params2.setOperations(null));
        testValidateField("timeoutSec", "minimum", params2 -> params2.setTimeoutSec(-1));

        // check edge cases
        params.setTimeoutSec(0);
        assertFalse(params.validate(CONTAINER).isValid());

        params.setTimeoutSec(1);
        assertTrue(params.validate(CONTAINER).isValid());
    }

    private void testValidateField(String fieldName, String expected,
                    Consumer<BidirectionalTopicActorParams> makeInvalid) {

        // original params should be valid
        ValidationResult result = params.validate(CONTAINER);
        assertTrue(result.isValid(), fieldName);

        // make invalid params
        BidirectionalTopicActorParams params2 = makeBidirectionalTopicActorParams();
        makeInvalid.accept(params2);
        result = params2.validate(CONTAINER);
        assertFalse(result.isValid(), fieldName);
        assertThat(result.getResult()).contains(CONTAINER).contains(fieldName).contains(expected);
    }

    private BidirectionalTopicActorParams makeBidirectionalTopicActorParams() {
        BidirectionalTopicActorParams params2 = new BidirectionalTopicActorParams();
        params2.setSinkTopic(DFLT_SINK);
        params2.setSourceTopic(DFLT_SOURCE);
        params2.setTimeoutSec(DFLT_TIMEOUT);
        params2.setOperations(operMap);

        return params2;
    }
}
