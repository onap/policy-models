/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2020-2021 Nordix Foundation.
 * Modifications Copyright (C) 2021 AT&T Intellectual Property. All rights reserved.
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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@ToString(callSuper = true)
public class ToscaCapabilityAssignment extends ToscaWithTypeAndObjectProperties {
    private Map<String, Object> attributes;
    private List<Object> occurrences;

    /**
     * Copy constructor.
     *
     * @param copyObject object to copy
     */
    public ToscaCapabilityAssignment(@NonNull ToscaCapabilityAssignment copyObject) {
        super(copyObject);

        if (copyObject.attributes != null) {
            attributes = new LinkedHashMap<>(copyObject.attributes);
        }

        if (copyObject.occurrences != null) {
            occurrences = new ArrayList<>(copyObject.occurrences);
        }
    }
}
