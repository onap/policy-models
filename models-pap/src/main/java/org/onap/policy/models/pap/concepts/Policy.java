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

package org.onap.policy.models.pap.concepts;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

/**
 * Class to represent a policy running in a PDP.
 *
 * @author Ram Krishna Verma (ram.krishna.verma@est.tech)
 */
@Getter
@Setter
@ToString
public class Policy {

    private String name;
    private String policyVersion;
    private String policyType;
    private String policyTypeVersion;
    private String policyTypeImpl;

    /**
     * Constructs the object.
     */
    public Policy() {
        super();
    }

    /**
     * Constructs the object, making a deep copy from the source.
     *
     * @param source source from which to copy fields
     */
    public Policy(@NonNull Policy source) {
        this.name = source.name;
        this.policyVersion = source.policyVersion;
        this.policyType = source.policyType;
        this.policyTypeVersion = source.policyTypeVersion;
        this.policyTypeImpl = source.policyTypeImpl;
    }
}
