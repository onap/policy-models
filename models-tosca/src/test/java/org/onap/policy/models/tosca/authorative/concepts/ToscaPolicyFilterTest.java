/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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

package org.onap.policy.models.tosca.authorative.concepts;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.gson.GsonBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.resources.ResourceUtils;
import org.onap.policy.models.base.PfKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

/**
 * Test of the {@link ToscaPolicyFilter} class.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class ToscaPolicyFilterTest {
    private static final String VERSION_100 = "1.0.0";

    private static final String VERSION_000 = "0.0.0";

    // Logger for this class
    private static final Logger LOGGER = LoggerFactory.getLogger(ToscaPolicyFilterTest.class);

    // @formatter:off
    private static final String[] policyResourceNames = {
        "policies/vCPE.policies.optimization.input.tosca.yaml",
        "policies/vCPE.policy.monitoring.input.tosca.json",
        "policies/vCPE.policy.monitoring.input.tosca.yaml",
        "policies/vCPE.policy.operational.input.tosca.yaml",
        "policies/vDNS.policy.guard.frequency.input.tosca.json",
        "policies/vDNS.policy.guard.frequency.input.tosca.yaml",
        "policies/vDNS.policy.guard.minmax.input.tosca.yaml",
        "policies/vDNS.policy.monitoring.input.tosca.json",
        "policies/vDNS.policy.monitoring.input.tosca.yaml",
        "policies/vDNS.policy.operational.input.tosca.yaml",
        "policies/vFirewall.policy.monitoring.input.tosca.json",
        "policies/vFirewall.policy.monitoring.input.tosca.yaml",
        "policies/vFirewall.policy.operational.input.tosca.json",
        "policies/vFirewall.policy.operational.input.tosca.yaml"
    };
    // @formatter:on

    private static List<ToscaPolicy> policyList = new ArrayList<>();

    /**
     * Set up a Tosca Policy type list for filtering.
     *
     * @throws CoderException on JSON decoding errors
     */
    @BeforeClass
    public static void setupTypeList() throws CoderException {
        for (String policyResourceName : policyResourceNames) {
            String policyString = ResourceUtils.getResourceAsString(policyResourceName);
            if (policyResourceName.endsWith("yaml")) {
                Object yamlObject = new Yaml().load(policyString);
                policyString = new GsonBuilder().setPrettyPrinting().create().toJson(yamlObject);
            }

            ToscaServiceTemplate serviceTemplate = new StandardCoder().decode(policyString, ToscaServiceTemplate.class);
            assertNotNull(serviceTemplate);

            for (Map<String, ToscaPolicy> foundPolicyMap : serviceTemplate.getToscaTopologyTemplate().getPolicies()) {
                addPolicies(foundPolicyMap);
            }
        }

        for (ToscaPolicy policy : policyList) {
            LOGGER.info("using policy-" + policy.getName() + ":" + policy.getVersion() + ", type-" + policy.getType()
                    + ":" + policy.getTypeVersion());
        }
    }

    private static void addPolicies(Map<String, ToscaPolicy> foundPolicyMap) {
        for (Entry<String, ToscaPolicy> policyEntry : foundPolicyMap.entrySet()) {
            ToscaPolicy policy = policyEntry.getValue();
            if (policy.getName() == null) {
                policy.setName(policyEntry.getKey());
            }

            if (policy.getVersion() == null) {
                policy.setVersion(PfKey.NULL_KEY_VERSION);
            }
            if (policy.getTypeVersion() == null) {
                policy.setTypeVersion(PfKey.NULL_KEY_VERSION);
            }
            if (!policyList.contains(policy)) {
                policyList.add(policy);
            }
        }
    }

    @Test
    public void testNullList() {
        ToscaPolicyFilter filter = ToscaPolicyFilter.builder().build();

        assertThatThrownBy(() -> {
            filter.filter(null);
        }).hasMessage("originalList is marked @NonNull but is null");
    }

    @Test
    public void testFilterNothing() {
        ToscaPolicyFilter filter = ToscaPolicyFilter.builder().build();

        List<ToscaPolicy> filteredList = filter.filter(policyList);
        assertTrue(filteredList.containsAll(policyList));
    }

    @Test
    public void testFilterLatestVersion() {
        ToscaPolicyFilter filter = ToscaPolicyFilter.builder().version(ToscaPolicyFilter.LATEST_VERSION).build();

        List<ToscaPolicy> filteredList = filter.filter(policyList);
        assertEquals(15, filteredList.size());
        assertEquals(VERSION_100, filteredList.get(7).getVersion());
        assertEquals(VERSION_100, filteredList.get(12).getVersion());

        assertEquals(17, policyList.size());
        assertEquals(15, filteredList.size());

        policyList.get(10).setVersion("2.0.0");
        policyList.get(16).setVersion("3.4.5");
        filteredList = filter.filter(policyList);
        assertEquals(15, filteredList.size());
        assertEquals("2.0.0", filteredList.get(7).getVersion());
        assertEquals("3.4.5", filteredList.get(12).getVersion());

        policyList.get(10).setVersion(VERSION_100);
        policyList.get(16).setVersion(VERSION_100);
        filteredList = filter.filter(policyList);
        assertEquals(15, filteredList.size());
        assertEquals(VERSION_100, filteredList.get(7).getVersion());
        assertEquals(VERSION_100, filteredList.get(12).getVersion());
    }

    @Test
    public void testFilterNameVersion() {
        ToscaPolicyFilter filter = ToscaPolicyFilter.builder().name("operational.modifyconfig").build();
        List<ToscaPolicy> filteredList = filter.filter(policyList);
        assertEquals(2, filteredList.size());

        filter = ToscaPolicyFilter.builder().name("guard.frequency.scaleout").build();
        filteredList = filter.filter(policyList);
        assertEquals(2, filteredList.size());

        filter = ToscaPolicyFilter.builder().name("guard.frequency.scalein").build();
        filteredList = filter.filter(policyList);
        assertEquals(0, filteredList.size());

        filter = ToscaPolicyFilter.builder().version(VERSION_100).build();
        filteredList = filter.filter(policyList);
        assertEquals(17, filteredList.size());

        filter = ToscaPolicyFilter.builder().name("OSDF_CASABLANCA.SubscriberPolicy_v1").version(VERSION_100).build();
        filteredList = filter.filter(policyList);
        assertEquals(1, filteredList.size());

        filter = ToscaPolicyFilter.builder().name("operational.modifyconfig").version(VERSION_100).build();
        filteredList = filter.filter(policyList);
        assertEquals(2, filteredList.size());
    }

    @Test
    public void testFilterVersionPrefix() {
        // null pattern
        ToscaPolicyFilter filter = ToscaPolicyFilter.builder().versionPrefix(null).build();
        List<ToscaPolicy> filteredList = filter.filter(policyList);
        assertEquals(17, filteredList.size());

        filter = ToscaPolicyFilter.builder().versionPrefix("1.").build();
        filteredList = filter.filter(policyList);
        assertEquals(17, filteredList.size());

        filter = ToscaPolicyFilter.builder().versionPrefix("100.").build();
        filteredList = filter.filter(policyList);
        assertEquals(0, filteredList.size());
    }

    @Test
    public void testFilterTypeVersion() {
        ToscaPolicyFilter filter = ToscaPolicyFilter.builder().type("onap.policies.controlloop.Operational").build();
        List<ToscaPolicy> filteredList = filter.filter(policyList);
        assertEquals(4, filteredList.size());

        filter = ToscaPolicyFilter.builder().type("onap.policies.monitoring.cdap.tca.hi.lo.app").build();
        filteredList = filter.filter(policyList);
        assertEquals(3, filteredList.size());

        filter = ToscaPolicyFilter.builder().type("onap.policies.controlloop.NonOperational").build();
        filteredList = filter.filter(policyList);
        assertEquals(0, filteredList.size());

        filter = ToscaPolicyFilter.builder().typeVersion(VERSION_000).build();
        filteredList = filter.filter(policyList);
        assertEquals(17, filteredList.size());

        filter = ToscaPolicyFilter.builder().type("onap.policies.optimization.HpaPolicy").typeVersion(VERSION_000)
                        .build();
        filteredList = filter.filter(policyList);
        assertEquals(1, filteredList.size());

        filter = ToscaPolicyFilter.builder().type("onap.policies.controlloop.Operational").typeVersion(VERSION_000)
                        .build();
        filteredList = filter.filter(policyList);
        assertEquals(4, filteredList.size());
    }
}
