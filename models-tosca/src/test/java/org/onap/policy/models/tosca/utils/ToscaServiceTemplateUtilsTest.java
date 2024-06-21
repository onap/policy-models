/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020-2021 Nordix Foundation.
 *  Modifications Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.models.tosca.utils;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Iterator;
import org.junit.jupiter.api.Test;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.Validated;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaDataType;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaDataTypes;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaPolicies;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaPolicy;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaPolicyType;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaPolicyTypes;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaServiceTemplate;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaTopologyTemplate;

/**
 * Import the {@link ToscaServiceTemplateUtilsTest} class.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
class ToscaServiceTemplateUtilsTest {
    @Test
    void testAddFragmentNulls() {
        assertThatThrownBy(() -> {
            ToscaServiceTemplateUtils.addFragment(null, null);
        }).hasMessageMatching("originalTemplate is marked .*on.*ull but is null");

        assertThatThrownBy(() -> {
            ToscaServiceTemplateUtils.addFragment(null, new JpaToscaServiceTemplate());
        }).hasMessageMatching("originalTemplate is marked .*on.*ull but is null");

        assertThatThrownBy(() -> {
            ToscaServiceTemplateUtils.addFragment(new JpaToscaServiceTemplate(), null);
        }).hasMessageMatching("fragmentTemplate is marked .*on.*ull but is null");

        assertEquals(new JpaToscaServiceTemplate(),
                ToscaServiceTemplateUtils.addFragment(new JpaToscaServiceTemplate(), new JpaToscaServiceTemplate()));
    }

    @Test
    void testAddFragment() {
        JpaToscaServiceTemplate originalTemplate = new JpaToscaServiceTemplate();
        JpaToscaServiceTemplate fragmentTemplate00 = new JpaToscaServiceTemplate();

        assertEquals(originalTemplate, ToscaServiceTemplateUtils.addFragment(originalTemplate, originalTemplate));
        assertEquals(originalTemplate, ToscaServiceTemplateUtils.addFragment(originalTemplate, fragmentTemplate00));
        assertEquals(originalTemplate, ToscaServiceTemplateUtils.addFragment(fragmentTemplate00, originalTemplate));
        assertEquals(originalTemplate, ToscaServiceTemplateUtils.addFragment(fragmentTemplate00, fragmentTemplate00));
        assertEquals(fragmentTemplate00, ToscaServiceTemplateUtils.addFragment(originalTemplate, originalTemplate));
        assertEquals(fragmentTemplate00, ToscaServiceTemplateUtils.addFragment(originalTemplate, fragmentTemplate00));
        assertEquals(fragmentTemplate00, ToscaServiceTemplateUtils.addFragment(fragmentTemplate00, originalTemplate));
        assertEquals(fragmentTemplate00, ToscaServiceTemplateUtils.addFragment(fragmentTemplate00, fragmentTemplate00));

        fragmentTemplate00.setDataTypes(new JpaToscaDataTypes());
        fragmentTemplate00.setPolicyTypes(new JpaToscaPolicyTypes());

        JpaToscaServiceTemplate compositeTemplate00 =
                ToscaServiceTemplateUtils.addFragment(originalTemplate, fragmentTemplate00);
        checkFragments(compositeTemplate00, fragmentTemplate00);

        JpaToscaDataType dt0 = new JpaToscaDataType();
        dt0.setKey(new PfConceptKey("dt0", "0.0.1"));
        dt0.setDescription("dt0 description");
        JpaToscaServiceTemplate fragmentTemplate01 = new JpaToscaServiceTemplate();
        fragmentTemplate01.setDataTypes(new JpaToscaDataTypes());
        fragmentTemplate01.getDataTypes().getConceptMap().put(dt0.getKey(), dt0);

        JpaToscaServiceTemplate compositeTemplate01 =
                ToscaServiceTemplateUtils.addFragment(originalTemplate, fragmentTemplate01);
        checkFragments(compositeTemplate01, fragmentTemplate01);

        JpaToscaServiceTemplate compositeTemplate02 =
                ToscaServiceTemplateUtils.addFragment(compositeTemplate00, fragmentTemplate01);
        checkFragments(compositeTemplate02, fragmentTemplate01);

        JpaToscaDataType otherDt0 = new JpaToscaDataType();
        otherDt0.setKey(new PfConceptKey("dt0", "0.0.1"));
        otherDt0.setDescription("dt0 description");
        JpaToscaServiceTemplate fragmentTemplate02 = new JpaToscaServiceTemplate();
        fragmentTemplate02.setDataTypes(new JpaToscaDataTypes());
        fragmentTemplate02.getDataTypes().getConceptMap().put(otherDt0.getKey(), otherDt0);

        compositeTemplate00 = ToscaServiceTemplateUtils.addFragment(compositeTemplate00, fragmentTemplate02);
        assertEquals(compositeTemplate00,
                ToscaServiceTemplateUtils.addFragment(compositeTemplate00, fragmentTemplate02));

        JpaToscaDataType badOtherDt0 = new JpaToscaDataType();
        badOtherDt0.setKey(new PfConceptKey("dt0", "0.0.1"));
        badOtherDt0.setDescription("dt0 bad description");
        JpaToscaServiceTemplate fragmentTemplate03 = new JpaToscaServiceTemplate();
        fragmentTemplate03.setDataTypes(new JpaToscaDataTypes());
        fragmentTemplate03.getDataTypes().getConceptMap().put(otherDt0.getKey(), otherDt0);
        fragmentTemplate03.getDataTypes().getConceptMap().put(badOtherDt0.getKey(), badOtherDt0);

        final JpaToscaServiceTemplate compositeTestTemplate = new JpaToscaServiceTemplate(compositeTemplate00);
        assertThatThrownBy(() -> {
            ToscaServiceTemplateUtils.addFragment(compositeTestTemplate, fragmentTemplate03);
        }).hasMessageContaining("incoming fragment").hasMessageContaining("entity").hasMessageContaining("dt0:0.0.1")
                        .hasMessageContaining("does not equal existing entity");

        JpaToscaServiceTemplate fragmentTemplate04 = new JpaToscaServiceTemplate();
        fragmentTemplate04.setDescription("Another service template");
        assertThatThrownBy(() -> {
            ToscaServiceTemplateUtils.addFragment(compositeTestTemplate, fragmentTemplate04);
        }).hasMessageContaining("service template").hasMessageContaining("does not equal existing service template");

        JpaToscaServiceTemplate fragmentTemplate05 = new JpaToscaServiceTemplate();
        fragmentTemplate05.setTopologyTemplate(new JpaToscaTopologyTemplate());
        fragmentTemplate05.getTopologyTemplate().setDescription("topology template description");
        JpaToscaServiceTemplate compositeTemplate03 =
                ToscaServiceTemplateUtils.addFragment(compositeTemplate02, fragmentTemplate05);
        assertEquals(fragmentTemplate05.getTopologyTemplate(), compositeTemplate03.getTopologyTemplate());

        JpaToscaServiceTemplate fragmentTemplate06 = new JpaToscaServiceTemplate();
        fragmentTemplate06.setTopologyTemplate(new JpaToscaTopologyTemplate());
        fragmentTemplate06.getTopologyTemplate().setDescription("topology template description");
        JpaToscaServiceTemplate compositeTemplate04 =
                ToscaServiceTemplateUtils.addFragment(compositeTemplate03, fragmentTemplate06);
        assertEquals(fragmentTemplate06.getTopologyTemplate(), compositeTemplate04.getTopologyTemplate());

        JpaToscaServiceTemplate fragmentTemplate07 = new JpaToscaServiceTemplate();
        fragmentTemplate07.setTopologyTemplate(new JpaToscaTopologyTemplate());
        fragmentTemplate07.getTopologyTemplate().setDescription("topology template other description");
        assertThatThrownBy(() -> {
            ToscaServiceTemplateUtils.addFragment(compositeTemplate04, fragmentTemplate07);
        }).hasMessageContaining("incoming fragment").hasMessageContaining("topology template")
                        .hasMessageContaining("does not equal existing topology template");

        JpaToscaDataType dt1 = new JpaToscaDataType();
        dt1.setKey(new PfConceptKey("dt1", "0.0.1"));
        dt1.setDescription("dt1 description");

        JpaToscaPolicyType pt0 = new JpaToscaPolicyType();
        pt0.setKey(new PfConceptKey("pt0", "0.0.1"));
        pt0.setDescription("pt0 description");

        JpaToscaPolicy p0 = new JpaToscaPolicy();
        p0.setKey(new PfConceptKey("p0", "0.0.1"));
        p0.setDescription("pt0 description");

        JpaToscaServiceTemplate fragmentTemplate08 = new JpaToscaServiceTemplate();

        fragmentTemplate08.setDataTypes(new JpaToscaDataTypes());
        fragmentTemplate08.getDataTypes().getConceptMap().put(dt1.getKey(), dt1);

        fragmentTemplate08.setPolicyTypes(new JpaToscaPolicyTypes());
        fragmentTemplate08.getPolicyTypes().getConceptMap().put(pt0.getKey(), pt0);

        fragmentTemplate08.setTopologyTemplate(new JpaToscaTopologyTemplate());
        fragmentTemplate08.getTopologyTemplate().setDescription("topology template description");

        fragmentTemplate08.getTopologyTemplate().setPolicies(new JpaToscaPolicies());
        fragmentTemplate08.getTopologyTemplate().getPolicies().getConceptMap().put(p0.getKey(), p0);

        assertThatThrownBy(() -> {
            ToscaServiceTemplateUtils.addFragment(compositeTemplate04, fragmentTemplate08);
        }).hasMessageContaining("type").hasMessageContaining(Validated.IS_A_NULL_KEY);

        p0.setType(pt0.getKey());

        JpaToscaServiceTemplate compositeTemplate05 =
                ToscaServiceTemplateUtils.addFragment(compositeTemplate04, fragmentTemplate08);
        Iterator<JpaToscaDataType> dtIterator = compositeTemplate05.getDataTypes().getAll(null).iterator();
        assertEquals(dt0, dtIterator.next());
        assertEquals(dt1, dtIterator.next());
        assertEquals(pt0, compositeTemplate05.getPolicyTypes().getAll(null).iterator().next());
        assertEquals(p0, compositeTemplate05.getTopologyTemplate().getPolicies().getAll(null).iterator().next());

        JpaToscaServiceTemplate fragmentTemplate09 = new JpaToscaServiceTemplate();

        fragmentTemplate09.setDataTypes(new JpaToscaDataTypes());
        fragmentTemplate09.getDataTypes().getConceptMap().put(dt1.getKey(), dt1);

        fragmentTemplate09.setPolicyTypes(new JpaToscaPolicyTypes());
        fragmentTemplate09.getPolicyTypes().getConceptMap().put(pt0.getKey(), pt0);

        fragmentTemplate09.setTopologyTemplate(null);

        JpaToscaServiceTemplate compositeTemplate06 =
                ToscaServiceTemplateUtils.addFragment(compositeTemplate05, fragmentTemplate09);
        assertEquals(compositeTemplate05.getTopologyTemplate(), compositeTemplate06.getTopologyTemplate());
    }

    private void checkFragments(JpaToscaServiceTemplate compositeTemplate, JpaToscaServiceTemplate fragmentTemplate) {
        assertEquals(compositeTemplate,
                ToscaServiceTemplateUtils.addFragment(compositeTemplate, fragmentTemplate));
        assertEquals(compositeTemplate,
                ToscaServiceTemplateUtils.addFragment(compositeTemplate, new JpaToscaServiceTemplate()));
        assertEquals(compositeTemplate,
                ToscaServiceTemplateUtils.addFragment(new JpaToscaServiceTemplate(), compositeTemplate));
    }
}
