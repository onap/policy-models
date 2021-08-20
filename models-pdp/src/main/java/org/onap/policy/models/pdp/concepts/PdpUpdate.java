/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
 *  Modifications Copyright (C) 2019, 2021 AT&T Intellectual Property.
 *  Modifications Copyright (C) 2021 Nordix Foundation.
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

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.onap.policy.models.pdp.enums.PdpMessageType;
import org.onap.policy.models.tosca.authorative.concepts.ToscaConceptIdentifier;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicy;

/**
 * Class to represent the PDP_UPDATE message that PAP will send to a PDP. When a PDP
 * receives this message, it should save the group and subgroup and pass them to
 * {@link #appliesTo(String, String, String)} of subsequent messages that it receives.
 *
 * @author Ram Krishna Verma (ram.krishna.verma@est.tech)
 */
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class PdpUpdate extends PdpMessage {

    /**
     * System from which the message originated.
     */
    private String source;

    /**
     * Description of the PDP group.
     */
    private String description;

    private Long pdpHeartbeatIntervalMs;

    /**
     * Policies that the PDP should deploy.
     */
    private List<ToscaPolicy> policiesToBeDeployed = new LinkedList<>();

    /**
     * Policies that the PDP should undeploy.
     */
    private List<ToscaConceptIdentifier> policiesToBeUndeployed = new LinkedList<>();

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
        super(source);

        this.source = source.source;
        this.description = source.description;
        this.pdpHeartbeatIntervalMs = source.pdpHeartbeatIntervalMs;
        this.policiesToBeDeployed = (source.policiesToBeDeployed == null ? null
                : source.policiesToBeDeployed.stream().map(ToscaPolicy::new).collect(Collectors.toList()));
        this.policiesToBeUndeployed = (source.policiesToBeUndeployed == null ? null
                : source.policiesToBeUndeployed.stream().map(ToscaConceptIdentifier::new).collect(Collectors.toList()));
    }
}
