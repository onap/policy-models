/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
 *  Modifications Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.TreeMap;
import org.junit.Test;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfReferenceKey;
import org.onap.policy.models.base.PfValidationResult;
import org.onap.policy.models.tosca.authorative.concepts.ToscaTopologyTemplate;

/**
 * DAO test for ToscaDatatype.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class JpaToscaTopologyTemplateTest {

    private static final String A_DESCRIPTION = "A Description";
    private static final String VERSION_001 = "0.0.1";

    @Test
    public void testTopologyTemplatePojo() {
        assertNotNull(new JpaToscaTopologyTemplate());
        assertNotNull(new JpaToscaTopologyTemplate(new PfReferenceKey()));
        assertNotNull(new JpaToscaTopologyTemplate(new JpaToscaTopologyTemplate()));
        assertNotNull(new JpaToscaTopologyTemplate(new ToscaTopologyTemplate()));

        assertThatThrownBy(() -> new JpaToscaTopologyTemplate((PfReferenceKey) null))
                        .hasMessage("key is marked @NonNull but is null");

        assertThatThrownBy(() -> new JpaToscaTopologyTemplate((JpaToscaTopologyTemplate) null))
                        .hasMessage("copyConcept is marked @NonNull but is null");

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
        assertEquals(ttt, tttClone0);
        assertEquals(0, ttt.compareTo(tttClone0));

        JpaToscaTopologyTemplate tttClone1 = new JpaToscaTopologyTemplate();
        ttt.copyTo(tttClone1);
        assertEquals(ttt, tttClone1);
        assertEquals(0, ttt.compareTo(tttClone1));

        assertEquals(-1, ttt.compareTo(null));
        assertEquals(0, ttt.compareTo(ttt));
        assertFalse(ttt.compareTo(ttt.getKey()) == 0);

        PfReferenceKey otherDtKey = new PfReferenceKey("otherSt", VERSION_001, "otherDt");
        JpaToscaTopologyTemplate otherDt = new JpaToscaTopologyTemplate(otherDtKey);

        assertFalse(ttt.compareTo(otherDt) == 0);
        otherDt.setKey(tttKey);
        assertFalse(ttt.compareTo(otherDt) == 0);
        otherDt.setDescription(A_DESCRIPTION);
        assertFalse(ttt.compareTo(otherDt) == 0);
        otherDt.setPolicies(policies);
        assertEquals(0, ttt.compareTo(otherDt));

        assertThatThrownBy(() -> ttt.copyTo(null)).hasMessage("target is marked @NonNull but is null");

        assertEquals(4, ttt.getKeys().size());
        assertEquals(1, new JpaToscaTopologyTemplate().getKeys().size());

        new JpaToscaTopologyTemplate().clean();
        ttt.clean();
        assertEquals(tttClone0, ttt);

        assertTrue(new JpaToscaTopologyTemplate().validate(new PfValidationResult()).isValid());
        assertTrue(ttt.validate(new PfValidationResult()).isValid());

        ttt.setKey(PfReferenceKey.getNullKey());
        assertFalse(ttt.validate(new PfValidationResult()).isValid());
        ttt.setKey(tttKey);
        assertTrue(ttt.validate(new PfValidationResult()).isValid());

        ttt.setDescription(null);
        assertTrue(ttt.validate(new PfValidationResult()).isValid());
        ttt.setDescription("");
        assertFalse(ttt.validate(new PfValidationResult()).isValid());
        ttt.setDescription(A_DESCRIPTION);
        assertTrue(ttt.validate(new PfValidationResult()).isValid());

        assertThatThrownBy(() -> ttt.validate(null)).hasMessage("resultIn is marked @NonNull but is null");
    }
}
