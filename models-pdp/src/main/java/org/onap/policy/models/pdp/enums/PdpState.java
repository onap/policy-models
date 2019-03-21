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
 * Class to hold the possible values for state of PDP.
 *
 * @author Ram Krishna Verma (ram.krishna.verma@est.tech)
 */
public enum PdpState {

    /**
     * Policy execution is always rejected irrespective of PDP type.
     */
    PASSIVE,

    /**
     * Policy execution proceeds, but changes to domain state or context are not carried out. The PDP returns an
     * indication that it is running in SAFE mode together with the action it would have performed if it was operating
     * in ACTIVE mode.
     */
    SAFE,

    /**
     * Policy execution proceeds and changes to domain and state are carried out in a test environment. The PDP returns
     * an indication that it is running in TEST mode together with the action it has performed on the test environment.
     */
    TEST,

    /**
     * Policy execution is executed in the live environment by the PDP.
     */
    ACTIVE,

    /**
     * Policy execution is terminated and not available.
     */
    TERMINATED
}
