/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.policy.models.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.onap.policy.models.base.testconcepts.DummyPfModel;

public class ModelServiceTest {

    @Test
    public void testModelService() {
        PfModelService.clear();

        assertFalse(PfModelService.existsModel("NonExistantName"));
        try {
            PfModelService.getModel("NonExistantName");
        } catch (final Exception e) {
            assertEquals("Model for name NonExistantName not found in model service", e.getMessage());
        }

        PfModelService.registerModel("ModelName", new DummyPfModel());
        assertTrue(PfModelService.existsModel("ModelName"));
        assertNotNull(PfModelService.getModel("ModelName"));

        PfModelService.deregisterModel("ModelName");

        assertFalse(PfModelService.existsModel("ModelName"));
        try {
            PfModelService.getModel("ModelName");
        } catch (final Exception e) {
            assertEquals("Model for name ModelName not found in model service", e.getMessage());
        }

        PfModelService.registerModel("ModelName", new DummyPfModel());
        assertTrue(PfModelService.existsModel("ModelName"));
        assertNotNull(PfModelService.getModel("ModelName"));

        PfModelService.clear();
        assertFalse(PfModelService.existsModel("ModelName"));
        try {
            PfModelService.getModel("ModelName");
        } catch (final Exception e) {
            assertEquals("Model for name ModelName not found in model service", e.getMessage());
        }

        try {
            PfModelService.registerModel(null, null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("modelKey is marked @NonNull but is null", exc.getMessage());
        }

        try {
            PfModelService.registerModel("nullModelName", null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("model is marked @NonNull but is null", exc.getMessage());
        }

        try {
            PfModelService.registerModel(null, new DummyPfModel());
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("modelKey is marked @NonNull but is null", exc.getMessage());
        }

        try {
            PfModelService.deregisterModel(null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("modelKey is marked @NonNull but is null", exc.getMessage());
        }

        try {
            PfModelService.getModel(null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("modelKey is marked @NonNull but is null", exc.getMessage());
        }
    }
}
