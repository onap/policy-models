/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2024-2025 Nordix Foundation
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

package org.onap.policy.models.tosca.simple.concepts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onap.policy.common.parameters.annotations.NotNull;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfKey;
import org.onap.policy.models.base.PfReferenceKey;
import org.onap.policy.models.tosca.authorative.concepts.ToscaProperty;
import org.onap.policy.models.tosca.authorative.concepts.ToscaWithToscaProperties;

class JpaToscaWithToscaPropertiesTest {
    private static final String SOME_DESCRIPTION = "some description";
    private static final String KEY1 = "abc";
    private static final String KEY2 = "def";
    private static final PfConceptKey CONCEPT_KEY1 = new PfConceptKey("hello", "1.2.3");
    private static final PfConceptKey CONCEPT_KEY2 = new PfConceptKey("world", "3.2.1");
    private static final PfReferenceKey REF_KEY1 = new PfReferenceKey(CONCEPT_KEY1);
    private static final PfReferenceKey REF_KEY2 = new PfReferenceKey(CONCEPT_KEY2);
    private static final JpaToscaProperty JPA_PROP1 = new JpaToscaProperty(REF_KEY1);
    private static final JpaToscaProperty JPA_PROP2 = new JpaToscaProperty(REF_KEY2);
    private static ToscaProperty toscaPropertyOne;
    private static ToscaProperty toscaPropertyTwo;
    private static final String DESCRIPT1 = "description A";
    private static final String DESCRIPT2 = "description B";

    private MyJpa jpa;

    /**
     * Initializes the properties.
     */
    @BeforeAll
    static void setUpBeforeClass() {
        JPA_PROP1.setDescription(DESCRIPT1);
        JPA_PROP2.setDescription(DESCRIPT2);

        toscaPropertyOne = JPA_PROP1.toAuthorative();
        toscaPropertyTwo = JPA_PROP2.toAuthorative();
    }

    @BeforeEach
    void setUp() {
        jpa = new MyJpa();
    }

    @Test
    void testGetKeys() {
        PfConceptKey key = new PfConceptKey("bye", "9.8.7");

        jpa = new MyJpa(key);
        jpa.setDescription(SOME_DESCRIPTION);
        jpa.setProperties(Map.of(KEY1, JPA_PROP1, KEY2, JPA_PROP2));

        // properties should be included

        List<PfKey> keys = jpa.getKeys();
        Collections.sort(keys);

        assertThat(keys).isEqualTo(
                        List.of(PfConceptKey.getNullKey(), PfConceptKey.getNullKey(), key, REF_KEY1, REF_KEY2));
    }

    @Test
    void testClean() {
        jpa.clean();

        jpa.setDescription("  some description  ");

        JpaToscaProperty prop1 = new JpaToscaProperty(JPA_PROP1);
        prop1.setDescription(DESCRIPT1 + " ");

        JpaToscaProperty prop2 = new JpaToscaProperty(JPA_PROP2);
        prop2.setDescription(" " + DESCRIPT2);

        jpa.setProperties(Map.of(KEY1, prop1, KEY2, prop2));

        jpa.clean();
        assertEquals(SOME_DESCRIPTION, jpa.getDescription());
        assertThat(jpa.getProperties()).isEqualTo(Map.of(KEY1, JPA_PROP1, KEY2, JPA_PROP2));
    }

    @Test
    void testToAuthorative() {
        jpa.setDescription(SOME_DESCRIPTION);
        jpa.setProperties(Map.of(KEY1, JPA_PROP1, KEY2, JPA_PROP2));

        MyTosca tosca = jpa.toAuthorative();
        assertEquals(SOME_DESCRIPTION, tosca.getDescription());
        assertThat(tosca.getProperties()).isEqualTo(Map.of(KEY1, toscaPropertyOne, KEY2, toscaPropertyTwo));
    }

    @Test
    void testFromAuthorative() {
        MyTosca tosca = new MyTosca();
        tosca.setDescription(SOME_DESCRIPTION);

        jpa.fromAuthorative(tosca);
        assertEquals(SOME_DESCRIPTION, jpa.getDescription());
        assertThat(jpa.getProperties()).isNull();

        tosca.setProperties(Map.of(KEY1, toscaPropertyOne, KEY2, toscaPropertyTwo));

        JpaToscaProperty jpa1 = new JpaToscaProperty(toscaPropertyOne);
        jpa1.setKey(new PfReferenceKey(jpa.getKey(), KEY1));

        JpaToscaProperty jpa2 = new JpaToscaProperty(toscaPropertyTwo);
        jpa2.setKey(new PfReferenceKey(jpa.getKey(), KEY2));

        jpa = new MyJpa();
        jpa.fromAuthorative(tosca);
        assertEquals(SOME_DESCRIPTION, jpa.getDescription());
        assertThat(jpa.getProperties()).isEqualTo(Map.of(KEY1, jpa1, KEY2, jpa2));
    }

    @Test
    void testCompareTo() {
        jpa.setDescription(SOME_DESCRIPTION);
        jpa.setProperties(Map.of(KEY1, JPA_PROP1, KEY2, JPA_PROP2));

        assertThat(jpa).isNotEqualByComparingTo(null).isEqualByComparingTo(jpa).isNotEqualByComparingTo(new MyJpa2());

        MyJpa jpa2 = new MyJpa();
        jpa2.setDescription(SOME_DESCRIPTION);
        jpa2.setProperties(Map.of(KEY1, JPA_PROP1, KEY2, JPA_PROP2));
        assertThat(jpa).isEqualByComparingTo(jpa2);

        jpa2.setProperties(Map.of(KEY1, JPA_PROP1));
        assertThat(jpa).isNotEqualByComparingTo(jpa2);
    }

    @Test
    void testJpaToscaWithToscaProperties() {
        assertThat(jpa.getProperties()).isNull();
        assertThat(jpa.getKey().isNullKey()).isTrue();
    }

    @Test
    void testJpaToscaWithToscaPropertiesPfConceptKey() {
        jpa = new MyJpa(CONCEPT_KEY1);
        assertEquals(CONCEPT_KEY1, jpa.getKey());
    }

    @Test
    void testJpaToscaWithToscaPropertiesJpaToscaWithToscaPropertiesOfT() {
        jpa.setDescription(SOME_DESCRIPTION);
        assertEquals(jpa, new MyJpa(jpa));

        jpa.setProperties(Map.of(KEY1, JPA_PROP1, KEY2, JPA_PROP2));
        assertEquals(jpa, new MyJpa(jpa));
    }

    @Test
    void testJpaToscaWithToscaPropertiesT() {
        MyTosca tosca = new MyTosca();
        tosca.setName("world");
        tosca.setVersion("3.2.1");
        tosca.setDescription(SOME_DESCRIPTION);
        tosca.setProperties(Map.of(KEY1, toscaPropertyOne, KEY2, toscaPropertyTwo));

        jpa = new MyJpa(tosca);
        assertEquals(SOME_DESCRIPTION, jpa.getDescription());

        JpaToscaProperty jpa1 = new JpaToscaProperty(toscaPropertyOne);
        jpa1.setKey(new PfReferenceKey(jpa.getKey(), KEY1));

        JpaToscaProperty jpa2 = new JpaToscaProperty(toscaPropertyTwo);
        jpa2.setKey(new PfReferenceKey(jpa.getKey(), KEY2));

        assertThat(jpa.getProperties()).isEqualTo(Map.of(KEY1, jpa1, KEY2, jpa2));

        assertEquals(new PfConceptKey("world", "3.2.1"), jpa.getKey());
    }

    @Test
    void testValidateWithKey() {
        // null key - should fail
        jpa.setText("some text");
        assertThat(jpa.validateWithKey("fieldA").isValid()).isFalse();

        // valid
        jpa.setKey(new PfConceptKey("xyz", "2.3.4"));
        assertThat(jpa.validateWithKey("fieldB").isValid()).isTrue();

        // null text - bean validator should fail
        jpa.setText(null);
        assertThat(jpa.validateWithKey("fieldA").isValid()).isFalse();
    }

    @Test
    void testGetReferencedDataTypes() {
        assertThat(jpa.getReferencedDataTypes()).isEmpty();

        // one with a schema
        PfConceptKey schemaKey = new PfConceptKey("schemaZ", "9.8.7");
        JpaToscaSchemaDefinition schema = new JpaToscaSchemaDefinition();
        schema.setType(schemaKey);
        JpaToscaProperty prop1 = new JpaToscaProperty(JPA_PROP1);
        prop1.setType(CONCEPT_KEY1);
        prop1.setEntrySchema(schema);

        // one property without a schema
        JpaToscaProperty prop2 = new JpaToscaProperty(JPA_PROP2);
        prop2.setType(CONCEPT_KEY2);
        prop2.setEntrySchema(null);

        jpa.setProperties(Map.of(KEY1, prop1, KEY2, prop2));

        List<PfConceptKey> keys = new ArrayList<>(jpa.getReferencedDataTypes());
        Collections.sort(keys);

        assertThat(keys).isEqualTo(List.of(CONCEPT_KEY1, schemaKey, CONCEPT_KEY2));
    }


    protected static class MyTosca extends ToscaWithToscaProperties {

    }

    @Setter
    @Getter
    @NoArgsConstructor
    protected static class MyJpa extends JpaToscaWithToscaProperties<MyTosca> {
        @Serial
        private static final long serialVersionUID = 1L;

        @NotNull
        private String text;

        public MyJpa(MyJpa jpa) {
            super(jpa);
        }

        public MyJpa(PfConceptKey key) {
            super(key);
        }

        public MyJpa(MyTosca tosca) {
            super(tosca);
        }

        @Override
        public MyTosca toAuthorative() {
            this.setToscaEntity(new MyTosca());
            return super.toAuthorative();
        }
    }

    private static class MyJpa2 extends MyJpa {
        @Serial
        private static final long serialVersionUID = 1L;
    }
}
