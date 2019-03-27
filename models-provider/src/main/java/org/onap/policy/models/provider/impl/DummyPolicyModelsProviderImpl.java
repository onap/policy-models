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

package org.onap.policy.models.provider.impl;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response;

import lombok.NonNull;

import org.onap.policy.common.utils.resources.TextFileUtils;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.base.PfModelRuntimeException;
import org.onap.policy.models.pdp.concepts.PdpGroups;
import org.onap.policy.models.provider.PolicyModelsProvider;
import org.onap.policy.models.provider.PolicyModelsProviderParameters;
import org.onap.policy.models.tosca.legacy.concepts.LegacyGuardPolicyInput;
import org.onap.policy.models.tosca.legacy.concepts.LegacyGuardPolicyOutput;
import org.onap.policy.models.tosca.legacy.concepts.LegacyOperationalPolicy;
import org.onap.policy.models.tosca.simple.concepts.ToscaServiceTemplate;
import org.onap.policy.models.tosca.simple.serialization.ToscaServiceTemplateMessageBodyHandler;

/**
 * This class provides a dummy implementation of the Policy Models Provider for the ONAP Policy Framework.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class DummyPolicyModelsProviderImpl implements PolicyModelsProvider {
    /**
     * Constructor that takes the parameters.
     *
     * @param parameters the parameters for the provider
     */
    public DummyPolicyModelsProviderImpl(@NonNull final PolicyModelsProviderParameters parameters) {}

    @Override
    public void init() throws PfModelException {
        // Not required on the dummy provider
    }

    @Override
    public void close() {
        // Not required on the dummy provider
    }

    @Override
    public ToscaServiceTemplate getPolicyTypes(@NonNull final PfConceptKey policyTypeKey) throws PfModelException {
        return getDummyResponse("src/main/resources/dummyimpl/DummyToscaPolicyTypeGetResponse.json");
    }

    @Override
    public ToscaServiceTemplate createPolicyTypes(@NonNull final ToscaServiceTemplate serviceTemplate)
            throws PfModelException {
        return serviceTemplate;
    }

    @Override
    public ToscaServiceTemplate updatePolicyTypes(@NonNull final ToscaServiceTemplate serviceTemplate)
            throws PfModelException {
        return serviceTemplate;
    }

    @Override
    public ToscaServiceTemplate deletePolicyTypes(@NonNull final PfConceptKey policyTypeKey) throws PfModelException {
        return getDummyResponse("src/main/resources/dummyimpl/DummyToscaPolicyTypeDeleteResponse.json");
    }

    @Override
    public ToscaServiceTemplate getPolicies(@NonNull final PfConceptKey policyKey) throws PfModelException {
        return getDummyResponse("src/main/resources/dummyimpl/DummyToscaPolicyGetResponse.json");
    }

    @Override
    public ToscaServiceTemplate createPolicies(@NonNull final ToscaServiceTemplate serviceTemplate)
            throws PfModelException {
        return serviceTemplate;
    }

    @Override
    public ToscaServiceTemplate updatePolicies(@NonNull final ToscaServiceTemplate serviceTemplate)
            throws PfModelException {
        return serviceTemplate;
    }

    @Override
    public ToscaServiceTemplate deletePolicies(@NonNull final PfConceptKey policyKey) throws PfModelException {
        return getDummyResponse("src/main/resources/dummyimpl/DummyToscaPolicyDeleteResponse.json");
    }

    @Override

    public LegacyOperationalPolicy getOperationalPolicy(@NonNull final String policyId) throws PfModelException {
        return new LegacyOperationalPolicy();
    }

    @Override
    public LegacyOperationalPolicy createOperationalPolicy(
            @NonNull final LegacyOperationalPolicy legacyOperationalPolicy) throws PfModelException {
        return legacyOperationalPolicy;
    }

    @Override
    public LegacyOperationalPolicy updateOperationalPolicy(
            @NonNull final LegacyOperationalPolicy legacyOperationalPolicy) throws PfModelException {
        return legacyOperationalPolicy;
    }

    @Override
    public LegacyOperationalPolicy deleteOperationalPolicy(@NonNull final String policyId) throws PfModelException {
        return new LegacyOperationalPolicy();
    }

    @Override
    public Map<String, LegacyGuardPolicyOutput> getGuardPolicy(@NonNull final String policyId) throws PfModelException {
        return new HashMap<>();
    }

    @Override
    public Map<String, LegacyGuardPolicyOutput> createGuardPolicy(
            @NonNull final LegacyGuardPolicyInput legacyGuardPolicy) throws PfModelException {
        return new HashMap<>();
    }

    @Override
    public Map<String, LegacyGuardPolicyOutput> updateGuardPolicy(
            @NonNull final LegacyGuardPolicyInput legacyGuardPolicy) throws PfModelException {
        return new HashMap<>();
    }

    @Override
    public Map<String, LegacyGuardPolicyOutput> deleteGuardPolicy(@NonNull final String policyId)
            throws PfModelException {
        return new HashMap<>();
    }

    @Override
    public PdpGroups getPdpGroups(@NonNull String pdpGroupFilter) throws PfModelException {
        return new PdpGroups();
    }

    @Override
    public PdpGroups createPdpGroups(@NonNull PdpGroups pdpGroups) throws PfModelException {
        return new PdpGroups();
    }

    @Override
    public PdpGroups updatePdpGroups(@NonNull PdpGroups pdpGroups) throws PfModelException {
        return new PdpGroups();
    }

    @Override
    public PdpGroups deletePdpGroups(@NonNull String pdpGroupFilter) throws PfModelException {
        return new PdpGroups();
    }

    /**
     * Return a ToscaServicetemplate dummy response.
     *
     * @param fileName the file name containing the dummy response
     * @return the ToscaServiceTemplate with the dummy response
     */
    protected ToscaServiceTemplate getDummyResponse(@NonNull final String fileName) {
        Gson gson = new ToscaServiceTemplateMessageBodyHandler().getGson();
        ToscaServiceTemplate serviceTemplate;

        try {
            serviceTemplate = gson.fromJson(TextFileUtils.getTextFileAsString(fileName), ToscaServiceTemplate.class);
        } catch (Exception exc) {
            throw new PfModelRuntimeException(Response.Status.INTERNAL_SERVER_ERROR, "error serializing object", exc);
        }

        return serviceTemplate;
    }
}
