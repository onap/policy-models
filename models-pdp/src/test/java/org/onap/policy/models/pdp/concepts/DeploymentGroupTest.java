/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy Models
 * ================================================================================
 * Copyright (C) 2019-2020 AT&T Intellectual Property. All rights reserved.
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.onap.policy.common.parameters.ValidationResult;
import org.onap.policy.models.pdp.concepts.DeploymentSubGroup.Action;

/**
 * Test methods not tested by {@link ModelsTest}.
 */
class DeploymentGroupTest {
    private static final String NAME = "my-name";
    private static final String PDP_TYPE1 = "type-1";
    private static final String PDP_TYPE2 = "type-2";
    private static final String PDP_TYPE3 = "type-3";

    @Test
    void testCopyConstructor() {
        assertThatThrownBy(() -> new DeploymentGroup(null)).isInstanceOf(NullPointerException.class);

        DeploymentGroup orig = new DeploymentGroup();

        // verify with null values
        assertEquals("DeploymentGroup(name=null, deploymentSubgroups=[])", new DeploymentGroup(orig).toString());

        // verify with all values
        orig.setName(NAME);

        DeploymentSubGroup sub1 = new DeploymentSubGroup();
        DeploymentSubGroup sub2 = new DeploymentSubGroup();
        orig.setDeploymentSubgroups(Arrays.asList(sub1, sub2));

        assertEquals("DeploymentGroup(name=my-name, "
                        + "deploymentSubgroups=[DeploymentSubGroup(pdpType=null, action=null, policies=[]), "
                        + "DeploymentSubGroup(pdpType=null, action=null, policies=[])])",
                        new DeploymentGroup(orig).toString());
    }

    @Test
    void testHashCode() {
        DeploymentGroup group = new DeploymentGroup();
        group.setName("A");
        int hash = group.hashCode();

        assertEquals(hash, group.hashCode());

        group.setName("B");
        assertNotEquals(hash, group.hashCode());
    }

    @Test
    void testValidatePapRest() {
        DeploymentGroup group = new DeploymentGroup();
        group.setName(NAME);

        DeploymentSubGroup subgroup1 = new DeploymentSubGroup();
        subgroup1.setPdpType(PDP_TYPE1);
        subgroup1.setAction(Action.PATCH);
        subgroup1.setPolicies(Collections.emptyList());

        DeploymentSubGroup subgroup2 = new DeploymentSubGroup(subgroup1);
        subgroup2.setPdpType(PDP_TYPE2);

        DeploymentSubGroup subgroup3 = new DeploymentSubGroup(subgroup1);
        subgroup3.setPdpType(PDP_TYPE3);

        group.setDeploymentSubgroups(Arrays.asList(subgroup1, subgroup2, subgroup3));

        // valid
        assertValid(group);

        // null name
        DeploymentGroup group2 = new DeploymentGroup(group);
        group2.setName(null);
        assertInvalid(group2);

        // null subgroup list
        group2 = new DeploymentGroup(group);
        group2.setDeploymentSubgroups(null);
        assertInvalid(group2);

        // empty subgroup list
        group2 = new DeploymentGroup(group);
        group2.setDeploymentSubgroups(Collections.emptyList());
        assertInvalid(group2);

        // null subgroup
        group2 = new DeploymentGroup(group);
        group2.setDeploymentSubgroups(Arrays.asList(subgroup1, null));
        assertInvalid(group2);

        // invalid subgroup
        group2 = new DeploymentGroup(group);
        DeploymentSubGroup subgroupX = new DeploymentSubGroup(subgroup1);
        subgroupX.setPdpType(null);
        group2.setDeploymentSubgroups(Arrays.asList(subgroupX));
        assertInvalid(group2);
    }

    @Test
    void testCheckDuplicateSubgroups() {
        DeploymentGroup group = new DeploymentGroup();
        group.setName(NAME);

        DeploymentSubGroup subgroup1 = new DeploymentSubGroup();
        subgroup1.setPdpType(PDP_TYPE1);
        subgroup1.setAction(Action.POST);
        subgroup1.setPolicies(Collections.emptyList());

        DeploymentSubGroup subgroup2 = new DeploymentSubGroup(subgroup1);
        subgroup2.setPdpType(PDP_TYPE2);
        subgroup2.setAction(Action.PATCH);

        DeploymentSubGroup subgroup3 = new DeploymentSubGroup(subgroup1);
        subgroup3.setPdpType(PDP_TYPE3);
        subgroup3.setAction(Action.DELETE);

        group.setDeploymentSubgroups(Arrays.asList(subgroup1, subgroup2, subgroup3));

        // no duplicates
        assertValid(group);

        /*
         * Allowed duplicates
         */
        DeploymentSubGroup subgroup1b = new DeploymentSubGroup(subgroup1);
        subgroup1b.setAction(Action.POST);

        DeploymentSubGroup subgroup1c = new DeploymentSubGroup(subgroup1);
        subgroup1c.setAction(Action.DELETE);

        DeploymentSubGroup subgroup1d = new DeploymentSubGroup(subgroup1);
        subgroup1d.setAction(Action.DELETE);

        group.setDeploymentSubgroups(
                        Arrays.asList(subgroup1, subgroup2, subgroup3, subgroup1b, subgroup1c, subgroup1d));

        // still ok
        assertValid(group);

        /*
         * Not allowed
         */
        DeploymentSubGroup subgroup1e = new DeploymentSubGroup(subgroup1);
        subgroup1e.setAction(Action.PATCH);

        group.setDeploymentSubgroups(
                        Arrays.asList(subgroup1, subgroup2, subgroup3, subgroup1b, subgroup1c, subgroup1d, subgroup1e));

        assertInvalid(group);
    }

    private void assertValid(DeploymentGroup group) {
        ValidationResult result = group.validatePapRest();
        assertNotNull(result);
        assertNull(result.getResult());
        assertTrue(result.isValid());
    }

    private void assertInvalid(DeploymentGroup group) {
        ValidationResult result = group.validatePapRest();
        assertNotNull(result);
        assertFalse(result.isValid());
        assertNotNull(result.getResult());
    }
}
