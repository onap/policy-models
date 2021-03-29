/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation.
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

package org.onap.policy.models.pap.concepts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.resources.TextFileUtils;
import org.onap.policy.models.tosca.authorative.concepts.ToscaConceptIdentifierOptVersion;

/**
 * This only tests the methods that aren't already tested via TestModels.
 */
public class PdpDeployPoliciesTest {
    private static StandardCoder CODER = new StandardCoder();

    @Test
    public void testPapPolicyIdentifier() throws CoderException, IOException {
        assertNotNull(new PdpDeployPolicies());

        List<ToscaConceptIdentifierOptVersion> tciListIn = new ArrayList<>();

        ToscaConceptIdentifierOptVersion tci0 = new ToscaConceptIdentifierOptVersion("MyPolicy0", "1.2.0");
        tciListIn.add(tci0);
        ToscaConceptIdentifierOptVersion tci1 = new ToscaConceptIdentifierOptVersion("MyPolicy1", "1.2.1");
        tciListIn.add(tci1);
        ToscaConceptIdentifierOptVersion tci2 = new ToscaConceptIdentifierOptVersion("MyPolicy2", "1.2.2");
        tciListIn.add(tci2);

        PdpDeployPolicies policies = new PdpDeployPolicies();
        policies.setPolicies(null);
        assertNull(policies.getPolicies());

        policies.setPolicies(tciListIn);

        assertEquals(3, policies.getPolicies().size());

        assertEquals("MyPolicy0", policies.getPolicies().get(0).getName());
        assertEquals("1.2.0", policies.getPolicies().get(0).getVersion());
        assertEquals("MyPolicy1", policies.getPolicies().get(1).getName());
        assertEquals("1.2.1", policies.getPolicies().get(1).getVersion());
        assertEquals("MyPolicy2", policies.getPolicies().get(2).getName());
        assertEquals("1.2.2", policies.getPolicies().get(2).getVersion());

        List<ToscaConceptIdentifierOptVersion> tciListOut = policies.getPolicies();
        assertEquals(tciListIn, tciListOut);
    }

    @Test
    public void testPapPolicyIdentifierSerialization() throws CoderException, IOException {
        String idListString = TextFileUtils.getTextFileAsString("src/test/resources/json/PapPoliciesList.json");

        PdpDeployPolicies policies = CODER.decode(idListString, PdpDeployPolicies.class);

        assertEquals(3, policies.getPolicies().size());

        assertEquals("MyPolicy0", policies.getPolicies().get(0).getName());
        assertEquals("1.2.0", policies.getPolicies().get(0).getVersion());
        assertEquals("MyPolicy1", policies.getPolicies().get(1).getName());
        assertEquals("1.2.1", policies.getPolicies().get(1).getVersion());
        assertEquals("MyPolicy2", policies.getPolicies().get(2).getName());
        assertEquals("1.2.2", policies.getPolicies().get(2).getVersion());

        String idListStringBack = CODER.encode(policies);
        assertEquals(idListString.replaceAll("\\s+", ""), idListStringBack.replaceAll("\\s+", ""));
    }

    @Test
    public void testValidatePapRest() throws IOException, CoderException {
        // valid list
        String idListString = TextFileUtils.getTextFileAsString("src/test/resources/json/PapPoliciesList.json");
        PdpDeployPolicies policies = CODER.decode(idListString, PdpDeployPolicies.class);
        assertThat(policies.validatePapRest().getResult()).isNull();

        // null list
        policies = new PdpDeployPolicies();
        assertThat(policies.validatePapRest().getResult()).contains("policies");

        // list containing null item
        idListString = TextFileUtils.getTextFileAsString("src/test/resources/json/PapPoliciesNullItem.json");
        policies = CODER.decode(idListString, PdpDeployPolicies.class);
        assertThat(policies.validatePapRest().getResult()).contains("policies").contains("null");

        // list containing an invalid policy
        idListString = TextFileUtils.getTextFileAsString("src/test/resources/json/PapPoliciesInvalidPolicy.json");
        policies = CODER.decode(idListString, PdpDeployPolicies.class);
        assertThat(policies.validatePapRest().getResult()).contains("policies").contains("name").contains("null")
                        .doesNotContain("\"value\"");
    }
}
