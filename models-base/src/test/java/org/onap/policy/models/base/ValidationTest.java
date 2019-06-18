/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
 *  Modifications Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.policy.models.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.onap.policy.models.base.PfValidationResult.ValidationResult;

public class ValidationTest {

    private static final String HELLO = "hello";
    private static final String SOME_MESSAGE = "Some message";

    @Test
    public void test() {
        PfValidationResult result = new PfValidationResult();
        PfConceptKey pfKeyey = new PfConceptKey("PK", "0.0.1");
        result = pfKeyey.validate(result);

        assertNotNull(result);
        assertTrue(result.isOk());
        assertTrue(result.isValid());
        assertEquals(PfValidationResult.ValidationResult.VALID, result.getValidationResult());
        assertNotNull(result.getMessageList());

        PfValidationMessage vmess0 = new PfValidationMessage(PfConceptKey.getNullKey(), PfConceptKey.class,
                        ValidationResult.VALID, SOME_MESSAGE);
        result.addValidationMessage(vmess0);

        assertTrue(result.isOk());
        assertTrue(result.isValid());
        assertEquals(PfValidationResult.ValidationResult.VALID, result.getValidationResult());
        assertNotNull(result.getMessageList());
        assertNotNull(HELLO, result.toString());

        PfValidationMessage vmess1 = new PfValidationMessage(PfConceptKey.getNullKey(), PfConceptKey.class,
                        ValidationResult.OBSERVATION, SOME_MESSAGE);
        result.addValidationMessage(vmess1);

        assertTrue(result.isOk());
        assertTrue(result.isValid());
        assertEquals(PfValidationResult.ValidationResult.OBSERVATION, result.getValidationResult());
        assertNotNull(result.getMessageList());
        assertNotNull(HELLO, result.toString());

        PfValidationMessage vmess2 = new PfValidationMessage(PfConceptKey.getNullKey(), PfConceptKey.class,
                        ValidationResult.WARNING, SOME_MESSAGE);
        result.addValidationMessage(vmess2);

        assertFalse(result.isOk());
        assertTrue(result.isValid());
        assertEquals(PfValidationResult.ValidationResult.WARNING, result.getValidationResult());
        assertNotNull(result.getMessageList());
        assertNotNull(HELLO, result.toString());

        PfValidationMessage vmess3 = new PfValidationMessage(PfConceptKey.getNullKey(), PfConceptKey.class,
                        ValidationResult.INVALID, SOME_MESSAGE);
        result.addValidationMessage(vmess3);

        assertFalse(result.isOk());
        assertFalse(result.isValid());
        assertEquals(PfValidationResult.ValidationResult.INVALID, result.getValidationResult());
        assertNotNull(result.getMessageList());
        assertNotNull(HELLO, result.toString());

        assertEquals(PfValidationResult.ValidationResult.INVALID, result.getMessageList().get(3).getValidationResult());
        assertEquals(SOME_MESSAGE, result.getMessageList().get(3).getMessage());
        assertEquals(PfConceptKey.class.getName(), result.getMessageList().get(3).getObservedClass());
        assertEquals(PfConceptKey.getNullKey(), result.getMessageList().get(3).getObservedKey());
    }
}
