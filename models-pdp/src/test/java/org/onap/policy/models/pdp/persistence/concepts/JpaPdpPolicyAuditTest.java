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
import java.time.temporal.ChronoUnit;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.models.pdp.concepts.PdpPolicyAudit;
import org.onap.policy.models.pdp.concepts.PdpPolicyAudit.AuditAction;
import org.onap.policy.models.pdp.concepts.PdpPolicyStatus;
import org.onap.policy.models.pdp.concepts.PdpPolicyStatus.State;
import org.onap.policy.models.tosca.authorative.concepts.ToscaConceptIdentifier;

public class JpaPdpPolicyAuditTest {

    private static final ToscaConceptIdentifier MY_POLICY = new ToscaConceptIdentifier("MyPolicy", "1.2.3");
    private static final String PDP_GROUP = "pdpGroupxyz";
    private static final String USER = "user";

    PdpPolicyAudit audit;

    /**
     * Setup an audit for usage on unit tests.
     */
    @Before
    public void setup() {
        audit = PdpPolicyAudit.builder().auditId(1L).pdpGroup(PDP_GROUP).pdpType("pdpType")
                .policy(MY_POLICY).action(AuditAction.DEPLOYMENT)
                .timestamp(Instant.now().truncatedTo(ChronoUnit.SECONDS)).build();
    }

    @Test
    public void testCompareTo() {
        JpaPdpPolicyAudit jpaAudit = new JpaPdpPolicyAudit(audit);
        assertEquals(-1, jpaAudit.compareTo(null));
        assertEquals(0, jpaAudit.compareTo(jpaAudit));
        assertEquals(0, jpaAudit.compareTo(new JpaPdpPolicyAudit(jpaAudit)));

        PdpPolicyStatus idw = PdpPolicyStatus.builder().deploy(true).state(State.SUCCESS).pdpGroup(PDP_GROUP)
                .pdpId("wId").policy(MY_POLICY).policyType(MY_POLICY).build();
        JpaPdpPolicyStatus jpaStatus = new JpaPdpPolicyStatus(idw);
        assertNotEquals(0, jpaAudit.compareTo(jpaStatus));
    }

    @Test
    public void testKeys() {
        JpaPdpPolicyAudit jpaAudit = new JpaPdpPolicyAudit();
        assertThat(jpaAudit.getKeys()).isNotNull();
        assertTrue(jpaAudit.getKey().isNullKey());

        jpaAudit = new JpaPdpPolicyAudit(audit);
        assertFalse(jpaAudit.getKey().isNullKey());
    }

    @Test
    public void testClean() {
        audit.setUser("   user");
        JpaPdpPolicyAudit jpaAudit = new JpaPdpPolicyAudit(audit);
        assertThatNoException().isThrownBy(() -> jpaAudit.clean());
        assertEquals(USER, jpaAudit.getUser());
    }

    @Test
    public void testToAuthorative() {
        audit.setUser(USER);
        JpaPdpPolicyAudit jpaAudit = new JpaPdpPolicyAudit(audit);
        PdpPolicyAudit convertedAudit = jpaAudit.toAuthorative();

        assertEquals(audit, convertedAudit);
        assertEquals(USER, convertedAudit.getUser());

        audit.setTimestamp(null);
        JpaPdpPolicyAudit jpaAuditWithNullTimestamp = new JpaPdpPolicyAudit(audit);
        PdpPolicyAudit convertedAudit2 = jpaAuditWithNullTimestamp.toAuthorative();
        assertThat(convertedAudit2.getTimestamp()).isNull();
    }
}
