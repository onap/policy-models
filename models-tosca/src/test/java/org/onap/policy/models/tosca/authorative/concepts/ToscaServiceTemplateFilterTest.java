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

package org.onap.policy.models.tosca.authorative.concepts;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;
import org.junit.Test;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardYamlCoder;
import org.onap.policy.models.base.PfKey;

/**
 * Test of the {@link ToscaServiceTemplateFilter} class.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class ToscaServiceTemplateFilterTest {
    @Test
    public void testNullList() {
        ToscaServiceTemplateFilter filter = ToscaServiceTemplateFilter.builder().build();

        assertThatThrownBy(() -> {
            filter.filter(null);
        }).hasMessageMatching("originalList is marked .*on.*ull but is null");
    }

    @Test
    public void testFilterNothing() throws CoderException {
        ToscaServiceTemplates serviceTemplates = new StandardYamlCoder().decode(
                new File("src/test/resources/servicetemplates/TestServiceTemplates.yaml"), ToscaServiceTemplates.class);

        ToscaServiceTemplateFilter filter = ToscaServiceTemplateFilter.builder().build();

        List<ToscaServiceTemplate> filteredList = filter.filter(serviceTemplates.getServiceTemplates());
        assertTrue(filteredList.containsAll(serviceTemplates.getServiceTemplates()));
    }

    @Test
    public void testFilterLatestVersion() throws CoderException {
        ToscaServiceTemplates serviceTemplates = new StandardYamlCoder().decode(
                new File("src/test/resources/servicetemplates/TestServiceTemplates.yaml"), ToscaServiceTemplates.class);

        ToscaServiceTemplateFilter filter =
                ToscaServiceTemplateFilter.builder().version(ToscaServiceTemplateFilter.LATEST_VERSION).build();

        List<ToscaServiceTemplate> filteredList = filter.filter(serviceTemplates.getServiceTemplates());
        assertEquals(4, filteredList.size());
        assertEquals("0.0.0", filteredList.get(0).getVersion());
        assertEquals("1.2.8", filteredList.get(1).getVersion());
        assertEquals("1.2.3", filteredList.get(2).getVersion());
        assertEquals("1.8.3", filteredList.get(3).getVersion());

        filter = ToscaServiceTemplateFilter.builder().version("1.2.3").build();
        filteredList = filter.filter(serviceTemplates.getServiceTemplates());
        assertEquals(3, filteredList.size());
        filter = ToscaServiceTemplateFilter.builder().version(PfKey.NULL_KEY_VERSION).build();
        filteredList = filter.filter(serviceTemplates.getServiceTemplates());
        assertEquals(6, filteredList.size());

        serviceTemplates.getServiceTemplates().get(12).setVersion("0.0.0");
        filteredList = filter.filter(serviceTemplates.getServiceTemplates());
        assertEquals(7, filteredList.size());
        assertEquals(PfKey.NULL_KEY_VERSION, filteredList.get(0).getVersion());
        assertEquals(PfKey.NULL_KEY_VERSION, filteredList.get(6).getVersion());
    }

    @Test
    public void testFilterNameVersion() throws CoderException {
        ToscaServiceTemplates serviceTemplates = new StandardYamlCoder().decode(
                new File("src/test/resources/servicetemplates/TestServiceTemplates.yaml"), ToscaServiceTemplates.class);

        ToscaServiceTemplateFilter filter = ToscaServiceTemplateFilter.builder().name("name0").build();
        List<ToscaServiceTemplate> filteredList = filter.filter(serviceTemplates.getServiceTemplates());
        assertEquals(13, filteredList.size());

        filter = ToscaServiceTemplateFilter.builder().name("not.found").build();
        filteredList = filter.filter(serviceTemplates.getServiceTemplates());
        assertEquals(0, filteredList.size());

        filter = ToscaServiceTemplateFilter.builder().name(PfKey.NULL_KEY_NAME).build();
        filteredList = filter.filter(serviceTemplates.getServiceTemplates());
        assertEquals(2, filteredList.size());

        filter = ToscaServiceTemplateFilter.builder().version(PfKey.NULL_KEY_VERSION).build();
        filteredList = filter.filter(serviceTemplates.getServiceTemplates());
        assertEquals(6, filteredList.size());

        filter = ToscaServiceTemplateFilter.builder().name(PfKey.NULL_KEY_NAME).version(PfKey.NULL_KEY_VERSION).build();
        filteredList = filter.filter(serviceTemplates.getServiceTemplates());
        assertEquals(2, filteredList.size());

        filter = ToscaServiceTemplateFilter.builder().name("name2").build();
        filteredList = filter.filter(serviceTemplates.getServiceTemplates());
        assertEquals(1, filteredList.size());

        filter = ToscaServiceTemplateFilter.builder().name("name2").version("1.8.3").build();
        filteredList = filter.filter(serviceTemplates.getServiceTemplates());
        assertEquals(1, filteredList.size());
        assertEquals("name2", filteredList.get(0).getName());
        assertEquals("1.8.3", filteredList.get(0).getVersion());
    }
}
