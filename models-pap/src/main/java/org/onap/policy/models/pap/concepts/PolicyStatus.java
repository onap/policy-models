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

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyIdentifier;

@Data
@NoArgsConstructor
public class PolicyStatus {

    @SerializedName("policy-id")
    private ToscaPolicyIdentifier policyId;

    /**
     * Number of PDPs that have successfully added/deleted the policy.
     */
    @SerializedName("success-count")
    private int successCount = 0;

    /**
     * Number of PDPs that were unable to add/delete the policy.
     */
    @SerializedName("failure-count")
    private int failureCount = 0;

    /**
     * Number of PDPs that have not completed the add/delete operation.
     */
    @SerializedName("incomplete-count")
    private int incompleteCount = 0;


    public PolicyStatus(ToscaPolicyIdentifier policyId) {
        this.policyId = policyId;
    }
}
