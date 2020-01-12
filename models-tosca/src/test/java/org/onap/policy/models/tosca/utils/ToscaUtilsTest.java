/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2020 Nordix Foundation.
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

package org.onap.policy.models.tosca.utils;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaDataTypes;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaPolicies;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaPolicyTypes;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaServiceTemplate;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaTopologyTemplate;

/**
 * Import the {@link ToscaUtils} class.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class ToscaUtilsTest {

    @Test
    public void testAssertDataTypes() {
        JpaToscaServiceTemplate jpaToscaServiceTemplate = new JpaToscaServiceTemplate();

        assertFalse(ToscaUtils.doDataTypesExist(jpaToscaServiceTemplate));
        assertEquals("no data types specified on service template",
                ToscaUtils.checkDataTypesExist(jpaToscaServiceTemplate));
        assertThatThrownBy(() -> {
            ToscaUtils.assertDataTypesExist(jpaToscaServiceTemplate);
        }).hasMessage("no data types specified on service template");

        jpaToscaServiceTemplate.setDataTypes(new JpaToscaDataTypes());

        assertFalse(ToscaUtils.doDataTypesExist(jpaToscaServiceTemplate));
        assertEquals("list of data types specified on service template is empty",
                ToscaUtils.checkDataTypesExist(jpaToscaServiceTemplate));
        assertThatThrownBy(() -> {
            ToscaUtils.assertDataTypesExist(jpaToscaServiceTemplate);
        }).hasMessage("list of data types specified on service template is empty");

        jpaToscaServiceTemplate.getDataTypes().getConceptMap().put(new PfConceptKey(), null);

        assertTrue(ToscaUtils.doDataTypesExist(jpaToscaServiceTemplate));
        assertEquals(null, ToscaUtils.checkDataTypesExist(jpaToscaServiceTemplate));
        assertThatCode(() -> {
            ToscaUtils.assertDataTypesExist(jpaToscaServiceTemplate);
        }).doesNotThrowAnyException();

    }

    @Test
    public void testAssertPolicyTypes() {
        JpaToscaServiceTemplate jpaToscaServiceTemplate = new JpaToscaServiceTemplate();

        assertFalse(ToscaUtils.doPolicyTypesExist(jpaToscaServiceTemplate));
        assertEquals("no policy types specified on service template",
                ToscaUtils.checkPolicyTypesExist(jpaToscaServiceTemplate));
        assertThatThrownBy(() -> {
            ToscaUtils.assertPolicyTypesExist(jpaToscaServiceTemplate);
        }).hasMessage("no policy types specified on service template");

        jpaToscaServiceTemplate.setPolicyTypes(new JpaToscaPolicyTypes());

        assertFalse(ToscaUtils.doPolicyTypesExist(jpaToscaServiceTemplate));
        assertEquals("list of policy types specified on service template is empty",
                ToscaUtils.checkPolicyTypesExist(jpaToscaServiceTemplate));
        assertThatThrownBy(() -> {
            ToscaUtils.assertPolicyTypesExist(jpaToscaServiceTemplate);
        }).hasMessage("list of policy types specified on service template is empty");

        jpaToscaServiceTemplate.getPolicyTypes().getConceptMap().put(new PfConceptKey(), null);

        assertTrue(ToscaUtils.doPolicyTypesExist(jpaToscaServiceTemplate));
        assertEquals(null, ToscaUtils.checkPolicyTypesExist(jpaToscaServiceTemplate));
        assertThatCode(() -> {
            ToscaUtils.assertPolicyTypesExist(jpaToscaServiceTemplate);
        }).doesNotThrowAnyException();
    }

    @Test
    public void testAssertPolicies() {
        JpaToscaServiceTemplate jpaToscaServiceTemplate = new JpaToscaServiceTemplate();

        assertFalse(ToscaUtils.doPoliciesExist(jpaToscaServiceTemplate));
        assertEquals("topology template not specified on service template",
                ToscaUtils.checkPoliciesExist(jpaToscaServiceTemplate));
        assertThatThrownBy(() -> {
            ToscaUtils.assertPoliciesExist(jpaToscaServiceTemplate);
        }).hasMessage("topology template not specified on service template");

        jpaToscaServiceTemplate.setTopologyTemplate(new JpaToscaTopologyTemplate());

        assertFalse(ToscaUtils.doPoliciesExist(jpaToscaServiceTemplate));
        assertEquals("no policies specified on topology template of service template",
                ToscaUtils.checkPoliciesExist(jpaToscaServiceTemplate));
        assertThatThrownBy(() -> {
            ToscaUtils.assertPoliciesExist(jpaToscaServiceTemplate);
        }).hasMessage("no policies specified on topology template of service template");

        jpaToscaServiceTemplate.getTopologyTemplate().setPolicies(new JpaToscaPolicies());

        assertFalse(ToscaUtils.doPoliciesExist(jpaToscaServiceTemplate));
        assertEquals("list of policies specified on topology template of service template is empty",
                ToscaUtils.checkPoliciesExist(jpaToscaServiceTemplate));
        assertThatThrownBy(() -> {
            ToscaUtils.assertPoliciesExist(jpaToscaServiceTemplate);
        }).hasMessage("list of policies specified on topology template of service template is empty");

        jpaToscaServiceTemplate.getTopologyTemplate().getPolicies().getConceptMap().put(new PfConceptKey(), null);

        assertTrue(ToscaUtils.doPoliciesExist(jpaToscaServiceTemplate));
        assertEquals(null, ToscaUtils.checkPoliciesExist(jpaToscaServiceTemplate));
        assertThatCode(() -> {
            ToscaUtils.assertPoliciesExist(jpaToscaServiceTemplate);
        }).doesNotThrowAnyException();
    }
}
