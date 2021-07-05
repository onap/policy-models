/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2021 Nordix Foundation.
 *  Modifications Copyright (C) 2019 AT&T Intellectual Property.
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


import java.time.Instant;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.onap.policy.models.base.PfUtils;

/**
 * Class to represent statistics of a running PDP.
 *
 * @author Ram Krishna Verma (ram.krishna.verma@est.tech)
 */
@Data
@NoArgsConstructor
public class PdpStatistics {

    private String pdpInstanceId;
    private Instant timeStamp;
    private Long generatedId;
    private String pdpGroupName;
    private String pdpSubGroupName;
    private long policyExecutedCount;
    private long policyExecutedSuccessCount;
    private long policyExecutedFailCount;
    private long policyDeployCount;
    private long policyDeploySuccessCount;
    private long policyDeployFailCount;
    private long policyUndeployCount;
    private long policyUndeploySuccessCount;
    private long policyUndeployFailCount;
    private List<PdpEngineWorkerStatistics> engineStats;

    /**
     * Constructs the object, making a deep copy.
     *
     * @param source source from which to copy
     */
    public PdpStatistics(@NonNull PdpStatistics source) {
        this.pdpInstanceId = source.pdpInstanceId;
        this.timeStamp = source.timeStamp;
        this.generatedId = source.generatedId;
        this.pdpGroupName = source.pdpGroupName;
        this.pdpSubGroupName = source.pdpSubGroupName;
        this.policyExecutedCount = source.policyExecutedCount;
        this.policyExecutedFailCount = source.policyExecutedFailCount;
        this.policyExecutedSuccessCount = source.policyExecutedSuccessCount;
        this.policyDeployCount = source.policyDeployCount;
        this.policyDeployFailCount = source.policyDeployFailCount;
        this.policyDeploySuccessCount = source.policyDeploySuccessCount;
        this.policyUndeployCount = source.policyUndeployCount;
        this.policyUndeployFailCount = source.policyUndeployFailCount;
        this.policyUndeploySuccessCount = source.policyUndeploySuccessCount;
        this.engineStats = PfUtils.mapList(source.engineStats, PdpEngineWorkerStatistics::new, null);
    }
}
