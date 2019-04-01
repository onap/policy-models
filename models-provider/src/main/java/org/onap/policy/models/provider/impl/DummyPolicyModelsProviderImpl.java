/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
 *  Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;
import lombok.NonNull;

import org.apache.commons.lang3.tuple.Pair;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.resources.ResourceUtils;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.base.PfModelRuntimeException;
import org.onap.policy.models.pdp.concepts.PdpGroup;
import org.onap.policy.models.pdp.concepts.PdpGroups;
import org.onap.policy.models.pdp.concepts.PdpStatistics;
import org.onap.policy.models.pdp.concepts.PdpSubGroup;
import org.onap.policy.models.provider.PolicyModelsProvider;
import org.onap.policy.models.provider.PolicyModelsProviderParameters;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicy;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyType;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;
import org.onap.policy.models.tosca.legacy.concepts.LegacyGuardPolicyInput;
import org.onap.policy.models.tosca.legacy.concepts.LegacyGuardPolicyOutput;
import org.onap.policy.models.tosca.legacy.concepts.LegacyOperationalPolicy;

/**
 * This class provides a dummy implementation of the Policy Models Provider for the ONAP Policy Framework.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 * @author Chenfei Gao (cgao@research.att.com)
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
    public ToscaServiceTemplate getPolicyTypes(final String name, final String version) throws PfModelException {
        return getDummyResponse("dummyimpl/DummyToscaPolicyTypeGetResponse.json");
    }

    @Override
    public List<ToscaPolicyType> getPolicyTypeList(final String name, final String version) throws PfModelException {
        return new ArrayList<>();
    }

    @Override
    public ToscaServiceTemplate getLatestPolicyTypes(final String name) throws PfModelException {
        return null;
    }

    @Override
    public List<ToscaPolicyType> getLatestPolicyTypeList(final String name) throws PfModelException {
        return new ArrayList<>();
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
    public ToscaServiceTemplate deletePolicyType(@NonNull final String name, @NonNull final String version)
            throws PfModelException {
        return getDummyResponse("dummyimpl/DummyToscaPolicyTypeDeleteResponse.json");
    }

    @Override
    public ToscaServiceTemplate getPolicies(final String name, final String version) throws PfModelException {
        return getDummyResponse("dummyimpl/DummyToscaPolicyGetResponse.json");
    }

    @Override
    public List<ToscaPolicy> getPolicyList(final String name, final String version) throws PfModelException {
        return new ArrayList<>();
    }

    @Override
    public List<ToscaPolicy> getPolicyList4PolicyType(@NonNull final String policyTypeName) throws PfModelException {
        return new ArrayList<>();
    }

    @Override
    public ToscaServiceTemplate getLatestPolicies(final String name) throws PfModelException {
        return null;
    }

    @Override
    public List<ToscaPolicy> getLatestPolicyList(final String name) throws PfModelException {
        return new ArrayList<>();
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
    public ToscaServiceTemplate deletePolicy(@NonNull final String name, @NonNull final String version)
            throws PfModelException {
        return getDummyResponse("dummyimpl/DummyToscaPolicyDeleteResponse.json");
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
    public PdpGroups getPdpGroups(final String name, final String version) throws PfModelException {
        return null;
    }

    @Override
    public PdpGroups getLatestPdpGroups(final String name) throws PfModelException {
        return null;
    }

    @Override
    public PdpGroups getFilteredPdpGroups(@NonNull final String pdpType,
            @NonNull final List<Pair<String, String>> supportedPolicyTypes) {
        return null;
    }

    @Override
    public PdpGroups createPdpGroups(@NonNull final PdpGroups pdpGroups) throws PfModelException {
        return null;
    }

    @Override
    public PdpGroups updatePdpGroups(@NonNull final PdpGroups pdpGroups) throws PfModelException {
        return null;
    }

    @Override
    public void updatePdpSubGroup(@NonNull final String pdpGroupName, @NonNull final String pdpGroupVersion,
            @NonNull final PdpSubGroup pdpSubGroup) throws PfModelException {
        // Not implemented
    }

    @Override
    public PdpGroup deletePdpGroup(@NonNull final String name, @NonNull final String version) throws PfModelException {
        return null;
    }

    @Override
    public List<PdpStatistics> getPdpStatistics(final String name, final String version) throws PfModelException {
        return new ArrayList<>();
    }

    @Override
    public void updatePdpStatistics(@NonNull final String pdpGroupName, @NonNull final String pdpGroupVersion,
            @NonNull final String pdpType, @NonNull final String pdpInstanceId,
            @NonNull final PdpStatistics pdppStatistics)  throws PfModelException {
        // Not implemented
    }

    @Override
    public Map<PdpGroup, List<ToscaPolicy>> getDeployedPolicyList(final String name) throws PfModelException {
        return null;
    }

    /**
     * Return a ToscaServicetemplate dummy response.
     *
     * @param fileName the file name containing the dummy response
     * @return the ToscaServiceTemplate with the dummy response
     */
    protected ToscaServiceTemplate getDummyResponse(@NonNull final String fileName) {
        StandardCoder standardCoder = new StandardCoder();
        ToscaServiceTemplate serviceTemplate;

        try {
            serviceTemplate =
                    standardCoder.decode(ResourceUtils.getResourceAsString(fileName), ToscaServiceTemplate.class);
            if (serviceTemplate == null) {
                throw new PfModelException(Response.Status.INTERNAL_SERVER_ERROR, "error reading specified file");
            }
        } catch (Exception exc) {
            throw new PfModelRuntimeException(Response.Status.INTERNAL_SERVER_ERROR, "error serializing object", exc);
        }

        return serviceTemplate;
    }
}
