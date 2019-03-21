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

package org.onap.policy.models.tosca.legacy.mapping;

import java.util.HashMap;
import java.util.Map;

import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfReferenceKey;
import org.onap.policy.models.tosca.legacy.concepts.LegacyOperationalPolicy;
import org.onap.policy.models.tosca.simple.concepts.ToscaPolicies;
import org.onap.policy.models.tosca.simple.concepts.ToscaPolicy;
import org.onap.policy.models.tosca.simple.concepts.ToscaServiceTemplate;
import org.onap.policy.models.tosca.simple.concepts.ToscaTopologyTemplate;
import org.onap.policy.models.tosca.simple.mapping.ToscaServiceTemplateMapper;

/**
 * This class maps a legacy operational policy to and from a TOSCA service template.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class LegacyOperationalPolicyMapper
        implements ToscaServiceTemplateMapper<LegacyOperationalPolicy, LegacyOperationalPolicy> {

    // TODO: Do this correctly with an atomic integer
    private static int nextVersion = 1;

    @Override
    public ToscaServiceTemplate toToscaServiceTemplate(LegacyOperationalPolicy legacyOperationalPolicy) {
        PfConceptKey policyKey =
                new PfConceptKey(legacyOperationalPolicy.getPolicyId(), getNextVersion());

        ToscaPolicy toscaPolicy = new ToscaPolicy(policyKey);

        // TODO: Find out how to parse the PolicyType from the content
        // TODO: Check if this is the correct way to set the policy type version
        toscaPolicy.setType(new PfConceptKey("SomeDerivedPolicyType", "1.0.1"));

        Map<String, String> propertyMap = new HashMap<>();
        toscaPolicy.setProperties(propertyMap);
        toscaPolicy.getProperties().put("Content", legacyOperationalPolicy.getContent());

        PfConceptKey serviceTemplateKey = new PfConceptKey("ServiceTemplate", "1.0.2");
        ToscaServiceTemplate serviceTemplate = new ToscaServiceTemplate(serviceTemplateKey);
        serviceTemplate.setToscaDefinitionsVersion("tosca_simple_yaml_1_0");

        PfReferenceKey topologyTemplateKey = new PfReferenceKey(serviceTemplateKey, "TopolocyTemplate");
        serviceTemplate.setTopologyTemplate(new ToscaTopologyTemplate(topologyTemplateKey));

        PfConceptKey policiesKey = new PfConceptKey("Policies", "1.0.3");
        serviceTemplate.getTopologyTemplate().setPolicies(new ToscaPolicies(policiesKey));
        serviceTemplate.getTopologyTemplate().getPolicies().getConceptMap().put(policyKey, toscaPolicy);

        return serviceTemplate;
    }

    @Override
    public LegacyOperationalPolicy fromToscaServiceTemplate(ToscaServiceTemplate serviceTemplate) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Get the next policy version.
     *
     * @return the next version
     */
    private static String getNextVersion() {
        return "1.0." + nextVersion++;
    }
}
