/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy Models
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019-2024 Nordix Foundation.
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
 * ============LICENSE_END=========================================================
 */

package org.onap.policy.models.pdp.concepts;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import org.junit.jupiter.api.Test;
import org.onap.policy.common.parameters.ValidationResult;
import org.onap.policy.common.utils.coder.Coder;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.models.tosca.authorative.concepts.ToscaConceptIdentifier;

/**
 * Test methods not tested by {@link ModelsTest}.
 */
class PdpSubGroupTest {
    private static final String VERSION_300 = "3.0.0";
    private static final Coder coder = new StandardCoder();

    @Test
    void testCopyConstructor() {
        assertThatThrownBy(() -> new PdpSubGroup(null)).isInstanceOf(NullPointerException.class);

        final PdpSubGroup orig = new PdpSubGroup();

        // verify with null values
        assertEquals("PdpSubGroup(pdpType=null, supportedPolicyTypes=[], policies=[], "
                        + "currentInstanceCount=0, desiredInstanceCount=0, properties=null, pdpInstances=[])",
                        new PdpSubGroup(orig).toString());

        // verify with all values
        orig.setCurrentInstanceCount(10);
        orig.setDesiredInstanceCount(11);

        final Pdp inst1 = new Pdp();
        inst1.setInstanceId("my-id-A");
        final Pdp inst2 = new Pdp();
        inst2.setInstanceId("my-id-B");
        orig.setPdpInstances(Arrays.asList(inst1, inst2));

        orig.setPdpType("my-type");

        final ToscaConceptIdentifier pol1 = new ToscaConceptIdentifier();
        pol1.setName("policy-A");
        pol1.setVersion("1.0.0");
        final ToscaConceptIdentifier pol2 = new ToscaConceptIdentifier();
        pol2.setName("policy-B");
        pol1.setVersion("2.0.0");
        orig.setPolicies(Arrays.asList(pol1, pol2));

        final Map<String, String> props = new TreeMap<>();
        props.put("key-A", "value-A");
        props.put("key-B", "value-B");
        orig.setProperties(props);

        final ToscaConceptIdentifier supp1 = new ToscaConceptIdentifier("supp-A", "1.2");
        final ToscaConceptIdentifier supp2 = new ToscaConceptIdentifier("supp-B", "3.4");
        orig.setSupportedPolicyTypes(Arrays.asList(supp1, supp2));

        assertEquals(orig.toString(), new PdpSubGroup(orig).toString());
    }

    @Test
    void testValidatePapRest_GroupUpdateFlow() throws Exception {
        PdpSubGroup subgrp = new PdpSubGroup();
        // with supported policy type and policies
        subgrp.setDesiredInstanceCount(1);
        subgrp.setPdpType("pdp-type");
        subgrp.setSupportedPolicyTypes(
                        Arrays.asList(makeIdent("type-X", VERSION_300, ToscaConceptIdentifier.class)));
        subgrp.setPolicies(Arrays.asList(makeIdent("policy-X", "4.0.0", ToscaConceptIdentifier.class)));

        ValidationResult result = subgrp.validatePapRest(false);
        assertNotNull(result);
        assertTrue(result.isValid());
        assertNull(result.getResult());

        // without supported policy type and policies
        PdpSubGroup subgrp2 = new PdpSubGroup();
        subgrp2.setDesiredInstanceCount(1);
        subgrp2.setPdpType("pdp-type");

        // valid
        result = subgrp2.validatePapRest(true);
        assertNotNull(result);
        assertTrue(result.isValid());
        assertNull(result.getResult());

        // invalid
        result = subgrp2.validatePapRest(false);
        assertNotNull(result);
        assertFalse(result.isValid());
        assertNotNull(result.getResult());
    }

    @Test
    void testValidatePapRest() throws Exception {
        PdpSubGroup subgrp = new PdpSubGroup();

        subgrp.setDesiredInstanceCount(1);
        subgrp.setPdpType("pdp-type");
        subgrp.setSupportedPolicyTypes(
                        Arrays.asList(makeIdent("type-X", VERSION_300, ToscaConceptIdentifier.class)));
        subgrp.setPolicies(Arrays.asList(makeIdent("policy-X", "4.0.0", ToscaConceptIdentifier.class)));

        // valid
        ValidationResult result = subgrp.validatePapRest(false);
        assertNotNull(result);
        assertTrue(result.isValid());
        assertNull(result.getResult());

        // zero count
        PdpSubGroup sub2 = new PdpSubGroup(subgrp);
        sub2.setDesiredInstanceCount(0);
        assertInvalid(sub2);

        // negative count
        sub2 = new PdpSubGroup(subgrp);
        sub2.setDesiredInstanceCount(-1);
        assertInvalid(sub2);

        // null pdp type
        sub2 = new PdpSubGroup(subgrp);
        sub2.setPdpType(null);
        assertInvalid(sub2);

        // null policy types
        sub2 = new PdpSubGroup(subgrp);
        sub2.setSupportedPolicyTypes(null);
        assertInvalid(sub2);

        // empty policy types
        sub2 = new PdpSubGroup(subgrp);
        sub2.setSupportedPolicyTypes(Collections.emptyList());
        assertInvalid(sub2);

        // null policy type item
        sub2 = new PdpSubGroup(subgrp);
        sub2.getSupportedPolicyTypes().set(0, null);
        assertInvalid(sub2);

        // invalid policy type item
        sub2 = new PdpSubGroup(subgrp);
        sub2.getSupportedPolicyTypes().set(0, makeIdent(null, VERSION_300, ToscaConceptIdentifier.class));
        assertInvalid(sub2);

        // null policies
        sub2 = new PdpSubGroup(subgrp);
        sub2.setPolicies(null);
        assertInvalid(sub2);

        // null policy item
        sub2 = new PdpSubGroup(subgrp);
        sub2.getPolicies().set(0, null);
        assertInvalid(sub2);

        // invalid policy item
        sub2 = new PdpSubGroup(subgrp);
        sub2.getPolicies().set(0, makeIdent(null, VERSION_300, ToscaConceptIdentifier.class));
        assertInvalid(sub2);
    }

    private void assertInvalid(PdpSubGroup sub2) {
        ValidationResult result = sub2.validatePapRest(false);
        assertNotNull(result);
        assertFalse(result.isValid());
        assertNotNull(result.getResult());
    }

    /**
     * Makes an identifier. Uses JSON which does no error checking.
     *
     * @param name name to put into the identifier
     * @param version version to put into the identifier
     * @param clazz type of identifier to create
     * @return a new identifier
     * @throws CoderException if the JSON cannot be decoded
     */
    public <T> T makeIdent(String name, String version, Class<T> clazz) throws CoderException {
        StringBuilder bldr = new StringBuilder();
        bldr.append("{");

        if (name != null) {
            bldr.append("'name':'");
            bldr.append(name);
            bldr.append("'");
        }

        if (version != null) {
            if (name != null) {
                bldr.append(',');
            }

            bldr.append("'version':'");
            bldr.append(version);
            bldr.append("'");
        }

        bldr.append("}");

        String json = bldr.toString().replace('\'', '"');

        return coder.decode(json, clazz);
    }
}
