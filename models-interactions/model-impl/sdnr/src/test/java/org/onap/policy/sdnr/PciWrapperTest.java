/*-
 * ============LICENSE_START=======================================================
 * sdnr
 * ================================================================================
 * Copyright (C) 2018 Wipro Limited Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019, 2024 Nordix Foundation.
 * Modifications Copyright (C) 2019-2021 AT&T Intellectual Property. All rights reserved.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.function.BiConsumer;
import org.junit.jupiter.api.Test;

class PciWrapperTest {

    private static final String YELLOW_BRICK_ROAD = "YellowBrickRoad";
    private static final String TORNADO = "Tornado";
    private static final String THE_EMERALD_CITY = "The Emerald City";
    private static final String MUNCHKIN = "Munchkin";
    private static final String VERSION_19 = "19.3.9";

    @Test
    void testPciWrapper() {
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

        assertEquals("PciWrapper(version=19.3.9, cambriaPartition=The ", wrapper.toString().substring(0, 48));

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

        checkField(VERSION_19, PciWrapper::setVersion);
        checkField(THE_EMERALD_CITY, PciWrapper::setCambriaPartition);
        checkField(TORNADO, PciWrapper::setRpcName);
        checkField(YELLOW_BRICK_ROAD, PciWrapper::setCorrelationId);
        checkField(MUNCHKIN, PciWrapper::setType);
    }

    private <T> void checkField(T value, BiConsumer<PciWrapper, T> setter) {
        PciWrapper details1 = new PciWrapper();
        PciWrapper details2 = new PciWrapper();

        setter.accept(details2, null);

        setter.accept(details1, value);
        assertNotEquals(details1, details2);

        setter.accept(details2, value);
        assertEquals(details1, details2);

        setter.accept(details1, null);
        assertNotEquals(details1, details2);
    }
}
