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

import java.util.concurrent.atomic.AtomicLong;

/**
 * Class to store the statistics related to deployment actions on policies.
 *
 * @author Adheli Tavares (adheli.tavares@est.tech)
 *
 */
public class PolicyDeploymentStatistics {

    private static final long ZERO = 0;
    private final AtomicLong policyDeployCount = new AtomicLong(ZERO);
    private final AtomicLong policyDeploySuccessCount = new AtomicLong(ZERO);
    private final AtomicLong policyDeployFailureCount = new AtomicLong(ZERO);
    private final AtomicLong policyUndeployCount = new AtomicLong(ZERO);
    private final AtomicLong policyUndeploySuccessCount = new AtomicLong(ZERO);
    private final AtomicLong policyUndeployFailureCount = new AtomicLong(ZERO);

    /**
     * Reset all the statistics counts to 0.
     */
    public void resetAllStatistics() {
        policyDeployCount.set(ZERO);
        policyDeploySuccessCount.set(ZERO);
        policyDeployFailureCount.set(ZERO);
        policyUndeployCount.set(ZERO);
        policyUndeploySuccessCount.set(ZERO);
        policyUndeployFailureCount.set(ZERO);
    }

    /**
     * Method to update the policy deploy count.
     * If {@code isSuccess} is {@code true}, updates success count
     * If {@code isSuccess} is {@code false}, updates failure count
     *
     * @param isSuccess {@code true} if deploy was successful, otherwise {@code false}
     * @return the updated value of policyDeployCount
     */
    public long updatePolicyDeployCount(final boolean isSuccess) {
        if (isSuccess) {
            policyDeploySuccessCount.incrementAndGet();
        } else {
            policyDeployFailureCount.incrementAndGet();
        }
        return policyDeployCount.incrementAndGet();
    }

    /**
     * Method to update the policy undeploy count.
     * If {@code isSuccess} is {@code true}, updates success count
     * If {@code isSuccess} is {@code false}, updates failure count
     *
     * @param isSuccess {@code true} if undeploy was successful, otherwise {@code false}
     * @return the updated value of policyUndeployCount
     */
    public long updatePolicyUndeployCount(final boolean isSuccess) {
        if (isSuccess) {
            policyUndeploySuccessCount.incrementAndGet();
        } else {
            policyUndeployFailureCount.incrementAndGet();
        }
        return policyUndeployCount.incrementAndGet();
    }

    public long getPolicyDeployCount() {
        return policyDeployCount.get();
    }

    public long getPolicyDeploySuccessCount() {
        return policyDeploySuccessCount.get();
    }

    public long getPolicyDeployFailureCount() {
        return policyDeployFailureCount.get();
    }

    public long getPolicyUndeployCount() {
        return policyUndeployCount.get();
    }

    public long getPolicyUndeploySuccessCount() {
        return policyUndeploySuccessCount.get();
    }

    public long getPolicyUndeployFailureCount() {
        return policyUndeployFailureCount.get();
    }
}
