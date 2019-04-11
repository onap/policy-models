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

package org.onap.policy.models.provider;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.onap.policy.common.parameters.GroupValidationResult;

/**
 * Test of {@link PolicyModelsProviderParameters} class.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class PolicyModelsProviderParametersTest {

    @Test
    public void testParameters() {
        PolicyModelsProviderParameters pars = new PolicyModelsProviderParameters();
        pars.setDatabaseDriver("MichaelsShumacher");
        pars.setDatabaseUrl("jdbc://www.acmecorp/roadrunner");
        pars.setPersistenceUnit("WileECoyote");

        GroupValidationResult result = pars.validate();
        assertTrue(result.isValid());

        pars.setImplementation(null);
        result = pars.validate();
        assertFalse(result.isValid());
        pars.setImplementation("An Implementation");
        result = pars.validate();
        assertTrue(result.isValid());

        pars.setDatabaseUrl(null);
        result = pars.validate();
        assertFalse(result.isValid());
        pars.setDatabaseUrl("jdbc://www.acmecorp/roadrunner");
        result = pars.validate();
        assertTrue(result.isValid());

        pars.setPersistenceUnit(null);
        result = pars.validate();
        assertFalse(result.isValid());
        pars.setPersistenceUnit("WileECoyote");
        result = pars.validate();
        assertTrue(result.isValid());

        pars.setDatabaseDriver(null);
        result = pars.validate();
        assertFalse(result.isValid());
        pars.setDatabaseDriver("MichaelsShumacher");
        result = pars.validate();
        assertTrue(result.isValid());
    }
}
