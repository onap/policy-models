/*
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

package org.onap.policy.models.pdp.concepts;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import org.onap.policy.models.pdp.enums.PdpHealthStatus;
import org.onap.policy.models.pdp.enums.PdpState;

/**
 * Class to represent details of a running instance of PDP.
 *
 * @author Ram Krishna Verma (ram.krishna.verma@est.tech)
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
public class PdpInstanceDetails {

    @NonNull
    private String instanceId;

    @NonNull
    private PdpState pdpState;

    private PdpHealthStatus healthy;
    private String message;

    /**
     * Constructs the object, creating a deep copy of the fields from the source.
     *
     * @param source source from which to copy the fields
     */
    public PdpInstanceDetails(PdpInstanceDetails source) {
        this.instanceId = source.instanceId;
        this.pdpState = source.pdpState;
        this.healthy = source.healthy;
        this.message = source.message;
    }
}
