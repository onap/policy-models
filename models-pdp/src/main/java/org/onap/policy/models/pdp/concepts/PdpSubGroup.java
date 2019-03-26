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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import org.onap.policy.models.base.PfUtils;
import org.onap.policy.models.tosca.simple.concepts.ToscaEntityType;
import org.onap.policy.models.tosca.simple.concepts.ToscaPolicy;

/**
 * Class to represent a group of all PDP's of the same pdp type running for a particular
 * domain.
 *
 * @author Ram Krishna Verma (ram.krishna.verma@est.tech)
 */
@Getter
@Setter
@ToString
public class PdpSubGroup extends ToscaEntityType {
    private static final long serialVersionUID = 1L;

    private String pdpType;
    private List<PolicyTypeIdent> supportedPolicyTypes;
    private List<ToscaPolicy> policies;
    private int currentInstanceCount;
    private int desiredInstanceCount;
    private Map<String, String> properties;
    private List<PdpInstanceDetails> pdpInstances;

    /**
     * Constructs the object.
     */
    public PdpSubGroup() {
        super();
    }

    /**
     * Constructs the object, making a deep copy from the source.
     *
     * @param source source from which to copy fields
     */
    public PdpSubGroup(@NonNull PdpSubGroup source) {
        this.pdpType = source.pdpType;
        this.supportedPolicyTypes = PfUtils.mapList(source.supportedPolicyTypes, PolicyTypeIdent::new);
        this.policies = PfUtils.mapList(source.policies, ToscaPolicy::new);
        this.currentInstanceCount = source.currentInstanceCount;
        this.desiredInstanceCount = source.desiredInstanceCount;
        this.properties = (source.properties == null ? null : new LinkedHashMap<>(source.properties));
        this.pdpInstances = PfUtils.mapList(source.pdpInstances, PdpInstanceDetails::new);
    }
}
