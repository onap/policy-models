/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
 *  Modifications Copyright (C) 2019, 2021 AT&T Intellectual Property.
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
 * Class to hold the possible values for the type of PDP messages.
 *
 * @author Ram Krishna Verma (ram.krishna.verma@est.tech)
 */
public enum PdpMessageType {

    /**
     * Used by PDPs to report status to PAP.
     */
    PDP_STATUS,

    /**
     * Used by PAP to update the policies running on PDPs, triggers a PDP_STATUS message with the result of the
     * PDP_UPDATE operation.
     */
    PDP_UPDATE,

    /**
     * Used by PAP to change the state of PDPs, triggers a PDP_STATUS message with the result of the PDP_STATE_CHANGE
     * operation.
     */
    PDP_STATE_CHANGE,

    /**
     * Used by PAP to order a health check on PDPs, triggers a PDP_STATUS message with the result of the
     * PDP_HEALTH_CHECK operation.
     */
    PDP_HEALTH_CHECK,

    /**
     * Used by PDPs to check their ability to send and receive messages on the PDP-PAP topic.
     */
    PDP_TOPIC_CHECK,
}
