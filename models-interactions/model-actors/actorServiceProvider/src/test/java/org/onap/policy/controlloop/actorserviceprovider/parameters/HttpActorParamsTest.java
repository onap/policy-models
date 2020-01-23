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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Function;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.parameters.ValidationResult;
import org.onap.policy.controlloop.actorserviceprovider.ParameterValidationRuntimeException;

public class HttpActorParamsTest {

    private static final String CONTAINER = "my-container";
    private static final String CLIENT = "my-client";
    private static final long TIMEOUT = 10;

    private static final String PATH1 = "path #1";
    private static final String PATH2 = "path #2";
    private static final String URI1 = "uri #1";
    private static final String URI2 = "uri #2";

    private Map<String, String> paths;
    private HttpActorParams params;

    /**
     * Initializes {@link #paths} with two items and {@link params} with a fully populated
     * object.
     */
    @Before
    public void setUp() {
        paths = new TreeMap<>();
        paths.put(PATH1, URI1);
        paths.put(PATH2, URI2);

        params = makeHttpActorParams();
    }

    private HttpActorParams makeHttpActorParams() {
        HttpActorParams params2 = new HttpActorParams();
        params2.setClientName(CLIENT);
        params2.setTimeoutSec(TIMEOUT);
        params2.setPath(paths);

        return params2;
    }

    @Test
    public void testMakeOperationParameters() {
        Function<String, Map<String, Object>> maker = params.makeOperationParameters(CONTAINER);
        assertNull(maker.apply("unknown-operation"));

        Map<String, Object> subparam = maker.apply(PATH1);
        assertNotNull(subparam);
        assertEquals("{clientName=my-client, path=uri #1, timeoutSec=10}", new TreeMap<>(subparam).toString());

        subparam = maker.apply(PATH2);
        assertNotNull(subparam);
        assertEquals("{clientName=my-client, path=uri #2, timeoutSec=10}", new TreeMap<>(subparam).toString());
    }

    @Test
    public void testDoValidation() {
        assertThatCode(() -> params.doValidation(CONTAINER)).doesNotThrowAnyException();

        // invalid param
        params.setClientName(null);
        assertThatThrownBy(() -> params.doValidation(CONTAINER))
                        .isInstanceOf(ParameterValidationRuntimeException.class);
    }

    @Test
    public void testValidate() {
        testValidate("clientName", "null", params2 -> params2.setClientName(null));
        testValidate("path", "null", params2 -> params2.setPath(null));
        testValidate("timeoutSec", "minimum", params2 -> params2.setTimeoutSec(-1));

        // check edge cases
        params.setTimeoutSec(0);
        assertTrue(params.validate(CONTAINER).isValid());

        params.setTimeoutSec(1);
        assertTrue(params.validate(CONTAINER).isValid());

        // one path value is null
        testValidate(PATH2, "null", params2 -> paths.put(PATH2, null));
    }

    private void testValidate(String fieldName, String expected, Consumer<HttpActorParams> makeInvalid) {

        // original params should be valid
        ValidationResult result = params.validate(CONTAINER);
        assertTrue(fieldName, result.isValid());

        // make invalid params
        HttpActorParams params2 = makeHttpActorParams();
        makeInvalid.accept(params2);
        result = params2.validate(CONTAINER);
        assertFalse(fieldName, result.isValid());
        assertThat(result.getResult()).contains(CONTAINER).contains(fieldName).contains(expected);
    }
}
