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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.parameters.ValidationResult;
import org.onap.policy.controlloop.actorserviceprovider.Util;

public class TopicPairActorParamsTest {
    private static final String MY_NAME = "my-name";

    private static final String DFLT_SOURCE = "default-source";
    private static final String DFLT_TARGET = "default-target";
    private static final int DFLT_TIMEOUT = 10;

    private static final String OPER1_NAME = "oper A";
    private static final String OPER1_SOURCE = "source A";
    private static final String OPER1_TARGET = "target A";
    private static final int OPER1_TIMEOUT = 20;

    // oper2 uses some default values
    private static final String OPER2_NAME = "oper B";
    private static final String OPER2_SOURCE = "source B";

    // oper3 uses default values for everything
    private static final String OPER3_NAME = "oper C";

    private TopicPairParams defaults;
    private Map<String, Map<String, Object>> operMap;
    private TopicPairActorParams params;


    /**
     * Sets up.
     */
    @Before
    public void setUp() {
        defaults = TopicPairParams.builder().source(DFLT_SOURCE).target(DFLT_TARGET).timeoutSec(DFLT_TIMEOUT).build();

        TopicPairParams oper1 = TopicPairParams.builder().source(OPER1_SOURCE).target(OPER1_TARGET)
                        .timeoutSec(OPER1_TIMEOUT).build();

        Map<String, Object> oper1Map = Util.translateToMap(OPER1_NAME, oper1);
        Map<String, Object> oper2Map = Map.of("source", OPER2_SOURCE);
        Map<String, Object> oper3Map = Collections.emptyMap();
        operMap = Map.of(OPER1_NAME, oper1Map, OPER2_NAME, oper2Map, OPER3_NAME, oper3Map);

        params = TopicPairActorParams.builder().defaults(defaults).operation(operMap).build();

    }

    @Test
    public void testTopicPairActorParams() {
        assertSame(defaults, params.getDefaults());
        assertSame(operMap, params.getOperation());
    }

    @Test
    public void testDoValidation() {
        assertSame(params, params.doValidation(MY_NAME));

        // test with invalid parameters
        defaults.setTimeoutSec(-1);
        assertThatThrownBy(() -> params.doValidation(MY_NAME)).isInstanceOf(ParameterValidationRuntimeException.class);
    }

    @Test
    public void testValidate() {
        ValidationResult result;

        // null defaults
        params.setDefaults(null);
        result = params.validate(MY_NAME);
        assertFalse(result.isValid());
        assertThat(result.getResult()).contains("defaults").contains("null");
        params.setDefaults(defaults);

        // invalid value in defaults
        defaults.setTimeoutSec(-1);
        result = params.validate(MY_NAME);
        assertFalse(result.isValid());
        assertThat(result.getResult()).contains("defaults").contains("timeoutSec");
        defaults.setTimeoutSec(DFLT_TIMEOUT);

        // null map
        params.setOperation(null);
        result = params.validate(MY_NAME);
        assertFalse(result.isValid());
        assertThat(result.getResult()).contains("operation");
        params.setOperation(operMap);

        // null entry in the map
        Map<String, Map<String, Object>> map2 = new TreeMap<>(operMap);
        map2.put(OPER2_NAME, null);
        params.setOperation(map2);
        result = params.validate(MY_NAME);
        assertFalse(result.isValid());
        assertThat(result.getResult()).contains("operation").contains("null");
        params.setOperation(operMap);

        // test success case
        assertTrue(params.validate(MY_NAME).isValid());
    }
}
