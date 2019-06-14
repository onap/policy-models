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

/**
 * DAO test for ToscaDatatype.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class JpaToscaServiceTemplateTest {

    private static final String KEY_IS_NULL = "key is marked @NonNull but is null";
    private static final String VERSION_001 = "0.0.1";

    @Test
    public void testServiceTemplatePojo() {
        assertNotNull(new JpaToscaServiceTemplate());
        assertNotNull(new JpaToscaServiceTemplate(new PfConceptKey()));
        assertNotNull(new JpaToscaServiceTemplate(new PfConceptKey(), ""));
        assertNotNull(new JpaToscaServiceTemplate(new JpaToscaServiceTemplate()));

        assertThatThrownBy(() -> new JpaToscaServiceTemplate((PfConceptKey) null)).hasMessage(KEY_IS_NULL);

        assertThatThrownBy(() -> new JpaToscaServiceTemplate(null, null)).hasMessage(KEY_IS_NULL);

        assertThatThrownBy(() -> new JpaToscaServiceTemplate(null, "")).hasMessage(KEY_IS_NULL);

        assertThatThrownBy(() -> new JpaToscaServiceTemplate(new PfConceptKey(), null))
                        .hasMessage("toscaDefinitionsVersion is marked @NonNull but is null");

        assertThatThrownBy(() -> new JpaToscaServiceTemplate((JpaToscaServiceTemplate) null))
                        .hasMessage("copyConcept is marked @NonNull but is null");

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

        PfConceptKey otherDtKey = new PfConceptKey("otherDt", VERSION_001);
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

        assertThatThrownBy(() -> tst.copyTo(null)).hasMessage("target is marked @NonNull but is null");

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

        assertThatThrownBy(() -> tst.validate(null)).hasMessage("resultIn is marked @NonNull but is null");
    }
}
