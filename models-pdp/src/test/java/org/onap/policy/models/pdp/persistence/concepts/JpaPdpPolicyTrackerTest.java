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
import static org.junit.Assert.assertTrue;

import java.time.Instant;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.models.pdp.concepts.PdpPolicyStatus;
import org.onap.policy.models.pdp.concepts.PdpPolicyStatus.State;
import org.onap.policy.models.pdp.concepts.PdpPolicyTracker;
import org.onap.policy.models.pdp.concepts.PdpPolicyTracker.TrackerAction;
import org.onap.policy.models.tosca.authorative.concepts.ToscaConceptIdentifier;

public class JpaPdpPolicyTrackerTest {

    private static final ToscaConceptIdentifier MY_POLICY = new ToscaConceptIdentifier("MyPolicy", "1.2.3");
    private static final String PDP_GROUP = "pdpGroupxyz";

    PdpPolicyTracker tracker;

    @Before
    public void setup() {
        tracker = PdpPolicyTracker.builder().trackerId(1L).pdpGroup(PDP_GROUP).pdpType("pdpType").policy(MY_POLICY)
                .action(TrackerAction.DEPLOYMENT).timestamp(Date.from(Instant.now())).build();
    }

    @Test
    public void testCompareTo() {
        JpaPdpPolicyTracker jpaTracker = new JpaPdpPolicyTracker(tracker);
        assertEquals(-1, jpaTracker.compareTo(null));
        assertEquals(0, jpaTracker.compareTo(jpaTracker));
        assertEquals(0, jpaTracker.compareTo(new JpaPdpPolicyTracker(jpaTracker)));

        PdpPolicyStatus idw = PdpPolicyStatus.builder().deploy(true).state(State.SUCCESS).pdpGroup(PDP_GROUP)
                .pdpId("wId").policy(MY_POLICY).policyType(MY_POLICY).build();
        JpaPdpPolicyStatus jpaStatus = new JpaPdpPolicyStatus(idw);
        assertEquals(1, jpaTracker.compareTo(jpaStatus));
    }

    @Test
    public void testKeys() {
        JpaPdpPolicyTracker jpaTracker = new JpaPdpPolicyTracker();
        assertThat(jpaTracker.getKeys()).isNotNull();
        assertTrue(jpaTracker.getKey().isNullKey());

        jpaTracker = new JpaPdpPolicyTracker(tracker);
        assertFalse(jpaTracker.getKey().isNullKey());
    }

    @Test
    public void testClean() {
        JpaPdpPolicyTracker jpaTracker = new JpaPdpPolicyTracker(tracker);
        assertThatNoException().isThrownBy(() -> jpaTracker.clean());
    }

    @Test
    public void testToAuthorative() {
        JpaPdpPolicyTracker jpaTracker = new JpaPdpPolicyTracker(tracker);
        PdpPolicyTracker covertedTracker = jpaTracker.toAuthorative();

        assertEquals(tracker, covertedTracker);
    }
}
