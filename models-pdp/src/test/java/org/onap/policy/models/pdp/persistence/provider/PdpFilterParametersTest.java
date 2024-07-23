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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;

class PdpFilterParametersTest {

    private static final String GROUP = "my-group";
    private static final String SUBGROUP = "my-subgroup";

    @Test
    void testGetFilterMap() {
        assertThat(PdpFilterParameters.builder().build().getFilterMap()).isEmpty();

        assertThat(PdpFilterParameters.builder().subGroup(SUBGROUP).build().getFilterMap()).isEmpty();

        PdpFilterParameters params = PdpFilterParameters.builder().group(GROUP).build();
        Map<String, Object> map = params.getFilterMap();
        assertThat(map).isEqualTo(Map.of("pdpGroupName", GROUP));

        // should not re-create the map
        assertThat(params.getFilterMap()).isSameAs(map);

        params = PdpFilterParameters.builder().group(GROUP).subGroup(SUBGROUP).build();
        map = params.getFilterMap();
        assertThat(map).isEqualTo(Map.of("pdpGroupName", GROUP, "pdpSubGroupName", SUBGROUP));

        // should not re-create the map
        assertThat(params.getFilterMap()).isSameAs(map);
    }
}
