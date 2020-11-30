/*-
 * ============LICENSE_START=======================================================
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
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ToscaCapabilityAssignment extends ToscaEntity implements Comparable<ToscaEntity> {
    private Map<String, Object> properties;
    private Map<String, Object> attributes;
    private List<Object> occurrences;

    @Override
    public int compareTo(final ToscaEntity other) {
        return compareNameVersion(this, other);
    }

    @Override
    public boolean equals(final Object other) {
        return compareTo((ToscaEntity) other) == 0;
    }
}
