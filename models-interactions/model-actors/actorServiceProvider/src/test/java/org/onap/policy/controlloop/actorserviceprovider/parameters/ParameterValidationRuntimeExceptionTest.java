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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.parameters.ObjectValidationResult;
import org.onap.policy.common.parameters.ValidationResult;
import org.onap.policy.common.parameters.ValidationStatus;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ParameterValidationRuntimeException;

public class ParameterValidationRuntimeExceptionTest {

    private static final String THE_MESSAGE = "the message";
    private static final IllegalStateException EXPECTED_EXCEPTION = new IllegalStateException("expected exception");

    private ValidationResult result;

    @Before
    public void setUp() {
        result = new ObjectValidationResult("param", null, ValidationStatus.INVALID, "null");
    }

    @Test
    public void testParameterValidationExceptionValidationResult() {
        ParameterValidationRuntimeException ex = new ParameterValidationRuntimeException(result);
        assertSame(result, ex.getResult());
        assertNull(ex.getMessage());
    }

    @Test
    public void testParameterValidationExceptionValidationResultString() {
        ParameterValidationRuntimeException ex = new ParameterValidationRuntimeException(THE_MESSAGE, result);
        assertSame(result, ex.getResult());
        assertEquals(THE_MESSAGE, ex.getMessage());
    }

    @Test
    public void testParameterValidationExceptionValidationResultThrowable() {
        ParameterValidationRuntimeException ex = new ParameterValidationRuntimeException(EXPECTED_EXCEPTION, result);
        assertSame(result, ex.getResult());
        assertEquals(EXPECTED_EXCEPTION.toString(), ex.getMessage());
        assertEquals(EXPECTED_EXCEPTION, ex.getCause());
    }

    @Test
    public void testParameterValidationExceptionValidationResultStringThrowable() {
        ParameterValidationRuntimeException ex =
                        new ParameterValidationRuntimeException(THE_MESSAGE, EXPECTED_EXCEPTION,  result);
        assertSame(result, ex.getResult());
        assertEquals(THE_MESSAGE, ex.getMessage());
        assertEquals(EXPECTED_EXCEPTION, ex.getCause());
    }

    @Test
    public void testGetResult() {
        ParameterValidationRuntimeException ex = new ParameterValidationRuntimeException(result);
        assertSame(result, ex.getResult());
    }
}
