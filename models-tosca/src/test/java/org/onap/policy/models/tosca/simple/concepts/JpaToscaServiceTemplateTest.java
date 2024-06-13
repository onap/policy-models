/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2021, 2023 Nordix Foundation.
 *  Modifications Copyright (C) 2019-2021 AT&T Intellectual Property. All rights reserved.
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import org.junit.jupiter.api.Test;
import org.onap.policy.common.parameters.BeanValidationResult;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfReferenceKey;
import org.onap.policy.models.base.Validated;

/**
 * DAO test for ToscaDatatype.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
class JpaToscaServiceTemplateTest {

    private static final String KEY_IS_NULL = "key is marked .*on.*ull but is null";
    private static final String VERSION_001 = "0.0.1";

    @Test
    void testServiceTemplateNull() {
        assertNotNull(new JpaToscaServiceTemplate());
        assertNotNull(new JpaToscaServiceTemplate(new PfConceptKey()));
        assertNotNull(new JpaToscaServiceTemplate(new PfConceptKey(), ""));
        assertNotNull(new JpaToscaServiceTemplate(new JpaToscaServiceTemplate()));

        assertThatThrownBy(() -> new JpaToscaServiceTemplate((PfConceptKey) null)).hasMessageMatching(KEY_IS_NULL);

        assertThatThrownBy(() -> new JpaToscaServiceTemplate(null, null)).hasMessageMatching(KEY_IS_NULL);

        assertThatThrownBy(() -> new JpaToscaServiceTemplate(null, "")).hasMessageMatching(KEY_IS_NULL);

        assertThatThrownBy(() -> new JpaToscaServiceTemplate(new PfConceptKey(), null))
                .hasMessageMatching("toscaDefinitionsVersion is marked .*on.*ull but is null");

        assertThatThrownBy(() -> new JpaToscaServiceTemplate((JpaToscaServiceTemplate) null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void testServiceTemplatePojo() {
        PfConceptKey tstKey = new PfConceptKey("tst", VERSION_001);
        JpaToscaServiceTemplate tst = new JpaToscaServiceTemplate(tstKey, "Tosca Version");

        PfConceptKey dataTypeKey = new PfConceptKey("DataType", VERSION_001);
        JpaToscaDataType dataType0 = new JpaToscaDataType(dataTypeKey);
        PfConceptKey dtsKey = new PfConceptKey("dts", VERSION_001);
        Map<PfConceptKey, JpaToscaDataType> dataTypeMap = new TreeMap<>();
        dataTypeMap.put(dataTypeKey, dataType0);
        JpaToscaDataTypes dataTypes = new JpaToscaDataTypes(dtsKey, dataTypeMap);
        tst.setDataTypes(dataTypes);
        assertEquals(dataTypes, tst.getDataTypes());

        PfConceptKey policyTypeKey = new PfConceptKey("DataType", VERSION_001);
        JpaToscaPolicyType policyType0 = new JpaToscaPolicyType(policyTypeKey);
        PfConceptKey ptsKey = new PfConceptKey("dts", VERSION_001);
        Map<PfConceptKey, JpaToscaPolicyType> policyTypeMap = new TreeMap<>();
        policyTypeMap.put(policyTypeKey, policyType0);
        JpaToscaPolicyTypes policyTypes = new JpaToscaPolicyTypes(ptsKey, policyTypeMap);
        tst.setPolicyTypes(policyTypes);
        assertEquals(policyTypes, tst.getPolicyTypes());

        PfReferenceKey tttKey = new PfReferenceKey(tstKey, "TopologyTemplate");
        JpaToscaTopologyTemplate ttt = new JpaToscaTopologyTemplate(tttKey);
        tst.setTopologyTemplate(ttt);
        assertEquals(ttt, tst.getTopologyTemplate());

        assertCloneAndCopies(tstKey, tst, dataTypes, policyTypes, ttt);

        assertTrue(new JpaToscaServiceTemplate().validate("").isValid());
        assertTrue(tst.validate("").isValid());

        tst.setDescription(null);
        assertTrue(tst.validate("").isValid());
        tst.setDescription("");
        assertFalse(tst.validate("").isValid());
        tst.setDescription("A Description");
        assertTrue(tst.validate("").isValid());

        assertThatThrownBy(() -> tst.validate(null)).hasMessageMatching("fieldName is marked .*on.*ull but is null");

        tst.setToscaDefinitionsVersion(null);
        BeanValidationResult result = tst.validate("");
        assertThat(result.getResult()).contains("tosca_definitions_version").contains(Validated.IS_NULL);

        tst.setToscaDefinitionsVersion(JpaToscaServiceTemplate.DEFAULT_TOSCA_DEFINITIONS_VERSION);
        tst.setDataTypes(null);
        result = tst.validate("");
        assertTrue(result.isValid());

        JpaToscaPolicyType pt0 = new JpaToscaPolicyType(new PfConceptKey("pt0:0.0.1"));
        tst.getPolicyTypes().getConceptMap().put(pt0.getKey(), pt0);
        result = tst.validate("");
        assertTrue(result.isValid());

        JpaToscaDataType dt0 = new JpaToscaDataType(new PfConceptKey("dt0:0.0.1"));
        JpaToscaProperty prop0 = new JpaToscaProperty(new PfReferenceKey(pt0.getKey(), "prop0"));
        prop0.setType(dt0.getKey());

        assertDataTypesAndToscaTopology(tst, pt0, dt0, prop0);

        tst.setPolicyTypes(null);
        result = tst.validate("");
        assertTrue(result.isValid());

        assertNoPolicyType(tst, policyTypes, pt0, dt0);

    }

    private static void assertDataTypesAndToscaTopology(JpaToscaServiceTemplate tst, JpaToscaPolicyType pt0,
                                                        JpaToscaDataType dt0, JpaToscaProperty prop0) {
        BeanValidationResult result;
        pt0.setProperties(new LinkedHashMap<>());
        pt0.getProperties().put(prop0.getKey().getLocalName(), prop0);
        result = tst.validate("");
        assertFalse(result.isValid());
        assertThat(result.getResult()).contains("data type").contains("dt0:0.0.1").contains(Validated.NOT_FOUND);

        tst.setDataTypes(null);
        result = tst.validate("");
        assertFalse(result.isValid());
        assertThat(result.getResult()).contains("data type").contains("dt0:0.0.1").contains(Validated.NOT_FOUND);

        tst.setDataTypes(new JpaToscaDataTypes());
        result = tst.validate("");
        assertFalse(result.isValid());
        assertThat(result.getResult()).contains("data type").contains("dt0:0.0.1").contains(Validated.NOT_FOUND);

        tst.getDataTypes().getConceptMap().put(dt0.getKey(), dt0);
        result = tst.validate("");
        assertTrue(result.isValid());

        tst.setTopologyTemplate(null);
        result = tst.validate("");
        assertTrue(result.isValid());

        tst.setTopologyTemplate(new JpaToscaTopologyTemplate());
        result = tst.validate("");
        assertTrue(result.isValid());

        tst.getTopologyTemplate().setPolicies(new JpaToscaPolicies());
        result = tst.validate("");
        assertTrue(result.isValid());
    }

    private static void assertCloneAndCopies(PfConceptKey tstKey, JpaToscaServiceTemplate tst,
                                             JpaToscaDataTypes dataTypes, JpaToscaPolicyTypes policyTypes,
                                             JpaToscaTopologyTemplate ttt) {
        JpaToscaServiceTemplate tttClone0 = new JpaToscaServiceTemplate(tst);
        assertEquals(tst, tttClone0);
        assertEquals(0, tst.compareTo(tttClone0));

        JpaToscaServiceTemplate tttClone1 = new JpaToscaServiceTemplate(tst);
        assertEquals(tst, tttClone1);
        assertEquals(0, tst.compareTo(tttClone1));

        assertEquals(-1, tst.compareTo(null));
        assertEquals(0, tst.compareTo(tst));
        assertNotEquals(0, tst.compareTo(tst.getKey()));

        PfConceptKey otherDtKey = new PfConceptKey("otherDt", VERSION_001);
        JpaToscaServiceTemplate otherDt = new JpaToscaServiceTemplate(otherDtKey);

        assertNotEquals(0, tst.compareTo(otherDt));
        otherDt.setKey(tstKey);
        assertNotEquals(0, tst.compareTo(otherDt));
        otherDt.setToscaDefinitionsVersion("Tosca Version");
        assertNotEquals(0, tst.compareTo(otherDt));
        otherDt.setDataTypes(dataTypes);
        assertNotEquals(0, tst.compareTo(otherDt));
        otherDt.setPolicyTypes(policyTypes);
        assertNotEquals(0, tst.compareTo(otherDt));
        otherDt.setTopologyTemplate(ttt);
        assertEquals(0, tst.compareTo(otherDt));

        assertEquals(6, tst.getKeys().size());
        assertEquals(1, new JpaToscaServiceTemplate().getKeys().size());

        new JpaToscaServiceTemplate().clean();
        tst.clean();
        assertEquals(tttClone0, tst);
    }

    private static void assertNoPolicyType(JpaToscaServiceTemplate tst, JpaToscaPolicyTypes policyTypes,
                                           JpaToscaPolicyType pt0, JpaToscaDataType dt0) {
        BeanValidationResult result;
        JpaToscaPolicy pol0 = new JpaToscaPolicy(new PfConceptKey("pol0:0.0.1"));
        tst.getTopologyTemplate().getPolicies().getConceptMap().put(pol0.getKey(), pol0);
        result = tst.validate("");
        assertFalse(result.isValid());
        assertThat(result.getResult()).contains("type").contains(Validated.IS_A_NULL_KEY);

        pol0.setType(new PfConceptKey("i.dont.Exist:0.0.1"));
        result = tst.validate("");
        assertFalse(result.isValid());
        assertThat(result.getResult()).contains(
                "no policy types are defined on the service template for the policies in the topology template");

        tst.setPolicyTypes(policyTypes);
        result = tst.validate("");
        assertFalse(result.isValid());
        assertThat(result.getResult()).contains("policy type").contains("i.dont.Exist:0.0.1")
                        .contains(Validated.NOT_FOUND);

        pol0.setType(dt0.getKey());
        result = tst.validate("");
        assertFalse(result.isValid());
        assertThat(result.getResult()).contains("policy type").contains("dt0:0.0.1").contains(Validated.NOT_FOUND);

        pol0.setType(pt0.getKey());
        result = tst.validate("");
        assertTrue(result.isValid());

        tst.setPolicyTypes(null);
        result = tst.validate("");
        assertFalse(result.isValid());
        assertThat(result.getResult()).contains(
                "no policy types are defined on the service template for the policies in the topology template");

        tst.setPolicyTypes(policyTypes);
        pol0.setType(pt0.getKey());
        result = tst.validate("");
        assertTrue(result.isValid());

        tst.setPolicyTypes(new JpaToscaPolicyTypes());
        result = tst.validate("");
        assertFalse(result.isValid());
        assertThat(result.getResult()).contains(
                "no policy types are defined on the service template for the policies in the topology template");
    }
}
