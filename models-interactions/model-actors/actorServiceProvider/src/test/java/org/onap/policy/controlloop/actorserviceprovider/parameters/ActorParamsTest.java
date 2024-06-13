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
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.Setter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onap.policy.common.parameters.ValidationResult;
import org.onap.policy.controlloop.actorserviceprovider.Util;

class ActorParamsTest {

    private static final String CONTAINER = "my-container";

    private static final String PATH1 = "path #1";
    private static final String PATH2 = "path #2";
    private static final String URI1 = "uri #1";
    private static final String URI2 = "uri #2";
    private static final String TEXT1 = "hello";
    private static final String TEXT2 = "world";
    private static final String TEXT2B = "bye";

    private Map<String, Map<String, Object>> operations;
    private ActorParams params;

    /**
     * Initializes {@link #operations} with two items and {@link params} with a fully
     * populated object.
     */
    @BeforeEach
     void setUp() {
        operations = new TreeMap<>();
        operations.put(PATH1, Map.of("path", URI1));
        operations.put(PATH2, Map.of("path", URI2, "text2", TEXT2B));

        params = makeActorParams();
    }

    @Test
     void testMakeOperationParameters() {
        Function<String, Map<String, Object>> maker = params.makeOperationParameters(CONTAINER);
        assertNull(maker.apply("unknown-operation"));

        Map<String, Object> subparam = maker.apply(PATH1);
        assertNotNull(subparam);
        assertEquals("{path=uri #1, text1=hello, text2=world}", new TreeMap<>(subparam).toString());

        subparam = maker.apply(PATH2);
        assertNotNull(subparam);
        assertEquals("{path=uri #2, text1=hello, text2=bye}", new TreeMap<>(subparam).toString());
    }

    @Test
     void testDoValidation() {
        assertThatCode(() -> params.doValidation(CONTAINER)).doesNotThrowAnyException();

        // invalid param
        params.setOperations(null);
        assertThatThrownBy(() -> params.doValidation(CONTAINER))
                        .isInstanceOf(ParameterValidationRuntimeException.class);
    }

    @Test
     void testValidate() {
        assertTrue(params.validate(CONTAINER).isValid());

        // only a few fields are required
        ActorParams sparse = Util.translate(CONTAINER,
                        Map.of(ActorParams.OPERATIONS_FIELD, operations, "timeoutSec", 1),
                        ActorParams.class);
        assertTrue(sparse.validate(CONTAINER).isValid());

        testValidateField(ActorParams.OPERATIONS_FIELD, "null", params2 -> params2.setOperations(null));
    }

    private void testValidateField(String fieldName, String expected, Consumer<ActorParams> makeInvalid) {

        // original params should be valid
        ValidationResult result = params.validate(CONTAINER);
        assertTrue(result.isValid(), fieldName);

        // make invalid params
        ActorParams params2 = makeActorParams();
        makeInvalid.accept(params2);
        result = params2.validate(CONTAINER);
        assertFalse(result.isValid(), fieldName);
        assertThat(result.getResult()).contains(CONTAINER).contains(fieldName).contains(expected);
    }

    private ActorParams makeActorParams() {
        MyParams params2 = new MyParams();
        params2.setOperations(operations);
        params2.setText1(TEXT1);
        params2.setText2(TEXT2);

        return params2;
    }

    @Setter
    static class MyParams extends ActorParams {
        @SuppressWarnings("unused")
        private String text1;

        @SuppressWarnings("unused")
        private String text2;
    }
}
