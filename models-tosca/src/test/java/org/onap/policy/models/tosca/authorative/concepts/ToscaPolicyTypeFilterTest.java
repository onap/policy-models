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
 * Test of the {@link ToscaPolicyTypeFilter} class.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class ToscaPolicyTypeFilterTest {
    private static final String VERSION_100 = "1.0.0";

    private static final String VERSION_000 = "0.0.0";

    // Logger for this class
    private static final Logger LOGGER = LoggerFactory.getLogger(ToscaPolicyTypeFilterTest.class);

    // @formatter:off
    private static final String[] policyTypeResourceNames = {
        "policytypes/onap.policies.monitoring.dcaegen2.collectors.datafile.datafile-app-server.yaml",
        "policytypes/onap.policies.optimization.AffinityPolicy.yaml",
        "policytypes/onap.policies.optimization.DistancePolicy.yaml",
        "policytypes/onap.policies.optimization.HpaPolicy.yaml",
        "policytypes/onap.policies.optimization.OptimizationPolicy.yaml",
        "policytypes/onap.policies.optimization.PciPolicy.yaml",
        "policytypes/onap.policies.optimization.QueryPolicy.yaml",
        "policytypes/onap.policies.optimization.SubscriberPolicy.yaml",
        "policytypes/onap.policies.optimization.Vim_fit.yaml",
        "policytypes/onap.policies.optimization.VnfPolicy.yaml",
        "policytypes/onap.policies.monitoring.cdap.tca.hi.lo.app.yaml"
    };
    // @formatter:on

    private static List<ToscaPolicyType> typeList = new ArrayList<>();

    /**
     * Set up a Tosca Policy type list for filtering.
     *
     * @throws CoderException on JSON decoding errors
     */
    @BeforeClass
    public static void setupTypeList() throws CoderException {
        for (String policyTypeResourceName : policyTypeResourceNames) {
            String policyTypeString = ResourceUtils.getResourceAsString(policyTypeResourceName);
            Object yamlObject = new Yaml().load(policyTypeString);
            String yamlAsJsonString = new GsonBuilder().setPrettyPrinting().create().toJson(yamlObject);

            ToscaServiceTemplate serviceTemplate =
                    new StandardCoder().decode(yamlAsJsonString, ToscaServiceTemplate.class);
            assertNotNull(serviceTemplate);

            for (Map<String, ToscaPolicyType> foundPolicyTypeMap : serviceTemplate.getPolicyTypes()) {
                addPolicyTypes(foundPolicyTypeMap);
            }
        }

        for (ToscaPolicyType type : typeList) {
            LOGGER.info("using policy type-" + type.getName() + ":" + type.getVersion());
        }
    }

    private static void addPolicyTypes(Map<String, ToscaPolicyType> foundPolicyTypeMap) {
        for (Entry<String, ToscaPolicyType> policyTypeEntry : foundPolicyTypeMap.entrySet()) {
            ToscaPolicyType policyType = policyTypeEntry.getValue();
            if (policyType.getName() == null) {
                policyType.setName(policyTypeEntry.getKey());
            }
            if (policyType.getVersion() == null) {
                policyType.setVersion(PfKey.NULL_KEY_VERSION);
            }
            if (!typeList.contains(policyType)) {
                typeList.add(policyType);
            }
        }
    }

    @Test
    public void testNullList() {
        ToscaPolicyTypeFilter filter = ToscaPolicyTypeFilter.builder().build();

        assertThatThrownBy(() -> {
            filter.filter(null);
        }).hasMessage("originalList is marked @NonNull but is null");
    }

    @Test
    public void testFilterNothing() {
        ToscaPolicyTypeFilter filter = ToscaPolicyTypeFilter.builder().build();

        List<ToscaPolicyType> filteredList = filter.filter(typeList);
        assertTrue(filteredList.containsAll(typeList));
    }

    @Test
    public void testFilterLatestVersion() {
        ToscaPolicyTypeFilter filter =
                ToscaPolicyTypeFilter.builder().version(ToscaPolicyTypeFilter.LATEST_VERSION).build();

        List<ToscaPolicyType> filteredList = filter.filter(typeList);
        assertEquals(13, filteredList.size());
        assertEquals(VERSION_100, filteredList.get(0).getVersion());
        assertEquals(VERSION_000, filteredList.get(4).getVersion());

        typeList.get(12).setVersion("2.0.0");
        filteredList = filter.filter(typeList);
        assertEquals(13, filteredList.size());
        assertEquals("2.0.0", filteredList.get(0).getVersion());
        assertEquals(VERSION_000, filteredList.get(4).getVersion());

        typeList.get(12).setVersion(VERSION_100);
        filteredList = filter.filter(typeList);
        assertEquals(13, filteredList.size());
        assertEquals(VERSION_100, filteredList.get(0).getVersion());
        assertEquals(VERSION_000, filteredList.get(4).getVersion());
    }

    @Test
    public void testFilterNameVersion() {
        ToscaPolicyTypeFilter filter = ToscaPolicyTypeFilter.builder().name("onap.policies.Monitoring").build();
        List<ToscaPolicyType> filteredList = filter.filter(typeList);
        assertEquals(2, filteredList.size());

        filter = ToscaPolicyTypeFilter.builder().name("onap.policies.monitoring.cdap.tca.hi.lo.app").build();
        filteredList = filter.filter(typeList);
        assertEquals(1, filteredList.size());

        filter = ToscaPolicyTypeFilter.builder().name("onap.policies.optimization.LpaPolicy").build();
        filteredList = filter.filter(typeList);
        assertEquals(0, filteredList.size());

        filter = ToscaPolicyTypeFilter.builder().version(VERSION_000).build();
        filteredList = filter.filter(typeList);
        assertEquals(9, filteredList.size());

        filter = ToscaPolicyTypeFilter.builder().name("onap.policies.optimization.Vim_fit").version(VERSION_000)
                        .build();
        filteredList = filter.filter(typeList);
        assertEquals(1, filteredList.size());

        filter = ToscaPolicyTypeFilter.builder().name("onap.policies.optimization.Vim_fit").version("0.0.1").build();
        filteredList = filter.filter(typeList);
        assertEquals(0, filteredList.size());
    }
}
