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

import java.util.Map;

import javax.ws.rs.core.Response;

import lombok.NonNull;

import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.base.PfModelRuntimeException;
import org.onap.policy.models.pdp.concepts.PdpGroups;
import org.onap.policy.models.provider.PolicyModelsProvider;
import org.onap.policy.models.tosca.legacy.concepts.LegacyGuardPolicyInput;
import org.onap.policy.models.tosca.legacy.concepts.LegacyGuardPolicyOutput;
import org.onap.policy.models.tosca.legacy.concepts.LegacyOperationalPolicy;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaServiceTemplate;

/**
 * Dummy implementation of {@link PolicyModelsProvider} with bad constructor.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class DummyBadProviderImpl implements PolicyModelsProvider {
    public DummyBadProviderImpl() {
        throw new PfModelRuntimeException(Response.Status.BAD_REQUEST, "Bad Request");
    }

    @Override
    public void close() throws Exception {}

    @Override
    public void init() throws PfModelException {}

    @Override
    public JpaToscaServiceTemplate getPolicyTypes(@NonNull PfConceptKey policyTypeKey) throws PfModelException {
        return null;
    }

    @Override
    public JpaToscaServiceTemplate createPolicyTypes(@NonNull JpaToscaServiceTemplate serviceTemplate)
            throws PfModelException {
        return null;
    }

    @Override
    public JpaToscaServiceTemplate updatePolicyTypes(@NonNull JpaToscaServiceTemplate serviceTemplate)
            throws PfModelException {
        return null;
    }

    @Override
    public JpaToscaServiceTemplate deletePolicyTypes(@NonNull PfConceptKey policyTypeKey) throws PfModelException {
        return null;
    }

    @Override
    public JpaToscaServiceTemplate getPolicies(@NonNull PfConceptKey policyKey) throws PfModelException {
        return null;
    }

    @Override
    public JpaToscaServiceTemplate createPolicies(@NonNull JpaToscaServiceTemplate serviceTemplate)
            throws PfModelException {
        return null;
    }

    @Override
    public JpaToscaServiceTemplate updatePolicies(@NonNull JpaToscaServiceTemplate serviceTemplate)
            throws PfModelException {
        return null;
    }

    @Override
    public JpaToscaServiceTemplate deletePolicies(@NonNull PfConceptKey policyKey) throws PfModelException {
        return null;
    }

    @Override
    public LegacyOperationalPolicy getOperationalPolicy(@NonNull String policyId) throws PfModelException {
        return null;
    }

    @Override
    public LegacyOperationalPolicy createOperationalPolicy(@NonNull LegacyOperationalPolicy legacyOperationalPolicy)
            throws PfModelException {
        return null;
    }

    @Override
    public LegacyOperationalPolicy updateOperationalPolicy(@NonNull LegacyOperationalPolicy legacyOperationalPolicy)
            throws PfModelException {
        return null;
    }

    @Override
    public LegacyOperationalPolicy deleteOperationalPolicy(@NonNull String policyId) throws PfModelException {
        return null;
    }

    @Override
    public Map<String, LegacyGuardPolicyOutput> getGuardPolicy(@NonNull String policyId) throws PfModelException {
        return null;
    }

    @Override
    public Map<String, LegacyGuardPolicyOutput> createGuardPolicy(@NonNull LegacyGuardPolicyInput legacyGuardPolicy)
            throws PfModelException {
        return null;
    }

    @Override
    public Map<String, LegacyGuardPolicyOutput> updateGuardPolicy(@NonNull LegacyGuardPolicyInput legacyGuardPolicy)
            throws PfModelException {
        return null;
    }

    @Override
    public Map<String, LegacyGuardPolicyOutput> deleteGuardPolicy(@NonNull String policyId) throws PfModelException {
        return null;
    }

    @Override
    public PdpGroups getPdpGroups(@NonNull String pdpGroupFilter) throws PfModelException {
        return null;
    }

    @Override
    public PdpGroups createPdpGroups(@NonNull PdpGroups pdpGroups) throws PfModelException {
        return null;
    }

    @Override
    public PdpGroups updatePdpGroups(@NonNull PdpGroups pdpGroups) throws PfModelException {
        return null;
    }

    @Override
    public PdpGroups deletePdpGroups(@NonNull String pdpGroupFilter) throws PfModelException {
        return null;
    }
}
