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

import lombok.Getter;
import lombok.Setter;
import org.onap.policy.models.pdp.enums.PdpMessageType;
import org.onap.policy.models.pdp.enums.PdpState;

/**
 * Class to represent the PDP_STATE_CHANGE message that PAP will send to either
 * PDPGroup/Subgroup or a PDP.
 *
 * @author Ram Krishna Verma (ram.krishna.verma@est.tech)
 */
@Getter
@Setter
public class PdpStateChange extends PdpMessage {

    private PdpState state;

    /**
     * Constructor for instantiating PdpStateChange class with message name.
     *
     */
    public PdpStateChange() {
        super(PdpMessageType.PDP_STATE_CHANGE);
    }

    /**
     * Constructs the object, making a deep copy.
     *
     * @param source source from which to copy
     */
    public PdpStateChange(PdpStateChange source) {
        super(source);

        this.state = source.state;
    }

    @Override
    public String toString() {
        return "PdpStateChange [state=" + state + ", getName()=" + getName() + ", getPdpGroup()=" + getPdpGroup()
                        + ", getPdpSubgroup()=" + getPdpSubgroup() + "]";
    }
}
