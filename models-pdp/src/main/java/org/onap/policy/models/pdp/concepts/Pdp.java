/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
 *  Modifications Copyright (C) 2019, 2021 AT&T Intellectual Property. All rights reserved.
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
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import org.onap.policy.models.pdp.enums.PdpHealthStatus;
import org.onap.policy.models.pdp.enums.PdpState;

/**
 * Class to represent details of a running instance of PDP.
 *
 * @author Ram Krishna Verma (ram.krishna.verma@est.tech)
 */
@ToString
@NoArgsConstructor
@Data
public class Pdp {

    @NonNull
    private String instanceId;

    @NonNull
    private PdpState pdpState;

    private PdpHealthStatus healthy;
    private String message;

    /**
     * Time when the record was last updated as a result of receiving a message from the
     * PDP.
     */
    private Instant lastUpdate;


    /**
     * Constructs the object, creating a deep copy of the fields from the source.
     *
     * @param source source from which to copy the fields
     */
    public Pdp(Pdp source) {
        this.instanceId = source.instanceId;
        this.pdpState = source.pdpState;
        this.healthy = source.healthy;
        this.message = source.message;
        this.lastUpdate = source.lastUpdate;
    }
}
