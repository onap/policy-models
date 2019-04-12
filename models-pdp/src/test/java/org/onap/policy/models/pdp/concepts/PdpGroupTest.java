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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.junit.Test;
import org.onap.policy.common.parameters.ValidationResult;
import org.onap.policy.models.pdp.enums.PdpState;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyTypeIdentifier;

/**
 * Test methods not tested by {@link ModelsTest}.
 */
public class PdpGroupTest {
    private static final String NAME = "my-name";
    private static final String PDP_TYPE1 = "type-1";
    private static final String PDP_TYPE2 = "type-2";
    private static final String PDP_TYPE3 = "type-3";

    @Test
    public void testCopyConstructor() {
        assertThatThrownBy(() -> new PdpGroup(null)).isInstanceOf(NullPointerException.class);

        PdpGroup orig = new PdpGroup();

        // verify with null values
        assertEquals("PdpGroup(name=null, version=null, description=null, pdpGroupState=null, "
                + "properties=null, pdpSubgroups=[])", new PdpGroup(orig).toString());

        // verify with all values
        orig.setDescription("my-descript");
        orig.setName(NAME);
        orig.setVersion("1.2.3");
        orig.setDescription("my-description");
        orig.setPdpGroupState(PdpState.SAFE);

        PdpSubGroup sub1 = new PdpSubGroup();
        sub1.setCurrentInstanceCount(10);
        PdpSubGroup sub2 = new PdpSubGroup();
        sub2.setCurrentInstanceCount(11);
        orig.setPdpSubgroups(Arrays.asList(sub1, sub2));

        Map<String, String> props = new TreeMap<>();
        props.put("key-A", "value-A");
        props.put("key-B", "value-B");
        orig.setProperties(props);

        assertEquals("PdpGroup(name=my-name, version=1.2.3, description=my-description, "
                + "pdpGroupState=SAFE, properties={key-A=value-A, key-B=value-B}, "
                + "pdpSubgroups=[PdpSubGroup(pdpType=null, supportedPolicyTypes=[], policies=[], "
                + "currentInstanceCount=10, desiredInstanceCount=0, properties=null, pdpInstances=[]), "
                + "PdpSubGroup(pdpType=null, supportedPolicyTypes=[], policies=[], currentInstanceCount=11, "
                + "desiredInstanceCount=0, properties=null, pdpInstances=[])])", new PdpGroup(orig).toString());
    }

    @Test
    public void testHashCode() {
        PdpGroup group = new PdpGroup();
        group.setDescription("A");
        int hash = group.hashCode();

        assertEquals(hash, group.hashCode());

        group.setDescription("B");
        assertTrue(hash != group.hashCode());
    }

    @Test
    public void testCompareTo() {
        PdpGroup pdpGroup0 = new PdpGroup();
        pdpGroup0.setName("Name0");
        pdpGroup0.setVersion("1.2.3");

        PdpGroup pdpGroup1 = new PdpGroup();
        pdpGroup1.setName("Name0");
        pdpGroup1.setVersion("1.2.3");

        assertEquals(0, pdpGroup0.compareTo(pdpGroup1));

        PdpGroups pdpGroups = new PdpGroups();
        pdpGroups.setGroups(new ArrayList<>());
        pdpGroups.getGroups().add(pdpGroup0);
        pdpGroups.getGroups().add(pdpGroup1);

        List<Map<String, PdpGroup>> mapList = pdpGroups.toMapList();

        assertEquals(1, mapList.size());
        assertEquals(1, mapList.get(0).size());
    }

    @Test
    public void testValidatePapRest() {
        PdpGroup group = new PdpGroup();
        group.setName(NAME);

        PdpSubGroup subgroup1 = new PdpSubGroup();
        subgroup1.setDesiredInstanceCount(1);
        subgroup1.setPdpType(PDP_TYPE1);
        subgroup1.setSupportedPolicyTypes(Arrays.asList(new ToscaPolicyTypeIdentifier("a-type-name", "3.2.1")));
        subgroup1.setPolicies(Collections.emptyList());

        PdpSubGroup subgroup2 = new PdpSubGroup(subgroup1);
        subgroup2.setPdpType(PDP_TYPE2);

        PdpSubGroup subgroup3 = new PdpSubGroup(subgroup1);
        subgroup3.setPdpType(PDP_TYPE3);

        group.setPdpSubgroups(Arrays.asList(subgroup1, subgroup2, subgroup3));

        // valid
        ValidationResult result = group.validatePapRest();
        assertNotNull(result);
        assertTrue(result.isValid());
        assertNull(result.getResult());

        // null name
        PdpGroup group2 = new PdpGroup(group);
        group2.setName(null);
        assertInvalid(group2);

        // null subgroup list
        group2 = new PdpGroup(group);
        group2.setPdpSubgroups(null);
        assertInvalid(group2);

        // null subgroup
        group2 = new PdpGroup(group);
        group2.setPdpSubgroups(Arrays.asList(subgroup1, null));
        assertInvalid(group2);

        // invalid subgroup
        group2 = new PdpGroup(group);
        PdpSubGroup subgroupX = new PdpSubGroup(subgroup1);
        subgroupX.setPdpType(null);
        group2.setPdpSubgroups(Arrays.asList(subgroupX));
        assertInvalid(group2);

        // duplicate PDP type
        group2 = new PdpGroup(group);
        group2.setPdpSubgroups(Arrays.asList(subgroup1, subgroup2, subgroup1));
        assertInvalid(group2);
    }

    private void assertInvalid(PdpGroup group) {
        ValidationResult result = group.validatePapRest();
        assertNotNull(result);
        assertFalse(result.isValid());
        assertNotNull(result.getResult());
    }
}
