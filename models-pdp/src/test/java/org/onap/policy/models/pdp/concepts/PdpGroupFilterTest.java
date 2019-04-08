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

package org.onap.policy.models.pdp.concepts;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.resources.ResourceUtils;

/**
 * Test of the {@link PdpGroupFilter} class.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class PdpGroupFilterTest {
    private List<PdpGroup> pdpGroupList;

    /**
     * Set up a PDP group list for filtering.
     *
     * @throws CoderException on JSON decoding errors
     */
    @Before
    public void setupPdpGroupList() throws CoderException {
        String originalJson = ResourceUtils.getResourceAsString("testdata/PdpGroupsForFiltering.json");
        PdpGroups pdpGroups = new StandardCoder().decode(originalJson, PdpGroups.class);
        pdpGroupList = pdpGroups.getGroups();
    }

    @Test
    public void testNullList() {
        PdpGroupFilter filter = PdpGroupFilter.builder().build();

        assertThatThrownBy(() -> {
            filter.filter(null);
        }).hasMessage("originalList is marked @NonNull but is null");
    }

    @Test
    public void testFilterNothing() {
        PdpGroupFilter filter = PdpGroupFilter.builder().build();

        List<PdpGroup> filteredList = filter.filter(pdpGroupList);
        assertTrue(filteredList.containsAll(pdpGroupList));
    }

    @Test
    public void testFilterLatestVersion() {
        PdpGroupFilter filter = PdpGroupFilter.builder().version(PdpGroupFilter.LATEST_VERSION).build();

        List<PdpGroup> filteredList = filter.filter(pdpGroupList);
        assertEquals(2, filteredList.size());
        assertEquals("1.2.4", filteredList.get(0).getVersion());
        assertEquals("1.2.3", filteredList.get(1).getVersion());
    }

    @Test
    public void testFilterNameVersion() {
        PdpGroupFilter filter = PdpGroupFilter.builder().name("PdpGroup0").build();
        List<PdpGroup> filteredList = filter.filter(pdpGroupList);
        assertEquals(3, filteredList.size());

        filter = PdpGroupFilter.builder().name("PdpGroup1").build();
        filteredList = filter.filter(pdpGroupList);
        assertEquals(2, filteredList.size());

        filter = PdpGroupFilter.builder().name("PdpGroup2").build();
        filteredList = filter.filter(pdpGroupList);
        assertEquals(0, filteredList.size());

        filter = PdpGroupFilter.builder().version("1.2.3").build();
        filteredList = filter.filter(pdpGroupList);
        assertEquals(2, filteredList.size());

        filter = PdpGroupFilter.builder().name("PdpGroup0").version("1.2.3").build();
        filteredList = filter.filter(pdpGroupList);
        assertEquals(1, filteredList.size());

        filter = PdpGroupFilter.builder().name("PdpGroup1").version("1.2.9").build();
        filteredList = filter.filter(pdpGroupList);
        assertEquals(0, filteredList.size());
    }
}
