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

import org.junit.Test;

/**
 * Test the other constructors, as {@link PojosTest} tests the other methods.
 */
public class ToscaPolicyIdentifierOptVersionTest extends ToscaIdentifierTestBase<ToscaPolicyIdentifierOptVersion> {

    public ToscaPolicyIdentifierOptVersionTest() {
        super(ToscaPolicyIdentifierOptVersion.class, "policy-id", "policy-version");
    }

    @Test
    public void testAllArgsConstructor_testIsNullVersion() {
        assertThatThrownBy(() -> new ToscaPolicyIdentifierOptVersion(null, VERSION))
                        .isInstanceOf(NullPointerException.class);

        // with null version
        ToscaPolicyIdentifierOptVersion orig = new ToscaPolicyIdentifierOptVersion(NAME, null);
        assertEquals(NAME, orig.getName());
        assertEquals(null, orig.getVersion());

        orig = new ToscaPolicyIdentifierOptVersion(NAME, VERSION);
        assertEquals(NAME, orig.getName());
        assertEquals(VERSION, orig.getVersion());
    }

    @Test
    public void testCopyConstructor() throws Exception {
        assertThatThrownBy(() -> new ToscaPolicyIdentifierOptVersion((ToscaPolicyIdentifierOptVersion) null))
                        .isInstanceOf(NullPointerException.class);

        ToscaPolicyIdentifierOptVersion orig = new ToscaPolicyIdentifierOptVersion();

        // verify with null values
        assertEquals(orig.toString(), new ToscaPolicyIdentifierOptVersion(orig).toString());

        // verify with all values
        orig = makeIdent(NAME, VERSION);
        assertEquals(orig.toString(), new ToscaPolicyIdentifierOptVersion(orig).toString());
    }

    @Test
    public void testCopyToscaPolicyIdentifierConstructor() throws Exception {
        assertThatThrownBy(() -> new ToscaPolicyIdentifierOptVersion((ToscaPolicyIdentifier) null))
                        .isInstanceOf(NullPointerException.class);

        ToscaPolicyIdentifier orig = new ToscaPolicyIdentifier();

        // verify with null values
        ToscaPolicyIdentifierOptVersion newIdent = new ToscaPolicyIdentifierOptVersion(orig);
        assertEquals(null, newIdent.getName());
        assertEquals(null, newIdent.getVersion());

        // verify with all values
        orig.setName(NAME);
        orig.setVersion(VERSION);
        newIdent = new ToscaPolicyIdentifierOptVersion(orig);
        assertEquals(NAME, newIdent.getName());
        assertEquals(VERSION, newIdent.getVersion());
    }

    @Test
    public void testCompareTo() throws Exception {
        super.testCompareTo();
    }
}
