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

package org.onap.policy.models.pdp.concepts;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.onap.policy.models.base.PfValidationResult;

/**
 * Test the other constructors, as {@link TestModels} tests the other methods.
 */
public class TestPolicyIdent extends IdentTestBase<PolicyIdent> {
    private static final String NAME = "my-name";
    private static final String VERSION = "1.2.3";

    public TestPolicyIdent() {
        super(PolicyIdent.class);
    }

    @Test
    public void testAllArgsConstructor() {
        assertThatThrownBy(() -> new PolicyIdent(null, VERSION)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new PolicyIdent(NAME, null)).isInstanceOf(NullPointerException.class);

        PolicyIdent orig = new PolicyIdent(NAME, VERSION);
        assertEquals(NAME, orig.getName());
        assertEquals(VERSION, orig.getVersion());
    }

    @Test
    public void testCopyConstructor() {
        assertThatThrownBy(() -> new PolicyIdent(null)).isInstanceOf(NullPointerException.class);

        PolicyIdent orig = new PolicyIdent();

        // verify with null values
        assertEquals(orig.toString(), new PolicyIdent(orig).toString());

        // verify with all values
        orig = new PolicyIdent(NAME, VERSION);
        assertEquals(orig.toString(), new PolicyIdent(orig).toString());
    }

    @Test
    public void testValidate() throws Exception {
        assertTrue(makeIdent(NAME, VERSION).validate(new PfValidationResult()).isValid());

        // everything is null
        PfValidationResult result = makeIdent(null, null).validate(new PfValidationResult());
        assertFalse(result.isValid());
        assertEquals(2, result.getMessageList().size());

        // name is null
        result = makeIdent(null, VERSION).validate(new PfValidationResult());
        assertFalse(result.isValid());
        assertEquals(1, result.getMessageList().size());

        // version is null
        result = makeIdent(NAME, null).validate(new PfValidationResult());
        assertFalse(result.isValid());
        assertEquals(1, result.getMessageList().size());

        // version is invalid
        result = makeIdent(NAME, "!!!" + VERSION).validate(new PfValidationResult());
        assertFalse(result.isValid());
        assertEquals(1, result.getMessageList().size());
    }
}
