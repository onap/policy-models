/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy Model
 * ================================================================================
 * Copyright (C) 2020 Nordix Foundation.
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

package org.onap.policy.models.tosca.authorative.concepts;

import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ToscaNodeTemplate extends ToscaEntity implements Comparable<ToscaNodeTemplate> {
    private String type;
    private Map<String, Object> properties;
    private List<Map<String, ToscaRequirement>> requirements;
    private Map<String, ToscaCapabilityAssignment> capabilities;

    @Override
    public int compareTo(final ToscaNodeTemplate other) {
        return compareNameVersion(this, other);
    }

    @Override
    public boolean equals(final Object other) {
        return compareTo((ToscaNodeTemplate) other) == 0;
    }
}
