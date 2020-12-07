/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix Foundation.
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

package org.onap.policy.models.tosca.simple.concepts;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Test;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.tosca.authorative.concepts.ToscaCapabilityAssignment;

/**
 * DAO test for JpaToscaCapabilityAssignment.
 */
public class JpaToscaCapabilityAssignmentTest {

    private static final String KEY_IS_NULL = "key is marked .*on.*ull but is null";

    @Test
    public void testPropertyPojo() {
        assertNotNull(new JpaToscaCapabilityAssignment());
        assertNotNull(new JpaToscaCapabilityAssignment(new PfConceptKey()));
        assertNotNull(new JpaToscaCapabilityAssignment(new JpaToscaCapabilityAssignment()));
        assertNotNull(new JpaToscaCapabilityAssignment(new ToscaCapabilityAssignment()));

        assertThatThrownBy(() -> new JpaToscaCapabilityAssignment((PfConceptKey) null)).hasMessageMatching(KEY_IS_NULL);
        assertThatThrownBy(() -> new JpaToscaCapabilityAssignment((JpaToscaCapabilityAssignment) null))
        .hasMessageMatching("copyConcept is marked .*on.*ull but is null");
        assertThatThrownBy(() -> new JpaToscaCapabilityAssignment((ToscaCapabilityAssignment) null))
        .hasMessageMatching("authorativeConcept is marked .*on.*ull but is null");

        PfConceptKey caKey = new PfConceptKey("tParentKey", "0.0.1");
        JpaToscaCapabilityAssignment ca = new JpaToscaCapabilityAssignment(caKey);

        assertEquals(ca, new JpaToscaCapabilityAssignment(ca));
        
        assertEquals(caKey, ca.getKeys().get(0));
        
        ca.clean();
        assertEquals(0, ca.getProperties().size());
        assertEquals(0, ca.getAttributes().size());

        ca.setProperties(null);
        ca.setAttributes(null);
        ca.clean();
        assertEquals(null, ca.getProperties());
        assertEquals(null, ca.getAttributes());
        
        Map<String, String> properties = new LinkedHashMap<>();
        properties.put("Key0", "  Untrimmed Value  ");
        ca.setProperties(properties);
        
        Map<String, String> attributes = new LinkedHashMap<>();
        attributes.put("Key0", "  Untrimmed Value  ");
        ca.setAttributes(attributes);
        
        ca.clean();
        assertEquals("Untrimmed Value", ca.getProperties().get("Key0"));
        assertEquals("Untrimmed Value", ca.getAttributes().get("Key0"));
    }
}
