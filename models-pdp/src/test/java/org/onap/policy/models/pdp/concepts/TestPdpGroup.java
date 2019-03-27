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
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import org.junit.Test;
import org.onap.policy.models.pdp.enums.PdpState;

/**
 * Test the copy constructor, as {@link TestModels} tests the other methods.
 */
public class TestPdpGroup {

    @Test
    public void testCopyConstructor() {
        assertThatThrownBy(() -> new PdpGroup(null)).isInstanceOf(NullPointerException.class);

        PdpGroup orig = new PdpGroup();

        // verify with null values
        assertEquals("PdpGroup(pdpGroupState=null, properties=null, pdpSubgroups=[])", new PdpGroup(orig).toString());

        // verify with all values
        orig.setDescription("my-descript");
        orig.getKey().setName("my-name");
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

        assertEquals("PdpGroup(pdpGroupState=SAFE, properties={key-A=value-A, key-B=value-B}, "
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
}
