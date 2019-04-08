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

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Test;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyIdentifier;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyTypeIdentifier;

/**
 * Test the copy constructor, as {@link ModelsTest} tests the other methods.
 */
public class PdpSubGroupTest {

    @Test
    public void testCopyConstructor() {
        assertThatThrownBy(() -> new PdpSubGroup(null)).isInstanceOf(NullPointerException.class);

        final PdpSubGroup orig = new PdpSubGroup();

        // verify with null values
        assertEquals(
                "PdpSubGroup(pdpType=null, supportedPolicyTypes=[], policies=[], "
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
}
