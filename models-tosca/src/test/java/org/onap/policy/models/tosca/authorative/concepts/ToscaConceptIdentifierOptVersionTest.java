/*
 * ============LICENSE_START=======================================================
 * ONAP Policy Models
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

import org.junit.jupiter.api.Test;

/**
 * Test the other constructors, as {@link PojosTest} tests the other methods.
 */
class ToscaConceptIdentifierOptVersionTest extends ToscaIdentifierTestBase<ToscaConceptIdentifierOptVersion> {

    public ToscaConceptIdentifierOptVersionTest() {
        super(ToscaConceptIdentifierOptVersion.class, "name", "version");
    }

    @Test
    void testAllArgsConstructor_testIsNullVersion() {
        assertThatThrownBy(() -> new ToscaConceptIdentifierOptVersion(null, VERSION))
                        .isInstanceOf(NullPointerException.class);

        // with null version
        ToscaConceptIdentifierOptVersion orig = new ToscaConceptIdentifierOptVersion(NAME, null);
        assertEquals(NAME, orig.getName());
        assertEquals(null, orig.getVersion());

        orig = new ToscaConceptIdentifierOptVersion(NAME, VERSION);
        assertEquals(NAME, orig.getName());
        assertEquals(VERSION, orig.getVersion());
    }

    @Test
    void testCopyConstructor() throws Exception {
        assertThatThrownBy(() -> new ToscaConceptIdentifierOptVersion((ToscaConceptIdentifierOptVersion) null))
                        .isInstanceOf(NullPointerException.class);

        ToscaConceptIdentifierOptVersion orig = new ToscaConceptIdentifierOptVersion();

        // verify with null values
        assertEquals(orig.toString(), new ToscaConceptIdentifierOptVersion(orig).toString());

        // verify with all values
        orig = makeIdent(NAME, VERSION);
        assertEquals(orig.toString(), new ToscaConceptIdentifierOptVersion(orig).toString());
    }

    @Test
    void testCopyToscaPolicyIdentifierConstructor() {
        assertThatThrownBy(() -> new ToscaConceptIdentifierOptVersion((ToscaConceptIdentifier) null))
                        .isInstanceOf(NullPointerException.class);

        ToscaConceptIdentifier orig = new ToscaConceptIdentifier();

        // verify with null values
        ToscaConceptIdentifierOptVersion newIdent = new ToscaConceptIdentifierOptVersion(orig);
        assertEquals(null, newIdent.getName());
        assertEquals(null, newIdent.getVersion());

        // verify with all values
        orig.setName(NAME);
        orig.setVersion(VERSION);
        newIdent = new ToscaConceptIdentifierOptVersion(orig);
        assertEquals(NAME, newIdent.getName());
        assertEquals(VERSION, newIdent.getVersion());
    }

    @Test
    @Override
    void testCompareTo() throws Exception {
        super.testCompareTo();
    }
}
