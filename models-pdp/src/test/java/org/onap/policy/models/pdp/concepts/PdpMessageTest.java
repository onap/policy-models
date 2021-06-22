/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy Models
 * ================================================================================
 * Copyright (C) 2019, 2021 AT&T Intellectual Property. All rights reserved.
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
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.onap.policy.models.pdp.enums.PdpMessageType;

public class PdpMessageTest {
    private static final String PDP_GROUP_MSG = " pdp group ";
    private static final String PDP_NAME = "pdpA";
    private static final String PDP_GROUP = "groupA";
    private static final String PDP_SUBGROUP = "subgroupA";
    private static final String DIFFERENT = "differentValue";

    private PdpMessage message;

    @Test
    public void testCopyConstructor() {
        assertThatThrownBy(() -> new PdpMessage((PdpMessage) null)).isInstanceOf(NullPointerException.class);

        // verify with null values
        message = new PdpMessage(PdpMessageType.PDP_STATE_CHANGE);

        PdpMessage newmsg = new PdpMessage(message);
        newmsg.setRequestId(message.getRequestId());
        newmsg.setTimestampMs(message.getTimestampMs());
        assertEquals(message.toString(), newmsg.toString());

        // verify with all values
        message = makeMessage(PDP_NAME, PDP_GROUP, PDP_SUBGROUP);

        newmsg = new PdpMessage(message);
        newmsg.setRequestId(message.getRequestId());
        newmsg.setTimestampMs(message.getTimestampMs());
        assertEquals(message.toString(), newmsg.toString());
    }

    @Test
    public void testAppliesTo_NameCombos() {
        /*
         * Test cases where the name matches.
         */
        for (String msgGroup : new String[] {null, PDP_GROUP, DIFFERENT}) {
            for (String msgSubgroup : new String[] {null, PDP_SUBGROUP, DIFFERENT}) {
                message = makeMessage(PDP_NAME, msgGroup, msgSubgroup);
                testName(PDP_NAME, true);
            }
        }

        /*
         * Test cases where the name does not match.
         */
        for (String msgGroup : new String[] {null, PDP_GROUP, DIFFERENT}) {
            for (String msgSubgroup : new String[] {null, PDP_SUBGROUP, DIFFERENT}) {
                message = makeMessage(PDP_NAME, msgGroup, msgSubgroup);
                testName(DIFFERENT, false);
            }
        }
    }

    private void testName(String pdpName, boolean expectMatch) {
        for (String pdpGroup : new String[] {null, PDP_GROUP, DIFFERENT}) {
            for (String pdpSubgroup : new String[] {null, PDP_SUBGROUP, DIFFERENT}) {
                assertEquals("name msg " + message + PDP_GROUP_MSG + pdpGroup + "/" + pdpSubgroup, expectMatch,
                                message.appliesTo(pdpName, pdpGroup, pdpSubgroup));
            }
        }
    }

    @Test
    public void testAppliesTo_BroadcastGroup() {
        /*
         * Test cases where the group matches.
         */
        for (String msgSubgroup : new String[] {null, PDP_SUBGROUP}) {
            message = makeMessage(null, PDP_GROUP, msgSubgroup);

            assertTrue("group msg " + message, message.appliesTo(PDP_NAME, PDP_GROUP, PDP_SUBGROUP));
        }

        /*
         * Test cases where the group does not match.
         */
        for (String msgGroup : new String[] {null, PDP_GROUP}) {
            for (String msgSubgroup : new String[] {null, PDP_SUBGROUP}) {
                message = makeMessage(null, msgGroup, msgSubgroup);

                for (String pdpGroup : new String[] {null, DIFFERENT}) {
                    assertFalse("group msg " + message + PDP_GROUP_MSG + pdpGroup,
                                    message.appliesTo(PDP_NAME, pdpGroup, PDP_SUBGROUP));
                }
            }
        }
    }

    @Test
    public void testAppliesTo_BroadcastSubGroup() {
        /*
         * Test cases where the subgroup matches.
         */
        message = makeMessage(null, PDP_GROUP, PDP_SUBGROUP);
        assertTrue("subgroup msg " + message, message.appliesTo(PDP_NAME, PDP_GROUP, PDP_SUBGROUP));

        /*
         * Test cases where the subgroup does not match.
         */
        message = makeMessage(null, PDP_GROUP, PDP_SUBGROUP);

        for (String pdpSubgroup : new String[] {null, DIFFERENT}) {
            assertFalse("subgroup msg " + message + " pdp subgroup " + pdpSubgroup,
                            message.appliesTo(PDP_NAME, PDP_GROUP, pdpSubgroup));
        }
    }

    @Test
    public void testAppliesTo_NullPdpName() {
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
