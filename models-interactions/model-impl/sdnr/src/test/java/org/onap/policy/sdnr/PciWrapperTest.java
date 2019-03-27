/*-
 * ============LICENSE_START=======================================================
 * sdnr
 * ================================================================================
 * Copyright (C) 2018 Wipro Limited Intellectual Property. All rights reserved.
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

package org.onap.policy.sdnr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PciWrapperTest {

    @Test
    public void testPciWrapper() {
        PciWrapper wrapper = new PciWrapper();
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

        PciWrapper copiedPciWrapper = new PciWrapper();
        copiedPciWrapper.setVersion(wrapper.getVersion());
        copiedPciWrapper.setCambriaPartition(wrapper.getCambriaPartition());
        copiedPciWrapper.setRpcName(wrapper.getRpcName());
        copiedPciWrapper.setCorrelationId(wrapper.getCorrelationId());
        copiedPciWrapper.setType(wrapper.getType());

        assertTrue(wrapper.equals(wrapper));
        assertTrue(wrapper.equals(copiedPciWrapper));
        assertFalse(wrapper.equals(null));
        assertFalse(wrapper.equals("Hello"));

        wrapper.setVersion(null);
        assertFalse(wrapper.equals(copiedPciWrapper));
        copiedPciWrapper.setVersion(null);
        assertTrue(wrapper.equals(copiedPciWrapper));
        wrapper.setVersion("19.3.9");
        assertFalse(wrapper.equals(copiedPciWrapper));
        copiedPciWrapper.setVersion("19.3.9");
        assertTrue(wrapper.equals(copiedPciWrapper));

        wrapper.setCambriaPartition(null);
        assertFalse(wrapper.equals(copiedPciWrapper));
        copiedPciWrapper.setCambriaPartition(null);
        assertTrue(wrapper.equals(copiedPciWrapper));
        wrapper.setCambriaPartition("The Emerald City");
        assertFalse(wrapper.equals(copiedPciWrapper));
        copiedPciWrapper.setCambriaPartition("The Emerald City");
        assertTrue(wrapper.equals(copiedPciWrapper));

        wrapper.setRpcName(null);
        assertFalse(wrapper.equals(copiedPciWrapper));
        copiedPciWrapper.setRpcName(null);
        assertTrue(wrapper.equals(copiedPciWrapper));
        wrapper.setRpcName("Tornado");
        assertFalse(wrapper.equals(copiedPciWrapper));
        copiedPciWrapper.setRpcName("Tornado");
        assertTrue(wrapper.equals(copiedPciWrapper));

        wrapper.setCorrelationId(null);
        assertFalse(wrapper.equals(copiedPciWrapper));
        copiedPciWrapper.setCorrelationId(null);
        assertTrue(wrapper.equals(copiedPciWrapper));
        wrapper.setCorrelationId("YellowBrickRoad");
        assertFalse(wrapper.equals(copiedPciWrapper));
        copiedPciWrapper.setCorrelationId("YellowBrickRoad");
        assertTrue(wrapper.equals(copiedPciWrapper));

        wrapper.setType(null);
        assertFalse(wrapper.equals(copiedPciWrapper));
        copiedPciWrapper.setType(null);
        assertTrue(wrapper.equals(copiedPciWrapper));
        wrapper.setType("Munchkin");
        assertFalse(wrapper.equals(copiedPciWrapper));
        copiedPciWrapper.setType("Munchkin");
        assertTrue(wrapper.equals(copiedPciWrapper));
    }
}
