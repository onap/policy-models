/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation.
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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class PolicyDeploymentStatisticsTest {

    @Test
    public void testResetAllStatistics() {
        PolicyDeploymentStatistics statistics = new PolicyDeploymentStatistics();

        statistics.updatePolicyDeployCount(true);
        statistics.updatePolicyDeployCount(false);
        statistics.updatePolicyUndeployCount(true);
        statistics.updatePolicyUndeployCount(false);

        assertCounts(statistics, 1, 1);

        statistics.resetAllStatistics();
        assertCounts(statistics, 0, 0);
    }

    @Test
    public void testUpdatePolicyDeployCount() {
        PolicyDeploymentStatistics statistics = new PolicyDeploymentStatistics();

        statistics.updatePolicyDeployCount(true);
        statistics.updatePolicyDeployCount(false);
        assertCounts(statistics, 1, 0);
    }

    @Test
    public void testUpdatePolicyUndeployCount() {
        PolicyDeploymentStatistics statistics = new PolicyDeploymentStatistics();

        statistics.updatePolicyUndeployCount(true);
        statistics.updatePolicyUndeployCount(false);
        assertCounts(statistics, 0, 1);
    }

    private void assertCounts(PolicyDeploymentStatistics statistics, long deploy, long undeploy) {
        assertEquals(deploy, statistics.getPolicyDeploySuccessCount());
        assertEquals(deploy, statistics.getPolicyDeployFailureCount());
        assertEquals(undeploy, statistics.getPolicyUndeploySuccessCount());
        assertEquals(undeploy, statistics.getPolicyUndeployFailureCount());

        long totalDeploy = statistics.getPolicyDeploySuccessCount() + statistics.getPolicyDeployFailureCount();
        assertEquals(totalDeploy, statistics.getPolicyDeployCount());

        long totalUndeploy = statistics.getPolicyUndeploySuccessCount() + statistics.getPolicyUndeployFailureCount();
        assertEquals(totalUndeploy, statistics.getPolicyUndeployCount());
    }

}
