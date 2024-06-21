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

package org.onap.policy.models.tosca.authorative.concepts;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.Map;
import org.junit.jupiter.api.Test;

class ToscaWithToscaPropertiesTest {

    @Test
    void test() {
        ToscaProperty prop1 = new ToscaProperty();
        prop1.setDescription("description A");

        ToscaProperty prop2 = new ToscaProperty();
        prop2.setDescription("description B");

        ToscaWithToscaProperties tosca = new ToscaWithToscaProperties();
        assertEquals(tosca, new ToscaWithToscaProperties(tosca));

        tosca.setProperties(Map.of("abc", prop1, "def", prop2));
        assertEquals(tosca, new ToscaWithToscaProperties(tosca));

        assertNotEquals(tosca, new ToscaWithToscaProperties());

        assertThatThrownBy(() -> new ToscaWithToscaProperties(null)).hasMessageContaining("copyObject")
                        .hasMessageContaining("is null");
    }
}
