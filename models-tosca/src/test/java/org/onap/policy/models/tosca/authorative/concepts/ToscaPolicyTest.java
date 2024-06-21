/*
 * ============LICENSE_START=======================================================
 * ONAP Policy Models
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2020-2024 Nordix Foundation.
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
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.LinkedHashMap;
import org.junit.jupiter.api.Test;

/**
 * Tests methods not tested by {@link PojosTest}.
 */
class ToscaPolicyTest {

    @Test
    void testGetIdentifier_testGetTypeIdentifier() {
        assertThatThrownBy(() -> {
            new ToscaPolicy(null);
        }).hasMessageMatching("copyObject is marked .*on.*ull but is null");

        ToscaPolicy policy = new ToscaPolicy();

        policy.setName("my_name");
        policy.setVersion("1.2.3");
        policy.setType("my_type");
        policy.setTypeVersion("3.2.1");

        ToscaEntity te = new ToscaEntity();
        assertNull(te.getType());
        assertNull(te.getTypeVersion());

        assertEquals("ToscaEntityKey(name=my_name, version=1.2.3)", policy.getKey().toString());
        assertEquals(new ToscaConceptIdentifier("my_name", "1.2.3"), policy.getKey().asIdentifier());

        ToscaConceptIdentifier ident = policy.getIdentifier();
        assertEquals("my_name", ident.getName());
        assertEquals("1.2.3", ident.getVersion());

        ToscaConceptIdentifier type = policy.getTypeIdentifier();
        assertEquals("my_type", type.getName());
        assertEquals("3.2.1", type.getVersion());

        ToscaPolicy clonedPolicy0 = new ToscaPolicy(policy);
        assertEquals(0, new ToscaEntityComparator<ToscaPolicy>().compare(policy, clonedPolicy0));

        policy.setProperties(new LinkedHashMap<String, Object>());
        policy.getProperties().put("PropertyKey", "PropertyValue");
        ToscaPolicy clonedPolicy1 = new ToscaPolicy(policy);
        assertEquals(0, new ToscaEntityComparator<ToscaPolicy>().compare(policy, clonedPolicy1));
    }
}
