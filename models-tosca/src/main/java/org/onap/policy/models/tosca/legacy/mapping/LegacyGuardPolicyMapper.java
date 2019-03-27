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

import javax.ws.rs.core.Response;

import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfModelRuntimeException;
import org.onap.policy.models.tosca.legacy.concepts.LegacyGuardPolicyContent;
import org.onap.policy.models.tosca.legacy.concepts.LegacyGuardPolicyInput;
import org.onap.policy.models.tosca.legacy.concepts.LegacyGuardPolicyOutput;
import org.onap.policy.models.tosca.simple.concepts.ToscaPolicies;
import org.onap.policy.models.tosca.simple.concepts.ToscaPolicy;
import org.onap.policy.models.tosca.simple.concepts.ToscaServiceTemplate;
import org.onap.policy.models.tosca.simple.concepts.ToscaTopologyTemplate;
import org.onap.policy.models.tosca.simple.mapping.ToscaServiceTemplateMapper;
import org.onap.policy.models.tosca.utils.ToscaUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class maps a legacy guard policy to and from a TOSCA service template.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class LegacyGuardPolicyMapper
        implements ToscaServiceTemplateMapper<LegacyGuardPolicyInput, Map<String, LegacyGuardPolicyOutput>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(LegacyGuardPolicyMapper.class);

    private static final Map<String, PfConceptKey> GUARD_POLICY_TYPE_MAP = new LinkedHashMap<>();

    static {
        GUARD_POLICY_TYPE_MAP.put("guard.frequency.scaleout",
                new PfConceptKey("onap.policies.controlloop.guard.FrequencyLimiter:1.0.0"));
        GUARD_POLICY_TYPE_MAP.put("guard.minmax.scaleout",
                new PfConceptKey("onap.policies.controlloop.guard.MinMax:1.0.0"));
        GUARD_POLICY_TYPE_MAP.put("guard.minmax.scaleout",
                new PfConceptKey("onap.policies.controlloop.guard.MinMax:1.0.0"));
        GUARD_POLICY_TYPE_MAP.put("guard.blacklist",
                new PfConceptKey("onap.policies.controlloop.guard.Blacklist:1.0.0"));
    }

    @Override
    public ToscaServiceTemplate toToscaServiceTemplate(final LegacyGuardPolicyInput legacyGuardPolicyInput) {
        PfConceptKey guardPolicyType = GUARD_POLICY_TYPE_MAP.get(legacyGuardPolicyInput.getPolicyId());
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

        final ToscaPolicy toscaPolicy = new ToscaPolicy(policyKey);
        toscaPolicy.setType(guardPolicyType);
        toscaPolicy.setProperties(legacyGuardPolicyInput.getContent().getAsPropertyMap());

        final ToscaServiceTemplate serviceTemplate = new ToscaServiceTemplate();
        serviceTemplate.setToscaDefinitionsVersion("tosca_simimport java.util.HashMap;\n" + "ple_yaml_1_0");

        serviceTemplate.setTopologyTemplate(new ToscaTopologyTemplate());

        serviceTemplate.getTopologyTemplate().setPolicies(new ToscaPolicies());
        serviceTemplate.getTopologyTemplate().getPolicies().getConceptMap().put(policyKey, toscaPolicy);

        return serviceTemplate;
    }

    @Override
    public Map<String, LegacyGuardPolicyOutput> fromToscaServiceTemplate(final ToscaServiceTemplate serviceTemplate) {
        ToscaUtils.assertPoliciesExist(serviceTemplate);

        final Map<String, LegacyGuardPolicyOutput> legacyGuardPolicyOutputMap = new LinkedHashMap<>();

        for (ToscaPolicy toscaPolicy : serviceTemplate.getTopologyTemplate().getPolicies().getConceptMap().values()) {

            final LegacyGuardPolicyOutput legacyGuardPolicyOutput = new LegacyGuardPolicyOutput();
            legacyGuardPolicyOutput.setType(toscaPolicy.getType().getName());
            legacyGuardPolicyOutput.setVersion(toscaPolicy.getType().getVersion());

            final Map<String, Object> metadata = new LinkedHashMap<>();
            metadata.put("policy-id", toscaPolicy.getKey().getName());
            metadata.put("policy-version", toscaPolicy.getKey().getMajorVersion());
            legacyGuardPolicyOutput.setMetadata(metadata);

            if (toscaPolicy.getProperties() == null) {
                String errorMessage = "no properties defined on TOSCA policy";
                LOGGER.warn(errorMessage);
                throw new PfModelRuntimeException(Response.Status.BAD_REQUEST, errorMessage);
            }

            final LegacyGuardPolicyContent content = new LegacyGuardPolicyContent();
            // @formatter:off
            content.setActor(           toscaPolicy.getProperties().get("actor"));
            content.setClname(          toscaPolicy.getProperties().get("clname"));
            content.setGuardActiveEnd(  toscaPolicy.getProperties().get("guardActiveEnd"));
            content.setGuardActiveStart(toscaPolicy.getProperties().get("guardActiveStart"));
            content.setLimit(           toscaPolicy.getProperties().get("limit"));
            content.setMax(             toscaPolicy.getProperties().get("max"));
            content.setMin(             toscaPolicy.getProperties().get("min"));
            content.setRecipe(          toscaPolicy.getProperties().get("recipe"));
            content.setTargets(         toscaPolicy.getProperties().get("targets"));
            content.setTimeUnits(       toscaPolicy.getProperties().get("timeUnits"));
            content.setTimeWindow(      toscaPolicy.getProperties().get("timeWindow"));
            // @formatter:on

            final Map<String, LegacyGuardPolicyContent> propertiesMap = new LinkedHashMap<>();
            propertiesMap.put("content", content);
            legacyGuardPolicyOutput.setProperties(propertiesMap);

            if (toscaPolicy.getProperties() == null) {
                String errorMessage = "property \"Content\" not defined on TOSCA policy";
                LOGGER.warn(errorMessage);
                throw new PfModelRuntimeException(Response.Status.BAD_REQUEST, errorMessage);
            }

            legacyGuardPolicyOutputMap.put(toscaPolicy.getKey().getName(), legacyGuardPolicyOutput);
        }

        return legacyGuardPolicyOutputMap;
    }
}
