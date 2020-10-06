/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import org.junit.Test;
import org.onap.policy.common.parameters.BeanValidationResult;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;

public class SimulatorParametersTest {

    @Test
    public void testValidate() throws CoderException {
        SimulatorParameters params = new StandardCoder().decode(new File("src/test/resources/simParameters.json"),
                        SimulatorParameters.class);
        assertNull(params.validate("ValidParams").getResult());
    }

    @Test
    public void testValidateEmptyRestServer() throws CoderException {
        SimulatorParameters params = new StandardCoder()
                        .decode(new File("src/test/resources/emptyRestServer.json"), SimulatorParameters.class);
        assertNull(params.validate("ValidParams").getResult());
    }

    @Test
    public void testValidateInvalidDmaapProvider() throws CoderException {
        SimulatorParameters params = new StandardCoder()
                        .decode(new File("src/test/resources/invalidDmaapProvider.json"), SimulatorParameters.class);
        BeanValidationResult result = params.validate("InvalidDmaapParams");
        assertFalse(result.isValid());
        assertNotNull(result.getResult());
    }

    @Test
    public void testValidateInvalidDmaapName() throws CoderException {
        SimulatorParameters params = new StandardCoder().decode(
                        new File("src/test/resources/invalidDmaapName.json"), SimulatorParameters.class);
        BeanValidationResult result = params.validate("InvalidDmaapParams");
        assertFalse(result.isValid());
        assertThat(result.getResult()).contains("item \"name\" value \"null\"");
    }

    @Test
    public void testValidateInvalidTopicSweep() throws CoderException {
        SimulatorParameters params = new StandardCoder().decode(
                        new File("src/test/resources/invalidTopicSweep.json"), SimulatorParameters.class);
        BeanValidationResult result = params.validate("InvalidDmaapParams");
        assertFalse(result.isValid());
        assertThat(result.getResult()).contains("topicSweepSec");
    }

    @Test
    public void testValidateInvalidGrpcServer() throws CoderException {
        SimulatorParameters params = new StandardCoder()
                        .decode(new File("src/test/resources/invalidGrpcServer.json"), SimulatorParameters.class);
        BeanValidationResult result = params.validate("InvalidGrpcParams");
        assertFalse(result.isValid());
        assertNotNull(result.getResult());
    }
}
