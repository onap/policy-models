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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Tests methods not tested by {@link TestPojos}.
 */
public class TestToscaPolicy {

    @Test
    public void testGetIdentifier_testGetTypeIdentifier() {
        ToscaPolicy policy = new ToscaPolicy();

        policy.setName("my_name");
        policy.setVersion("1.2.3");
        policy.setType("my_type");
        policy.setTypeVersion("3.2.1");

        ToscaPolicyIdentifier ident = policy.getIdentifier();
        assertEquals("my_name", ident.getName());
        assertEquals("1.2.3", ident.getVersion());

        ToscaPolicyTypeIdentifier type = policy.getTypeIdentifier();
        assertEquals("my_type", type.getName());
        assertEquals("3.2.1", type.getVersion());
    }
}
