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

package org.onap.policy.models.tosca.authorative.concepts;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.Map;
import org.junit.Test;

public class ToscaWithObjectPropertiesTest {

    @Test
    public void testCopyConstructor() {
        ToscaWithObjectProperties tosca = new ToscaWithObjectProperties();
        assertEquals(tosca, new ToscaWithObjectProperties(tosca));

        tosca.setProperties(Map.of("abc", 10, "def", "world"));
        assertEquals(tosca, new ToscaWithObjectProperties(tosca));

        assertNotEquals(tosca, new ToscaWithObjectProperties());

        assertThatThrownBy(() -> new ToscaWithObjectProperties(null)).hasMessageContaining("copyObject")
                        .hasMessageContaining("is null");
    }
}
