/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy Models
 * ================================================================================
 * Copyright (C) 2019, 2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019, 2024 Nordix Foundation.
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
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onap.policy.models.pdp.enums.PdpMessageType;

class PdpMessageTest {
    private static final String PDP_GROUP_MSG = " pdp group ";
    private static final String PDP_NAME = "pdpA";
    private static final String PDP_GROUP = "groupA";
    private static final String PDP_SUBGROUP = "subgroupA";
    private static final String DIFFERENT = "differentValue";

    private PdpMessage message;

    @BeforeEach
    void setUp() {
        message = new PdpMessage(PdpMessageType.PDP_STATE_CHANGE);
    }

    @Test
    void testCopyConstructorAndEquals() {
        assertThatThrownBy(() -> new PdpMessage((PdpMessage) null)).isInstanceOf(NullPointerException.class);

        // Verify with null values
        PdpMessage newMsg = new PdpMessage(message);
        newMsg.setRequestId(message.getRequestId());
        newMsg.setTimestampMs(message.getTimestampMs());
        assertEquals(message.toString(), newMsg.toString());
        assertEquals(message, newMsg);

        // Verify with all values
        message = makeMessage(PDP_NAME, PDP_GROUP, PDP_SUBGROUP);
        newMsg = new PdpMessage(message);
        newMsg.setRequestId(message.getRequestId());
        newMsg.setTimestampMs(message.getTimestampMs());
        assertEquals(message.toString(), newMsg.toString());
        assertEquals(message, newMsg);

        newMsg.setTimestampMs(1);
        assertNotEquals(message, newMsg);
    }

    @Test
    void testAppliesTo_NameCombos() {
        // Test cases where the name matches.
        for (String msgGroup : new String[]{null, PDP_GROUP, DIFFERENT}) {
            for (String msgSubgroup : new String[]{null, PDP_SUBGROUP, DIFFERENT}) {
                message = makeMessage(PDP_NAME, msgGroup, msgSubgroup);
                testName(PDP_NAME, true);
            }
        }

        // Test cases where the name does not match.
        for (String msgGroup : new String[]{null, PDP_GROUP, DIFFERENT}) {
            for (String msgSubgroup : new String[]{null, PDP_SUBGROUP, DIFFERENT}) {
                message = makeMessage(PDP_NAME, msgGroup, msgSubgroup);
                testName(DIFFERENT, false);
            }
        }
    }

    private void testName(String pdpName, boolean expectMatch) {
        for (String pdpGroup : new String[]{null, PDP_GROUP, DIFFERENT}) {
            for (String pdpSubgroup : new String[]{null, PDP_SUBGROUP, DIFFERENT}) {
                assertEquals(expectMatch, message.appliesTo(pdpName, pdpGroup, pdpSubgroup),
                    "name msg " + message + PDP_GROUP_MSG + pdpGroup + "/" + pdpSubgroup);
            }
        }
    }

    @Test
    void testAppliesTo_BroadcastGroup() {
        // Test cases where the group matches.
        for (String msgSubgroup : new String[]{null, PDP_SUBGROUP}) {
            message = makeMessage(null, PDP_GROUP, msgSubgroup);
            assertTrue(message.appliesTo(PDP_NAME, PDP_GROUP, PDP_SUBGROUP), "group msg " + message);
        }

        // Test cases where the group does not match.
        for (String msgGroup : new String[]{null, PDP_GROUP}) {
            for (String msgSubgroup : new String[]{null, PDP_SUBGROUP}) {
                message = makeMessage(null, msgGroup, msgSubgroup);
                for (String pdpGroup : new String[]{null, DIFFERENT}) {
                    assertFalse(message.appliesTo(PDP_NAME, pdpGroup, PDP_SUBGROUP),
                        "group msg " + message + PDP_GROUP_MSG + pdpGroup);
                }
            }
        }
    }

    @Test
    void testAppliesTo_BroadcastSubGroup() {
        // Test cases where the subgroup matches.
        message = makeMessage(null, PDP_GROUP, PDP_SUBGROUP);
        assertTrue(message.appliesTo(PDP_NAME, PDP_GROUP, PDP_SUBGROUP), "subgroup msg " + message);

        // Test cases where the subgroup does not match.
        message = makeMessage(null, PDP_GROUP, PDP_SUBGROUP);
        for (String pdpSubgroup : new String[]{null, DIFFERENT}) {
            assertFalse(message.appliesTo(PDP_NAME, PDP_GROUP, pdpSubgroup),
                "subgroup msg " + message + " pdp subgroup " + pdpSubgroup);
        }
    }

    @Test
    void testAppliesTo_NullPdpName() {
        message = makeMessage(PDP_NAME, PDP_GROUP, PDP_SUBGROUP);
        assertThatThrownBy(() -> message.appliesTo(null, PDP_GROUP, PDP_SUBGROUP))
            .isInstanceOf(NullPointerException.class);
    }

    private PdpMessage makeMessage(String pdpName, String pdpGroup, String pdpSubgroup) {
        PdpMessage msg = new PdpMessage(PdpMessageType.PDP_STATE_CHANGE);
        msg.setName(pdpName);
        msg.setPdpGroup(pdpGroup);
        msg.setPdpSubgroup(pdpSubgroup);
        return msg;
    }
}
