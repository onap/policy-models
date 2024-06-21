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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.TreeMap;
import org.junit.jupiter.api.Test;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfReferenceKey;
import org.onap.policy.models.tosca.authorative.concepts.ToscaTopologyTemplate;

/**
 * DAO test for ToscaDatatype.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
class JpaToscaTopologyTemplateTest {

    private static final String A_DESCRIPTION = "A Description";
    private static final String VERSION_001 = "0.0.1";

    @Test
    void testTopologyTemplatePojo() {
        assertNotNull(new JpaToscaTopologyTemplate());
        assertNotNull(new JpaToscaTopologyTemplate(new PfReferenceKey()));
        assertNotNull(new JpaToscaTopologyTemplate(new JpaToscaTopologyTemplate()));
        assertNotNull(new JpaToscaTopologyTemplate(new ToscaTopologyTemplate()));

        assertThatThrownBy(() -> new JpaToscaTopologyTemplate((PfReferenceKey) null))
                .hasMessageMatching("key is marked .*on.*ull but is null");

        assertThatThrownBy(() -> new JpaToscaTopologyTemplate((JpaToscaTopologyTemplate) null))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new JpaToscaTopologyTemplate((JpaToscaTopologyTemplate) null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void testTopologyTemplates() {
        PfReferenceKey tttKey = new PfReferenceKey("tst", VERSION_001, "ttt");
        JpaToscaTopologyTemplate ttt = new JpaToscaTopologyTemplate(tttKey);

        ttt.setDescription(A_DESCRIPTION);
        assertEquals(A_DESCRIPTION, ttt.getDescription());

        PfConceptKey policy0TypeKey = new PfConceptKey("Policy0Type", VERSION_001);
        PfConceptKey policy0Key = new PfConceptKey("Policy0", VERSION_001);

        JpaToscaPolicy policy0 = new JpaToscaPolicy(policy0Key, policy0TypeKey);
        PfConceptKey polsKey = new PfConceptKey("pols", VERSION_001);
        Map<PfConceptKey, JpaToscaPolicy> policyMap = new TreeMap<>();
        policyMap.put(policy0Key, policy0);
        JpaToscaPolicies policies = new JpaToscaPolicies(polsKey, policyMap);
        ttt.setPolicies(policies);

        JpaToscaTopologyTemplate tttClone0 = new JpaToscaTopologyTemplate(ttt);
        checkEqualsTopologyTemplate(ttt, tttClone0);

        JpaToscaTopologyTemplate tttClone1 = new JpaToscaTopologyTemplate(ttt);
        checkEqualsTopologyTemplate(ttt, tttClone1);

        assertEquals(-1, ttt.compareTo(null));
        assertEquals(0, ttt.compareTo(ttt));
        assertNotEquals(0, ttt.compareTo(ttt.getKey()));

        PfReferenceKey otherDtKey = new PfReferenceKey("otherSt", VERSION_001, "otherDt");
        JpaToscaTopologyTemplate otherDt = new JpaToscaTopologyTemplate(otherDtKey);

        assertNotEquals(0, ttt.compareTo(otherDt));
        otherDt.setKey(tttKey);
        assertNotEquals(0, ttt.compareTo(otherDt));
        otherDt.setDescription(A_DESCRIPTION);
        assertNotEquals(0, ttt.compareTo(otherDt));
        otherDt.setPolicies(policies);
        assertEquals(0, ttt.compareTo(otherDt));

        assertEquals(4, ttt.getKeys().size());
        assertEquals(1, new JpaToscaTopologyTemplate().getKeys().size());
    }

    @Test
    void testTopologyTemplateValidation() {
        PfReferenceKey tttKey = new PfReferenceKey("tst", VERSION_001, "ttt");
        JpaToscaTopologyTemplate ttt = new JpaToscaTopologyTemplate(tttKey);

        JpaToscaTopologyTemplate tttClone0 = new JpaToscaTopologyTemplate(ttt);

        new JpaToscaTopologyTemplate().clean();
        ttt.clean();
        assertEquals(tttClone0, ttt);

        assertTrue(new JpaToscaTopologyTemplate().validate("").isValid());
        assertTrue(ttt.validate("").isValid());

        ttt.setKey(PfReferenceKey.getNullKey());
        assertFalse(ttt.validate("").isValid());
        ttt.setKey(tttKey);
        assertTrue(ttt.validate("").isValid());

        ttt.setDescription(null);
        assertTrue(ttt.validate("").isValid());
        ttt.setDescription("");
        assertFalse(ttt.validate("").isValid());
        ttt.setDescription(A_DESCRIPTION);
        assertTrue(ttt.validate("").isValid());

        assertThatThrownBy(() -> ttt.validate(null)).hasMessageMatching("fieldName is marked .*on.*ull but is null");
    }

    private void checkEqualsTopologyTemplate(JpaToscaTopologyTemplate ttt1, JpaToscaTopologyTemplate ttt2) {
        assertEquals(ttt1, ttt2);
        assertEquals(0, ttt1.compareTo(ttt2));
    }
}
