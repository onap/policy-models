/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2020, 2024 Nordix Foundation.
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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.onap.policy.models.base.testconcepts.DummyPfNameVersion;

/**
 * Test the {@link PfNameVersion} interface.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
class PfNameVersionTest {

    @Test
    void testPfNameVersion() {
        DummyPfNameVersion dnv0 = new DummyPfNameVersion();
        DummyPfNameVersion dnv1 = new DummyPfNameVersion();

        assertEquals(0, dnv0.compareNameVersion(dnv0, dnv1));
        assertEquals(0, dnv0.compareNameVersion(null, null));
        assertEquals(-1, dnv0.compareNameVersion(dnv0, null));
        assertEquals(1, dnv0.compareNameVersion(null, dnv1));

        dnv1.setName("name1");
        assertEquals(-1, dnv0.compareNameVersion(dnv0, dnv1));

        dnv0.setName("name0");
        assertEquals(-1, dnv0.compareNameVersion(dnv0, dnv1));

        dnv1.setName("name0");
        assertEquals(0, dnv0.compareNameVersion(dnv0, dnv1));

        dnv1.setVersion("4.5.6");
        assertEquals(1, dnv0.compareNameVersion(dnv0, dnv1));

        dnv0.setVersion("1.2.3");
        assertEquals(-1, dnv0.compareNameVersion(dnv0, dnv1));

        dnv1.setVersion(null);
        assertEquals(-1, dnv0.compareNameVersion(dnv0, dnv1));

        dnv1.setVersion("1.2.3");
        assertEquals(0, dnv0.compareNameVersion(dnv0, dnv1));
    }
}
