/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2024-2025 Nordix Foundation
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

package org.onap.policy.controlloop.actorserviceprovider.pipeline;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;

class PipelineUtilTest {

    @Test
    void testPipelineUtil() {
        ControlLoopOperationParams params = ControlLoopOperationParams.builder().build();
        PipelineUtil util = new PipelineUtil(params);

        assertThatThrownBy(util::start).isInstanceOf(UnsupportedOperationException.class);
    }
}
