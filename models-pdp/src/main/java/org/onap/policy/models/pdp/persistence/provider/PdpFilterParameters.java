/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2021 AT&T Intellectual Property. All rights reserved.
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
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import org.onap.policy.models.dao.PfFilterParametersIntfc;

@Getter
@Builder
public class PdpFilterParameters implements PfFilterParametersIntfc {
    private String name;
    private String version;
    private String group;
    private String subGroup;
    private Instant startTime;
    private Instant endTime;

    private int recordNum;
    private String sortOrder;

    @Override
    public Map<String, Object> getFilterMap() {
        if (group == null) {
            return null;
        } else if (subGroup == null) {
            return Map.of("pdpGroupName", group);
        } else {
            return Map.of("pdpGroupName", group, "pdpSubGroupName", subGroup);
        }
    }
}
