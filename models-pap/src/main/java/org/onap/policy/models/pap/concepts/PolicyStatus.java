/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyIdentifier;

@Getter
@Setter
@ToString
public class PolicyStatus {
    private ToscaPolicyIdentifier policyId;

    /**
     * Number of PDPs that have successfully added/deleted the policy.
     */
    private int successCount;

    /**
     * Number of PDPs that were unable to add/delete the policy.
     */
    private int failureCount;

    /**
     * Number of PDPs that have not completed the add/delete operation.
     */
    private int incompleteCount;


    public PolicyStatus() {
        super();
    }

    public PolicyStatus(ToscaPolicyIdentifier policyId) {
        this.policyId = policyId;
    }
}
