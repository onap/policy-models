/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Map;
import java.util.TreeMap;

import org.junit.Test;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfReferenceKey;
import org.onap.policy.models.base.PfValidationResult;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaDataType;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaDataTypes;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaPolicyType;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaPolicyTypes;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaServiceTemplate;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaTopologyTemplate;

/**
 * DAO test for ToscaDatatype.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class JpaToscaServiceTemplateTest {

    @Test
    public void testServiceTemplatePojo() {
        assertNotNull(new JpaToscaServiceTemplate());
        assertNotNull(new JpaToscaServiceTemplate(new PfConceptKey()));
        assertNotNull(new JpaToscaServiceTemplate(new PfConceptKey(), ""));
        assertNotNull(new JpaToscaServiceTemplate(new JpaToscaServiceTemplate()));

        try {
            new JpaToscaServiceTemplate((PfConceptKey) null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("key is marked @NonNull but is null", exc.getMessage());
        }

        try {
            new JpaToscaServiceTemplate(null, null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("key is marked @NonNull but is null", exc.getMessage());
        }

        try {
            new JpaToscaServiceTemplate(null, "");
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("key is marked @NonNull but is null", exc.getMessage());
        }

        try {
            new JpaToscaServiceTemplate(new PfConceptKey(), null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("toscaDefinitionsVersion is marked @NonNull but is null", exc.getMessage());
        }

        try {
            new JpaToscaServiceTemplate((JpaToscaServiceTemplate) null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("copyConcept is marked @NonNull but is null", exc.getMessage());
        }

        PfConceptKey tstKey = new PfConceptKey("tst", "0.0.1");
        JpaToscaServiceTemplate tst = new JpaToscaServiceTemplate(tstKey, "Tosca Version");

        PfConceptKey dataTypeKey = new PfConceptKey("DataType", "0.0.1");
        JpaToscaDataType dataType0 = new JpaToscaDataType(dataTypeKey);
        PfConceptKey dtsKey = new PfConceptKey("dts", "0.0.1");
        Map<PfConceptKey, JpaToscaDataType> dataTypeMap = new TreeMap<>();
        dataTypeMap.put(dataTypeKey, dataType0);
        JpaToscaDataTypes dataTypes = new JpaToscaDataTypes(dtsKey, dataTypeMap);
        tst.setDataTypes(dataTypes);
        assertEquals(dataTypes, tst.getDataTypes());

        PfConceptKey policyTypeKey = new PfConceptKey("DataType", "0.0.1");
        JpaToscaPolicyType policyType0 = new JpaToscaPolicyType(policyTypeKey);
        PfConceptKey ptsKey = new PfConceptKey("dts", "0.0.1");
        Map<PfConceptKey, JpaToscaPolicyType> policyTypeMap = new TreeMap<>();
        policyTypeMap.put(policyTypeKey, policyType0);
        JpaToscaPolicyTypes policyTypes = new JpaToscaPolicyTypes(ptsKey, policyTypeMap);
        tst.setPolicyTypes(policyTypes);
        assertEquals(policyTypes, tst.getPolicyTypes());

        PfReferenceKey tttKey = new PfReferenceKey(tstKey, "TopologyTemplate");
        JpaToscaTopologyTemplate ttt = new JpaToscaTopologyTemplate(tttKey);
        tst.setTopologyTemplate(ttt);
        assertEquals(ttt, tst.getTopologyTemplate());

        JpaToscaServiceTemplate tttClone0 = new JpaToscaServiceTemplate(tst);
        assertEquals(tst, tttClone0);
        assertEquals(0, tst.compareTo(tttClone0));

        JpaToscaServiceTemplate tttClone1 = new JpaToscaServiceTemplate();
        tst.copyTo(tttClone1);
        assertEquals(tst, tttClone1);
        assertEquals(0, tst.compareTo(tttClone1));

        assertEquals(-1, tst.compareTo(null));
        assertEquals(0, tst.compareTo(tst));
        assertFalse(tst.compareTo(tst.getKey()) == 0);

        PfConceptKey otherDtKey = new PfConceptKey("otherDt", "0.0.1");
        JpaToscaServiceTemplate otherDt = new JpaToscaServiceTemplate(otherDtKey);

        assertFalse(tst.compareTo(otherDt) == 0);
        otherDt.setKey(tstKey);
        assertFalse(tst.compareTo(otherDt) == 0);
        otherDt.setToscaDefinitionsVersion("Tosca Version");
        assertFalse(tst.compareTo(otherDt) == 0);
        otherDt.setDataTypes(dataTypes);
        assertFalse(tst.compareTo(otherDt) == 0);
        otherDt.setPolicyTypes(policyTypes);
        assertFalse(tst.compareTo(otherDt) == 0);
        otherDt.setTopologyTemplate(ttt);
        assertEquals(0, tst.compareTo(otherDt));

        try {
            tst.copyTo(null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("target is marked @NonNull but is null", exc.getMessage());
        }

        assertEquals(6, tst.getKeys().size());
        assertEquals(1, new JpaToscaServiceTemplate().getKeys().size());

        new JpaToscaServiceTemplate().clean();
        tst.clean();
        assertEquals(tttClone0, tst);

        assertTrue(new JpaToscaServiceTemplate().validate(new PfValidationResult()).isValid());
        assertTrue(tst.validate(new PfValidationResult()).isValid());

        tst.setDescription(null);
        assertTrue(tst.validate(new PfValidationResult()).isValid());
        tst.setDescription("");
        assertFalse(tst.validate(new PfValidationResult()).isValid());
        tst.setDescription("A Description");
        assertTrue(tst.validate(new PfValidationResult()).isValid());

        try {
            tst.validate(null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("resultIn is marked @NonNull but is null", exc.getMessage());
        }
    }
}
