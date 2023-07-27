/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2020, 2023 Nordix Foundation.
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

import jakarta.persistence.Embeddable;
import java.io.Serial;
import java.io.Serializable;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.onap.policy.models.pdp.enums.PdpEngineWorkerState;

@Embeddable
@Data
@NoArgsConstructor
public class PdpEngineWorkerStatistics implements Serializable {
    @Serial
    private static final long serialVersionUID = 8262176849743624013L;

    private String engineId;
    private PdpEngineWorkerState engineWorkerState;
    private long engineTimeStamp;
    private long eventCount;
    private long lastExecutionTime;
    private double averageExecutionTime;
    private long upTime;
    private long lastEnterTime;
    private long lastStart;

    /**
     * Constructs the object, making a deep copy.
     *
     * @param source source from which to copy
     */
    public PdpEngineWorkerStatistics(@NonNull PdpEngineWorkerStatistics source) {
        this.engineId = source.engineId;
        this.engineWorkerState = source.engineWorkerState;
        this.engineTimeStamp = source.engineTimeStamp;
        this.eventCount = source.eventCount;
        this.lastExecutionTime = source.lastExecutionTime;
        this.averageExecutionTime = source.averageExecutionTime;
        this.upTime = source.upTime;
        this.lastEnterTime = source.lastEnterTime;
        this.lastStart = source.lastStart;
    }

    /**
     * Tidy up any superfluous information such as leading and trailing white space.
     */
    public void clean() {
        if (engineId != null) {
            engineId = engineId.trim();
        }
    }
}
