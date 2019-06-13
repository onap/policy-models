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

    private static final String YELLOW_BRICK_ROAD = "YellowBrickRoad";
    private static final String TORNADO = "Tornado";
    private static final String THE_EMERALD_CITY = "The Emerald City";
    private static final String MUNCHKIN = "Munchkin";
    private static final String VERSION19 = "19.3.9";

    @Test
    public void testLcmWrapper() {
        LcmWrapper wrapper = new LcmWrapper();
        assertNotNull(wrapper);
        assertNotEquals(0, wrapper.hashCode());

        wrapper.setVersion(VERSION19);
        assertEquals(VERSION19, wrapper.getVersion());

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
        wrapper.setVersion(VERSION19);
        assertFalse(wrapper.equals(copiedLcmWrapper));
        copiedLcmWrapper.setVersion(VERSION19);
        assertTrue(wrapper.equals(copiedLcmWrapper));

        wrapper.setCambriaPartition(null);
        assertFalse(wrapper.equals(copiedLcmWrapper));
        copiedLcmWrapper.setCambriaPartition(null);
        assertTrue(wrapper.equals(copiedLcmWrapper));
        wrapper.setCambriaPartition(THE_EMERALD_CITY);
        assertFalse(wrapper.equals(copiedLcmWrapper));
        copiedLcmWrapper.setCambriaPartition(THE_EMERALD_CITY);
        assertTrue(wrapper.equals(copiedLcmWrapper));

        wrapper.setRpcName(null);
        assertFalse(wrapper.equals(copiedLcmWrapper));
        copiedLcmWrapper.setRpcName(null);
        assertTrue(wrapper.equals(copiedLcmWrapper));
        wrapper.setRpcName(TORNADO);
        assertFalse(wrapper.equals(copiedLcmWrapper));
        copiedLcmWrapper.setRpcName(TORNADO);
        assertTrue(wrapper.equals(copiedLcmWrapper));

        wrapper.setCorrelationId(null);
        assertFalse(wrapper.equals(copiedLcmWrapper));
        copiedLcmWrapper.setCorrelationId(null);
        assertTrue(wrapper.equals(copiedLcmWrapper));
        wrapper.setCorrelationId(YELLOW_BRICK_ROAD);
        assertFalse(wrapper.equals(copiedLcmWrapper));
        copiedLcmWrapper.setCorrelationId(YELLOW_BRICK_ROAD);
        assertTrue(wrapper.equals(copiedLcmWrapper));

        wrapper.setType(null);
        assertFalse(wrapper.equals(copiedLcmWrapper));
        copiedLcmWrapper.setType(null);
        assertTrue(wrapper.equals(copiedLcmWrapper));
        wrapper.setType(MUNCHKIN);
        assertFalse(wrapper.equals(copiedLcmWrapper));
        copiedLcmWrapper.setType(MUNCHKIN);
        assertTrue(wrapper.equals(copiedLcmWrapper));
    }
}
