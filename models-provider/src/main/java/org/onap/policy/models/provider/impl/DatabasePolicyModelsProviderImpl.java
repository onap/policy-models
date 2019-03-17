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

import lombok.NonNull;

import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.provider.PolicyModelsProvider;
import org.onap.policy.models.tosca.legacy.concepts.LegacyGuardPolicy;
import org.onap.policy.models.tosca.legacy.concepts.LegacyOperationalPolicy;
import org.onap.policy.models.tosca.simple.concepts.ToscaServiceTemplate;

/**
 * This class provides an implementation of the Policy Models Provider for the ONAP Policy Framework
 * that works towards a relational database.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class DatabasePolicyModelsProviderImpl implements PolicyModelsProvider {

    @Override
    public ToscaServiceTemplate getPolicyTypes(@NonNull PfConceptKey policyTypeKey) throws PfModelException {
        return null;
    }

    @Override
    public ToscaServiceTemplate createPolicyTypes(@NonNull ToscaServiceTemplate serviceTemplate)
            throws PfModelException {
        return null;
    }

    @Override
    public ToscaServiceTemplate updatePolicyTypes(@NonNull ToscaServiceTemplate serviceTemplate)
            throws PfModelException {
        return null;
    }

    @Override
    public ToscaServiceTemplate deletePolicyTypes(@NonNull PfConceptKey policyTypeKey) throws PfModelException {
        return null;
    }

    @Override
    public ToscaServiceTemplate getPolicies(@NonNull PfConceptKey policyKey) throws PfModelException {
        return null;
    }

    @Override
    public ToscaServiceTemplate createPolicies(@NonNull ToscaServiceTemplate serviceTemplate) throws PfModelException {
        return null;
    }

    @Override
    public ToscaServiceTemplate updatePolicies(@NonNull ToscaServiceTemplate serviceTemplate) throws PfModelException {
        return null;
    }

    @Override
    public ToscaServiceTemplate deletePolicies(@NonNull PfConceptKey policyKey) throws PfModelException {
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
    public LegacyGuardPolicy getGuardPolicy(@NonNull String policyId) throws PfModelException {
        return null;
    }

    @Override
    public LegacyGuardPolicy createGuardPolicy(@NonNull LegacyGuardPolicy legacyGuardPolicy) throws PfModelException {
        return null;
    }

    @Override
    public LegacyGuardPolicy updateGuardPolicy(@NonNull LegacyGuardPolicy legacyGuardPolicy) throws PfModelException {
        return null;
    }

    @Override
    public LegacyGuardPolicy deleteGuardPolicy(@NonNull String policyId) throws PfModelException {
        return null;
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

}
