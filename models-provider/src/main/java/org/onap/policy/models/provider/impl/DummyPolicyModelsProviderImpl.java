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

import javax.ws.rs.core.Response;

import lombok.NonNull;
import org.onap.policy.common.utils.resources.ResourceUtils;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.base.PfModelRuntimeException;
import org.onap.policy.models.provider.PolicyModelsProvider;
import org.onap.policy.models.tosca.legacy.concepts.LegacyGuardPolicy;
import org.onap.policy.models.tosca.legacy.concepts.LegacyOperationalPolicy;
import org.onap.policy.models.tosca.simple.concepts.ToscaServiceTemplate;
import org.onap.policy.models.tosca.simple.serialization.ToscaServiceTemplateMessageBodyHandler;

/**
 * This class provides a dummy implementation of the Policy Models Provider for the ONAP Policy
 * Framework.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class DummyPolicyModelsProviderImpl implements PolicyModelsProvider {
    @Override
    public ToscaServiceTemplate getPolicyTypes(@NonNull PfConceptKey policyTypeKey) throws PfModelException {
        return getDummyResponse("dummyimpl/DummyToscaPolicyTypeGetResponse.json");
    }

    @Override
    public ToscaServiceTemplate createPolicyTypes(@NonNull ToscaServiceTemplate serviceTemplate)
            throws PfModelException {
        return serviceTemplate;
    }

    @Override
    public ToscaServiceTemplate updatePolicyTypes(@NonNull ToscaServiceTemplate serviceTemplate)
            throws PfModelException {
        return serviceTemplate;
    }

    @Override
    public ToscaServiceTemplate deletePolicyTypes(@NonNull PfConceptKey policyTypeKey) throws PfModelException {
        return getDummyResponse("dummyimpl/DummyToscaPolicyTypeDeleteResponse.json");
    }

    @Override
    public ToscaServiceTemplate getPolicies(@NonNull PfConceptKey policyKey) throws PfModelException {
        return getDummyResponse("dummyimpl/DummyToscaPolicyGetResponse.json");
    }

    @Override
    public ToscaServiceTemplate createPolicies(@NonNull ToscaServiceTemplate serviceTemplate) throws PfModelException {
        return serviceTemplate;
    }

    @Override
    public ToscaServiceTemplate updatePolicies(@NonNull ToscaServiceTemplate serviceTemplate) throws PfModelException {
        return serviceTemplate;
    }

    @Override
    public ToscaServiceTemplate deletePolicies(@NonNull PfConceptKey policyKey) throws PfModelException {
        return getDummyResponse("dummyimpl/DummyToscaPolicyDeleteResponse.json");
    }

    @Override
    public LegacyOperationalPolicy getOperationalPolicy(@NonNull String policyId) throws PfModelException {
        return new LegacyOperationalPolicy();
    }

    @Override
    public LegacyOperationalPolicy createOperationalPolicy(@NonNull LegacyOperationalPolicy legacyOperationalPolicy)
            throws PfModelException {
        return legacyOperationalPolicy;
    }

    @Override
    public LegacyOperationalPolicy updateOperationalPolicy(@NonNull LegacyOperationalPolicy legacyOperationalPolicy)
            throws PfModelException {
        return legacyOperationalPolicy;
    }

    @Override
    public LegacyOperationalPolicy deleteOperationalPolicy(@NonNull String policyId) throws PfModelException {
        return new LegacyOperationalPolicy();
    }

    @Override
    public LegacyGuardPolicy getGuardPolicy(@NonNull String policyId) throws PfModelException {
        return new LegacyGuardPolicy();
    }

    @Override
    public LegacyGuardPolicy createGuardPolicy(@NonNull LegacyGuardPolicy legacyGuardPolicy) throws PfModelException {
        return legacyGuardPolicy;
    }

    @Override
    public LegacyGuardPolicy updateGuardPolicy(@NonNull LegacyGuardPolicy legacyGuardPolicy) throws PfModelException {
        return legacyGuardPolicy;
    }

    @Override
    public LegacyGuardPolicy deleteGuardPolicy(@NonNull String policyId) throws PfModelException {
        return new LegacyGuardPolicy();
    }

    @Override
    public Object getPdpGroups(@NonNull Object somePdpGroupFilter) throws PfModelException {
        return null;
    }

    @Override
    public Object createPdpGroups(@NonNull Object somePdpGroupSpecification) throws PfModelException {
        return null;
    }

    @Override
    public Object updatePdpGroups(@NonNull Object somePdpGroupSpecification) throws PfModelException {
        return null;
    }

    @Override
    public Object deletePdpGroups(@NonNull Object somePdpGroupFilter) throws PfModelException {
        return null;
    }

    /**
     * Return a ToscaServicetemplate dummy response.
     *
     * @param fileName the file name containing the dummy response
     * @return the ToscaServiceTemplate with the dummy response
     */
    private ToscaServiceTemplate getDummyResponse(@NonNull final String fileName) {
        Gson gson = new ToscaServiceTemplateMessageBodyHandler().getGson();
        ToscaServiceTemplate serviceTemplate;

        try {
            serviceTemplate = gson.fromJson(ResourceUtils.getResourceAsString(fileName), ToscaServiceTemplate.class);
        } catch (Exception exc) {
            throw new PfModelRuntimeException(Response.Status.INTERNAL_SERVER_ERROR, "error serializing object", exc);
        }

        return serviceTemplate;
    }
}
