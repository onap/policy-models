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

package org.onap.policy.models.tosca.simple.concepts;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.junit.Test;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyType;

public class JpaToscaPolicyTypesTest {

    private static final String KEY_IS_NULL = "key is marked @NonNull but is null";

    @Test
    public void testPolicyTypes() {
        assertNotNull(new JpaToscaPolicyTypes());
        assertNotNull(new JpaToscaPolicyTypes(new PfConceptKey()));
        assertNotNull(new JpaToscaPolicyTypes(new PfConceptKey(), new TreeMap<PfConceptKey, JpaToscaPolicyType>()));
        assertNotNull(new JpaToscaPolicyTypes(new JpaToscaPolicyTypes()));

        assertThatThrownBy(() -> new JpaToscaPolicyTypes((PfConceptKey) null)).hasMessage(KEY_IS_NULL);

        assertThatThrownBy(() -> new JpaToscaPolicyTypes((JpaToscaPolicyTypes) null))
                        .hasMessage("copyConcept is marked @NonNull but is null");

        assertThatThrownBy(() -> new JpaToscaPolicyTypes(null, null)).hasMessage(KEY_IS_NULL);

        assertThatThrownBy(() -> new JpaToscaPolicyTypes(new PfConceptKey(), null))
                        .hasMessage("conceptMap is marked @NonNull but is null");

        assertThatThrownBy(() -> new JpaToscaPolicyTypes(null, new TreeMap<PfConceptKey, JpaToscaPolicyType>()))
                        .hasMessage(KEY_IS_NULL);

        List<Map<String, ToscaPolicyType>> ptMapList = new ArrayList<>();
        ptMapList.add(new LinkedHashMap<>());
        ptMapList.get(0).put("policyType", new ToscaPolicyType());
        assertNotNull(new JpaToscaPolicyTypes(ptMapList));
    }
}
