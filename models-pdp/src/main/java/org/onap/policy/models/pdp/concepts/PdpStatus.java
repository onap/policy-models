/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2021 Nordix Foundation.
 *  Modifications Copyright (C) 2019, 2021 AT&T Intellectual Property.
 *  Modifications Copyright (C) 2023 Bell Canada. All rights reserved.
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

import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.onap.policy.models.base.PfUtils;
import org.onap.policy.models.pdp.enums.PdpHealthStatus;
import org.onap.policy.models.pdp.enums.PdpMessageType;
import org.onap.policy.models.pdp.enums.PdpState;
import org.onap.policy.models.tosca.authorative.concepts.ToscaConceptIdentifier;

/**
 * Class to represent the PDP_STATUS message that all the PDP's will send to PAP.
 *
 * @author Ram Krishna Verma (ram.krishna.verma@est.tech)
 */
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class PdpStatus extends PdpMessage {

    private String pdpType;
    private PdpState state;
    private PdpHealthStatus healthy;

    /**
     * Description of the PDP or the PDP type. May be left {@code null}.
     */
    private String description;

    private List<ToscaConceptIdentifier> policies;
    private String deploymentInstanceInfo;
    private String properties;
    private PdpResponseDetails response;

    /**
     * Constructor for instantiating PdpStatus class with message name.
     *
     */
    public PdpStatus() {
        super(PdpMessageType.PDP_STATUS);
    }

    /**
     * Constructs the object, making a deep copy.
     *
     * @param source source from which to copy
     */
    public PdpStatus(final PdpStatus source) {
        super(source);

        this.pdpType = source.pdpType;
        this.state = source.state;
        this.healthy = source.healthy;
        this.description = source.description;
        this.policies = PfUtils.mapList(source.policies, ToscaConceptIdentifier::new, new ArrayList<>(0));
        this.deploymentInstanceInfo = source.deploymentInstanceInfo;
        this.properties = source.properties;
        this.response = (source.response == null ? null : new PdpResponseDetails(source.response));
    }
}
