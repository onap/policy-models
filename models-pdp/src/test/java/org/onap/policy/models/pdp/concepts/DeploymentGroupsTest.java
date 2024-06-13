/*
 * ============LICENSE_START=======================================================
 * ONAP Policy Models
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2024 Nordix Foundation
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.onap.policy.common.parameters.ValidationResult;
import org.onap.policy.models.pdp.concepts.DeploymentSubGroup.Action;

class DeploymentGroupsTest {

    @Test
    void testValidatePapRest_toMapList() {
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
