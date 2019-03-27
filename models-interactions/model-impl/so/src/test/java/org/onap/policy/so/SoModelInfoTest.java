/*-
 * ============LICENSE_START=======================================================
 * so
 * ================================================================================
 * 
 * Modifications Copyright (C) 2019 AT&T Intellectual Property. All rights reserved
 * Modifications Copyright (C) 2019 Nordix Foundation.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SoModelInfoTest {

    @Test
    public void testConstructor() {
        SoModelInfo obj = new SoModelInfo();

        assertTrue(obj.getModelCustomizationId() == null);
        assertTrue(obj.getModelCustomizationName() == null);
        assertTrue(obj.getModelInvariantId() == null);
        assertTrue(obj.getModelName() == null);
        assertTrue(obj.getModelType() == null);
        assertTrue(obj.getModelVersion() == null);
        assertTrue(obj.getModelVersionId() == null);
    }

    @Test
    public void testSetGet() {
        SoModelInfo obj = new SoModelInfo();

        obj.setModelCustomizationId("modelCustomizationId");
        assertEquals("modelCustomizationId", obj.getModelCustomizationId());

        obj.setModelCustomizationName("modelCustomizationName");
        assertEquals("modelCustomizationName", obj.getModelCustomizationName());

        obj.setModelInvariantId("modelInvariantId");
        assertEquals("modelInvariantId", obj.getModelInvariantId());

        obj.setModelName("modelName");
        assertEquals("modelName", obj.getModelName());

        obj.setModelType("modelType");
        assertEquals("modelType", obj.getModelType());

        obj.setModelVersion("modelVersion");
        assertEquals("modelVersion", obj.getModelVersion());

        obj.setModelVersionId("modelVersionId");
        assertEquals("modelVersionId", obj.getModelVersionId());

    }
}
