/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021-2022 Nordix Foundation.
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

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response;
import lombok.NonNull;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.base.PfModelRuntimeException;
import org.onap.policy.models.dao.PfDao;
import org.onap.policy.models.pap.concepts.PolicyAudit;
import org.onap.policy.models.pap.persistence.provider.PolicyAuditProvider;
import org.onap.policy.models.pap.persistence.provider.PolicyAuditProvider.AuditFilter;
import org.onap.policy.models.pdp.concepts.Pdp;
import org.onap.policy.models.pdp.concepts.PdpGroup;
import org.onap.policy.models.pdp.concepts.PdpGroupFilter;
import org.onap.policy.models.pdp.concepts.PdpPolicyStatus;
import org.onap.policy.models.pdp.concepts.PdpStatistics;
import org.onap.policy.models.pdp.concepts.PdpSubGroup;
import org.onap.policy.models.pdp.persistence.provider.PdpFilterParameters;
import org.onap.policy.models.pdp.persistence.provider.PdpProvider;
import org.onap.policy.models.pdp.persistence.provider.PdpStatisticsProvider;
import org.onap.policy.models.provider.PolicyModelsProvider;
import org.onap.policy.models.tosca.authorative.concepts.PolicyTypeImpl;
import org.onap.policy.models.tosca.authorative.concepts.ToscaConceptIdentifier;
import org.onap.policy.models.tosca.authorative.concepts.ToscaConceptIdentifierOptVersion;
import org.onap.policy.models.tosca.authorative.concepts.ToscaEntityFilter;
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
    public List<PolicyTypeImpl> createPolicyTypeImpl(@NonNull final List<PolicyTypeImpl> policyImpl)
        throws PfModelException {
        assertInitialized();
        return new AuthorativeToscaProvider().createPolicyTypeImpl(getPfDao(), policyImpl);
    }

    @Override
    public PolicyTypeImpl updatePolicyTypeImpl(@NonNull final PolicyTypeImpl policyImpl)
        throws PfModelException {
        assertInitialized();
        return new AuthorativeToscaProvider().updatePolicyTypeImpl(getPfDao(), policyImpl);
    }

    @Override
    public PolicyTypeImpl deletePolicyTypeImpl(@NonNull final String name, @NonNull final String version)
        throws PfModelException {
        assertInitialized();
        return new AuthorativeToscaProvider().deletePolicyTypeImpl(getPfDao(), name, version);
    }

    @Override
    public List<PolicyTypeImpl> getFilteredPolicyTypeImpl(Map<String, Object> filterMap) throws PfModelException {
        assertInitialized();
        return new AuthorativeToscaProvider().getFilteredPolicyTypeImpl(getPfDao(), filterMap);
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
    public List<PdpStatistics> getFilteredPdpStatistics(PdpFilterParameters filterParams) throws PfModelException {
        assertInitialized();
        return new PdpStatisticsProvider().getFilteredPdpStatistics(getPfDao(), filterParams);
    }

    @Override
    public List<PdpStatistics> createPdpStatistics(@NonNull final List<PdpStatistics> pdpStatisticsList)
            throws PfModelException {
        assertInitialized();
        return new PdpStatisticsProvider().createPdpStatistics(getPfDao(), pdpStatisticsList);
    }

    @Override
    public List<PdpStatistics> updatePdpStatistics(@NonNull final List<PdpStatistics> pdpStatisticsList)
            throws PfModelException {
        assertInitialized();
        return new PdpStatisticsProvider().updatePdpStatistics(getPfDao(), pdpStatisticsList);
    }

    @Override
    public List<PdpStatistics> deletePdpStatistics(@NonNull final String name, final Instant timestamp)
            throws PfModelException {
        assertInitialized();
        return new PdpStatisticsProvider().deletePdpStatistics(getPfDao(), name, timestamp);
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

    @Override
    public void createAuditRecords(List<PolicyAudit> auditRecords) {
        assertInitialized();
        new PolicyAuditProvider().createAuditRecords(getPfDao(), auditRecords);
    }

    @Override
    public List<PolicyAudit> getAuditRecords(AuditFilter auditFilter) {
        assertInitialized();
        return new PolicyAuditProvider().getAuditRecords(getPfDao(), auditFilter);
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
