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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.function.Function;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.parameters.ValidationResult;
import org.onap.policy.controlloop.actorserviceprovider.parameters.TopicParams.TopicParamsBuilder;

public class TopicParamsTest {

    private static final String CONTAINER = "my-container";
    private static final String TARGET = "my-target";
    private static final String SOURCE = "my-source";
    private static final long TIMEOUT = 10;

    private TopicParams params;

    @Before
    public void setUp() {
        params = TopicParams.builder().target(TARGET).source(SOURCE).timeoutSec(TIMEOUT).build();
    }

    @Test
    public void testValidate() {
        testValidate("target", "null", bldr -> bldr.target(null));
        testValidate("source", "null", bldr -> bldr.source(null));
        testValidate("timeoutSec", "minimum", bldr -> bldr.timeoutSec(-1));

        // check edge cases
        assertTrue(params.toBuilder().timeoutSec(0).build().validate(CONTAINER).isValid());
        assertTrue(params.toBuilder().timeoutSec(1).build().validate(CONTAINER).isValid());
    }

    private void testValidate(String fieldName, String expected,
                    Function<TopicParamsBuilder, TopicParamsBuilder> makeInvalid) {

        // original params should be valid
        ValidationResult result = params.validate(CONTAINER);
        assertTrue(fieldName, result.isValid());

        // make invalid params
        result = makeInvalid.apply(params.toBuilder()).build().validate(CONTAINER);
        assertFalse(fieldName, result.isValid());

        String msg = result.getResult();
        assertTrue(fieldName, msg.contains(fieldName));
        assertTrue(fieldName, msg.contains(expected));
    }

    @Test
    public void testBuilder_testToBuilder() {
        assertEquals(TARGET, params.getTarget());
        assertEquals(SOURCE, params.getSource());
        assertEquals(TIMEOUT, params.getTimeoutSec());

        assertEquals(params, params.toBuilder().build());
    }
}
