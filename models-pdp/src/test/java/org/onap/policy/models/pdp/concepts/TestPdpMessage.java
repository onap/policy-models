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

import org.junit.Test;
import org.onap.policy.models.pdp.enums.PdpMessageType;

/**
 * Test the copy constructor, as {@link TestModels} tests the other methods.
 */
public class TestPdpMessage {

    @Test
    public void testCopyConstructor() {
        assertThatThrownBy(() -> new PdpMessage((PdpMessage) null)).isInstanceOf(NullPointerException.class);

        PdpMessage orig = new PdpMessage(PdpMessageType.PDP_STATE_CHANGE);

        // verify with null values
        PdpMessage newmsg = new PdpMessage(orig);
        newmsg.setRequestId(orig.getRequestId());
        newmsg.setTimestampMs(orig.getTimestampMs());
        assertEquals(orig.toString(), newmsg.toString());

        // verify with all values
        orig.setName("my-name");
        orig.setPdpGroup("my-group");
        orig.setPdpSubgroup("my-subgroup");

        newmsg = new PdpMessage(orig);
        newmsg.setRequestId(orig.getRequestId());
        newmsg.setTimestampMs(orig.getTimestampMs());
        assertEquals(orig.toString(), newmsg.toString());
    }
}
