/*-
 * ============LICENSE_START=======================================================
 * sdnr
 * ================================================================================
 * Copyright (C) 2018 Wipro Limited Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019 Nordix Foundation.
 * Modifications Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

    private static final String YELLOW_BRICK_ROAD = "YellowBrickRoad";
    private static final String TORNADO = "Tornado";
    private static final String THE_EMERALD_CITY = "The Emerald City";
    private static final String MUNCHKIN = "Munchkin";
    private static final String VERSION_19 = "19.3.9";

    @Test
    public void testPciWrapper() {
        PciWrapper wrapper = new PciWrapper();
        assertNotNull(wrapper);
        assertNotEquals(0, wrapper.hashCode());

        wrapper.setVersion(VERSION_19);
        assertEquals(VERSION_19, wrapper.getVersion());

        wrapper.setCambriaPartition(THE_EMERALD_CITY);
        assertEquals(THE_EMERALD_CITY, wrapper.getCambriaPartition());

        wrapper.setRpcName(TORNADO);
        assertEquals(TORNADO, wrapper.getRpcName());

        wrapper.setCorrelationId(YELLOW_BRICK_ROAD);
        assertEquals(YELLOW_BRICK_ROAD, wrapper.getCorrelationId());

        wrapper.setType(MUNCHKIN);
        assertEquals(MUNCHKIN, wrapper.getType());

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
        wrapper.setVersion(VERSION_19);
        assertFalse(wrapper.equals(copiedPciWrapper));
        copiedPciWrapper.setVersion(VERSION_19);
        assertTrue(wrapper.equals(copiedPciWrapper));

        wrapper.setCambriaPartition(null);
        assertFalse(wrapper.equals(copiedPciWrapper));
        copiedPciWrapper.setCambriaPartition(null);
        assertTrue(wrapper.equals(copiedPciWrapper));
        wrapper.setCambriaPartition(THE_EMERALD_CITY);
        assertFalse(wrapper.equals(copiedPciWrapper));
        copiedPciWrapper.setCambriaPartition(THE_EMERALD_CITY);
        assertTrue(wrapper.equals(copiedPciWrapper));

        wrapper.setRpcName(null);
        assertFalse(wrapper.equals(copiedPciWrapper));
        copiedPciWrapper.setRpcName(null);
        assertTrue(wrapper.equals(copiedPciWrapper));
        wrapper.setRpcName(TORNADO);
        assertFalse(wrapper.equals(copiedPciWrapper));
        copiedPciWrapper.setRpcName(TORNADO);
        assertTrue(wrapper.equals(copiedPciWrapper));

        wrapper.setCorrelationId(null);
        assertFalse(wrapper.equals(copiedPciWrapper));
        copiedPciWrapper.setCorrelationId(null);
        assertTrue(wrapper.equals(copiedPciWrapper));
        wrapper.setCorrelationId(YELLOW_BRICK_ROAD);
        assertFalse(wrapper.equals(copiedPciWrapper));
        copiedPciWrapper.setCorrelationId(YELLOW_BRICK_ROAD);
        assertTrue(wrapper.equals(copiedPciWrapper));

        wrapper.setType(null);
        assertFalse(wrapper.equals(copiedPciWrapper));
        copiedPciWrapper.setType(null);
        assertTrue(wrapper.equals(copiedPciWrapper));
        wrapper.setType(MUNCHKIN);
        assertFalse(wrapper.equals(copiedPciWrapper));
        copiedPciWrapper.setType(MUNCHKIN);
        assertTrue(wrapper.equals(copiedPciWrapper));
    }
}
