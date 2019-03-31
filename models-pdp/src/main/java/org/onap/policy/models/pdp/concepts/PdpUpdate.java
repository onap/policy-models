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

package org.onap.policy.models.pdp.concepts;

import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.onap.policy.models.pdp.enums.PdpMessageType;
import org.onap.policy.models.tosca.simple.concepts.ToscaPolicy;

/**
 * Class to represent the PDP_UPDATE message that PAP will send to a PDP.
 *
 * @author Ram Krishna Verma (ram.krishna.verma@est.tech)
 */
@Getter
@Setter
@ToString
public class PdpUpdate extends PdpMessage {

    private String name;
    private String pdpType;
    private String description;
    private String pdpGroup;
    private String pdpSubgroup;
    private List<ToscaPolicy> policies;

    /**
     * Constructor for instantiating PdpUpdate class with message name.
     *
     */
    public PdpUpdate() {
        super(PdpMessageType.PDP_UPDATE);
    }

    /**
     * Constructs the object, making a deep copy.
     *
     * @param source source from which to copy
     */
    public PdpUpdate(PdpUpdate source) {
        super(PdpMessageType.PDP_UPDATE);

        this.name = source.name;
        this.pdpType = source.pdpType;
        this.description = source.description;
        this.pdpGroup = source.pdpGroup;
        this.pdpSubgroup = source.pdpSubgroup;
        this.policies = (source.policies == null ? null
                        : source.policies.stream().map(ToscaPolicy::new).collect(Collectors.toList()));
    }
}
