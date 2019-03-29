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
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicy;

/**
 * Test the copy constructor, as {@link TestModels} tests the other methods.
 */
public class TestPdpSubGroup {

    @Test
    public void testCopyConstructor() {
        assertThatThrownBy(() -> new PdpSubGroup(null)).isInstanceOf(NullPointerException.class);

        PdpSubGroup orig = new PdpSubGroup();

        // verify with null values
        assertEquals(
                "PdpSubGroup(pdpType=null, supportedPolicyTypes=[], policies=[], "
                        + "currentInstanceCount=0, desiredInstanceCount=0, properties=null, pdpInstances=[])",
                new PdpSubGroup(orig).toString());

        // verify with all values
        orig.setCurrentInstanceCount(10);
        orig.setDesiredInstanceCount(11);

        Pdp inst1 = new Pdp();
        inst1.setInstanceId("my-id-A");
        Pdp inst2 = new Pdp();
        inst2.setInstanceId("my-id-B");
        orig.setPdpInstances(Arrays.asList(inst1, inst2));

        orig.setPdpType("my-type");

        ToscaPolicy pol1 = new ToscaPolicy();
        pol1.setDescription("policy-A");
        ToscaPolicy pol2 = new ToscaPolicy();
        pol2.setDescription("policy-B");
        orig.setPolicies(Arrays.asList(pol1, pol2));

        Map<String, String> props = new TreeMap<>();
        props.put("key-A", "value-A");
        props.put("key-B", "value-B");
        orig.setProperties(props);

        PolicyTypeIdent supp1 = new PolicyTypeIdent("supp-A", "1.2");
        PolicyTypeIdent supp2 = new PolicyTypeIdent("supp-B", "3.4");
        orig.setSupportedPolicyTypes(Arrays.asList(supp1, supp2));

        assertEquals(orig.toString(), new PdpSubGroup(orig).toString());
    }
}
