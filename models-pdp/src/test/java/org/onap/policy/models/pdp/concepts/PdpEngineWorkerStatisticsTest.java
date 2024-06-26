/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020-2024 Nordix Foundation.
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

package org.onap.policy.models.pdp.concepts;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.onap.policy.models.pdp.enums.PdpEngineWorkerState;

class PdpEngineWorkerStatisticsTest {

    @Test
    void testCopyConstructor() {
        assertThatThrownBy(() -> new PdpEngineWorkerStatistics(null)).hasMessageContaining("source");

        PdpEngineWorkerStatistics stat = createPdpEngineWorkerStatistics();
        PdpEngineWorkerStatistics stat2 = new PdpEngineWorkerStatistics(stat);
        assertEquals(stat, stat2);
    }

    @Test
    void testClean() {
        PdpEngineWorkerStatistics stat = createPdpEngineWorkerStatistics();
        stat.setEngineId(" Engine0 ");
        stat.clean();
        assertEquals("Engine0", stat.getEngineId());
    }

    private PdpEngineWorkerStatistics createPdpEngineWorkerStatistics() {
        PdpEngineWorkerStatistics stat = new PdpEngineWorkerStatistics();
        stat.setEngineId("Engine0");
        stat.setEngineWorkerState(PdpEngineWorkerState.READY);
        stat.setEngineTimeStamp(Instant.now().getEpochSecond());
        stat.setEventCount(1);
        stat.setLastExecutionTime(100);
        stat.setAverageExecutionTime(99);
        stat.setUpTime(1000);
        stat.setLastEnterTime(2000);
        stat.setLastStart(3000);
        return stat;
    }
}