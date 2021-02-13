/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy Model
 * ================================================================================
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

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ToscaNodeTemplate extends ToscaWithObjectProperties {
    private String type;
    private List<Map<String, ToscaRequirement>> requirements;
    private Map<String, ToscaCapabilityAssignment> capabilities;

    /**
     * Copy constructor.
     *
     * @param copyObject the obejct to copy from.
     */
    public ToscaNodeTemplate(@NonNull ToscaNodeTemplate copyObject) {
        super(copyObject);

        this.type = copyObject.type;

        this.requirements = (copyObject.requirements != null ? new ArrayList<>(copyObject.requirements) : null);
        this.capabilities = (copyObject.capabilities != null ? new LinkedHashMap<>(copyObject.capabilities) : null);
    }
}
