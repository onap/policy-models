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

package org.onap.policy.models.decisions.serialization;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestDecisionRequestMessageBodyHandler {

    DecisionRequestMessageBodyHandler handler;

    @BeforeEach
    void setUp() {
        handler = new DecisionRequestMessageBodyHandler();
    }

    @Test
    void testDecisionRequestMessageBodyHandlerConstructor() {
        GsonBuilder builder = new GsonBuilder();
        DecisionRequestMessageBodyHandler handler2 =
            new DecisionRequestMessageBodyHandler(builder);
        assertNotNull(handler);
        assertNotNull(handler2);
    }
}
