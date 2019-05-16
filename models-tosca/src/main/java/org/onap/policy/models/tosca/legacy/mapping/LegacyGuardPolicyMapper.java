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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.core.Response;

import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfModelRuntimeException;
import org.onap.policy.models.tosca.legacy.concepts.LegacyGuardPolicyContent;
import org.onap.policy.models.tosca.legacy.concepts.LegacyGuardPolicyInput;
import org.onap.policy.models.tosca.legacy.concepts.LegacyGuardPolicyOutput;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaPolicies;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaPolicy;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaServiceTemplate;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaTopologyTemplate;
import org.onap.policy.models.tosca.simple.mapping.JpaToscaServiceTemplateMapper;
import org.onap.policy.models.tosca.utils.ToscaUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class maps a legacy guard policy to and from a TOSCA service template.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class LegacyGuardPolicyMapper
        implements JpaToscaServiceTemplateMapper<LegacyGuardPolicyInput, Map<String, LegacyGuardPolicyOutput>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(LegacyGuardPolicyMapper.class);

    // Tag for metadata fields
    private static final String POLICY_ID = "policy-id";
    private static final String POLICY_VERSION = "policy-version";

    private static final Map<String, PfConceptKey> GUARD_POLICY_TYPE_MAP = new LinkedHashMap<>();

    static {
        GUARD_POLICY_TYPE_MAP.put("guard.frequency.",
                new PfConceptKey("onap.policies.controlloop.guard.FrequencyLimiter:1.0.0"));
        GUARD_POLICY_TYPE_MAP.put("guard.minmax.",
                new PfConceptKey("onap.policies.controlloop.guard.MinMax:1.0.0"));
        GUARD_POLICY_TYPE_MAP.put("guard.blacklist",
                new PfConceptKey("onap.policies.controlloop.guard.Blacklist:1.0.0"));
    }

    @Override
    public JpaToscaServiceTemplate toToscaServiceTemplate(final LegacyGuardPolicyInput legacyGuardPolicyInput) {
        PfConceptKey guardPolicyType = getGuardPolicyType(legacyGuardPolicyInput);
        if (guardPolicyType == null) {
            String errorMessage =
                    "policy type for guard policy \"" + legacyGuardPolicyInput.getPolicyId() + "\" unknown";
            LOGGER.warn(errorMessage);
            throw new PfModelRuntimeException(Response.Status.BAD_REQUEST, errorMessage);
        }

        String version = legacyGuardPolicyInput.getPolicyVersion();
        if (version != null) {
            version = version + ".0.0";
        } else {
            version = guardPolicyType.getVersion();
        }

        PfConceptKey policyKey = new PfConceptKey(legacyGuardPolicyInput.getPolicyId(), version);

        final JpaToscaPolicy toscaPolicy = new JpaToscaPolicy(policyKey);
        toscaPolicy.setType(guardPolicyType);
        toscaPolicy.setProperties(legacyGuardPolicyInput.getContent().getAsPropertyMap());

        final Map<String, String> metadata = new LinkedHashMap<>();
        metadata.put(POLICY_ID, toscaPolicy.getKey().getName());
        metadata.put(POLICY_VERSION, Integer.toString(toscaPolicy.getKey().getMajorVersion()));
        toscaPolicy.setMetadata(metadata);

        final JpaToscaServiceTemplate serviceTemplate = new JpaToscaServiceTemplate();
        serviceTemplate.setToscaDefinitionsVersion("tosca_simple_yaml_1_0");

        serviceTemplate.setTopologyTemplate(new JpaToscaTopologyTemplate());

        serviceTemplate.getTopologyTemplate().setPolicies(new JpaToscaPolicies());
        serviceTemplate.getTopologyTemplate().getPolicies().getConceptMap().put(policyKey, toscaPolicy);

        return serviceTemplate;
    }

    @Override
    public Map<String, LegacyGuardPolicyOutput> fromToscaServiceTemplate(
            final JpaToscaServiceTemplate serviceTemplate) {
        ToscaUtils.assertPoliciesExist(serviceTemplate);

        final Map<String, LegacyGuardPolicyOutput> legacyGuardPolicyOutputMap = new LinkedHashMap<>();

        for (JpaToscaPolicy toscaPolicy : serviceTemplate.getTopologyTemplate().getPolicies().getConceptMap()
                .values()) {

            final LegacyGuardPolicyOutput legacyGuardPolicyOutput = new LegacyGuardPolicyOutput();
            legacyGuardPolicyOutput.setType(toscaPolicy.getType().getName());
            legacyGuardPolicyOutput.setVersion(toscaPolicy.getType().getVersion());

            if (toscaPolicy.getMetadata() == null) {
                String errorMessage = "no metadata defined on TOSCA policy";
                LOGGER.warn(errorMessage);
                throw new PfModelRuntimeException(Response.Status.BAD_REQUEST, errorMessage);
            }

            final Map<String, Object> metadata = new LinkedHashMap<>();
            for (Entry<String, String> metadataEntry : toscaPolicy.getMetadata().entrySet()) {
                if (POLICY_VERSION.equals(metadataEntry.getKey())) {
                    metadata.put(POLICY_VERSION, Integer.parseInt(metadataEntry.getValue()));
                } else {
                    metadata.put(metadataEntry.getKey(), metadataEntry.getValue());
                }
            }
            legacyGuardPolicyOutput.setMetadata(metadata);

            if (toscaPolicy.getProperties() == null) {
                String errorMessage = "no properties defined on TOSCA policy";
                LOGGER.warn(errorMessage);
                throw new PfModelRuntimeException(Response.Status.BAD_REQUEST, errorMessage);
            }

            final LegacyGuardPolicyContent content = new LegacyGuardPolicyContent();
            content.setContent(toscaPolicy.getProperties());

            final Map<String, LegacyGuardPolicyContent> propertiesMap = new LinkedHashMap<>();
            propertiesMap.put("content", content);
            legacyGuardPolicyOutput.setProperties(propertiesMap);

            legacyGuardPolicyOutputMap.put(toscaPolicy.getKey().getName(), legacyGuardPolicyOutput);
        }

        return legacyGuardPolicyOutputMap;
    }

    private PfConceptKey getGuardPolicyType(final LegacyGuardPolicyInput legacyGuardPolicyInput) {
        final String policyId = legacyGuardPolicyInput.getPolicyId();
        if (policyId == null) {
            return null;
        }

        for (Entry<String, PfConceptKey> guardPolicyTypeEntry : GUARD_POLICY_TYPE_MAP.entrySet()) {
            if (policyId.startsWith(guardPolicyTypeEntry.getKey())) {
                return guardPolicyTypeEntry.getValue();
            }
        }

        return null;
    }
}
