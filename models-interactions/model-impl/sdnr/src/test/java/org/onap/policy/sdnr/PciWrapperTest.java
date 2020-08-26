/*-
 * ============LICENSE_START=======================================================
 * sdnr
 * ================================================================================
 * Copyright (C) 2018 Wipro Limited Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019 Nordix Foundation.
 * Modifications Copyright (C) 2019-2020 AT&T Intellectual Property. All rights reserved.
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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

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

        assertEquals(wrapper, (Object) wrapper);
        assertEquals(wrapper, copiedPciWrapper);
        assertNotEquals(wrapper, null);
        assertNotEquals(wrapper, (Object) "Hello");

        wrapper.setVersion(null);
        assertNotEquals(wrapper, copiedPciWrapper);
        copiedPciWrapper.setVersion(null);
        assertEquals(wrapper, copiedPciWrapper);
        wrapper.setVersion(VERSION_19);
        assertNotEquals(wrapper, copiedPciWrapper);
        copiedPciWrapper.setVersion(VERSION_19);
        assertEquals(wrapper, copiedPciWrapper);

        wrapper.setCambriaPartition(null);
        assertNotEquals(wrapper, copiedPciWrapper);
        copiedPciWrapper.setCambriaPartition(null);
        assertEquals(wrapper, copiedPciWrapper);
        wrapper.setCambriaPartition(THE_EMERALD_CITY);
        assertNotEquals(wrapper, copiedPciWrapper);
        copiedPciWrapper.setCambriaPartition(THE_EMERALD_CITY);
        assertEquals(wrapper, copiedPciWrapper);

        wrapper.setRpcName(null);
        assertNotEquals(wrapper, copiedPciWrapper);
        copiedPciWrapper.setRpcName(null);
        assertEquals(wrapper, copiedPciWrapper);
        wrapper.setRpcName(TORNADO);
        assertNotEquals(wrapper, copiedPciWrapper);
        copiedPciWrapper.setRpcName(TORNADO);
        assertEquals(wrapper, copiedPciWrapper);

        wrapper.setCorrelationId(null);
        assertNotEquals(wrapper, copiedPciWrapper);
        copiedPciWrapper.setCorrelationId(null);
        assertEquals(wrapper, copiedPciWrapper);
        wrapper.setCorrelationId(YELLOW_BRICK_ROAD);
        assertNotEquals(wrapper, copiedPciWrapper);
        copiedPciWrapper.setCorrelationId(YELLOW_BRICK_ROAD);
        assertEquals(wrapper, copiedPciWrapper);

        wrapper.setType(null);
        assertNotEquals(wrapper, copiedPciWrapper);
        copiedPciWrapper.setType(null);
        assertEquals(wrapper, copiedPciWrapper);
        wrapper.setType(MUNCHKIN);
        assertNotEquals(wrapper, copiedPciWrapper);
        copiedPciWrapper.setType(MUNCHKIN);
        assertEquals(wrapper, copiedPciWrapper);
    }
}
