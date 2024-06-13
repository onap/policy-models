/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2020, 2024 Nordix Foundation.
 *  Modifications Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.onap.policy.models.base.testconcepts.DummyPfModel;

class ModelServiceTest {

    private static final String MODEL_KEY_IS_NULL = "modelKey is marked .*on.*ull but is null$";
    private static final String MODEL_NAME = "ModelName";

    @Test
    void testModelService() {
        PfModelService.clear();

        assertFalse(PfModelService.existsModel("NonExistantName"));
        assertThatThrownBy(() -> PfModelService.getModel("NonExistantName"))
            .hasMessage("Model for name NonExistantName not found in model service");

        PfModelService.registerModel(MODEL_NAME, new DummyPfModel());
        assertTrue(PfModelService.existsModel(MODEL_NAME));
        assertNotNull(PfModelService.getModel(MODEL_NAME));

        PfModelService.deregisterModel(MODEL_NAME);

        assertFalse(PfModelService.existsModel(MODEL_NAME));
        assertThatThrownBy(() -> PfModelService.getModel(MODEL_NAME))
            .hasMessage("Model for name ModelName not found in model service");

        PfModelService.registerModel(MODEL_NAME, new DummyPfModel());
        assertTrue(PfModelService.existsModel(MODEL_NAME));
        assertNotNull(PfModelService.getModel(MODEL_NAME));

        PfModelService.clear();
        assertFalse(PfModelService.existsModel(MODEL_NAME));
        assertThatThrownBy(() -> PfModelService.getModel(MODEL_NAME))
            .hasMessage("Model for name ModelName not found in model service");

        assertThatThrownBy(() -> PfModelService.registerModel(null, null)).hasMessageMatching(MODEL_KEY_IS_NULL);

        assertThatThrownBy(() -> PfModelService.registerModel("nullModelName", null))
            .hasMessageMatching("^model is marked .*on.*ull but is null$");

        assertThatThrownBy(() -> PfModelService.registerModel(null, new DummyPfModel()))
            .hasMessageMatching(MODEL_KEY_IS_NULL);

        assertThatThrownBy(() -> PfModelService.deregisterModel(null)).hasMessageMatching(MODEL_KEY_IS_NULL);

        assertThatThrownBy(() -> PfModelService.getModel(null)).hasMessageMatching(MODEL_KEY_IS_NULL);
    }
}
