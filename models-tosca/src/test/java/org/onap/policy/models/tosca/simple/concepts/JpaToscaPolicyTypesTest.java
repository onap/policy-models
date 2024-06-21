/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2021 Nordix Foundation.
 *  Modifications Copyright (C) 2019-2020 AT&T Intellectual Property. All rights reserved.
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
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyType;

class JpaToscaPolicyTypesTest {

    private static final String KEY_IS_NULL = "key is marked .*on.*ull but is null";

    @Test
    void testPolicyTypesNull() {
        assertNotNull(new JpaToscaPolicyTypes());
        assertNotNull(new JpaToscaPolicyTypes(new PfConceptKey()));
        assertNotNull(new JpaToscaPolicyTypes(new PfConceptKey(), new TreeMap<PfConceptKey, JpaToscaPolicyType>()));
        assertNotNull(new JpaToscaPolicyTypes(new JpaToscaPolicyTypes()));

        assertThatThrownBy(() -> new JpaToscaPolicyTypes((PfConceptKey) null)).hasMessageMatching(KEY_IS_NULL);

        assertThatThrownBy(() -> new JpaToscaPolicyTypes((JpaToscaPolicyTypes) null))
                .hasMessageMatching("copyConcept is marked .*on.*ull but is null");

        assertThatThrownBy(() -> new JpaToscaPolicyTypes(null, null)).hasMessageMatching(KEY_IS_NULL);

        assertThatThrownBy(() -> new JpaToscaPolicyTypes(new PfConceptKey(), null))
                .hasMessageMatching("conceptMap is marked .*on.*ull but is null");

        assertThatThrownBy(() -> new JpaToscaPolicyTypes(null, new TreeMap<PfConceptKey, JpaToscaPolicyType>()))
                .hasMessageMatching(KEY_IS_NULL);
    }

    @Test
    void testPolicyTypes() {
        List<Map<String, ToscaPolicyType>> ptMapList = new ArrayList<>();
        ptMapList.add(new LinkedHashMap<>());

        ToscaPolicyType pt0 = new ToscaPolicyType();
        pt0.setName("pt0");
        pt0.setVersion("0.0.1");
        pt0.setDescription("pt0 description");

        ptMapList.get(0).put("pt0", pt0);
        assertNotNull(new JpaToscaPolicyTypes(ptMapList));
        assertTrue(new JpaToscaPolicyTypes(ptMapList).validate("").isValid());
        assertThatThrownBy(() -> new JpaToscaPolicyTypes(ptMapList).validate(null))
                .hasMessageMatching("fieldName is marked .*on.*ull but is null");

        pt0.setDerivedFrom(null);
        assertTrue(new JpaToscaPolicyTypes(ptMapList).validate("").isValid());

        pt0.setDerivedFrom("tosca.PolicyTypes.Root");
        assertTrue(new JpaToscaPolicyTypes(ptMapList).validate("").isValid());

        pt0.setDerivedFrom("some.other.Thing");
        BeanValidationResult result = new JpaToscaPolicyTypes(ptMapList).validate("");
        assertFalse(result.isValid());
        assertThat(result.getResult()).contains("parent").contains("some.other.Thing:0.0.0")
                        .contains(Validated.NOT_FOUND);

        pt0.setDerivedFrom(null);
        assertTrue(new JpaToscaPolicyTypes(ptMapList).validate("").isValid());

        ToscaPolicyType pt1 = new ToscaPolicyType();
        pt1.setName("pt1");
        pt1.setVersion("0.0.1");
        pt1.setDescription("pt1 description");

        ptMapList.get(0).put("pt1", pt1);
        assertTrue(new JpaToscaPolicyTypes(ptMapList).validate("").isValid());

        pt1.setDerivedFrom("pt0");
        assertTrue(new JpaToscaPolicyTypes(ptMapList).validate("").isValid());

        pt1.setDerivedFrom("pt2");
        result = new JpaToscaPolicyTypes(ptMapList).validate("");
        assertFalse(result.isValid());
        assertThat(result.getResult()).contains("parent").contains("pt2:0.0.0").contains(Validated.NOT_FOUND);

        pt1.setDerivedFrom("pt0");
        assertTrue(new JpaToscaPolicyTypes(ptMapList).validate("").isValid());
    }
}
