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
import org.onap.policy.models.base.PfKey;
import org.onap.policy.models.base.PfValidationResult;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaDataType;
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

    @Test
    public void testGetentityTypeAncestors() {
        assertThatThrownBy(() -> {
            ToscaUtils.getEntityTypeAncestors(null, null, null);
        }).hasMessageMatching("entityTypes is marked .*on.*ull but is null");

        assertThatThrownBy(() -> {
            ToscaUtils.getEntityTypeAncestors(null, null, new PfValidationResult());
        }).hasMessageMatching("entityTypes is marked .*on.*ull but is null");

        assertThatThrownBy(() -> {
            ToscaUtils.getEntityTypeAncestors(null, new JpaToscaDataType(), null);
        }).hasMessageMatching("entityTypes is marked .*on.*ull but is null");

        assertThatThrownBy(() -> {
            ToscaUtils.getEntityTypeAncestors(null, new JpaToscaDataType(), new PfValidationResult());
        }).hasMessageMatching("entityTypes is marked .*on.*ull but is null");

        assertThatThrownBy(() -> {
            ToscaUtils.getEntityTypeAncestors(new JpaToscaDataTypes(), null, null);
        }).hasMessageMatching("entityType is marked .*on.*ull but is null");

        assertThatThrownBy(() -> {
            ToscaUtils.getEntityTypeAncestors(new JpaToscaDataTypes(), null, new PfValidationResult());
        }).hasMessageMatching("entityType is marked .*on.*ull but is null");

        assertThatThrownBy(() -> {
            ToscaUtils.getEntityTypeAncestors(new JpaToscaDataTypes(), new JpaToscaDataType(), null);
        }).hasMessageMatching("result is marked .*on.*ull but is null");

        JpaToscaDataTypes dataTypes = new JpaToscaDataTypes();
        JpaToscaDataType dt0 = new JpaToscaDataType();
        dt0.setKey(new PfConceptKey("dt0", "0.0.1"));
        dt0.setDescription("dt0 description");
        PfValidationResult result = new PfValidationResult();

        assertTrue(ToscaUtils.getEntityTypeAncestors(dataTypes, dt0, result).isEmpty());

        dataTypes.getConceptMap().put(dt0.getKey(), dt0);
        assertTrue(ToscaUtils.getEntityTypeAncestors(dataTypes, dt0, result).isEmpty());
        assertTrue(result.isValid());

        dt0.setDerivedFrom(null);
        assertTrue(ToscaUtils.getEntityTypeAncestors(dataTypes, dt0, result).isEmpty());
        assertTrue(result.isValid());

        dt0.setDerivedFrom(new PfConceptKey("tosca.datatyps.Root", PfKey.NULL_KEY_VERSION));
        assertTrue(ToscaUtils.getEntityTypeAncestors(dataTypes, dt0, result).isEmpty());
        assertTrue(result.isValid());

        dt0.setDerivedFrom(new PfConceptKey("some.thing.Else", PfKey.NULL_KEY_VERSION));
        assertTrue(ToscaUtils.getEntityTypeAncestors(dataTypes, dt0, result).isEmpty());
        assertFalse(result.isValid());
        assertTrue(result.toString().contains("parent some.thing.Else:0.0.0 of entity not found"));

        result = new PfValidationResult();
        dt0.setDerivedFrom(new PfConceptKey("tosca.datatyps.Root", PfKey.NULL_KEY_VERSION));

        JpaToscaDataType dt1 = new JpaToscaDataType();
        dt1.setKey(new PfConceptKey("dt1", "0.0.1"));
        dt1.setDescription("dt1 description");
        dataTypes.getConceptMap().put(dt1.getKey(), dt1);
        assertTrue(ToscaUtils.getEntityTypeAncestors(dataTypes, dt0, result).isEmpty());
        assertTrue(ToscaUtils.getEntityTypeAncestors(dataTypes, dt1, result).isEmpty());
        assertTrue(result.isValid());

        dt1.setDerivedFrom(dt0.getKey());
        assertTrue(ToscaUtils.getEntityTypeAncestors(dataTypes, dt0, result).isEmpty());
        assertFalse(ToscaUtils.getEntityTypeAncestors(dataTypes, dt1, result).isEmpty());
        assertEquals(1, ToscaUtils.getEntityTypeAncestors(dataTypes, dt1, result).size());
        assertTrue(result.isValid());

        JpaToscaDataType dt2 = new JpaToscaDataType();
        dt2.setKey(new PfConceptKey("dt2", "0.0.1"));
        dt2.setDescription("dt2 description");
        dataTypes.getConceptMap().put(dt2.getKey(), dt2);
        assertTrue(ToscaUtils.getEntityTypeAncestors(dataTypes, dt0, result).isEmpty());
        assertFalse(ToscaUtils.getEntityTypeAncestors(dataTypes, dt1, result).isEmpty());
        assertEquals(1, ToscaUtils.getEntityTypeAncestors(dataTypes, dt1, result).size());
        assertTrue(result.isValid());

        dt2.setDerivedFrom(dt1.getKey());
        assertTrue(ToscaUtils.getEntityTypeAncestors(dataTypes, dt0, result).isEmpty());
        assertFalse(ToscaUtils.getEntityTypeAncestors(dataTypes, dt1, result).isEmpty());
        assertFalse(ToscaUtils.getEntityTypeAncestors(dataTypes, dt2, result).isEmpty());
        assertEquals(1, ToscaUtils.getEntityTypeAncestors(dataTypes, dt1, result).size());
        assertEquals(2, ToscaUtils.getEntityTypeAncestors(dataTypes, dt2, result).size());
        assertTrue(result.isValid());

        dt1.setDerivedFrom(new PfConceptKey("tosca.datatyps.Root", PfKey.NULL_KEY_VERSION));
        assertTrue(ToscaUtils.getEntityTypeAncestors(dataTypes, dt0, result).isEmpty());
        assertTrue(ToscaUtils.getEntityTypeAncestors(dataTypes, dt1, result).isEmpty());
        assertFalse(ToscaUtils.getEntityTypeAncestors(dataTypes, dt2, result).isEmpty());
        assertEquals(0, ToscaUtils.getEntityTypeAncestors(dataTypes, dt1, result).size());
        assertEquals(1, ToscaUtils.getEntityTypeAncestors(dataTypes, dt2, result).size());
        assertTrue(result.isValid());

        dataTypes.getConceptMap().remove(dt1.getKey());
        assertTrue(ToscaUtils.getEntityTypeAncestors(dataTypes, dt2, result).isEmpty());
        assertFalse(result.isValid());
        assertTrue(result.toString().contains("parent dt1:0.0.1 of entity not found"));
    }
}
