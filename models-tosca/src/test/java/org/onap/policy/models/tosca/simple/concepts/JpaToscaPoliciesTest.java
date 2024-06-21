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
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicy;

class JpaToscaPoliciesTest {

    private static final String KEY_IS_NULL = "key is marked .*on.*ull but is null";

    @Test
    void testPoliciesErrors() {
        assertNotNull(new JpaToscaPolicies());
        assertNotNull(new JpaToscaPolicies(new PfConceptKey()));
        assertNotNull(new JpaToscaPolicies(new PfConceptKey(), new TreeMap<PfConceptKey, JpaToscaPolicy>()));
        assertNotNull(new JpaToscaPolicies(new JpaToscaPolicies()));

        assertThatThrownBy(() -> new JpaToscaPolicies((PfConceptKey) null)).hasMessageMatching(KEY_IS_NULL);

        assertThatThrownBy(() -> new JpaToscaPolicies((JpaToscaPolicies) null))
                .hasMessageMatching("copyConcept is marked .*on.*ull but is null");

        assertThatThrownBy(() -> new JpaToscaPolicies(null, null)).hasMessageMatching(KEY_IS_NULL);

        assertThatThrownBy(() -> new JpaToscaPolicies(new PfConceptKey(), null))
                .hasMessageMatching("conceptMap is marked .*on.*ull but is null");

        assertThatThrownBy(() -> new JpaToscaPolicies(null, new TreeMap<PfConceptKey, JpaToscaPolicy>()))
                .hasMessageMatching(KEY_IS_NULL);
    }

    @Test
    void testToscaPolicies() {
        List<Map<String, ToscaPolicy>> polMapList = new ArrayList<>();
        polMapList.add(new LinkedHashMap<>());

        ToscaPolicy pol0 = new ToscaPolicy();
        pol0.setName("pol0");
        pol0.setVersion("0.0.1");
        pol0.setDescription("pol0 description");
        pol0.setType("pt0");
        pol0.setTypeVersion("0.0.1");

        polMapList.get(0).put("pol0", pol0);
        assertNotNull(new JpaToscaPolicies(polMapList));
        assertTrue(new JpaToscaPolicies(polMapList).validate("").isValid());
        assertThatThrownBy(() -> new JpaToscaPolicies(polMapList).validate(null))
                .hasMessageMatching("fieldName is marked .*on.*ull but is null");

        pol0.setDerivedFrom(null);
        assertTrue(new JpaToscaPolicies(polMapList).validate("").isValid());

        pol0.setDerivedFrom("tosca.Policies.Root");
        assertTrue(new JpaToscaPolicies(polMapList).validate("").isValid());

        pol0.setDerivedFrom("some.other.Thing");
        BeanValidationResult result = new JpaToscaPolicies(polMapList).validate("");
        assertFalse(result.isValid());
        assertThat(result.getResult()).contains("parent").contains("some.other.Thing:0.0.0")
                        .contains(Validated.NOT_FOUND);

        pol0.setDerivedFrom(null);
        assertTrue(new JpaToscaPolicies(polMapList).validate("").isValid());

        ToscaPolicy pol1 = new ToscaPolicy();
        pol1.setName("pol1");
        pol1.setVersion("0.0.1");
        pol1.setDescription("pol1 description");
        pol1.setType("pt0");
        pol1.setTypeVersion("0.0.1");

        polMapList.get(0).put("pol1", pol1);
        assertTrue(new JpaToscaPolicies(polMapList).validate("").isValid());

        pol1.setDerivedFrom("pol0");
        assertTrue(new JpaToscaPolicies(polMapList).validate("").isValid());

        pol1.setDerivedFrom("pol2");
        result = new JpaToscaPolicies(polMapList).validate("");
        assertFalse(result.isValid());
        assertThat(result.getResult()).contains("parent").contains("pol2:0.0.0").contains(Validated.NOT_FOUND);

        pol1.setDerivedFrom("pol0");
        assertTrue(new JpaToscaPolicies(polMapList).validate("").isValid());
    }
}
