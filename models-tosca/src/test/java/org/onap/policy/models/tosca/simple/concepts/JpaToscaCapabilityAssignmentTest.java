/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020-2021 Nordix Foundation.
 *  Modifications Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
 *  Modifications Copyright (C) 2024 Nordix Foundation
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.Validated;
import org.onap.policy.models.tosca.authorative.concepts.ToscaCapabilityAssignment;

/**
 * DAO test for JpaToscaCapabilityAssignment.
 */
class JpaToscaCapabilityAssignmentTest {

    private static final String KEY_IS_NULL = "key is marked .*on.*ull but is null";

    @Test
    void testPropertyPojo() {
        ToscaCapabilityAssignment tca = new ToscaCapabilityAssignment();
        tca.setName("world");
        tca.setVersion("1.2.3");
        tca.setType("planet");
        tca.setTypeVersion("4.5.6");

        assertNotNull(new JpaToscaCapabilityAssignment());
        assertNotNull(new JpaToscaCapabilityAssignment(new PfConceptKey()));
        assertNotNull(new JpaToscaCapabilityAssignment(new JpaToscaCapabilityAssignment()));
        assertNotNull(new JpaToscaCapabilityAssignment(tca));

        assertThatThrownBy(() -> new JpaToscaCapabilityAssignment((PfConceptKey) null)).hasMessageMatching(KEY_IS_NULL);
        assertThatThrownBy(() -> new JpaToscaCapabilityAssignment((JpaToscaCapabilityAssignment) null))
                .hasMessageMatching("copyConcept is marked .*on.*ull but is null");
        assertThatThrownBy(() -> new JpaToscaCapabilityAssignment((ToscaCapabilityAssignment) null))
                .hasMessageMatching("authorativeConcept is marked .*on.*ull but is null");

        PfConceptKey caKey = new PfConceptKey("tParentKey", "0.0.1");

        JpaToscaCapabilityAssignment caNull = new JpaToscaCapabilityAssignment(caKey);
        caNull.setProperties(null);
        caNull.setAttributes(null);
        caNull.setOccurrences(null);

        assertEquals(caNull, new JpaToscaCapabilityAssignment(caNull));

        JpaToscaCapabilityAssignment ca = new JpaToscaCapabilityAssignment(caKey);

        assertEquals(ca, new JpaToscaCapabilityAssignment(ca));
        assertEquals(caKey, ca.getKeys().get(0));

        ca.clean();
        ca.validate("");
        assertThat(ca.getProperties()).isNullOrEmpty();
        assertThat(ca.getAttributes()).isNullOrEmpty();

        ca.setProperties(null);
        ca.setAttributes(null);
        ca.setOccurrences(null);
        ca.clean();
        ca.validate("");
        assertEquals(null, ca.getProperties());
        assertEquals(null, ca.getAttributes());

        Map<String, String> properties = new LinkedHashMap<>();
        properties.put("Key0", "  Untrimmed Value  ");
        ca.setProperties(properties);

        Map<String, String> attributes = new LinkedHashMap<>();
        attributes.put("Key0", "  Untrimmed Value  ");
        ca.setAttributes(attributes);

        List<Integer> occurrences = new ArrayList<>();
        occurrences.add(12345);
        ca.setOccurrences(occurrences);

        ca.clean();
        ca.validate("");
        assertEquals("Untrimmed Value", ca.getProperties().get("Key0"));
        assertEquals("Untrimmed Value", ca.getAttributes().get("Key0"));

        ca.getProperties().put("Key1", null);
        ca.getAttributes().put("Key1", null);
        ca.getOccurrences().add(null);
        ca.getOccurrences().add(-12345);
        assertThat(ca.validate("").getResult())
            .contains("properties").contains("Key1").contains(Validated.IS_NULL)
            .contains("attributes").contains("Key1").contains(Validated.IS_NULL)
            .contains("occurrence").contains("value").contains("is below the minimum value: 0");
    }

    @Test
    void testCompareTo() {
        assertEquals(-1, new JpaToscaCapabilityAssignment().compareTo(null));
        assertEquals(0, new JpaToscaCapabilityAssignment().compareTo(new JpaToscaCapabilityAssignment()));

        JpaToscaCapabilityAssignment ca = new JpaToscaCapabilityAssignment();
        assertEquals(0, ca.compareTo(ca));
        assertEquals(18, ca.compareTo(new PfConceptKey()));

        JpaToscaCapabilityAssignment ca2 = new JpaToscaCapabilityAssignment();
        ca2.getKey().setName("ca2");
        assertEquals(-21, ca.compareTo(ca2));

        ca.getKey().setName("ca");
        ca2.getKey().setName("ca");

        ca.setProperties(new LinkedHashMap<>());
        ca2.setProperties(new LinkedHashMap<>());
        ca.getProperties().put("Key0", "Value0");
        assertEquals(-1737938642, ca.compareTo(ca2));
        ca2.getProperties().put("Key0", "Value0");
        assertEquals(0, ca.compareTo(ca2));

        ca.setAttributes(new LinkedHashMap<>());
        ca2.setAttributes(new LinkedHashMap<>());
        ca.getAttributes().put("Key0", "Value0");
        assertEquals(-1737938642, ca.compareTo(ca2));
        ca2.getAttributes().put("Key0", "Value0");
        assertEquals(0, ca.compareTo(ca2));

        ca.setOccurrences(new ArrayList<>());
        ca2.setOccurrences(new ArrayList<>());
        ca.getOccurrences().add(12345);
        assertEquals(12375, ca.compareTo(ca2));
        ca2.getOccurrences().add(12345);
        assertEquals(0, ca.compareTo(ca2));
    }

    @Test
    void testAuthorative() {
        ToscaCapabilityAssignment tca = new ToscaCapabilityAssignment();
        tca.setName("world");
        tca.setVersion("1.2.3");
        tca.setType("planet");
        tca.setTypeVersion("4.5.6");

        ToscaCapabilityAssignment tcaConsTo =
                new JpaToscaCapabilityAssignment(tca).toAuthorative();

        assertEquals(tca, tcaConsTo);

        JpaToscaCapabilityAssignment jtca = new JpaToscaCapabilityAssignment(tcaConsTo);
        ToscaCapabilityAssignment tcaFromTo = jtca.toAuthorative();
        assertEquals(tca, tcaFromTo);
    }
}
