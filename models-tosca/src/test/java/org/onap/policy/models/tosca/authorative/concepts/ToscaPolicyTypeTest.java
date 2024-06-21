/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2021 Nordix Foundation.
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.LinkedHashMap;
import org.junit.jupiter.api.Test;

/**
 * Test of the {@link ToscaPolicyType} class.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
class ToscaPolicyTypeTest {

    @Test
    void testToscaPolicyType() {
        assertThatThrownBy(() -> {
            new ToscaPolicyType(null);
        }).hasMessageMatching("copyObject is marked .*on.*ull but is null");

        ToscaPolicyType tpt = new ToscaPolicyType();
        tpt.setName("AType");
        tpt.setVersion("1.2.3");
        tpt.setDerivedFrom("AParentType");
        tpt.setDescription("Desc");

        ToscaPolicyType clonedTpt0 = new ToscaPolicyType(tpt);
        assertEquals(0, new ToscaEntityComparator<ToscaPolicyType>().compare(tpt, clonedTpt0));

        tpt.setMetadata(new LinkedHashMap<>());
        tpt.setProperties(new LinkedHashMap<>());

        tpt.getMetadata().put("MetaKey0", "Metavalue 0");

        ToscaProperty tp = new ToscaProperty();
        tpt.getProperties().put("Property0", tp);

        ToscaPolicyType clonedTpt1 = new ToscaPolicyType(tpt);
        assertEquals(0, new ToscaEntityComparator<ToscaPolicyType>().compare(tpt, clonedTpt1));
    }
}
