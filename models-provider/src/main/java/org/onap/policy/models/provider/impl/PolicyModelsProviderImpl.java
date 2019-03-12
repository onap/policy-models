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
import org.onap.policy.models.base.PfReferenceKey;
import org.onap.policy.models.provider.PolicyModelsProvider;
import org.onap.policy.models.tosca.concepts.ToscaServiceTemplate;

/**
 * This class provides the implementaiton of the defalut Policy Models Provider for the ONAP Policy Framework.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class PolicyModelsProviderImpl implements PolicyModelsProvider {

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
    public ToscaServiceTemplate deletePolicyTypes(@NonNull PfConceptKey policyTypeKey) throws PfModelException {
        return null;
    }

    @Override
    public ToscaServiceTemplate getPolicies(@NonNull PfReferenceKey policyKey) throws PfModelException {
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
    public ToscaServiceTemplate deletePolicies(@NonNull PfReferenceKey policyKey) throws PfModelException {
        return null;
    }

    @Override
    public ToscaServiceTemplate updatePolicyTypes(@NonNull ToscaServiceTemplate serviceTemplate)
            throws PfModelException {
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
    public void deletePdpGroups(@NonNull Object somePdpGroupFilter) throws PfModelException {
    }
}
