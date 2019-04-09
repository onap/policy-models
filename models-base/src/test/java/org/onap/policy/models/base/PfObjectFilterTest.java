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

package org.onap.policy.models.base;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import org.onap.policy.models.base.testconcepts.DummyPfObject;
import org.onap.policy.models.base.testconcepts.DummyPfObjectFilter;

/**
 * Test the {@link PfObjectFilter} interface.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class PfObjectFilterTest {

    @Test
    public void testPfObjectInterface() {
        DummyPfObject do0 = new DummyPfObject();
        do0.setName("name0");
        do0.setVersion("1.0.0");
        do0.setDescription("desc0 ");

        DummyPfObject do1 = new DummyPfObject();
        do1.setName("name0");
        do1.setVersion("0.0.1");
        do1.setDescription("Desc 1");

        DummyPfObject do2 = new DummyPfObject();
        do2.setName("name0");
        do2.setVersion("0.0.2");
        do2.setDescription("Desc 1");

        DummyPfObject do3 = new DummyPfObject();
        do3.setName("name1");
        do3.setVersion("0.0.1");
        do3.setDescription("desc0 ");

        DummyPfObject do4 = new DummyPfObject();
        do4.setName("name1");
        do4.setVersion("0.1.2");
        do4.setDescription("Desc 1");

        DummyPfObject do5 = new DummyPfObject();
        do5.setName("aaaaa");
        do5.setVersion("0.0.2");
        do5.setDescription("Desc 1");

        List<DummyPfObject> doList = new ArrayList<>();
        doList.add(do0);
        doList.add(do1);
        doList.add(do2);
        doList.add(do3);
        doList.add(do4);
        doList.add(do5);

        DummyPfObjectFilter dof = new DummyPfObjectFilter();
        assertFalse(dof.filterString("Hello", "Goodbye"));
        assertTrue(dof.filterString("Hello", "Hello"));

        assertEquals(false, dof.filterString("Hello", "Goodbye"));
        assertEquals(true, dof.filterString("Hello", "Hello"));
        assertEquals(true, dof.filterString("Hello", null));

        List<DummyPfObject> latestVersionList = dof.latestVersionFilter(doList);
        assertEquals(3, latestVersionList.size());
        assertEquals("aaaaa", latestVersionList.get(0).getName());
        assertEquals("0.0.2", latestVersionList.get(0).getVersion());
        assertEquals("name0", latestVersionList.get(1).getName());
        assertEquals("1.0.0", latestVersionList.get(1).getVersion());
        assertEquals("name1", latestVersionList.get(2).getName());
        assertEquals("0.1.2", latestVersionList.get(2).getVersion());

        latestVersionList.remove(2);
        latestVersionList.remove(1);
        List<DummyPfObject> newestVersionList = dof.latestVersionFilter(latestVersionList);
        assertEquals(latestVersionList, newestVersionList);
    }
}
