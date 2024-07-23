/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy Models
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2021, 2024 Nordix Foundation.
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
 * ============LICENSE_END=========================================================
 */

package org.onap.policy.models.pap.concepts;

import java.util.List;
import lombok.ToString;
import org.onap.policy.models.tosca.authorative.concepts.ToscaConceptIdentifierOptVersion;

/**
 * Request deploy or update a set of policies using the <i>simple</i> PDP Group deployment REST API. Only the "name" and
 * "version" fields of a Policy are used, and only the "name" field is actually required.
 */
@ToString
public class PdpDeployPolicies {
    private List<PapPolicyIdentifier> policies;

    /**
     * Get the identifiers of the policies on the list.
     *
     * @return The list of identifiers
     */
    public List<ToscaConceptIdentifierOptVersion> getPolicies() {
        return policies == null ? null
                : policies.stream().map(PapPolicyIdentifier::getGenericIdentifier).toList();
    }

    /**
     * Set the identifiers of the policies on the list.
     *
     * @param policies The list of identifiers
     */
    public void setPolicies(final List<ToscaConceptIdentifierOptVersion> policies) {
        this.policies =
                policies == null ? null : policies.stream().map(PapPolicyIdentifier::new).toList();
    }
}
