/*
 * ============LICENSE_START=======================================================
 * ONAP Policy Models
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.onap.policy.common.parameters.ValidationResult;
import org.onap.policy.models.pdp.concepts.DeploymentSubGroup.Action;

public class DeploymentGroupsTest {

    @Test
    public void testValidatePapRest_toMapList() {
        DeploymentGroup group1 = new DeploymentGroup();
        group1.setName("group-1");

        DeploymentSubGroup subgrp = new DeploymentSubGroup();
        subgrp.setPdpType("pdp-type");
        subgrp.setAction(Action.DELETE);
        subgrp.setPolicies(Collections.emptyList());

        group1.setDeploymentSubgroups(Arrays.asList(subgrp));

        DeploymentGroup group2 = new DeploymentGroup();
        group2.setName("group-2");
        group2.setDeploymentSubgroups(Arrays.asList(subgrp));

        DeploymentGroups groups = new DeploymentGroups();
        groups.setGroups(Arrays.asList(group1, group2));

        // valid
        ValidationResult result = groups.validatePapRest();
        assertNotNull(result);
        assertNull(result.getResult());
        assertTrue(result.isValid());

        // check toMapList()
        List<Map<String, DeploymentGroup>> lst = groups.toMapList();
        assertEquals(1, lst.size());

        Map<String, DeploymentGroup> map = lst.get(0);
        assertEquals(2, map.size());

        Iterator<DeploymentGroup> iter = map.values().iterator();
        assertSame(group1, iter.next());
        assertSame(group2, iter.next());

        // null group list
        groups = new DeploymentGroups();
        groups.setGroups(null);
        assertInvalid(groups);

        // null group
        groups = new DeploymentGroups();
        groups.setGroups(Arrays.asList(group1, null));
        assertInvalid(groups);

        // invalid group
        DeploymentGroup groupX = new DeploymentGroup(group1);
        groupX.setName(null);
        groups.setGroups(Arrays.asList(group1, groupX));
        assertInvalid(groups);

        // duplicate groups
        groups = new DeploymentGroups();
        groups.setGroups(Arrays.asList(group1, group2, group1));
        assertInvalid(groups);
    }

    private void assertInvalid(DeploymentGroups groups) {
        ValidationResult result = groups.validatePapRest();
        assertNotNull(result);
        assertFalse(result.isValid());
        assertNotNull(result.getResult());
    }
}
