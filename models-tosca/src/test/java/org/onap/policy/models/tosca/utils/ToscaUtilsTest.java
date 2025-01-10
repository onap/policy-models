/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2021, 2025 Nordix Foundation.
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

package org.onap.policy.models.tosca.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.onap.policy.common.parameters.BeanValidationResult;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfKey;
import org.onap.policy.models.base.Validated;
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
class ToscaUtilsTest {

    @Test
    void testAssertDataTypes() {
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
        assertNull(ToscaUtils.checkDataTypesExist(jpaToscaServiceTemplate));
        assertThatCode(() -> ToscaUtils.assertDataTypesExist(jpaToscaServiceTemplate)).doesNotThrowAnyException();

    }

    @Test
    void testAssertPolicyTypes() {
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
        assertNull(ToscaUtils.checkPolicyTypesExist(jpaToscaServiceTemplate));
        assertThatCode(() -> ToscaUtils.assertPolicyTypesExist(jpaToscaServiceTemplate)).doesNotThrowAnyException();
    }

    @Test
    void testAssertPolicies() {
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
        assertNull(ToscaUtils.checkPoliciesExist(jpaToscaServiceTemplate));
        assertThatCode(() -> ToscaUtils.assertPoliciesExist(jpaToscaServiceTemplate)).doesNotThrowAnyException();
    }

    @Test
    void testGetEntityTypeAncestors() {
        assertThatThrownBy(() -> {
            ToscaUtils.getEntityTypeAncestors(null, null, null);
        }).hasMessageMatching("entityTypes is marked .*on.*ull but is null");

        assertThatThrownBy(() -> {
            ToscaUtils.getEntityTypeAncestors(null, null, new BeanValidationResult("", null));
        }).hasMessageMatching("entityTypes is marked .*on.*ull but is null");

        assertThatThrownBy(() -> {
            ToscaUtils.getEntityTypeAncestors(null, new JpaToscaDataType(), null);
        }).hasMessageMatching("entityTypes is marked .*on.*ull but is null");

        assertThatThrownBy(() -> {
            ToscaUtils.getEntityTypeAncestors(null, new JpaToscaDataType(), new BeanValidationResult("", null));
        }).hasMessageMatching("entityTypes is marked .*on.*ull but is null");

        assertThatThrownBy(() -> {
            ToscaUtils.getEntityTypeAncestors(new JpaToscaDataTypes(), null, null);
        }).hasMessageMatching("entityType is marked .*on.*ull but is null");

        assertThatThrownBy(() -> {
            ToscaUtils.getEntityTypeAncestors(new JpaToscaDataTypes(), null, new BeanValidationResult("", null));
        }).hasMessageMatching("entityType is marked .*on.*ull but is null");

        assertThatThrownBy(() -> {
            ToscaUtils.getEntityTypeAncestors(new JpaToscaDataTypes(), new JpaToscaDataType(), null);
        }).hasMessageMatching("result is marked .*on.*ull but is null");
    }

    @Test
    void testGetentityTypeAncestorsDataType() {

        JpaToscaDataTypes dataTypes = new JpaToscaDataTypes();
        JpaToscaDataType dt0 = new JpaToscaDataType();
        dt0.setKey(new PfConceptKey("dt0", "0.0.1"));
        dt0.setDescription("dt0 description");
        BeanValidationResult result = new BeanValidationResult("", null);

        assertThat(ToscaUtils.getEntityTypeAncestors(dataTypes, dt0, result)).isEmpty();

        dataTypes.getConceptMap().put(dt0.getKey(), dt0);
        checkSingleEmptyEntityTypeAncestor(dataTypes, dt0, result);

        dt0.setDerivedFrom(null);
        checkSingleEmptyEntityTypeAncestor(dataTypes, dt0, result);

        dt0.setDerivedFrom(new PfConceptKey("tosca.datatyps.Root", PfKey.NULL_KEY_VERSION));
        checkSingleEmptyEntityTypeAncestor(dataTypes, dt0, result);

        dt0.setDerivedFrom(new PfConceptKey("some.thing.Else", PfKey.NULL_KEY_VERSION));
        assertThat(ToscaUtils.getEntityTypeAncestors(dataTypes, dt0, result)).isEmpty();
        assertFalse(result.isValid());
        assertThat(result.getResult()).contains("parent").contains("some.thing.Else:0.0.0")
                        .contains(Validated.NOT_FOUND);

        result = new BeanValidationResult("", null);
        dt0.setDerivedFrom(new PfConceptKey("tosca.datatyps.Root", PfKey.NULL_KEY_VERSION));

        JpaToscaDataType dt1 = new JpaToscaDataType();
        dt1.setKey(new PfConceptKey("dt1", "0.0.1"));
        dt1.setDescription("dt1 description");
        dataTypes.getConceptMap().put(dt1.getKey(), dt1);
        checkSingleEmptyEntityTypeAncestor(dataTypes, dt0, result);
        checkSingleEmptyEntityTypeAncestor(dataTypes, dt1, result);

        dt1.setDerivedFrom(dt0.getKey());
        checkMultipleEmptyEntityTypeAncestors(dataTypes, dt0, dt1, result, 1);

        JpaToscaDataType dt2 = new JpaToscaDataType();
        dt2.setKey(new PfConceptKey("dt2", "0.0.1"));
        dt2.setDescription("dt2 description");
        dataTypes.getConceptMap().put(dt2.getKey(), dt2);
        checkMultipleEmptyEntityTypeAncestors(dataTypes, dt0, dt1, result, 1);

        dt2.setDerivedFrom(dt1.getKey());
        checkMultipleEmptyEntityTypeAncestors(dataTypes, dt0, dt1, result, 1);
        checkMultipleEmptyEntityTypeAncestors(dataTypes, dt0, dt2, result, 2);

        dt0.setDerivedFrom(dt0.getKey());
        assertThatThrownBy(() -> {
            ToscaUtils.getEntityTypeAncestors(dataTypes, dt0, new BeanValidationResult("", null));
        }).hasMessageContaining("entity type").hasMessageContaining("ancestor of itself");

        dt0.setDerivedFrom(null);
        assertEquals(2, ToscaUtils.getEntityTypeAncestors(dataTypes, dt2, result).size());

        dt1.setDerivedFrom(new PfConceptKey("tosca.datatyps.Root", PfKey.NULL_KEY_VERSION));
        checkSingleEmptyEntityTypeAncestor(dataTypes, dt0, result);
        checkMultipleEmptyEntityTypeAncestors(dataTypes, dt1, dt2, result);

        dataTypes.getConceptMap().remove(dt1.getKey());
        assertThat(ToscaUtils.getEntityTypeAncestors(dataTypes, dt2, result)).isEmpty();
        assertFalse(result.isValid());
        assertThat(result.getResult()).contains("parent").contains("dt1:0.0.1").contains(Validated.NOT_FOUND);
    }

    private void checkSingleEmptyEntityTypeAncestor(JpaToscaDataTypes dataTypes, JpaToscaDataType emptydt,
            BeanValidationResult result) {
        assertThat(ToscaUtils.getEntityTypeAncestors(dataTypes, emptydt, result)).isEmpty();
        assertTrue(result.isValid());
    }

    private void checkMultipleEmptyEntityTypeAncestors(JpaToscaDataTypes dataTypes, JpaToscaDataType emptydt,
            JpaToscaDataType notemptydt, BeanValidationResult result, int size1) {
        assertThat(ToscaUtils.getEntityTypeAncestors(dataTypes, emptydt, result)).isEmpty();
        assertFalse(ToscaUtils.getEntityTypeAncestors(dataTypes, notemptydt, result).isEmpty());
        assertEquals(size1, ToscaUtils.getEntityTypeAncestors(dataTypes, notemptydt, result).size());
        assertTrue(result.isValid());
    }

    private void checkMultipleEmptyEntityTypeAncestors(JpaToscaDataTypes dataTypes, JpaToscaDataType emptydt,
            JpaToscaDataType notemptydt, BeanValidationResult result) {
        assertThat(ToscaUtils.getEntityTypeAncestors(dataTypes, emptydt, result)).isEmpty();
        assertFalse(ToscaUtils.getEntityTypeAncestors(dataTypes, notemptydt, result).isEmpty());
        assertEquals(1, ToscaUtils.getEntityTypeAncestors(dataTypes, notemptydt, result).size());
        assertEquals(0, ToscaUtils.getEntityTypeAncestors(dataTypes, emptydt, result).size());
        assertTrue(result.isValid());
    }

    @Test
    void testGetPredefinedDataTypes() {
        assertTrue(ToscaUtils.getPredefinedDataTypes().contains(new PfConceptKey("string", PfKey.NULL_KEY_VERSION)));
    }

    @Test
    void testGetEntityTree() {
        assertThatThrownBy(() -> {
            ToscaUtils.getEntityTree(null, null, null);
        }).hasMessageMatching("entityTypes is marked .*on.*ull but is null");

        JpaToscaDataTypes dataTypes = new JpaToscaDataTypes(new PfConceptKey("datatypes", "0.0.1"));
        JpaToscaDataTypes filteredDataTypes = new JpaToscaDataTypes(new PfConceptKey("datatypes", "0.0.1"));
        ToscaUtils.getEntityTree(filteredDataTypes, "IDontExist", "0.0.0");
        assertEquals(dataTypes, filteredDataTypes);

        JpaToscaDataType dt0 = new JpaToscaDataType(new PfConceptKey("dt0", "0.0.1"));
        dataTypes.getConceptMap().put(dt0.getKey(), dt0);
        filteredDataTypes.getConceptMap().put(dt0.getKey(), dt0);
        ToscaUtils.getEntityTree(filteredDataTypes, "IDontExist", "0.0.0");
        assertNotEquals(dataTypes, filteredDataTypes);
        assertThat(filteredDataTypes.getConceptMap()).isEmpty();

        filteredDataTypes.getConceptMap().put(dt0.getKey(), dt0);
        ToscaUtils.getEntityTree(filteredDataTypes, dt0.getKey().getName(), dt0.getKey().getVersion());
        assertEquals(dataTypes, filteredDataTypes);

        JpaToscaDataType dt1 = new JpaToscaDataType(new PfConceptKey("dt1", "0.0.1"));
        dt1.setDerivedFrom(dt0.getKey());

        JpaToscaDataType dt2 = new JpaToscaDataType(new PfConceptKey("dt2", "0.0.1"));
        dt2.setDerivedFrom(dt0.getKey());

        JpaToscaDataType dt3 = new JpaToscaDataType(new PfConceptKey("dt3", "0.0.1"));
        dt3.setDerivedFrom(dt0.getKey());

        JpaToscaDataType dt4 = new JpaToscaDataType(new PfConceptKey("dt4", "0.0.1"));
        dt4.setDerivedFrom(dt3.getKey());

        JpaToscaDataType dt5 = new JpaToscaDataType(new PfConceptKey("dt5", "0.0.1"));
        dt5.setDerivedFrom(dt4.getKey());

        JpaToscaDataType dt6 = new JpaToscaDataType(new PfConceptKey("dt6", "0.0.1"));
        dt6.setDerivedFrom(dt5.getKey());

        JpaToscaDataType dt7 = new JpaToscaDataType(new PfConceptKey("dt7", "0.0.1"));

        JpaToscaDataType dt8 = new JpaToscaDataType(new PfConceptKey("dt8", "0.0.1"));
        dt8.setDerivedFrom(dt7.getKey());

        JpaToscaDataType dt9 = new JpaToscaDataType(new PfConceptKey("dt9", "0.0.1"));
        dt9.setDerivedFrom(dt8.getKey());

        dataTypes.getConceptMap().put(dt0.getKey(), dt0);
        dataTypes.getConceptMap().put(dt1.getKey(), dt1);
        dataTypes.getConceptMap().put(dt2.getKey(), dt2);
        dataTypes.getConceptMap().put(dt3.getKey(), dt3);
        dataTypes.getConceptMap().put(dt4.getKey(), dt4);
        dataTypes.getConceptMap().put(dt5.getKey(), dt5);
        dataTypes.getConceptMap().put(dt6.getKey(), dt6);
        dataTypes.getConceptMap().put(dt7.getKey(), dt7);
        dataTypes.getConceptMap().put(dt8.getKey(), dt8);
        dataTypes.getConceptMap().put(dt9.getKey(), dt9);

        ToscaUtils.getEntityTree(filteredDataTypes, dt0.getKey().getName(), dt0.getKey().getVersion());
        assertEquals(1, filteredDataTypes.getConceptMap().size());

        filteredDataTypes = new JpaToscaDataTypes(dataTypes);
        ToscaUtils.getEntityTree(filteredDataTypes, dt1.getKey().getName(), dt1.getKey().getVersion());
        assertEquals(2, filteredDataTypes.getConceptMap().size());

        filteredDataTypes = new JpaToscaDataTypes(dataTypes);
        ToscaUtils.getEntityTree(filteredDataTypes, dt2.getKey().getName(), dt2.getKey().getVersion());
        assertEquals(2, filteredDataTypes.getConceptMap().size());

        filteredDataTypes = new JpaToscaDataTypes(dataTypes);
        ToscaUtils.getEntityTree(filteredDataTypes, dt3.getKey().getName(), dt3.getKey().getVersion());
        assertEquals(2, filteredDataTypes.getConceptMap().size());

        filteredDataTypes = new JpaToscaDataTypes(dataTypes);
        ToscaUtils.getEntityTree(filteredDataTypes, dt4.getKey().getName(), dt4.getKey().getVersion());
        assertEquals(3, filteredDataTypes.getConceptMap().size());

        filteredDataTypes = new JpaToscaDataTypes(dataTypes);
        ToscaUtils.getEntityTree(filteredDataTypes, dt5.getKey().getName(), dt5.getKey().getVersion());
        assertEquals(4, filteredDataTypes.getConceptMap().size());

        filteredDataTypes = new JpaToscaDataTypes(dataTypes);
        ToscaUtils.getEntityTree(filteredDataTypes, dt6.getKey().getName(), dt6.getKey().getVersion());
        assertEquals(5, filteredDataTypes.getConceptMap().size());
        assertTrue(filteredDataTypes.getConceptMap().containsValue(dt0));
        assertFalse(filteredDataTypes.getConceptMap().containsValue(dt1));
        assertFalse(filteredDataTypes.getConceptMap().containsValue(dt2));
        assertTrue(filteredDataTypes.getConceptMap().containsValue(dt3));
        assertTrue(filteredDataTypes.getConceptMap().containsValue(dt4));
        assertTrue(filteredDataTypes.getConceptMap().containsValue(dt5));
        assertTrue(filteredDataTypes.getConceptMap().containsValue(dt6));

        filteredDataTypes = new JpaToscaDataTypes(dataTypes);
        ToscaUtils.getEntityTree(filteredDataTypes, dt7.getKey().getName(), dt7.getKey().getVersion());
        assertEquals(1, filteredDataTypes.getConceptMap().size());

        filteredDataTypes = new JpaToscaDataTypes(dataTypes);
        ToscaUtils.getEntityTree(filteredDataTypes, dt8.getKey().getName(), dt8.getKey().getVersion());
        assertEquals(2, filteredDataTypes.getConceptMap().size());

        filteredDataTypes = new JpaToscaDataTypes(dataTypes);
        ToscaUtils.getEntityTree(filteredDataTypes, dt9.getKey().getName(), dt9.getKey().getVersion());
        assertEquals(3, filteredDataTypes.getConceptMap().size());

        dt9.setDerivedFrom(new PfConceptKey("i.dont.Exist", "0.0.0"));

        assertThatThrownBy(() -> {
            final JpaToscaDataTypes badDataTypes = new JpaToscaDataTypes(dataTypes);
            ToscaUtils.getEntityTree(badDataTypes, dt9.getKey().getName(), dt9.getKey().getVersion());
        }).hasMessageContaining("parent").hasMessageContaining("i.dont.Exist:0.0.0")
                        .hasMessageContaining(Validated.NOT_FOUND);
    }
}
