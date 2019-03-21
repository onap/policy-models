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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import lombok.ToString;

import org.junit.Test;

/**
 * Test the {@link PolicyModelsProviderFactory} class.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
@ToString
public class PolicyModelsProviderFactoryTest {

    @Test
    public void testFactory() {
        PolicyModelsProviderFactory factory = new PolicyModelsProviderFactory();

        try {
            factory.createPolicyModelsProvider(null);
            fail("test should throw an exception here");
        } catch (Exception exc) {
            assertEquals("parameters is marked @NonNull but is null", exc.getMessage());
        }

        try {
            PolicyModelsProviderParameters pars = new PolicyModelsProviderParameters();
            pars.setImplementation(null);
            factory.createPolicyModelsProvider(pars);
            fail("test should throw an exception here");
        } catch (Exception exc) {
            assertEquals("could not find implementation of the \"PolicyModelsProvider\" interface \"null\"",
                    exc.getMessage());
        }

        try {
            PolicyModelsProviderParameters pars = new PolicyModelsProviderParameters();
            pars.setImplementation("com.acmecorp.RoadRunner");
            factory.createPolicyModelsProvider(pars);
            fail("test should throw an exception here");
        } catch (Exception exc) {
            assertEquals("could not find implementation of the \"PolicyModelsProvider\" "
                    + "interface \"com.acmecorp.RoadRunner\"", exc.getMessage());
        }

        try {
            PolicyModelsProviderParameters pars = new PolicyModelsProviderParameters();
            pars.setImplementation("java.lang.String");
            factory.createPolicyModelsProvider(pars);
            fail("test should throw an exception here");
        } catch (Exception exc) {
            assertEquals(
                    "the class \"java.lang.String\" is not an implementation of the \"PolicyModelsProvider\" interface",
                    exc.getMessage());
        }

        try {
            PolicyModelsProviderParameters pars = new PolicyModelsProviderParameters();
            pars.setImplementation("org.onap.policy.models.provider.impl.DummyBadProviderImpl");
            factory.createPolicyModelsProvider(pars);
            fail("test should throw an exception here");
        } catch (Exception exc) {
            assertEquals("could not create an instance of PolicyModelsProvider "
                    + "\"org.onap.policy.models.provider.impl.DummyBadProviderImpl\"", exc.getMessage());
        }
    }
}
