/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
 *  Modifications Copyright (C) 2021 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.models.sim.pdp;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Names of various items contained in the Registry.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PdpSimulatorConstants {
    // Registry keys
    public static final String REG_PDP_SIMULATOR_ACTIVATOR = "object:activator/pdp_simulator";
    public static final String REG_PDP_STATUS_OBJECT = "object:pdp/status";
    public static final String REG_PDP_TOSCA_POLICY_LIST = "object:pdp/tosca/policy/list";
    public static final String REG_PDP_STATUS_PUBLISHER = "object:pdp/status/publisher";
    public static final String REG_PDP_TOPIC_SINKS = "object:pdp/topic/sinks";
}
