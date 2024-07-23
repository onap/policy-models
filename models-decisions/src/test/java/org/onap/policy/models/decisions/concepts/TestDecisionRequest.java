/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy Decision Models
 * ================================================================================
 * Copyright (C) 2024 Nordix Foundation
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

package org.onap.policy.models.decisions.concepts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


class TestDecisionRequest {

    private DecisionRequest request1;

    @BeforeEach
    void setUp() {
        request1 = new DecisionRequest();
    }

    @Test
    void testConstructor() {
        DecisionRequest request2 = new DecisionRequest(request1);
        assertEquals(request1, request2);
    }

    @Test
    void testConstructorContextNotNull() {
        Map testMap = new HashMap<String, Object>();
        testMap.put("entry1", "test");
        request1.setContext(testMap);
        DecisionRequest request2 = new DecisionRequest(request1);
        assertThat(request2.toString()).contains("context={entry1=test}");
    }

    @Test
    void testConstructorResourceNotNull() {
        Map testMap = new HashMap<String, Object>();
        testMap.put("resourceEntry1", "test");
        request1.setResource(testMap);
        DecisionRequest request2 = new DecisionRequest(request1);
        assertThat(request2.toString()).contains("resource={resourceEntry1=test}");
    }
}
