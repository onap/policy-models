/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020-2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2024 Nordix Foundation
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

package org.onap.policy.controlloop.actor.xacml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onap.policy.common.parameters.ValidationResult;
import org.onap.policy.controlloop.actor.xacml.DecisionParams.DecisionParamsBuilder;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpParams.HttpParamsBuilder;

class DecisionParamsTest {
    private static final String CONTAINER = "my-container";
    private static final String CLIENT = "my-client";
    private static final String PATH = "my-path";
    private static final int TIMEOUT = 10;
    private static final String ONAP_NAME = "onap-nap";
    private static final String ONAP_COMP = "onap-component";
    private static final String ONAP_INST = "onap-instance";
    private static final String MY_ACTION = "my-action";

    private DecisionParams params;

    @BeforeEach
     void setUp() {
        params = DecisionParams.builder().onapName(ONAP_NAME).onapComponent(ONAP_COMP).onapInstance(ONAP_INST)
                        .action(MY_ACTION).clientName(CLIENT).path(PATH).timeoutSec(TIMEOUT).build();
    }

    @Test
     void testIsDisabled() {
        // disabled by default
        assertFalse(params.isDisabled());
    }

    @Test
     void testValidate() {
        assertTrue(params.validate(CONTAINER).isValid());

        testValidateField("onapName", "null", bldr -> bldr.onapName(null));
        testValidateField("onapComponent", "null", bldr -> bldr.onapComponent(null));
        testValidateField("onapInstance", "null", bldr -> bldr.onapInstance(null));
        testValidateField("action", "null", bldr -> bldr.action(null));

        // validate one of the superclass fields
        testValidateField("clientName", "null", bldr -> bldr.clientName(null));
    }

    @Test
     void testBuilder_testToBuilder() {
        assertEquals(CLIENT, params.getClientName());

        assertEquals(ONAP_NAME, params.getOnapName());
        assertEquals(ONAP_COMP, params.getOnapComponent());
        assertEquals(ONAP_INST, params.getOnapInstance());
        assertEquals(MY_ACTION, params.getAction());

        assertEquals(params, params.toBuilder().build());
    }

    private void testValidateField(String fieldName, String expected,
                    @SuppressWarnings("rawtypes") Function<DecisionParamsBuilder, HttpParamsBuilder> makeInvalid) {

        // original params should be valid
        ValidationResult result = params.validate(CONTAINER);
        assertTrue(result.isValid(), fieldName);

        // make invalid params
        result = makeInvalid.apply(params.toBuilder()).build().validate(CONTAINER);
        assertFalse(result.isValid(), fieldName);
        assertThat(result.getResult()).contains(fieldName).contains(expected);
    }
}
