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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onap.policy.common.parameters.ValidationResult;
import org.onap.policy.controlloop.actorserviceprovider.parameters.BidirectionalTopicParams.BidirectionalTopicParamsBuilder;

class BidirectionalTopicParamsTest {

    private static final String CONTAINER = "my-container";
    private static final String SINK = "my-sink";
    private static final String SOURCE = "my-source";
    private static final int TIMEOUT = 10;

    private BidirectionalTopicParams params;

    @BeforeEach
    void setUp() {
        params = BidirectionalTopicParams.builder().sinkTopic(SINK).sourceTopic(SOURCE).timeoutSec(TIMEOUT).build();
    }

    @Test
     void testValidate() {
        assertTrue(params.validate(CONTAINER).isValid());

        testValidateField("sink", "null", bldr -> bldr.sinkTopic(null));
        testValidateField("source", "null", bldr -> bldr.sourceTopic(null));
        testValidateField("timeoutSec", "minimum", bldr -> bldr.timeoutSec(-1));

        // check edge cases
        assertFalse(params.toBuilder().timeoutSec(0).build().validate(CONTAINER).isValid());
        assertTrue(params.toBuilder().timeoutSec(1).build().validate(CONTAINER).isValid());
    }

    @Test
     void testBuilder_testToBuilder() {
        assertEquals(SINK, params.getSinkTopic());
        assertEquals(SOURCE, params.getSourceTopic());
        assertEquals(TIMEOUT, params.getTimeoutSec());

        assertEquals(params, params.toBuilder().build());
    }

    // @formatter:off
    private void testValidateField(String fieldName, String expected,
            @SuppressWarnings("rawtypes") Function<BidirectionalTopicParamsBuilder, BidirectionalTopicParamsBuilder>
                makeInvalid) {
        // @formatter:on

        // original params should be valid
        ValidationResult result = params.validate(CONTAINER);
        assertTrue(result.isValid(), fieldName);

        // make invalid params
        result = makeInvalid.apply(params.toBuilder()).build().validate(CONTAINER);
        assertFalse(result.isValid(), fieldName);
        assertThat(result.getResult()).contains(fieldName).contains(expected);
    }
}
