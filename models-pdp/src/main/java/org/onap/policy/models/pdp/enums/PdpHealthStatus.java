/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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

package org.onap.policy.models.pdp.enums;

/**
 * Class to hold the possible values for health status of PDP.
 *
 * @author Ram Krishna Verma (ram.krishna.verma@est.tech)
 */
public enum PdpHealthStatus {

    /**
     * PDP is healthy and working fine.
     */
    HEALTHY,

    /**
     * PDP is not healthy.
     */
    NOT_HEALTHY,

    /**
     * PDP is currently under test state and performing tests.
     */
    TEST_IN_PROGRESS,
}
