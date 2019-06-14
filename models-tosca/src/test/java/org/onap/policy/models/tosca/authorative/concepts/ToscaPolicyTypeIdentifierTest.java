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

package org.onap.policy.models.tosca.authorative.concepts;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.onap.policy.common.parameters.ValidationResult;

/**
 * Test methods not tested by {@link PojosTest}.
 */
public class ToscaPolicyTypeIdentifierTest extends ToscaIdentifierTestBase<ToscaPolicyTypeIdentifier> {

    public ToscaPolicyTypeIdentifierTest() {
        super(ToscaPolicyTypeIdentifier.class, "name", "version");
    }

    @Test
    public void testAllArgsConstructor() {
        assertThatThrownBy(() -> new ToscaPolicyTypeIdentifier(null, VERSION)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new ToscaPolicyTypeIdentifier(NAME, null)).isInstanceOf(NullPointerException.class);

        ToscaPolicyTypeIdentifier orig = new ToscaPolicyTypeIdentifier(NAME, VERSION);
        assertEquals(NAME, orig.getName());
        assertEquals(VERSION, orig.getVersion());
    }

    @Test
    public void testCopyConstructor() {
        assertThatThrownBy(() -> new ToscaPolicyTypeIdentifier(null)).isInstanceOf(NullPointerException.class);

        ToscaPolicyTypeIdentifier orig = new ToscaPolicyTypeIdentifier();

        // verify with null values
        assertEquals(orig.toString(), new ToscaPolicyTypeIdentifier(orig).toString());

        // verify with all values
        orig = new ToscaPolicyTypeIdentifier(NAME, VERSION);
        assertEquals(orig.toString(), new ToscaPolicyTypeIdentifier(orig).toString());
    }

    @Test
    public void testValidatePapRest() throws Exception {
        ToscaPolicyTypeIdentifier ident = new ToscaPolicyTypeIdentifier(NAME, VERSION);
        ValidationResult result = ident.validatePapRest();
        assertNotNull(result);
        assertTrue(result.isValid());
        assertNull(result.getResult());

        ident = makeIdent(NAME, null);
        result = ident.validatePapRest();
        assertNotNull(result);
        assertFalse(result.isValid());
        assertNotNull(result.getResult());

        ident = makeIdent(null, VERSION);
        result = ident.validatePapRest();
        assertNotNull(result);
        assertFalse(result.isValid());
        assertNotNull(result.getResult());
    }

    @Test
    @Override
    public void testCompareTo() throws Exception {
        super.testCompareTo();
    }
}
