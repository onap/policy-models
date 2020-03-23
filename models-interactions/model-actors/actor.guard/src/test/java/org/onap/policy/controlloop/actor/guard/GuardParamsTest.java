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

package org.onap.policy.controlloop.actor.guard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.function.Function;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.parameters.ValidationResult;
import org.onap.policy.controlloop.actor.guard.GuardParams.GuardParamsBuilder;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpParams.HttpParamsBuilder;

public class GuardParamsTest {
    private static final String CONTAINER = "my-container";
    private static final String CLIENT = "my-client";
    private static final String PATH = "my-path";
    private static final int TIMEOUT = 10;
    private static final String ONAP_NAME = "onap-nap";
    private static final String ONAP_COMP = "onap-component";
    private static final String ONAP_INST = "onap-instance";
    private static final String MY_ACTION = "my-action";

    private GuardParams params;

    @Before
    public void setUp() {
        params = GuardParams.builder().onapName(ONAP_NAME).onapComponent(ONAP_COMP).onapInstance(ONAP_INST)
                        .action(MY_ACTION).clientName(CLIENT).path(PATH).timeoutSec(TIMEOUT).build();
    }

    @Test
    public void testIsDisabled() {
        // disabled by default
        assertFalse(params.isDisabled());
    }

    @Test
    public void testValidate() {
        assertTrue(params.validate(CONTAINER).isValid());

        testValidateField("onapName", "null", bldr -> bldr.onapName(null));
        testValidateField("onapComponent", "null", bldr -> bldr.onapComponent(null));
        testValidateField("onapInstance", "null", bldr -> bldr.onapInstance(null));
        testValidateField("action", "null", bldr -> bldr.action(null));

        // validate one of the superclass fields
        testValidateField("clientName", "null", bldr -> bldr.clientName(null));
    }

    @Test
    public void testBuilder_testToBuilder() {
        assertEquals(CLIENT, params.getClientName());

        assertEquals(ONAP_NAME, params.getOnapName());
        assertEquals(ONAP_COMP, params.getOnapComponent());
        assertEquals(ONAP_INST, params.getOnapInstance());
        assertEquals(MY_ACTION, params.getAction());

        assertEquals(params, params.toBuilder().build());
    }

    private void testValidateField(String fieldName, String expected,
                    @SuppressWarnings("rawtypes") Function<GuardParamsBuilder, HttpParamsBuilder> makeInvalid) {

        // original params should be valid
        ValidationResult result = params.validate(CONTAINER);
        assertTrue(fieldName, result.isValid());

        // make invalid params
        result = makeInvalid.apply(params.toBuilder()).build().validate(CONTAINER);
        assertFalse(fieldName, result.isValid());
        assertThat(result.getResult()).contains(fieldName).contains(expected);
    }
}
