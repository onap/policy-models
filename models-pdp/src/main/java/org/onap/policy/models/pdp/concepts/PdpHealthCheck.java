/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.onap.policy.models.pdp.enums.PdpMessageType;

/**
 * Class to represent the PDP_HEALTH_CHECK message that PAP will send to either PDPGroup/Subgroup or a PDP.
 *
 * @author Ram Krishna Verma (ram.krishna.verma@est.tech)
 */
@Getter
@Setter
@ToString
public class PdpHealthCheck extends PdpMessage {

    private String name;
    private String pdpGroup;
    private String pdpSubgroup;

    /**
     * Constructor for instantiating PdpHealthCheck class with message name.
     *
     */
    public PdpHealthCheck() {
        super(PdpMessageType.PDP_HEALTH_CHECK);
    }
}
