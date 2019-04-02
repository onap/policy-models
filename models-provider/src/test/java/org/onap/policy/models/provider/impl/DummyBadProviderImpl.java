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

import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import lombok.NonNull;

import org.apache.commons.lang3.tuple.Pair;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.base.PfModelRuntimeException;
import org.onap.policy.models.pdp.concepts.PdpGroup;
import org.onap.policy.models.pdp.concepts.PdpStatistics;
import org.onap.policy.models.pdp.concepts.PdpSubGroup;
import org.onap.policy.models.provider.PolicyModelsProvider;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicy;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyType;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;
import org.onap.policy.models.tosca.legacy.concepts.LegacyGuardPolicyInput;
import org.onap.policy.models.tosca.legacy.concepts.LegacyGuardPolicyOutput;
import org.onap.policy.models.tosca.legacy.concepts.LegacyOperationalPolicy;

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
    public void close() throws PfModelException {}

    @Override
    public void init() throws PfModelException {}

    @Override
    public ToscaServiceTemplate getPolicyTypes(final String name, final String version) throws PfModelException {
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
    public ToscaServiceTemplate deletePolicyType(final String name, final String version) throws PfModelException {
        return null;
    }

    @Override
    public ToscaServiceTemplate getPolicies(final String name, final String version) throws PfModelException {
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
    public ToscaServiceTemplate deletePolicy(final String name, final String version) throws PfModelException {
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
    public List<PdpGroup> getPdpGroups(String name, String version) throws PfModelException {
        return null;
    }

    @Override
    public List<PdpGroup> createPdpGroups(@NonNull List<PdpGroup> pdpGroups) throws PfModelException {
        return null;
    }

    @Override
    public List<PdpGroup> updatePdpGroups(@NonNull List<PdpGroup> pdpGroups) throws PfModelException {
        return null;
    }

    @Override
    public PdpGroup deletePdpGroup(@NonNull String name, @NonNull String verison) throws PfModelException {
        return null;
    }

    @Override
    public List<ToscaPolicyType> getPolicyTypeList(String name, String version) throws PfModelException {
        return null;
    }

    @Override
    public ToscaServiceTemplate getLatestPolicyTypes(String name) throws PfModelException {
        return null;
    }

    @Override
    public List<ToscaPolicyType> getLatestPolicyTypeList(String name) throws PfModelException {
        return null;
    }

    @Override
    public List<ToscaPolicy> getPolicyList(String name, String version) throws PfModelException {
        return null;
    }

    @Override
    public List<ToscaPolicy> getPolicyList4PolicyType(@NonNull String policyTypeName, final String policyTypeVersion)
            throws PfModelException {
        return null;
    }

    @Override
    public ToscaServiceTemplate getLatestPolicies(String name) throws PfModelException {
        return null;
    }

    @Override
    public List<ToscaPolicy> getLatestPolicyList(String name) throws PfModelException {
        return null;
    }

    @Override
    public List<PdpGroup> getLatestPdpGroups(String name) throws PfModelException {
        return null;
    }

    @Override
    public List<PdpGroup> getFilteredPdpGroups(@NonNull String pdpType,
            @NonNull List<Pair<String, String>> supportedPolicyTypes) {
        return null;
    }

    @Override
    public void updatePdpSubGroup(@NonNull String pdpGroupName, @NonNull String pdpGroupVersion,
            @NonNull PdpSubGroup pdpSubGroup) throws PfModelException {}

    @Override
    public List<PdpStatistics> getPdpStatistics(String name, String version) throws PfModelException {
        return null;
    }

    @Override
    public void updatePdpStatistics(@NonNull String pdpGroupName, @NonNull String pdpGroupVersion,
            @NonNull String pdpType, @NonNull String pdpInstanceId, @NonNull PdpStatistics pdppStatistics) {}

    @Override
    public Map<Pair<String, String>, List<ToscaPolicy>> getDeployedPolicyList(String name) throws PfModelException {
        return null;
    }
}
