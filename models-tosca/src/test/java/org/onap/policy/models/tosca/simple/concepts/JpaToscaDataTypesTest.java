/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2024 Nordix Foundation.
 *  Modifications Copyright (C) 2019-2020 AT&T Intellectual Property. All rights reserved.
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.junit.jupiter.api.Test;
import org.onap.policy.common.parameters.BeanValidationResult;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.Validated;
import org.onap.policy.models.tosca.authorative.concepts.ToscaDataType;

class JpaToscaDataTypesTest {

    private static final String KEY_IS_NULL = "key is marked .*on.*ull but is null";

    @Test
    void testDataTypes() {
        assertNotNull(new JpaToscaDataTypes());
        assertNotNull(new JpaToscaDataTypes(new PfConceptKey()));
        assertNotNull(new JpaToscaDataTypes(new PfConceptKey(), new TreeMap<PfConceptKey, JpaToscaDataType>()));
        assertNotNull(new JpaToscaDataTypes(new JpaToscaDataTypes()));

        assertThatThrownBy(() -> new JpaToscaDataTypes((PfConceptKey) null)).hasMessageMatching(KEY_IS_NULL);

        assertThatThrownBy(() -> new JpaToscaDataTypes((JpaToscaDataTypes) null))
                .hasMessageMatching("copyConcept is marked .*on.*ull but is null");

        assertThatThrownBy(() -> new JpaToscaDataTypes(null, null)).hasMessageMatching(KEY_IS_NULL);

        assertThatThrownBy(() -> new JpaToscaDataTypes(new PfConceptKey(), null))
                .hasMessageMatching("conceptMap is marked .*on.*ull but is null");

        assertThatThrownBy(() -> new JpaToscaDataTypes(null, new TreeMap<PfConceptKey, JpaToscaDataType>()))
                .hasMessageMatching(KEY_IS_NULL);
    }

    @Test
    void testDerivedDataTypes() {
        List<Map<String, ToscaDataType>> dtMapList = new ArrayList<>();
        dtMapList.add(new LinkedHashMap<>());

        ToscaDataType dt0 = new ToscaDataType();
        dt0.setName("dt0");
        dt0.setVersion("0.0.1");
        dt0.setDescription("dt0 description");

        dtMapList.get(0).put("dt0", dt0);
        assertNotNull(new JpaToscaDataTypes(dtMapList));
        assertTrue(new JpaToscaDataTypes(dtMapList).validate("").isValid());
        assertThatThrownBy(() -> new JpaToscaDataTypes(dtMapList).validate(null))
                .hasMessageMatching("fieldName is marked .*on.*ull but is null");

        dt0.setDerivedFrom(null);
        assertTrue(new JpaToscaDataTypes(dtMapList).validate("").isValid());

        dt0.setDerivedFrom("tosca.datatypes.Root");
        assertTrue(new JpaToscaDataTypes(dtMapList).validate("").isValid());

        dt0.setDerivedFrom("some.other.Thing");
        BeanValidationResult result = new JpaToscaDataTypes(dtMapList).validate("");
        assertFalse(result.isValid());
        assertThat(result.getResult()).contains("parent").contains("some.other.Thing:0.0.0")
                        .contains(Validated.NOT_FOUND);

        dt0.setDerivedFrom(null);
        assertTrue(new JpaToscaDataTypes(dtMapList).validate("").isValid());

        ToscaDataType dt1 = new ToscaDataType();
        dt1.setName("dt1");
        dt1.setVersion("0.0.1");
        dt1.setDescription("dt1 description");

        dtMapList.get(0).put("dt1", dt1);
        assertTrue(new JpaToscaDataTypes(dtMapList).validate("").isValid());

        dt1.setDerivedFrom("dt0");
        assertTrue(new JpaToscaDataTypes(dtMapList).validate("").isValid());

        dt1.setDerivedFrom("dt2");
        result = new JpaToscaDataTypes(dtMapList).validate("");
        assertFalse(result.isValid());
        assertThat(result.getResult()).contains("parent").contains("dt2:0.0.0").contains(Validated.NOT_FOUND);

        dt1.setDerivedFrom("dt0");
        assertTrue(new JpaToscaDataTypes(dtMapList).validate("").isValid());
    }
}
