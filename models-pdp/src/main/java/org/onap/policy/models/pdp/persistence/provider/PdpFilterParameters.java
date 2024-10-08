/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2024 Nordix Foundation
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

package org.onap.policy.models.pdp.persistence.provider;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.onap.policy.models.dao.PfFilterParametersIntfc;

@Getter
@Builder
public class PdpFilterParameters implements PfFilterParametersIntfc {
    // PDP instance ID
    private String name;

    private Instant startTime;
    private Instant endTime;

    @Setter
    private int recordNum;
    @Builder.Default
    private String sortOrder = "DESC";

    private String group;
    private String subGroup;

    // initialized lazily, if not set via the builder
    private Map<String, Object> filterMap;

    @Override
    public Map<String, Object> getFilterMap() {
        if (filterMap != null) {
            return filterMap;

        } else if (group == null) {
            return Collections.emptyMap();

        } else if (subGroup == null) {
            filterMap = Map.of("pdpGroupName", group);
            return filterMap;

        } else {
            filterMap = Map.of("pdpGroupName", group, "pdpSubGroupName", subGroup);
            return filterMap;
        }
    }

    @Override
    public String getVersion() {
        // version is not used
        return null;
    }
}
