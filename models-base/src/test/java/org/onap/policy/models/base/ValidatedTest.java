/*
 * ============LICENSE_START=======================================================
 * ONAP Policy Models
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.models.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class ValidatedTest {
    private static final String WORD_PAT = "\\w*";
    private static final String MY_TO_STRING = "[some text]";

    private Validated validated;

    @Before
    public void setUp() {
        validated = new Validated();
    }

    @Test
    public void testValidate() {
        PfValidationResult result = new PfValidationResult();
        assertSame(result, validated.validate(result));
        assertTrue(result.isValid());
        assertEquals(0, result.getMessageList().size());
    }

    @Test
    public void testValidateNotNull() {
        PfValidationResult result = new PfValidationResult();

        // null text
        validated.validateNotNull(this, "nullField", null, result);

        // invalid text
        validated.validateNotNull(this, "invalidField", "!!!", result);

        // valid text
        validated.validateNotNull(this, "validField", "abc123", result);

        assertFalse(result.isValid());
        assertEquals(1, result.getMessageList().size());

        // check result for null text
        PfValidationMessage msg = result.getMessageList().get(0);
        assertSame(ValidatedTest.class.getName(), msg.getObservedClass());
        assertSame(MY_TO_STRING, msg.getObservedKey().toString());
        assertTrue(msg.getMessage().contains("nullField invalid-null"));
    }

    @Test
    public void testValidateText() {
        PfValidationResult result = new PfValidationResult();

        // null text
        validated.validateText(this, "nullField", null, WORD_PAT, result);

        // invalid text
        validated.validateText(this, "invalidField", "!!!", WORD_PAT, result);

        // valid text
        validated.validateText(this, "validField", "abc123", WORD_PAT, result);

        assertFalse(result.isValid());
        assertEquals(1, result.getMessageList().size());

        // check result for invalid text
        PfValidationMessage msg = result.getMessageList().get(0);
        assertSame(ValidatedTest.class.getName(), msg.getObservedClass());
        assertSame(MY_TO_STRING, msg.getObservedKey().toString());
        assertTrue(msg.getMessage().contains("invalidField invalid-parameter invalidField"));
    }

    @Override
    public String toString() {
        return MY_TO_STRING;
    }
}
