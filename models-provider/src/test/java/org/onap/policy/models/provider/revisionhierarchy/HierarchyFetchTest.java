/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2020 Nordix Foundation.
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

package org.onap.policy.models.provider.revisionhierarchy;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Base64;

import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.policy.common.utils.coder.YamlJsonTranslator;
import org.onap.policy.common.utils.resources.TextFileUtils;
import org.onap.policy.models.base.PfModelRuntimeException;
import org.onap.policy.models.provider.PolicyModelsProvider;
import org.onap.policy.models.provider.PolicyModelsProviderFactory;
import org.onap.policy.models.provider.PolicyModelsProviderParameters;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;

public class HierarchyFetchTest {

    private static PolicyModelsProviderParameters parameters;

    @BeforeClass
    public static void beforeSetupParameters() {
        parameters = new PolicyModelsProviderParameters();
        parameters.setDatabaseDriver("org.h2.Driver");
        parameters.setDatabaseUrl("jdbc:h2:mem:testdb");
        parameters.setDatabaseUser("policy");
        parameters.setDatabasePassword(Base64.getEncoder().encodeToString("P01icY".getBytes()));
        parameters.setPersistenceUnit("ToscaConceptTest");
    }

    @Test
    public void testInitAndClose() throws Exception {
        PolicyModelsProvider databaseProvider =
            new PolicyModelsProviderFactory().createPolicyModelsProvider(parameters);

        ToscaServiceTemplate serviceTemplate =
            new YamlJsonTranslator().fromYaml(
                TextFileUtils
                    .getTextFileAsString("src/test/resources/servicetemplates/MultipleRevisionServiceTemplate.yaml"),
                ToscaServiceTemplate.class);

        assertThatThrownBy(() -> {
            databaseProvider.createPolicies(serviceTemplate);
        }).isInstanceOf(PfModelRuntimeException.class);
        
        databaseProvider.close();
    }
}
