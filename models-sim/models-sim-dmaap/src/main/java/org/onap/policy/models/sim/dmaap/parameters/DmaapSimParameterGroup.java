/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
 *  Modifications Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.models.sim.dmaap.parameters;

import lombok.Getter;
import org.onap.policy.common.parameters.ParameterGroupImpl;
import org.onap.policy.common.parameters.annotations.Min;
import org.onap.policy.common.parameters.annotations.NotBlank;
import org.onap.policy.common.parameters.annotations.NotNull;

/**
 * Class to hold all parameters needed for the DMaaP simulator component.
 */
@NotNull
@NotBlank
@Getter
public class DmaapSimParameterGroup extends ParameterGroupImpl {
    private RestServerParameters restServerParameters;

    /**
     * Frequency, in milliseconds, with which to sweep the topics of idle consumers. On
     * each sweep cycle, if a consumer group has had no new poll requests since the last
     * sweep cycle, it is removed.
     */
    @Min(1)
    private long topicSweepMs;

    /**
     * Create the DMaaP simulator parameter group.
     *
     * @param name the parameter group name
     */
    public DmaapSimParameterGroup(final String name) {
        super(name);
    }
}
