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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.function.Function;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.parameters.ValidationResult;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpParams.HttpParamsBuilder;

public class HttpParamsTest {

    private static final String CONTAINER = "my-container";
    private static final String CLIENT = "my-client";
    private static final String PATH = "my-path";
    private static final long TIMEOUT = 10;

    private HttpParams params;

    @Before
    public void setUp() {
        params = HttpParams.builder().clientName(CLIENT).path(PATH).timeoutSec(TIMEOUT).build();
    }

    @Test
    public void testValidate() {
        testValidateField("clientName", "null", bldr -> bldr.clientName(null));
        testValidateField("path", "null", bldr -> bldr.path(null));
        testValidateField("timeoutSec", "minimum", bldr -> bldr.timeoutSec(-1));

        // check edge cases
        assertTrue(params.toBuilder().timeoutSec(0).build().validate(CONTAINER).isValid());
        assertTrue(params.toBuilder().timeoutSec(1).build().validate(CONTAINER).isValid());
    }

    @Test
    public void testBuilder_testToBuilder() {
        assertEquals(CLIENT, params.getClientName());
        assertEquals(PATH, params.getPath());
        assertEquals(TIMEOUT, params.getTimeoutSec());

        assertEquals(params, params.toBuilder().build());
    }

    private void testValidateField(String fieldName, String expected,
                    Function<HttpParamsBuilder, HttpParamsBuilder> makeInvalid) {

        // original params should be valid
        ValidationResult result = params.validate(CONTAINER);
        assertTrue(fieldName, result.isValid());

        // make invalid params
        result = makeInvalid.apply(params.toBuilder()).build().validate(CONTAINER);
        assertFalse(fieldName, result.isValid());
        assertThat(result.getResult()).contains(fieldName).contains(expected);
    }
}
