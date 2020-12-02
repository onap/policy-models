/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix Foundation.
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

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.onap.policy.models.base.testconcepts.DummyBadPfConcept;
import org.onap.policy.models.base.testconcepts.DummyPfConcept;

public class PfConceptComparatorTest {

    @Test
    public void testPfConceptComparator() {
        assertEquals(0, new PfConceptComparator().compare(new DummyPfConcept(), new DummyPfConcept()));
    }
}
