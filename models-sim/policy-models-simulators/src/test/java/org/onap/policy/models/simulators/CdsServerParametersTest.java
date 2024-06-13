/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2020 Bell Canada.
 * Modifications Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.models.simulators;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import org.junit.jupiter.api.Test;
import org.onap.policy.common.parameters.ValidationResult;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;

class CdsServerParametersTest {
    @Test
    void testValidateString() throws CoderException {
        // some fields missing
        ValidationResult result = new CdsServerParameters().validate();
        assertFalse(result.isValid());
        assertNotNull(result.getResult());

        // everything populated
        SimulatorParameters simParams = new StandardCoder()
            .decode(new File("src/test/resources/simParameters.json"), SimulatorParameters.class);
        CdsServerParameters params = simParams.getGrpcServer();
        assertTrue(params.validate().isValid());
    }
}
