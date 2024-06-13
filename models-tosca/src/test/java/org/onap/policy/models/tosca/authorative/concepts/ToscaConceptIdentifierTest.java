/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy Models
 * ================================================================================
 * Copyright (C) 2019, 2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2021, 2024 Nordix Foundation.
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.onap.policy.common.parameters.ValidationResult;
import org.onap.policy.models.base.PfConceptKey;

/**
 * Test methods not tested by {@link PojosTest}.
 */
class ToscaConceptIdentifierTest extends ToscaIdentifierTestBase<ToscaConceptIdentifier> {

    public ToscaConceptIdentifierTest() {
        super(ToscaConceptIdentifier.class, "name", "version");
    }

    @Test
    void testAllArgsConstructor() {
        assertThatThrownBy(() -> new ToscaConceptIdentifier(null, VERSION)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new ToscaConceptIdentifier(NAME, null)).isInstanceOf(NullPointerException.class);

        ToscaConceptIdentifier orig = new ToscaConceptIdentifier(NAME, VERSION);
        assertEquals(NAME, orig.getName());
        assertEquals(VERSION, orig.getVersion());
    }

    @Test
    void testCopyConstructor() {
        assertThatThrownBy(() -> new ToscaConceptIdentifier((ToscaConceptIdentifier) null))
                .isInstanceOf(NullPointerException.class);

        ToscaConceptIdentifier orig = new ToscaConceptIdentifier();

        // verify with null values
        assertEquals(orig.toString(), new ToscaConceptIdentifier(orig).toString());

        // verify with all values
        orig = new ToscaConceptIdentifier(NAME, VERSION);
        assertEquals(orig.toString(), new ToscaConceptIdentifier(orig).toString());
    }


    @Test
    void testPfKey() {
        assertThatThrownBy(() -> new ToscaConceptIdentifier((PfConceptKey) null))
                .isInstanceOf(NullPointerException.class);

        PfConceptKey origKey = new PfConceptKey("Hello", "0.0.1");

        assertEquals(origKey.getName(), new ToscaConceptIdentifier(origKey).getName());

        assertEquals(origKey, new ToscaConceptIdentifier(origKey).asConceptKey());
    }

    @Test
    void testValidatePapRest() throws Exception {
        ToscaConceptIdentifier ident = new ToscaConceptIdentifier(NAME, VERSION);
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
    void testCompareTo() throws Exception {
        super.testCompareTo();
    }
}
