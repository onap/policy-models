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

import javax.ws.rs.core.Response;

import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfModelRuntimeException;
import org.onap.policy.models.tosca.legacy.concepts.LegacyOperationalPolicy;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaPolicies;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaPolicy;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaServiceTemplate;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaTopologyTemplate;
import org.onap.policy.models.tosca.simple.mapping.JpaToscaServiceTemplateMapper;
import org.onap.policy.models.tosca.utils.ToscaUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class maps a legacy operational policy to and from a TOSCA service template.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class LegacyOperationalPolicyMapper
        implements JpaToscaServiceTemplateMapper<LegacyOperationalPolicy, LegacyOperationalPolicy> {

    // Property name for the operational policy content
    private static final String CONTENT_PROPERTY = "content";

    private static final Logger LOGGER = LoggerFactory.getLogger(LegacyOperationalPolicyMapper.class);

    private static final PfConceptKey LEGACY_OPERATIONAL_TYPE =
            new PfConceptKey("onap.policies.controlloop.Operational", "1.0.0");

    @Override
    public JpaToscaServiceTemplate toToscaServiceTemplate(final LegacyOperationalPolicy legacyOperationalPolicy) {
        String incomingVersion = legacyOperationalPolicy.getPolicyVersion();
        if (incomingVersion == null) {
            incomingVersion = "1";
        }

        PfConceptKey policyKey = new PfConceptKey(legacyOperationalPolicy.getPolicyId(), incomingVersion + ".0.0");

        final JpaToscaPolicy toscaPolicy = new JpaToscaPolicy(policyKey);

        toscaPolicy.setType(LEGACY_OPERATIONAL_TYPE);

        final Map<String, String> propertyMap = new HashMap<>();
        toscaPolicy.setProperties(propertyMap);
        toscaPolicy.getProperties().put(CONTENT_PROPERTY, legacyOperationalPolicy.getContent());

        final JpaToscaServiceTemplate serviceTemplate = new JpaToscaServiceTemplate();
        serviceTemplate.setToscaDefinitionsVersion("tosca_simple_yaml_1_0");

        serviceTemplate.setTopologyTemplate(new JpaToscaTopologyTemplate());

        serviceTemplate.getTopologyTemplate().setPolicies(new JpaToscaPolicies());
        serviceTemplate.getTopologyTemplate().getPolicies().getConceptMap().put(policyKey, toscaPolicy);

        return serviceTemplate;
    }

    @Override
    public LegacyOperationalPolicy fromToscaServiceTemplate(final JpaToscaServiceTemplate serviceTemplate) {
        ToscaUtils.assertPoliciesExist(serviceTemplate);

        if (serviceTemplate.getTopologyTemplate().getPolicies().getConceptMap().size() > 1) {
            String errorMessage = "more than one policy found in service template";
            LOGGER.warn(errorMessage);
            throw new PfModelRuntimeException(Response.Status.BAD_REQUEST, errorMessage);
        }

        // Get the policy
        final JpaToscaPolicy toscaPolicy =
                serviceTemplate.getTopologyTemplate().getPolicies().getAll(null).iterator().next();

        final LegacyOperationalPolicy legacyOperationalPolicy = new LegacyOperationalPolicy();
        legacyOperationalPolicy.setPolicyId(toscaPolicy.getKey().getName());
        legacyOperationalPolicy.setPolicyVersion(Integer.toString(toscaPolicy.getKey().getMajorVersion()));

        if (toscaPolicy.getProperties() == null) {
            String errorMessage = "no properties defined on TOSCA policy";
            LOGGER.warn(errorMessage);
            throw new PfModelRuntimeException(Response.Status.BAD_REQUEST, errorMessage);
        }

        final String content = toscaPolicy.getProperties().get(CONTENT_PROPERTY);
        if (content == null) {
            String errorMessage = "property \"content\" not defined on TOSCA policy";
            LOGGER.warn(errorMessage);
            throw new PfModelRuntimeException(Response.Status.BAD_REQUEST, errorMessage);
        }

        legacyOperationalPolicy.setContent(content);

        return legacyOperationalPolicy;
    }
}
