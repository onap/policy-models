/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021-2023 Nordix Foundation.
 *  Modifications Copyright (C) 2022 Bell Canada. All rights reserved.
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

import jakarta.ws.rs.core.Response;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import lombok.NonNull;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.base.PfModelRuntimeException;
import org.onap.policy.models.dao.PfDao;
import org.onap.policy.models.pdp.concepts.Pdp;
import org.onap.policy.models.pdp.concepts.PdpGroup;
import org.onap.policy.models.pdp.concepts.PdpGroupFilter;
import org.onap.policy.models.pdp.concepts.PdpPolicyStatus;
import org.onap.policy.models.pdp.concepts.PdpSubGroup;
import org.onap.policy.models.pdp.persistence.provider.PdpProvider;
import org.onap.policy.models.provider.PolicyModelsProvider;
import org.onap.policy.models.tosca.authorative.concepts.ToscaConceptIdentifier;
import org.onap.policy.models.tosca.authorative.concepts.ToscaConceptIdentifierOptVersion;
import org.onap.policy.models.tosca.authorative.concepts.ToscaEntityFilter;
import org.onap.policy.models.tosca.authorative.concepts.ToscaEntityKey;
import org.onap.policy.models.tosca.authorative.concepts.ToscaNodeTemplate;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicy;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyType;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;
import org.onap.policy.models.tosca.authorative.concepts.ToscaTypedEntityFilter;
import org.onap.policy.models.tosca.authorative.provider.AuthorativeToscaProvider;

public abstract class AbstractPolicyModelsProvider implements PolicyModelsProvider {

    protected abstract PfDao getPfDao();

    @Override
    public List<ToscaServiceTemplate> getServiceTemplateList(final String name, final String version)
            throws PfModelException {
        assertInitialized();
        return new AuthorativeToscaProvider().getServiceTemplateList(getPfDao(), name, version);
    }


    @Override
    public List<ToscaServiceTemplate> getFilteredServiceTemplateList(
            @NonNull ToscaEntityFilter<ToscaServiceTemplate> filter) throws PfModelException {
        assertInitialized();
        return new AuthorativeToscaProvider().getFilteredServiceTemplateList(getPfDao(), filter);
    }

    @Override
    public ToscaServiceTemplate createServiceTemplate(@NonNull final ToscaServiceTemplate serviceTemplate)
            throws PfModelException {
        assertInitialized();
        return new AuthorativeToscaProvider().createServiceTemplate(getPfDao(), serviceTemplate);
    }

    @Override
    public ToscaServiceTemplate updateServiceTemplate(@NonNull final ToscaServiceTemplate serviceTemplate)
            throws PfModelException {
        assertInitialized();
        return new AuthorativeToscaProvider().updateServiceTemplate(getPfDao(), serviceTemplate);
    }

    @Override
    public ToscaServiceTemplate deleteServiceTemplate(@NonNull final String name, @NonNull final String version)
            throws PfModelException {
        assertInitialized();

        return new AuthorativeToscaProvider().deleteServiceTemplate(getPfDao(), name, version);
    }

    @Override
    public ToscaServiceTemplate getPolicyTypes(final String name, final String version) throws PfModelException {
        assertInitialized();
        return new AuthorativeToscaProvider().getPolicyTypes(getPfDao(), name, version);
    }

    @Override
    public List<ToscaPolicyType> getPolicyTypeList(final String name, final String version) throws PfModelException {
        assertInitialized();
        return new AuthorativeToscaProvider().getPolicyTypeList(getPfDao(), name, version);
    }

    @Override
    public ToscaServiceTemplate getFilteredPolicyTypes(@NonNull ToscaEntityFilter<ToscaPolicyType> filter)
            throws PfModelException {
        assertInitialized();
        return new AuthorativeToscaProvider().getFilteredPolicyTypes(getPfDao(), filter);
    }

    @Override
    public List<ToscaPolicyType> getFilteredPolicyTypeList(@NonNull ToscaEntityFilter<ToscaPolicyType> filter)
            throws PfModelException {
        assertInitialized();
        return new AuthorativeToscaProvider().getFilteredPolicyTypeList(getPfDao(), filter);
    }

    @Override
    public ToscaServiceTemplate createPolicyTypes(@NonNull final ToscaServiceTemplate serviceTemplate)
            throws PfModelException {
        assertInitialized();
        return new AuthorativeToscaProvider().createPolicyTypes(getPfDao(), serviceTemplate);
    }

    @Override
    public ToscaServiceTemplate updatePolicyTypes(@NonNull final ToscaServiceTemplate serviceTemplate)
            throws PfModelException {
        assertInitialized();
        return new AuthorativeToscaProvider().updatePolicyTypes(getPfDao(), serviceTemplate);
    }

    @Override
    public ToscaServiceTemplate deletePolicyType(@NonNull final String name, @NonNull final String version)
            throws PfModelException {
        assertInitialized();

        var policyTypeIdentifier = new ToscaConceptIdentifier(name, version);
        assertPolicyTypeNotSupportedInPdpGroup(policyTypeIdentifier);

        return new AuthorativeToscaProvider().deletePolicyType(getPfDao(), name, version);
    }

    @Override
    public ToscaServiceTemplate getPolicies(final String name, final String version) throws PfModelException {
        assertInitialized();
        return new AuthorativeToscaProvider().getPolicies(getPfDao(), name, version);
    }

    @Override
    public List<ToscaPolicy> getPolicyList(final String name, final String version) throws PfModelException {
        assertInitialized();
        return new AuthorativeToscaProvider().getPolicyList(getPfDao(), name, version);
    }

    @Override
    public ToscaServiceTemplate getFilteredPolicies(@NonNull ToscaTypedEntityFilter<ToscaPolicy> filter)
            throws PfModelException {
        assertInitialized();
        return new AuthorativeToscaProvider().getFilteredPolicies(getPfDao(), filter);
    }

    @Override
    public List<ToscaPolicy> getFilteredPolicyList(@NonNull ToscaTypedEntityFilter<ToscaPolicy> filter)
            throws PfModelException {
        assertInitialized();
        return new AuthorativeToscaProvider().getFilteredPolicyList(getPfDao(), filter);
    }

    @Override
    public ToscaServiceTemplate createPolicies(@NonNull final ToscaServiceTemplate serviceTemplate)
            throws PfModelException {
        assertInitialized();
        return new AuthorativeToscaProvider().createPolicies(getPfDao(), serviceTemplate);
    }

    @Override
    public ToscaServiceTemplate updatePolicies(@NonNull final ToscaServiceTemplate serviceTemplate)
            throws PfModelException {
        assertInitialized();
        return new AuthorativeToscaProvider().updatePolicies(getPfDao(), serviceTemplate);
    }

    @Override
    public ToscaServiceTemplate deletePolicy(@NonNull final String name, @NonNull final String version)
            throws PfModelException {
        assertInitialized();

        var policyIdentifier = new ToscaConceptIdentifier(name, version);
        assertPolicyNotDeployedInPdpGroup(policyIdentifier);

        return new AuthorativeToscaProvider().deletePolicy(getPfDao(), name, version);
    }

    @Override
    public ToscaServiceTemplate createToscaNodeTemplates(@NonNull final ToscaServiceTemplate serviceTemplate)
        throws PfModelException {
        assertInitialized();
        return new AuthorativeToscaProvider().createToscaNodeTemplates(getPfDao(), serviceTemplate);
    }

    @Override
    public ToscaServiceTemplate updateToscaNodeTemplates(@NonNull final ToscaServiceTemplate serviceTemplate)
        throws PfModelRuntimeException, PfModelException {
        assertInitialized();
        return new AuthorativeToscaProvider().updateToscaNodeTemplates(getPfDao(), serviceTemplate);
    }

    @Override
    public ToscaServiceTemplate deleteToscaNodeTemplate(@NonNull final String name, @NonNull final String version)
        throws PfModelException {
        assertInitialized();
        return new AuthorativeToscaProvider().deleteToscaNodeTemplate(getPfDao(), name, version);
    }

    @Override
    public List<Map<ToscaEntityKey, Map<String, Object>>> getNodeTemplateMetadataSets(final String name,
                                                                                      final String version)
        throws PfModelException {
        assertInitialized();
        return new AuthorativeToscaProvider().getNodeTemplateMetadataSet(getPfDao(), name, version);
    }

    @Override
    public List<ToscaNodeTemplate> getToscaNodeTemplate(final String name, final String version)
        throws PfModelException {
        assertInitialized();
        return new AuthorativeToscaProvider().getToscaNodeTemplate(getPfDao(), name, version);
    }



    @Override
    public List<PdpGroup> getPdpGroups(final String name) throws PfModelException {
        assertInitialized();
        return new PdpProvider().getPdpGroups(getPfDao(), name);
    }

    @Override
    public List<PdpGroup> getFilteredPdpGroups(@NonNull PdpGroupFilter filter) throws PfModelException {
        assertInitialized();
        return new PdpProvider().getFilteredPdpGroups(getPfDao(), filter);
    }

    @Override
    public List<PdpGroup> createPdpGroups(@NonNull final List<PdpGroup> pdpGroups) throws PfModelException {
        assertInitialized();
        return new PdpProvider().createPdpGroups(getPfDao(), pdpGroups);
    }

    @Override
    public List<PdpGroup> updatePdpGroups(@NonNull final List<PdpGroup> pdpGroups) throws PfModelException {
        assertInitialized();
        return new PdpProvider().updatePdpGroups(getPfDao(), pdpGroups);
    }

    @Override
    public void updatePdpSubGroup(@NonNull final String pdpGroupName, @NonNull final PdpSubGroup pdpSubGroup)
            throws PfModelException {
        assertInitialized();
        new PdpProvider().updatePdpSubGroup(getPfDao(), pdpGroupName, pdpSubGroup);
    }

    @Override
    public void updatePdp(@NonNull String pdpGroupName, @NonNull String pdpSubGroup, @NonNull Pdp pdp)
            throws PfModelException {
        new PdpProvider().updatePdp(getPfDao(), pdpGroupName, pdpSubGroup, pdp);
    }

    @Override
    public PdpGroup deletePdpGroup(@NonNull final String name) throws PfModelException {
        assertInitialized();
        return new PdpProvider().deletePdpGroup(getPfDao(), name);
    }

    @Override
    public List<PdpPolicyStatus> getAllPolicyStatus() throws PfModelException {
        assertInitialized();
        return new PdpProvider().getAllPolicyStatus(getPfDao());
    }

    @Override
    public List<PdpPolicyStatus> getAllPolicyStatus(@NonNull ToscaConceptIdentifierOptVersion policy)
            throws PfModelException {
        assertInitialized();
        return new PdpProvider().getAllPolicyStatus(getPfDao(), policy);
    }

    @Override
    public List<PdpPolicyStatus> getGroupPolicyStatus(@NonNull String groupName) throws PfModelException {
        assertInitialized();
        return new PdpProvider().getGroupPolicyStatus(getPfDao(), groupName);
    }

    @Override
    public void cudPolicyStatus(Collection<PdpPolicyStatus> createObjs, Collection<PdpPolicyStatus> updateObjs,
            Collection<PdpPolicyStatus> deleteObjs) {
        assertInitialized();
        new PdpProvider().cudPolicyStatus(getPfDao(), createObjs, updateObjs, deleteObjs);
    }

    /**
     * Check if the model provider is initialized.
     */
    private void assertInitialized() {
        if (getPfDao() == null) {
            var errorMessage = "policy models provider is not initilaized";
            throw new PfModelRuntimeException(Response.Status.BAD_REQUEST, errorMessage);
        }
    }

    /**
     * Assert that the policy type is not supported in any PDP group.
     *
     * @param policyTypeIdentifier the policy type identifier
     * @throws PfModelException if the policy type is supported in a PDP group
     */
    private void assertPolicyTypeNotSupportedInPdpGroup(ToscaConceptIdentifier policyTypeIdentifier)
            throws PfModelException {
        for (PdpGroup pdpGroup : getPdpGroups(null)) {
            for (PdpSubGroup pdpSubGroup : pdpGroup.getPdpSubgroups()) {
                if (pdpSubGroup.getSupportedPolicyTypes().contains(policyTypeIdentifier)) {
                    throw new PfModelRuntimeException(Response.Status.NOT_ACCEPTABLE,
                            "policy type is in use, it is referenced in PDP group " + pdpGroup.getName() + " subgroup "
                                    + pdpSubGroup.getPdpType());
                }
            }
        }
    }

    /**
     * Assert that the policy is not deployed in a PDP group.
     *
     * @param policyIdentifier the identifier of the policy
     * @throws PfModelException thrown if the policy is deployed in a PDP group
     */
    private void assertPolicyNotDeployedInPdpGroup(final ToscaConceptIdentifier policyIdentifier)
            throws PfModelException {
        for (PdpGroup pdpGroup : getPdpGroups(null)) {
            for (PdpSubGroup pdpSubGroup : pdpGroup.getPdpSubgroups()) {
                if (pdpSubGroup.getPolicies().contains(policyIdentifier)) {
                    throw new PfModelRuntimeException(Response.Status.NOT_ACCEPTABLE,
                            "policy is in use, it is deployed in PDP group " + pdpGroup.getName() + " subgroup "
                                    + pdpSubGroup.getPdpType());
                }
            }
        }
    }

}
