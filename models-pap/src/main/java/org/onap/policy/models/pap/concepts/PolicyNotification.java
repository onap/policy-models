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
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PolicyNotification {

    /**
     * Status of policies that are being added to PDPs.
     */
    @SerializedName("deployed-policies")
    private List<PolicyStatus> added = new ArrayList<>();

    /**
     * Status of policies that are being deleted from PDPs.
     */
    @SerializedName("undeployed-policies")
    private List<PolicyStatus> deleted = new ArrayList<>();


    public PolicyNotification(List<PolicyStatus> added, List<PolicyStatus> deleted) {
        this.added = added;
        this.deleted = deleted;
    }

    /**
     * Determines if the notification is empty (i.e., has no added or delete policy
     * notifications).
     *
     * @return {@code true} if the notification is empty, {@code false} otherwise
     */
    public boolean isEmpty() {
        return (added.isEmpty() && deleted.isEmpty());
    }
}
