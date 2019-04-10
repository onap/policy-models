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
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaServiceTemplate;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaServiceTemplates;

public class JpaToscaServiceTemplatesTest {

    @Test
    public void testServiceTemplates() {
        assertNotNull(new JpaToscaServiceTemplates());
        assertNotNull(new JpaToscaServiceTemplates(new PfConceptKey()));
        assertNotNull(
                new JpaToscaServiceTemplates(new PfConceptKey(), new TreeMap<PfConceptKey, JpaToscaServiceTemplate>()));
        assertNotNull(new JpaToscaServiceTemplates(new JpaToscaServiceTemplates()));

        assertThatThrownBy(() -> {
            new JpaToscaServiceTemplates((PfConceptKey) null);
        }).hasMessage("key is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new JpaToscaServiceTemplates((JpaToscaServiceTemplates) null);
        }).hasMessage("copyConcept is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new JpaToscaServiceTemplates(null, null);
        }).hasMessage("key is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new JpaToscaServiceTemplates(new PfConceptKey(), null);
        }).hasMessage("conceptMap is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new JpaToscaServiceTemplates(null, new TreeMap<PfConceptKey, JpaToscaServiceTemplate>());
        }).hasMessage("key is marked @NonNull but is null");

        List<Map<String, ToscaServiceTemplate>> tsMapList = new ArrayList<>();
        tsMapList.add(new LinkedHashMap<>());
        tsMapList.get(0).put("serviceTemplate", new ToscaServiceTemplate());
        assertNotNull(new JpaToscaServiceTemplates(tsMapList));
    }
}
