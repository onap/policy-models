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

import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.onap.policy.models.pdp.enums.PdpMessageType;

/**
 * Class to represent the base class for various messages that will be exchanged between
 * PAP and PDP.
 *
 * @author Ram Krishna Verma (ram.krishna.verma@est.tech)
 */
@Getter
@Setter
@ToString
public class PdpMessage {

    @Setter(AccessLevel.NONE)
    private PdpMessageType messageName;

    private String requestId = UUID.randomUUID().toString();

    /**
     * Time-stamp, in milliseconds, when the message was created. Defaults to the current
     * time.
     */
    private long timestampMs = System.currentTimeMillis();

    /**
     * PDP name, or {@code null} for state-change broadcast messages.
     */
    private String name;

    /**
     * Group associated with the PDP. For state-change messages, this may be {@code null},
     * if the {@link #name} is provided.
     */
    private String pdpGroup;

    /**
     * Group associated with the PDP. For state-change messages, this may be {@code null},
     * if the {@link #name} is provided.
     */
    private String pdpSubgroup;


    /**
     * Constructor for instantiating PdpMessage class with message name.
     *
     * @param messageName the message name
     */
    public PdpMessage(final PdpMessageType messageName) {
        this.messageName = messageName;
    }

    /**
     * Constructs the object, making a deep copy. Does <i>not</i> copy the request id or
     * the time stamp.
     *
     * @param source source from which to copy
     */
    public PdpMessage(final PdpMessage source) {
        this.messageName = source.messageName;
        this.name = source.name;
        this.pdpGroup = source.pdpGroup;
        this.pdpSubgroup = source.pdpSubgroup;
    }
}
