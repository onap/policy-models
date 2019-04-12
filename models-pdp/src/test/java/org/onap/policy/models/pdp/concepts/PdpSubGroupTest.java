/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy Models
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019 Nordix Foundation.
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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import org.junit.Test;
import org.onap.policy.common.parameters.ValidationResult;
import org.onap.policy.common.utils.coder.Coder;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyIdentifier;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyTypeIdentifier;

/**
 * Test methods not tested by {@link ModelsTest}.
 */
public class PdpSubGroupTest {
    private static final Coder coder = new StandardCoder();

    @Test
    public void testCopyConstructor() {
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

        final ToscaPolicyIdentifier pol1 = new ToscaPolicyIdentifier();
        pol1.setName("policy-A");
        pol1.setVersion("1.0.0");
        final ToscaPolicyIdentifier pol2 = new ToscaPolicyIdentifier();
        pol2.setName("policy-B");
        pol1.setVersion("2.0.0");
        orig.setPolicies(Arrays.asList(pol1, pol2));

        final Map<String, String> props = new TreeMap<>();
        props.put("key-A", "value-A");
        props.put("key-B", "value-B");
        orig.setProperties(props);

        final ToscaPolicyTypeIdentifier supp1 = new ToscaPolicyTypeIdentifier("supp-A", "1.2");
        final ToscaPolicyTypeIdentifier supp2 = new ToscaPolicyTypeIdentifier("supp-B", "3.4");
        orig.setSupportedPolicyTypes(Arrays.asList(supp1, supp2));

        assertEquals(orig.toString(), new PdpSubGroup(orig).toString());
    }

    @Test
    public void testValidatePapRest() throws Exception {
        PdpSubGroup subgrp = new PdpSubGroup();

        subgrp.setDesiredInstanceCount(1);
        subgrp.setPdpType("pdp-type");
        subgrp.setSupportedPolicyTypes(Arrays.asList(makeIdent("type-X", "3.0.0", ToscaPolicyTypeIdentifier.class)));
        subgrp.setPolicies(Arrays.asList(makeIdent("policy-X", "4.0.0", ToscaPolicyIdentifier.class)));

        // valid
        ValidationResult result = subgrp.validatePapRest();
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
        sub2.getSupportedPolicyTypes().set(0, makeIdent(null, "3.0.0", ToscaPolicyTypeIdentifier.class));
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
        sub2.getPolicies().set(0, makeIdent(null, "3.0.0", ToscaPolicyIdentifier.class));
        assertInvalid(sub2);
    }

    private void assertInvalid(PdpSubGroup sub2) {
        ValidationResult result = sub2.validatePapRest();
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
