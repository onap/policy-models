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

/**
 * Test the other constructors, as {@link TestModels} tests the other methods.
 */
public class TestPolicyIdentOptVersion extends IdentTestBase<PolicyIdentOptVersion> {
    private static final String NAME = "my-name";
    private static final String VERSION = "1.2.3";

    public TestPolicyIdentOptVersion() {
        super(PolicyIdentOptVersion.class);
    }

    @Test
    public void testAllArgsConstructor_testIsNullVersion() {
        assertThatThrownBy(() -> new PolicyIdentOptVersion(null, VERSION)).isInstanceOf(NullPointerException.class);

        // with null version
        PolicyIdentOptVersion orig = new PolicyIdentOptVersion(NAME, null);
        assertEquals(NAME, orig.getName());
        assertEquals(null, orig.getVersion());
        assertTrue(orig.isNullVersion());

        orig = new PolicyIdentOptVersion(NAME, VERSION);
        assertEquals(NAME, orig.getName());
        assertEquals(VERSION, orig.getVersion());
        assertFalse(orig.isNullVersion());
    }

    @Test
    public void testCopyConstructor() throws Exception {
        assertThatThrownBy(() -> new PolicyIdentOptVersion(null)).isInstanceOf(NullPointerException.class);

        PolicyIdentOptVersion orig = new PolicyIdentOptVersion();

        // verify with null values
        assertEquals(orig.toString(), new PolicyIdentOptVersion(orig).toString());

        // verify with all values
        orig = makeIdent(NAME, VERSION);
        assertEquals(orig.toString(), new PolicyIdentOptVersion(orig).toString());
    }
}
