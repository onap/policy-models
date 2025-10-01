/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2021, 2025 Nordix Foundation.
 *  Modifications Copyright (C) 2019-2020 AT&T Intellectual Property. All rights reserved.
 *  Modifications Copyright (C) 2024 Nordix Foundation
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.GsonBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.resources.ResourceUtils;
import org.onap.policy.models.base.PfKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

/**
 * Test of the {@link ToscaTypedEntityFilter} class.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
class ToscaTypedEntityFilterTest {
    private static final String VERSION_100 = "1.0.0";

    private static final String VERSION_000 = "0.0.0";

    // Logger for this class
    private static final Logger LOGGER = LoggerFactory.getLogger(ToscaTypedEntityFilterTest.class);

    // @formatter:off
    private static final String[] policyResourceNames = {
        "policies/vCPE.policies.optimization.input.tosca.yaml",
        "policies/vCPE.policy.monitoring.input.tosca.yaml",
        "policies/vCPE.policy.operational.input.tosca.yaml",
        "policies/vDNS.policy.monitoring.input.tosca.yaml",
        "policies/vDNS.policy.operational.input.tosca.yaml",
        "policies/vDNS.policy.guard.frequencylimiter.input.tosca.yaml",
        "policies/vDNS.policy.guard.minmaxvnfs.input.tosca.yaml",
        "policies/vFirewall.policy.monitoring.input.tosca.yaml",
        "policies/vFirewall.policy.operational.input.tosca.yaml"
    };
    // @formatter:on

    private static final List<ToscaPolicy> policyList = new ArrayList<>();

    /**
     * Set up a Tosca Policy type list for filtering.
     *
     * @throws CoderException on JSON decoding errors
     */
    @BeforeAll
    static void setupTypeList() throws CoderException {
        for (String policyResourceName : policyResourceNames) {
            String policyString = ResourceUtils.getResourceAsString(policyResourceName);
            if (policyResourceName.endsWith("yaml")) {
                LOGGER.info("loading {}", policyResourceName);
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
            LOGGER.info("using policy-{}:{}, type-{}:{}", policy.getName(), policy.getVersion(),
                policy.getType(), policy.getTypeVersion());
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
    void testNullList() {
        ToscaTypedEntityFilter<ToscaPolicy> filter = ToscaTypedEntityFilter.<ToscaPolicy>builder().build();

        assertThatThrownBy(() -> {
            filter.filter(null);
        }).hasMessageMatching("originalList is marked .*on.*ull but is null");
    }

    @Test
    void testFilterNothing() {
        ToscaTypedEntityFilter<ToscaPolicy> filter = ToscaTypedEntityFilter.<ToscaPolicy>builder().build();

        List<ToscaPolicy> filteredList = filter.filter(policyList);
        assertTrue(filteredList.containsAll(policyList));
    }

    @Test
    void testFilterLatestVersion() {
        ToscaTypedEntityFilter<ToscaPolicy> filter =
            ToscaTypedEntityFilter.<ToscaPolicy>builder().version(ToscaTypedEntityFilter.LATEST_VERSION).build();

        List<ToscaPolicy> filteredList = filter.filter(policyList);
        assertEquals(22, filteredList.size());
        assertEquals(VERSION_100, filteredList.get(7).getVersion());
        assertEquals(VERSION_100, filteredList.get(12).getVersion());

        assertEquals(22, policyList.size());
        assertEquals(22, filteredList.size());

        //
        // Change versions to a couple of policies
        //
        policyList.forEach(policy -> {
            if ("onap.vfirewall.tca".equals(policy.getName())) {
                policy.setVersion("2.0.0");
            } else if ("operational.modifyconfig".equals(policy.getName())) {
                policy.setVersion("3.4.5");
            }
        });
        //
        // We'll still get back the same number of policies
        //
        filteredList = filter.filter(policyList);
        assertEquals(22, filteredList.size());
        //
        // Assert that the correct versions are returned
        //
        policyList.forEach(policy -> {
            if ("onap.vfirewall.tca".equals(policy.getName())) {
                assertThat(policy.getVersion()).isEqualTo("2.0.0");
            } else if ("operational.modifyconfig".equals(policy.getName())) {
                assertThat(policy.getVersion()).isEqualTo("3.4.5");
            } else {
                assertThat(policy.getVersion()).isEqualTo(VERSION_100);
            }
        });

        //
        // Change versions back
        //
        policyList.forEach(policy -> {
            if ("onap.vfirewall.tca".equals(policy.getName()) || "operational.modifyconfig".equals(policy.getName())) {
                policy.setVersion(VERSION_100);
            }
        });
        //
        // We'll still get back the same number of policies
        //
        filteredList = filter.filter(policyList);
        assertEquals(22, filteredList.size());
        //
        // Assert that the correct versions are returned
        //
        policyList.forEach(policy -> assertThat(policy.getVersion()).isEqualTo(VERSION_100));
    }

    @Test
    void testFilterNameVersion() {
        ToscaTypedEntityFilter<ToscaPolicy> filter =
            ToscaTypedEntityFilter.<ToscaPolicy>builder().name("operational.modifyconfig").build();
        List<ToscaPolicy> filteredList = filter.filter(policyList);
        assertEquals(1, filteredList.size());

        filter = ToscaTypedEntityFilter.<ToscaPolicy>builder().name("guard.frequency.scaleout").build();
        filteredList = filter.filter(policyList);
        assertEquals(1, filteredList.size());

        filter = ToscaTypedEntityFilter.<ToscaPolicy>builder().name("guard.frequency.scalein").build();
        filteredList = filter.filter(policyList);
        assertEquals(0, filteredList.size());

        filter = ToscaTypedEntityFilter.<ToscaPolicy>builder().version(VERSION_100).build();
        filteredList = filter.filter(policyList);
        assertEquals(22, filteredList.size());

        filter = ToscaTypedEntityFilter.<ToscaPolicy>builder().name("OSDF_CASABLANCA.SubscriberPolicy_v1")
            .version(VERSION_100).build();
        filteredList = filter.filter(policyList);
        assertEquals(1, filteredList.size());

        filter = ToscaTypedEntityFilter.<ToscaPolicy>builder().name("operational.modifyconfig").version(VERSION_100)
            .build();
        filteredList = filter.filter(policyList);
        assertEquals(1, filteredList.size());
    }

    @Test
    void testFilterVersionPrefix() {
        // null pattern
        ToscaTypedEntityFilter<ToscaPolicy> filter =
            ToscaTypedEntityFilter.<ToscaPolicy>builder().versionPrefix(null).build();
        List<ToscaPolicy> filteredList = filter.filter(policyList);
        assertEquals(22, filteredList.size());

        filter = ToscaTypedEntityFilter.<ToscaPolicy>builder().versionPrefix("1.").build();
        filteredList = filter.filter(policyList);
        assertEquals(22, filteredList.size());

        filter = ToscaTypedEntityFilter.<ToscaPolicy>builder().versionPrefix("100.").build();
        filteredList = filter.filter(policyList);
        assertEquals(0, filteredList.size());
    }

    @Test
    void testFilterTypeVersion() {
        ToscaTypedEntityFilter<ToscaPolicy> filter =
            ToscaTypedEntityFilter.<ToscaPolicy>builder().type("onap.policies.controlloop.Operational").build();
        List<ToscaPolicy> filteredList = filter.filter(policyList);
        assertEquals(0, filteredList.size());

        filter = ToscaTypedEntityFilter.<ToscaPolicy>builder().type("onap.policies.controlloop.operational.common.Apex")
            .build();
        filteredList = filter.filter(policyList);
        assertEquals(0, filteredList.size());

        filter = ToscaTypedEntityFilter.<ToscaPolicy>builder()
            .type("onap.policies.controlloop.operational.common.Drools").build();
        filteredList = filter.filter(policyList);
        assertEquals(3, filteredList.size());

        filter = ToscaTypedEntityFilter.<ToscaPolicy>builder().type("onap.policies.monitoring.tcagen2").build();
        filteredList = filter.filter(policyList);
        assertEquals(3, filteredList.size());

        filter = ToscaTypedEntityFilter.<ToscaPolicy>builder().type("onap.policies.controlloop.NonOperational").build();
        filteredList = filter.filter(policyList);
        assertEquals(0, filteredList.size());

        filter = ToscaTypedEntityFilter.<ToscaPolicy>builder().typeVersion(VERSION_000).build();
        filteredList = filter.filter(policyList);
        assertEquals(0, filteredList.size());

        filter = ToscaTypedEntityFilter.<ToscaPolicy>builder().type("onap.policies.optimization.resource.HpaPolicy")
            .typeVersion(VERSION_100).build();
        filteredList = filter.filter(policyList);
        assertEquals(2, filteredList.size());

        filter = ToscaTypedEntityFilter.<ToscaPolicy>builder().type("onap.policies.controlloop.Operational")
            .typeVersion(VERSION_000).build();
        filteredList = filter.filter(policyList);
        assertEquals(0, filteredList.size());
    }
}
