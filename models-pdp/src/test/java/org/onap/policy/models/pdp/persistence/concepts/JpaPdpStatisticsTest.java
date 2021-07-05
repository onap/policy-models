/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2021 Nordix Foundation.
 *  Modifications Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.models.pdp.persistence.concepts;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.Instant;
import java.util.ArrayList;
import org.junit.Test;
import org.onap.policy.models.base.PfGeneratedIdKey;
import org.onap.policy.models.pdp.concepts.PdpStatistics;

/**
 * Test the {@link JpaPdpStatistics} class.
 */
public class JpaPdpStatisticsTest {

    @Test
    public void testConstructor() {
        assertThatThrownBy(() -> new JpaPdpStatistics((PfGeneratedIdKey) null)).hasMessageContaining("key");

        assertThatThrownBy(() -> new JpaPdpStatistics((JpaPdpStatistics) null)).hasMessageContaining("copyConcept");

        assertThatThrownBy(() -> new JpaPdpStatistics((PdpStatistics) null)).hasMessageContaining("authorativeConcept");

        assertNotNull(new JpaPdpStatistics());
        assertNotNull(new JpaPdpStatistics(new PfGeneratedIdKey()));

        PdpStatistics pdpStat = createPdpStatistics();
        JpaPdpStatistics jpaPdpStat = new JpaPdpStatistics(createPdpStatistics());
        checkEquals(pdpStat, jpaPdpStat);

        JpaPdpStatistics jpaPdpStat2 = new JpaPdpStatistics(jpaPdpStat);
        assertEquals(0, jpaPdpStat2.compareTo(jpaPdpStat));
    }

    @Test
    public void testFromAuthorative() {
        PdpStatistics pdpStat = createPdpStatistics();
        JpaPdpStatistics jpaPdpStat = new JpaPdpStatistics();
        jpaPdpStat.fromAuthorative(pdpStat);
        checkEquals(pdpStat, jpaPdpStat);
    }

    @Test
    public void testToAuthorative() {
        PdpStatistics pdpStat = createPdpStatistics();
        JpaPdpStatistics jpaPdpStat = new JpaPdpStatistics(pdpStat);
        PdpStatistics toPdpStat = jpaPdpStat.toAuthorative();
        assertEquals(pdpStat, toPdpStat);
    }

    @Test
    public void testCompareTo() {
        PdpStatistics pdpStat = createPdpStatistics();
        JpaPdpStatistics jpaPdpStat1 = new JpaPdpStatistics(pdpStat);
        assertEquals(-1, jpaPdpStat1.compareTo(null));

        JpaPdpStatistics jpaPdpStat2 = new JpaPdpStatistics(pdpStat);
        assertEquals(0, jpaPdpStat1.compareTo(jpaPdpStat2));

        PdpStatistics pdpStat3 = createPdpStatistics();
        pdpStat3.setPdpInstanceId("PDP3");
        JpaPdpStatistics jpaPdpStat3 = new JpaPdpStatistics(pdpStat3);
        assertNotEquals(0, jpaPdpStat1.compareTo(jpaPdpStat3));
    }

    @Test
    public void testValidate() {
        JpaPdpStatistics nullKeyJpaPdpStat = new JpaPdpStatistics();
        assertFalse(nullKeyJpaPdpStat.validate("").isValid());

        PdpStatistics pdpStat = createPdpStatistics();
        JpaPdpStatistics jpaPdpStat2 = new JpaPdpStatistics(pdpStat);
        assertTrue(jpaPdpStat2.validate("").isValid());
    }

    @Test
    public void testClean() {
        PdpStatistics pdpStat = createPdpStatistics();
        JpaPdpStatistics jpaPdpStat = new JpaPdpStatistics(pdpStat);
        jpaPdpStat.setPdpGroupName(" PDPGroup0 ");
        jpaPdpStat.setPdpSubGroupName(" PDPSubGroup0 ");
        jpaPdpStat.clean();
        assertEquals("PDPGroup0", jpaPdpStat.getPdpGroupName());
        assertEquals("PDPSubGroup0", jpaPdpStat.getPdpSubGroupName());
    }

    private void checkEquals(PdpStatistics pdpStat, JpaPdpStatistics jpaPdpStat) {
        assertEquals(pdpStat.getPdpInstanceId(), jpaPdpStat.getKey().getName());
        assertEquals(pdpStat.getPdpGroupName(), jpaPdpStat.getPdpGroupName());
        assertEquals(pdpStat.getPdpSubGroupName(), jpaPdpStat.getPdpSubGroupName());
        assertEquals(pdpStat.getTimeStamp(), jpaPdpStat.getTimeStamp().toInstant());
        assertEquals(pdpStat.getPolicyDeployCount(), jpaPdpStat.getPolicyDeployCount());
        assertEquals(pdpStat.getPolicyDeploySuccessCount(), jpaPdpStat.getPolicyDeploySuccessCount());
        assertEquals(pdpStat.getPolicyDeployFailCount(), jpaPdpStat.getPolicyDeployFailCount());
        assertEquals(pdpStat.getPolicyUndeployCount(), jpaPdpStat.getPolicyUndeployCount());
        assertEquals(pdpStat.getPolicyUndeploySuccessCount(), jpaPdpStat.getPolicyUndeploySuccessCount());
        assertEquals(pdpStat.getPolicyUndeployFailCount(), jpaPdpStat.getPolicyUndeployFailCount());
        assertEquals(pdpStat.getPolicyExecutedCount(), jpaPdpStat.getPolicyExecutedCount());
        assertEquals(pdpStat.getPolicyExecutedSuccessCount(), jpaPdpStat.getPolicyExecutedSuccessCount());
        assertEquals(pdpStat.getPolicyExecutedFailCount(), jpaPdpStat.getPolicyExecutedFailCount());
    }

    private PdpStatistics createPdpStatistics() {
        PdpStatistics pdpStat = new PdpStatistics();
        pdpStat.setPdpInstanceId("PDP0");
        pdpStat.setPdpGroupName("PDPGroup0");
        pdpStat.setPdpSubGroupName("PDPSubGroup0");
        pdpStat.setGeneratedId(10001L);
        pdpStat.setTimeStamp(Instant.EPOCH);
        pdpStat.setPolicyDeployCount(3);
        pdpStat.setPolicyDeploySuccessCount(1);
        pdpStat.setPolicyDeployFailCount(2);
        pdpStat.setPolicyUndeployCount(3);
        pdpStat.setPolicyUndeploySuccessCount(1);
        pdpStat.setPolicyUndeployFailCount(2);
        pdpStat.setPolicyExecutedCount(9);
        pdpStat.setPolicyExecutedSuccessCount(4);
        pdpStat.setPolicyExecutedFailCount(5);
        pdpStat.setEngineStats(new ArrayList<>());
        return pdpStat;
    }
}
