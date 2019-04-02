/*
 * ============LICENSE_START=======================================================
 * ONAP Policy Models
 * ================================================================================
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

package org.onap.policy.models.pdp.concepts;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Request deploy or update a set of groups via the PDP Group deployment
 * REST API.
 */
@Getter
@Setter
@ToString
public class PdpGroups {
    private List<PdpGroup> groups;

    /**
     * Get the contents of this class as a list of PDP group maps.
     * @return the PDP groups in a list of maps
     */
    public List<Map<String, PdpGroup>> toMapList() {
        final Map<String, PdpGroup> pdpGroupMap = new LinkedHashMap<>();
        for (PdpGroup pdpGroup : groups) {
            pdpGroupMap.put(pdpGroup.getName() + ':' + pdpGroup.getVersion() , pdpGroup);
        }

        return Collections.singletonList(pdpGroupMap);
    }
}
