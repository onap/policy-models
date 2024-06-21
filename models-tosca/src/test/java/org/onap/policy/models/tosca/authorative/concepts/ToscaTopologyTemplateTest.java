/*
 * ============LICENSE_START=======================================================
 * ONAP Policy Models
 * ================================================================================
 * Copyright (C) 2021 Nordix Foundation.
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

package org.onap.policy.models.tosca.authorative.concepts;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import org.junit.jupiter.api.Test;

class ToscaTopologyTemplateTest {

    @Test
    void testToscTopologyTemplate() {
        assertThatThrownBy(() -> {
            new ToscaTopologyTemplate(null);
        }).hasMessageMatching("copyObject is marked .*on.*ull but is null");

        assertNotNull(new ToscaTopologyTemplate(new ToscaTopologyTemplate()));

        ToscaTopologyTemplate origTt = new ToscaTopologyTemplate();

        assertEquals(origTt, new ToscaTopologyTemplate(origTt));

        origTt.setDescription("A Description");
        origTt.setInputs(new HashMap<>());
        origTt.setNodeTemplates(new HashMap<>());
        origTt.setPolicies(new ArrayList<>());
        assertEquals(origTt, new ToscaTopologyTemplate(origTt));

        assertEquals(origTt.getPoliciesAsMap(), new ToscaTopologyTemplate(origTt).getPoliciesAsMap());
    }
}
