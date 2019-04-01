/*-
 * ============LICENSE_START=======================================================
 * appc
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.appclcm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class LcmWrapperTest {

    @Test
    public void testLcmWrapper() {
        LcmWrapper wrapper = new LcmWrapper();
        assertNotNull(wrapper);
        assertNotEquals(0, wrapper.hashCode());

        wrapper.setVersion("19.3.9");
        assertEquals("19.3.9", wrapper.getVersion());

        wrapper.setCambriaPartition("The Emerald City");
        assertEquals("The Emerald City", wrapper.getCambriaPartition());

        wrapper.setRpcName("Tornado");
        assertEquals("Tornado", wrapper.getRpcName());

        wrapper.setCorrelationId("YellowBrickRoad");
        assertEquals("YellowBrickRoad", wrapper.getCorrelationId());

        wrapper.setType("Munchkin");
        assertEquals("Munchkin", wrapper.getType());

        assertNotEquals(0, wrapper.hashCode());

        assertEquals("Wrapper [version=19.3.9, cambriaPartition=The ", wrapper.toString().substring(0, 46));

        LcmWrapper copiedLcmWrapper = new LcmWrapper();
        copiedLcmWrapper.setVersion(wrapper.getVersion());
        copiedLcmWrapper.setCambriaPartition(wrapper.getCambriaPartition());
        copiedLcmWrapper.setRpcName(wrapper.getRpcName());
        copiedLcmWrapper.setCorrelationId(wrapper.getCorrelationId());
        copiedLcmWrapper.setType(wrapper.getType());

        assertTrue(wrapper.equals(wrapper));
        assertTrue(wrapper.equals(copiedLcmWrapper));
        assertFalse(wrapper.equals(null));
        assertFalse(wrapper.equals("Hello"));

        wrapper.setVersion(null);
        assertFalse(wrapper.equals(copiedLcmWrapper));
        copiedLcmWrapper.setVersion(null);
        assertTrue(wrapper.equals(copiedLcmWrapper));
        wrapper.setVersion("19.3.9");
        assertFalse(wrapper.equals(copiedLcmWrapper));
        copiedLcmWrapper.setVersion("19.3.9");
        assertTrue(wrapper.equals(copiedLcmWrapper));

        wrapper.setCambriaPartition(null);
        assertFalse(wrapper.equals(copiedLcmWrapper));
        copiedLcmWrapper.setCambriaPartition(null);
        assertTrue(wrapper.equals(copiedLcmWrapper));
        wrapper.setCambriaPartition("The Emerald City");
        assertFalse(wrapper.equals(copiedLcmWrapper));
        copiedLcmWrapper.setCambriaPartition("The Emerald City");
        assertTrue(wrapper.equals(copiedLcmWrapper));

        wrapper.setRpcName(null);
        assertFalse(wrapper.equals(copiedLcmWrapper));
        copiedLcmWrapper.setRpcName(null);
        assertTrue(wrapper.equals(copiedLcmWrapper));
        wrapper.setRpcName("Tornado");
        assertFalse(wrapper.equals(copiedLcmWrapper));
        copiedLcmWrapper.setRpcName("Tornado");
        assertTrue(wrapper.equals(copiedLcmWrapper));

        wrapper.setCorrelationId(null);
        assertFalse(wrapper.equals(copiedLcmWrapper));
        copiedLcmWrapper.setCorrelationId(null);
        assertTrue(wrapper.equals(copiedLcmWrapper));
        wrapper.setCorrelationId("YellowBrickRoad");
        assertFalse(wrapper.equals(copiedLcmWrapper));
        copiedLcmWrapper.setCorrelationId("YellowBrickRoad");
        assertTrue(wrapper.equals(copiedLcmWrapper));

        wrapper.setType(null);
        assertFalse(wrapper.equals(copiedLcmWrapper));
        copiedLcmWrapper.setType(null);
        assertTrue(wrapper.equals(copiedLcmWrapper));
        wrapper.setType("Munchkin");
        assertFalse(wrapper.equals(copiedLcmWrapper));
        copiedLcmWrapper.setType("Munchkin");
        assertTrue(wrapper.equals(copiedLcmWrapper));
    }
}
