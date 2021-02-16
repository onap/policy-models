/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2021 AT&T Intellectual Property. All rights reserved.
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
import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.parameters.annotations.NotNull;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.tosca.authorative.concepts.ToscaWithObjectProperties;

public class JpaToscaWithStringPropertiesTest {
    private static final String SOME_DESCRIPTION = "some description";
    private static final String KEY1 = "abc";
    private static final String KEY2 = "def";
    private static final String STRING1 = "10";
    private static final String STRING2 = "20";
    private static final int INT1 = 10;
    private static final int INT2 = 20;

    private MyJpa jpa;

    @Before
    public void setUp() {
        jpa = new MyJpa();
    }

    @Test
    public void testGetKeys() {
        PfConceptKey key = new PfConceptKey("bye", "9.8.7");

        jpa = new MyJpa(key);
        jpa.setDescription(SOME_DESCRIPTION);
        jpa.setProperties(Map.of(KEY1, STRING1, KEY2, STRING2));

        // properties should be ignored
        assertThat(jpa.getKeys()).isEqualTo(List.of(key));
    }

    @Test
    public void testClean() {
        jpa.setDescription("  some description  ");
        jpa.setProperties(Map.of(KEY1, "10 ", KEY2, " 20"));

        jpa.clean();
        assertEquals(SOME_DESCRIPTION, jpa.getDescription());
        assertThat(jpa.getProperties()).isEqualTo(Map.of(KEY1, STRING1, KEY2, STRING2));
    }

    @Test
    public void testToAuthorative() {
        jpa.setDescription(SOME_DESCRIPTION);
        jpa.setProperties(Map.of(KEY1, STRING1, KEY2, STRING2));

        MyTosca tosca = jpa.toAuthorative();
        assertEquals(SOME_DESCRIPTION, tosca.getDescription());
        assertThat(tosca.getProperties()).isEqualTo(Map.of(KEY1, INT1, KEY2, INT2));
    }

    @Test
    public void testFromAuthorative() {
        MyTosca tosca = new MyTosca();
        tosca.setDescription(SOME_DESCRIPTION);

        jpa.fromAuthorative(tosca);
        assertEquals(SOME_DESCRIPTION, jpa.getDescription());
        assertThat(jpa.getProperties()).isNull();

        tosca.setProperties(Map.of(KEY1, INT1, KEY2, INT2));

        jpa = new MyJpa();
        jpa.fromAuthorative(tosca);
        assertEquals(SOME_DESCRIPTION, jpa.getDescription());
        assertThat(jpa.getProperties()).isEqualTo(Map.of(KEY1, STRING1, KEY2, STRING2));
    }

    @Test
    public void testCompareTo() {
        jpa.setDescription(SOME_DESCRIPTION);
        jpa.setProperties(Map.of(KEY1, STRING1, KEY2, STRING2));

        assertThat(jpa.compareTo(null)).isNegative();
        assertThat(jpa.compareTo(jpa)).isZero();
        assertThat(jpa.compareTo(new PfConceptKey())).isNotZero();

        MyJpa jpa2 = new MyJpa();
        jpa2.setDescription(SOME_DESCRIPTION);
        jpa2.setProperties(Map.of(KEY1, STRING1, KEY2, STRING2));
        assertThat(jpa.compareTo(jpa2)).isZero();

        jpa2.setProperties(Map.of(KEY1, STRING1));
        assertThat(jpa.compareTo(jpa2)).isNotZero();
    }

    @Test
    public void testJpaToscaWithStringProperties() {
        assertThat(jpa.getProperties()).isNull();
        assertThat(jpa.getKey().isNullKey()).isTrue();

    }

    @Test
    public void testJpaToscaWithStringPropertiesPfConceptKey() {
        PfConceptKey key = new PfConceptKey("hello", "1.2.3");

        jpa = new MyJpa(key);
        assertEquals(key, jpa.getKey());
    }

    @Test
    public void testJpaToscaWithStringPropertiesJpaToscaWithStringPropertiesOfT() {
        jpa.setDescription(SOME_DESCRIPTION);
        assertEquals(jpa, new MyJpa(jpa));

        jpa.setProperties(Map.of(KEY1, STRING1, KEY2, STRING2));
        assertEquals(jpa, new MyJpa(jpa));
    }

    @Test
    public void testJpaToscaWithStringPropertiesT() {
        MyTosca tosca = new MyTosca();
        tosca.setName("world");
        tosca.setVersion("3.2.1");
        tosca.setDescription(SOME_DESCRIPTION);
        tosca.setProperties(Map.of(KEY1, INT1, KEY2, INT2));

        jpa = new MyJpa(tosca);
        assertEquals(SOME_DESCRIPTION, jpa.getDescription());
        assertThat(jpa.getProperties()).isEqualTo(Map.of(KEY1, STRING1, KEY2, STRING2));
        assertEquals(new PfConceptKey("world", "3.2.1"), jpa.getKey());
    }

    @Test
    public void testValidateWithKey() {
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

    private static class MyTosca extends ToscaWithObjectProperties {

    }

    @NoArgsConstructor
    protected static class MyJpa extends JpaToscaWithStringProperties<MyTosca> {
        private static final long serialVersionUID = 1L;

        @NotNull
        @Getter
        @Setter
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

        @Override
        protected Object deserializePropertyValue(String propValue) {
            return Integer.parseInt(propValue);
        }

        @Override
        protected String serializePropertyValue(Object propValue) {
            return propValue.toString();
        }
    }
}
