/*-
 * ============LICENSE_START=======================================================
 * so
 * ================================================================================
 *
 * Modifications Copyright (C) 2019-2020 AT&T Intellectual Property. All rights reserved
 * Modifications Copyright (C) 2019, 2024 Nordix Foundation.
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

package org.onap.policy.so;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

public class SoRelatedInstanceListElementTest {

    @Test
    public void testConstructor() {
        SoRelatedInstanceListElement obj = new SoRelatedInstanceListElement();

        assertNull(obj.getRelatedInstance());
    }

    @Test
    public void testSetGet() {
        SoRelatedInstanceListElement obj = new SoRelatedInstanceListElement();

        SoRelatedInstance relatedInstance = new SoRelatedInstance();
        obj.setRelatedInstance(relatedInstance);
        assertEquals(relatedInstance, obj.getRelatedInstance());
    }
}
