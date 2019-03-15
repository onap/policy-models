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

package org.onap.policy.models.tosca.concepts;

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

/**
 * DAO test for ToscaDatatype.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class ToscaServiceTemplateTest {

    @Test
    public void testServiceTemplatePojo() {
        assertNotNull(new ToscaServiceTemplate());
        assertNotNull(new ToscaServiceTemplate(new PfConceptKey()));
        assertNotNull(new ToscaServiceTemplate(new PfConceptKey(), ""));
        assertNotNull(new ToscaServiceTemplate(new ToscaServiceTemplate()));

        try {
            new ToscaServiceTemplate((PfConceptKey) null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("key is marked @NonNull but is null", exc.getMessage());
        }

        try {
            new ToscaServiceTemplate(null, null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("key is marked @NonNull but is null", exc.getMessage());
        }

        try {
            new ToscaServiceTemplate(null, "");
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("key is marked @NonNull but is null", exc.getMessage());
        }

        try {
            new ToscaServiceTemplate(new PfConceptKey(), null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("toscaDefinitionsVersion is marked @NonNull but is null", exc.getMessage());
        }

        try {
            new ToscaServiceTemplate((ToscaServiceTemplate) null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("copyConcept is marked @NonNull but is null", exc.getMessage());
        }

        PfConceptKey tstKey = new PfConceptKey("tst", "0.0.1");
        ToscaServiceTemplate tst = new ToscaServiceTemplate(tstKey, "Tosca Version");

        PfConceptKey dataTypeKey = new PfConceptKey("DataType", "0.0.1");
        ToscaDataType dataType0 = new ToscaDataType(dataTypeKey);
        PfConceptKey dtsKey = new PfConceptKey("dts", "0.0.1");
        Map<PfConceptKey, ToscaDataType> dataTypeMap = new TreeMap<>();
        dataTypeMap.put(dataTypeKey, dataType0);
        ToscaDataTypes dataTypes = new ToscaDataTypes(dtsKey, dataTypeMap);
        tst.setDataTypes(dataTypes);
        assertEquals(dataTypes, tst.getDataTypes());

        PfConceptKey policyTypeKey = new PfConceptKey("DataType", "0.0.1");
        ToscaPolicyType policyType0 = new ToscaPolicyType(policyTypeKey);
        PfConceptKey ptsKey = new PfConceptKey("dts", "0.0.1");
        Map<PfConceptKey, ToscaPolicyType> policyTypeMap = new TreeMap<>();
        policyTypeMap.put(policyTypeKey, policyType0);
        ToscaPolicyTypes policyTypes = new ToscaPolicyTypes(ptsKey, policyTypeMap);
        tst.setPolicyTypes(policyTypes);
        assertEquals(policyTypes, tst.getPolicyTypes());

        PfReferenceKey tttKey = new PfReferenceKey(tstKey, "TopologyTemplate");
        ToscaTopologyTemplate ttt = new ToscaTopologyTemplate(tttKey);
        tst.setTopologyTemplate(ttt);
        assertEquals(ttt, tst.getTopologyTemplate());

        ToscaServiceTemplate tttClone0 = new ToscaServiceTemplate(tst);
        assertEquals(tst, tttClone0);
        assertEquals(0, tst.compareTo(tttClone0));

        ToscaServiceTemplate tttClone1 = new ToscaServiceTemplate();
        tst.copyTo(tttClone1);
        assertEquals(tst, tttClone1);
        assertEquals(0, tst.compareTo(tttClone1));

        assertEquals(-1, tst.compareTo(null));
        assertEquals(0, tst.compareTo(tst));
        assertFalse(tst.compareTo(tst.getKey()) == 0);

        PfConceptKey otherDtKey = new PfConceptKey("otherDt", "0.0.1");
        ToscaServiceTemplate otherDt = new ToscaServiceTemplate(otherDtKey);

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
        assertEquals(1, new ToscaServiceTemplate().getKeys().size());

        new ToscaServiceTemplate().clean();
        tst.clean();
        assertEquals(tttClone0, tst);

        assertFalse(new ToscaServiceTemplate().validate(new PfValidationResult()).isValid());
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
