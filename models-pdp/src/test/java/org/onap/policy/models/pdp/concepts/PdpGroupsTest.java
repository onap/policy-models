/*
 * ============LICENSE_START=======================================================
 * ONAP Policy Models
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2021-2024 Nordix Foundation.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.onap.policy.common.parameters.ValidationResult;
import org.onap.policy.models.tosca.authorative.concepts.ToscaConceptIdentifier;

class PdpGroupsTest {

    @Test
    void testValidatePapRest_toMapList() {
        PdpGroup group1 = new PdpGroup();
        group1.setName("group-1");

        PdpSubGroup subgrp = new PdpSubGroup();
        subgrp.setDesiredInstanceCount(1);
        subgrp.setPdpType("pdp-type");
        subgrp.setSupportedPolicyTypes(Arrays.asList(new ToscaConceptIdentifier("policy-type", "9.8.7")));
        subgrp.setPolicies(Collections.emptyList());

        group1.setPdpSubgroups(Arrays.asList(subgrp));

        PdpGroup group2 = new PdpGroup();
        group2.setName("group-2");
        group2.setPdpSubgroups(Arrays.asList(subgrp));

        PdpGroups groups = new PdpGroups();
        groups.setGroups(Arrays.asList(group1, group2));

        // valid
        ValidationResult result = groups.validatePapRest();
        assertNotNull(result);
        assertTrue(result.isValid());
        assertNull(result.getResult());

        // check toMapList()
        List<Map<String, PdpGroup>> lst = groups.toMapList();
        assertEquals(1, lst.size());

        Map<String, PdpGroup> map = lst.get(0);
        assertEquals(2, map.size());

        Iterator<PdpGroup> iter = map.values().iterator();
        assertSame(group1, iter.next());
        assertSame(group2, iter.next());

        // null group list
        groups = new PdpGroups();
        groups.setGroups(null);
        assertInvalid(groups);

        // null group
        groups = new PdpGroups();
        groups.setGroups(Arrays.asList(group1, null));
        assertInvalid(groups);

        // invalid group
        PdpGroup groupX = new PdpGroup(group1);
        groupX.setName(null);
        groups.setGroups(Arrays.asList(group1, groupX));
        assertInvalid(groups);

        // duplicate groups
        groups = new PdpGroups();
        groups.setGroups(Arrays.asList(group1, group2, group1));
        assertInvalid(groups);
    }

    private void assertInvalid(PdpGroups groups) {
        ValidationResult result = groups.validatePapRest();
        assertNotNull(result);
        assertFalse(result.isValid());
        assertNotNull(result.getResult());
    }
}
