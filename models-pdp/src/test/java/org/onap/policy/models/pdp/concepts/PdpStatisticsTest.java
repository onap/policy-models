/*
 * ============LICENSE_START=======================================================
 * ONAP Policy Models
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2020-2021 Nordix Foundation.
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

import java.time.Instant;
import java.util.ArrayList;
import org.junit.Test;

public class PdpStatisticsTest {

    @Test
    public void testCopyConstructor() {
        assertThatThrownBy(() -> new PdpStatistics(null)).hasMessageContaining("source");

        PdpStatistics orig = createPdpStatistics();
        PdpStatistics copied = new PdpStatistics(orig);
        assertEquals(orig, copied);
    }

    private PdpStatistics createPdpStatistics() {
        PdpStatistics pdpStat = new PdpStatistics();
        pdpStat.setPdpInstanceId("PDP0");
        pdpStat.setPdpGroupName("PDPGroup0");
        pdpStat.setPdpSubGroupName("PDPSubGroup0");
        pdpStat.setTimeStamp(Instant.EPOCH);
        pdpStat.setPolicyExecutedCount(9);
        pdpStat.setPolicyExecutedSuccessCount(4);
        pdpStat.setPolicyExecutedFailCount(5);
        pdpStat.setPolicyDeployCount(3);
        pdpStat.setPolicyDeploySuccessCount(1);
        pdpStat.setPolicyDeployFailCount(2);
        pdpStat.setPolicyUndeployCount(5);
        pdpStat.setPolicyUndeploySuccessCount(3);
        pdpStat.setPolicyUndeployFailCount(2);
        pdpStat.setEngineStats(new ArrayList<>());
        return pdpStat;
    }
}
