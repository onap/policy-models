/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020, 2024 Nordix Foundation.
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Test the {@link PfObjectFilter} interface.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
class PfConceptFilterTest {

    @Test
    void testPfConceptFilter() {
        List<PfConcept> listToBeFiltered = new ArrayList<>();

        PfConceptFilter conceptFilter = new PfConceptFilter(null, null, null);
        List<PfConcept> filteredList = conceptFilter.filter(listToBeFiltered);
        assertTrue(filteredList.isEmpty());

        conceptFilter = new PfConceptFilter(null, PfConceptFilter.LATEST_VERSION, null);
        filteredList = conceptFilter.filter(listToBeFiltered);
        assertTrue(filteredList.isEmpty());

        assertThatThrownBy(() -> {
            final PfConceptFilter conceptFilterNull = new PfConceptFilter(null, null, null);
            conceptFilterNull.filter(null);
        }).hasMessageMatching("^originalList is marked .*on.*ull but is null$");

        conceptFilter.setName("hello");
        assertEquals("hello", conceptFilter.getName());

        conceptFilter.setVersion("1.2.3");
        assertEquals("1.2.3", conceptFilter.getVersion());

        conceptFilter.setVersionPrefix("AAA");
        assertEquals("AAA", conceptFilter.getVersionPrefix());
    }
}
