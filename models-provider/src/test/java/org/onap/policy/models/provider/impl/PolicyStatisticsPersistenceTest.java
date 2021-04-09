/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation.
 *  Modifications Copyright (C) 2021 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.models.provider.impl;

import static org.assertj.core.api.Assertions.assertThatCode;

import java.time.Instant;
import java.util.Arrays;
import org.junit.Test;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.pdp.concepts.PdpStatistics;
import org.onap.policy.models.provider.PolicyModelsProvider;
import org.onap.policy.models.provider.PolicyModelsProviderFactory;
import org.onap.policy.models.provider.PolicyModelsProviderParameters;

/**
 * Test persistence of PDP statistics to and from the database.
 */
public class PolicyStatisticsPersistenceTest {

    @Test
    public void testPdpStatiscticsPersistence() throws PfModelException {
        // Try the test on three providers
        for (int i = 0; i < 3; i++) {
            try (PolicyModelsProvider databaseProvider = setupProvider()) {
                testPdpStatiscticsPersistenceOneProvider(databaseProvider);
            }
        }
    }

    public void testPdpStatiscticsPersistenceOneProvider(PolicyModelsProvider databaseProvider) {
        PdpStatistics pdpStatistics = new PdpStatistics();
        pdpStatistics.setPdpInstanceId("TheInstance");
        pdpStatistics.setTimeStamp(Instant.now());

        // Try creating three identical statistics instances
        for (int i = 0; i < 3; i++) {
            assertThatCode(() -> databaseProvider.createPdpStatistics(Arrays.asList(pdpStatistics)))
                .doesNotThrowAnyException();
        }

        // Try creating three statistics instances with timestams incremented
        for (int i = 0; i < 3; i++) {
            pdpStatistics.setTimeStamp(pdpStatistics.getTimeStamp().plusSeconds(1));

            assertThatCode(() -> databaseProvider.createPdpStatistics(Arrays.asList(pdpStatistics)))
                .doesNotThrowAnyException();
        }
    }

    private PolicyModelsProvider setupProvider() throws PfModelException {
        PolicyModelsProviderParameters parameters = new PolicyModelsProviderParameters();

        if (System.getProperty("USE-MARIADB") != null) {
            parameters.setDatabaseDriver("org.mariadb.jdbc.Driver");
            parameters.setDatabaseUrl("jdbc:mariadb://localhost:3306/policy");
        } else {
            parameters.setDatabaseDriver("org.h2.Driver");
            parameters.setDatabaseUrl("jdbc:h2:mem:PolicyStatisticsPersistenceTest");
        }

        parameters.setDatabaseUser("policy");
        parameters.setDatabasePassword("P01icY");
        parameters.setPersistenceUnit("ToscaConceptTest");

        return new PolicyModelsProviderFactory().createPolicyModelsProvider(parameters);
    }
}
