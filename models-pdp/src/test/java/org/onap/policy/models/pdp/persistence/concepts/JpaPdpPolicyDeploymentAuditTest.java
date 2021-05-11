/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.policy.models.pdp.persistence.concepts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.time.Instant;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.models.pdp.concepts.PdpPolicyDeploymentAudit;
import org.onap.policy.models.pdp.concepts.PdpPolicyDeploymentAudit.AuditAction;
import org.onap.policy.models.pdp.concepts.PdpPolicyStatus;
import org.onap.policy.models.pdp.concepts.PdpPolicyStatus.State;
import org.onap.policy.models.tosca.authorative.concepts.ToscaConceptIdentifier;

public class JpaPdpPolicyDeploymentAuditTest {

    private static final ToscaConceptIdentifier MY_POLICY = new ToscaConceptIdentifier("MyPolicy", "1.2.3");
    private static final String PDP_GROUP = "pdpGroupxyz";

    PdpPolicyDeploymentAudit audit;

    @Before
    public void setup() {
        audit = PdpPolicyDeploymentAudit.builder().trackerId(1L).pdpGroup(PDP_GROUP).pdpType("pdpType")
                .policy(MY_POLICY).action(AuditAction.DEPLOYMENT).timestamp(Instant.now()).build();
    }

    @Test
    public void testCompareTo() {
        JpaPdpPolicyDeploymentAudit jpaAudit = new JpaPdpPolicyDeploymentAudit(audit);
        assertEquals(-1, jpaAudit.compareTo(null));
        assertEquals(0, jpaAudit.compareTo(jpaAudit));
        assertEquals(0, jpaAudit.compareTo(new JpaPdpPolicyDeploymentAudit(jpaAudit)));

        PdpPolicyStatus idw = PdpPolicyStatus.builder().deploy(true).state(State.SUCCESS).pdpGroup(PDP_GROUP)
                .pdpId("wId").policy(MY_POLICY).policyType(MY_POLICY).build();
        JpaPdpPolicyStatus jpaStatus = new JpaPdpPolicyStatus(idw);
        assertNotEquals(0, jpaAudit.compareTo(jpaStatus));
    }

    @Test
    public void testKeys() {
        JpaPdpPolicyDeploymentAudit jpaAudit = new JpaPdpPolicyDeploymentAudit();
        assertThat(jpaAudit.getKeys()).isNotNull();
        assertTrue(jpaAudit.getKey().isNullKey());

        jpaAudit = new JpaPdpPolicyDeploymentAudit(audit);
        assertFalse(jpaAudit.getKey().isNullKey());
    }

    @Test
    public void testClean() {
        JpaPdpPolicyDeploymentAudit jpaAudit = new JpaPdpPolicyDeploymentAudit(audit);
        assertThatNoException().isThrownBy(() -> jpaAudit.clean());
    }

    @Test
    public void testToAuthorative() {
        audit = PdpPolicyDeploymentAudit.builder().trackerId(1L).pdpGroup(PDP_GROUP).pdpType("pdpType")
        .policy(MY_POLICY).action(AuditAction.DEPLOYMENT).changedByUser("user").build();
        JpaPdpPolicyDeploymentAudit jpaAudit = new JpaPdpPolicyDeploymentAudit(audit);
        PdpPolicyDeploymentAudit convertedAudit = jpaAudit.toAuthorative();

        assertEquals(audit, convertedAudit);
        assertEquals("user", convertedAudit.getChangedByUser());
    }
}
