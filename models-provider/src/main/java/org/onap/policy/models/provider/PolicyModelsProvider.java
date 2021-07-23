/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2021 Nordix Foundation.
 *  Modifications Copyright (C) 2020 Bell Canada. All rights reserved.
 *  Modifications Copyright (C) 2021 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.models.provider;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import lombok.NonNull;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.pap.concepts.PolicyAudit;
import org.onap.policy.models.pap.persistence.provider.PolicyAuditProvider.AuditFilter;
import org.onap.policy.models.pdp.concepts.Pdp;
import org.onap.policy.models.pdp.concepts.PdpGroup;
import org.onap.policy.models.pdp.concepts.PdpGroupFilter;
import org.onap.policy.models.pdp.concepts.PdpPolicyStatus;
import org.onap.policy.models.pdp.concepts.PdpStatistics;
import org.onap.policy.models.pdp.concepts.PdpSubGroup;
import org.onap.policy.models.pdp.persistence.provider.PdpFilterParameters;
import org.onap.policy.models.tosca.authorative.concepts.ToscaConceptIdentifierOptVersion;
import org.onap.policy.models.tosca.authorative.concepts.ToscaEntityFilter;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicy;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyType;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;
import org.onap.policy.models.tosca.authorative.concepts.ToscaTypedEntityFilter;

/**
 * This interface describes the operations that are provided to users and components for reading objects from and
 * writing objects to the database.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public interface PolicyModelsProvider extends AutoCloseable {
    /**
     * Open the policy model provider initializing whatever internal handling it needs.
     *
     * @throws PfModelException on errors opening the models provider
     */
    public void init() throws PfModelException;

    @Override
    public void close() throws PfModelException;

    /**
     * Get service templates.
     *
     * @param name the name of the topology template to get, set to null to get all service templates
     * @param version the version of the service template to get, set to null to get all service templates
     * @return the topology templates found
     * @throws PfModelException on errors getting service templates
     */
    public List<ToscaServiceTemplate> getServiceTemplateList(final String name, final String version)
            throws PfModelException;

    /**
     * Get filtered service templates.
     *
     * @param filter the filter for the service templates to get
     * @return the service templates found
     * @throws PfModelException on errors getting service templates
     */
    public List<ToscaServiceTemplate> getFilteredServiceTemplateList(
            @NonNull final ToscaEntityFilter<ToscaServiceTemplate> filter) throws PfModelException;

    /**
     * Create service template.
     *
     * @param serviceTemplate the service template to be created
     * @return the created service template
     * @throws PfModelException on errors creating the service template
     */
    public ToscaServiceTemplate createServiceTemplate(@NonNull final ToscaServiceTemplate serviceTemplate)
            throws PfModelException;

    /**
     * Update service template.
     *
     * @param serviceTemplate the service template to be updated
     * @return the updated service template
     * @throws PfModelException on errors updating the service template
     */
    public ToscaServiceTemplate updateServiceTemplate(@NonNull final ToscaServiceTemplate serviceTemplate)
            throws PfModelException;

    /**
     * Delete service template.
     *
     * @param name the name of the service template to delete.
     * @param version the version of the service template to delete.
     * @return the TOSCA service template that was deleted
     * @throws PfModelException on errors deleting policy types
     */
    public ToscaServiceTemplate deleteServiceTemplate(@NonNull final String name, @NonNull final String version)
            throws PfModelException;

    /**
     * Get policy types.
     *
     * @param name the name of the policy type to get, set to null to get all policy types
     * @param version the version of the policy type to get, set to null to get all versions
     * @return the policy types found
     * @throws PfModelException on errors getting policy types
     */
    public ToscaServiceTemplate getPolicyTypes(final String name, final String version) throws PfModelException;

    /**
     * Get policy types.
     *
     * @param name the name of the policy type to get, set to null to get all policy types
     * @param version the version of the policy type to get, set to null to get all versions
     * @return the policy types found
     * @throws PfModelException on errors getting policy types
     */
    public List<ToscaPolicyType> getPolicyTypeList(final String name, final String version) throws PfModelException;

    /**
     * Get filtered policy types.
     *
     * @param filter the filter for the policy types to get
     * @return the policy types found
     * @throws PfModelException on errors getting policy types
     */
    public ToscaServiceTemplate getFilteredPolicyTypes(@NonNull final ToscaEntityFilter<ToscaPolicyType> filter)
            throws PfModelException;

    /**
     * Get filtered policy types.
     *
     * @param filter the filter for the policy types to get
     * @return the policy types found
     * @throws PfModelException on errors getting policy types
     */
    public List<ToscaPolicyType> getFilteredPolicyTypeList(@NonNull final ToscaEntityFilter<ToscaPolicyType> filter)
            throws PfModelException;

    /**
     * Create policy types.
     *
     * @param serviceTemplate the service template containing the definition of the policy types to be created
     * @return the TOSCA service template containing the created policy types
     * @throws PfModelException on errors creating policy types
     */
    public ToscaServiceTemplate createPolicyTypes(@NonNull final ToscaServiceTemplate serviceTemplate)
            throws PfModelException;

    /**
     * Update policy types.
     *
     * @param serviceTemplate the service template containing the definition of the policy types to be modified
     * @return the TOSCA service template containing the modified policy types
     * @throws PfModelException on errors updating policy types
     */
    public ToscaServiceTemplate updatePolicyTypes(@NonNull final ToscaServiceTemplate serviceTemplate)
            throws PfModelException;

    /**
     * Delete policy type.
     *
     * @param name the name of the policy type to delete.
     * @param version the version of the policy type to delete.
     * @return the TOSCA service template containing the policy type that was deleted
     * @throws PfModelException on errors deleting the policy type
     */
    public ToscaServiceTemplate deletePolicyType(@NonNull final String name, @NonNull final String version)
            throws PfModelException;

    /**
     * Get policies.
     *
     * @param name the name of the policy to get, null to get all policies
     * @param version the version of the policy to get, null to get all versions of a policy
     * @return the policies found
     * @throws PfModelException on errors getting policies
     */
    public ToscaServiceTemplate getPolicies(final String name, final String version) throws PfModelException;

    /**
     * Get policies.
     *
     * @param name the name of the policy to get, null to get all policies
     * @param version the version of the policy to get, null to get all versions of a policy
     * @return the policies found
     * @throws PfModelException on errors getting policies
     */
    public List<ToscaPolicy> getPolicyList(final String name, final String version) throws PfModelException;

    /**
     * Get filtered policies.
     *
     * @param filter the filter for the policies to get
     * @return the policies found
     * @throws PfModelException on errors getting policies
     */
    public ToscaServiceTemplate getFilteredPolicies(@NonNull final ToscaTypedEntityFilter<ToscaPolicy> filter)
            throws PfModelException;

    /**
     * Get filtered policies.
     *
     * @param filter the filter for the policies to get
     * @return the policies found
     * @throws PfModelException on errors getting policies
     */
    public List<ToscaPolicy> getFilteredPolicyList(@NonNull final ToscaTypedEntityFilter<ToscaPolicy> filter)
            throws PfModelException;

    /**
     * Create policies.
     *
     * @param serviceTemplate the service template containing the definitions of the new policies to be created.
     * @return the TOSCA service template containing the policy types that were created
     * @throws PfModelException on errors creating policies
     */
    public ToscaServiceTemplate createPolicies(@NonNull final ToscaServiceTemplate serviceTemplate)
            throws PfModelException;

    /**
     * Update policies.
     *
     * @param serviceTemplate the service template containing the definitions of the policies to be updated.
     * @return the TOSCA service template containing the policies that were updated
     * @throws PfModelException on errors updating policies
     */
    public ToscaServiceTemplate updatePolicies(@NonNull final ToscaServiceTemplate serviceTemplate)
            throws PfModelException;

    /**
     * Delete policy.
     *
     * @param name the name of the policy to delete.
     * @param version the version of the policy to delete.
     * @return the TOSCA service template containing the policy that was deleted
     * @throws PfModelException on errors deleting a policy
     */
    public ToscaServiceTemplate deletePolicy(@NonNull final String name, @NonNull final String version)
            throws PfModelException;

    /**
     * Get PDP groups.
     *
     * @param name the name of the policy to get, null to get all PDP groups
     * @return the PDP groups found
     * @throws PfModelException on errors getting PDP groups
     */
    public List<PdpGroup> getPdpGroups(final String name) throws PfModelException;

    /**
     * Get filtered PDP groups.
     *
     * @param filter the filter for the PDP groups to get
     * @return the PDP groups found
     * @throws PfModelException on errors getting policies
     */
    public List<PdpGroup> getFilteredPdpGroups(@NonNull final PdpGroupFilter filter) throws PfModelException;

    /**
     * Creates PDP groups.
     *
     * @param pdpGroups a specification of the PDP groups to create
     * @return the PDP groups created
     * @throws PfModelException on errors creating PDP groups
     */
    public List<PdpGroup> createPdpGroups(@NonNull final List<PdpGroup> pdpGroups) throws PfModelException;

    /**
     * Updates PDP groups.
     *
     * @param pdpGroups a specification of the PDP groups to update
     * @return the PDP groups updated
     * @throws PfModelException on errors updating PDP groups
     */
    public List<PdpGroup> updatePdpGroups(@NonNull final List<PdpGroup> pdpGroups) throws PfModelException;

    /**
     * Update a PDP subgroup.
     *
     * @param pdpGroupName the name of the PDP group of the PDP subgroup
     * @param pdpSubGroup the PDP subgroup to be updated
     * @throws PfModelException on errors updating PDP subgroups
     */
    public void updatePdpSubGroup(@NonNull final String pdpGroupName, @NonNull final PdpSubGroup pdpSubGroup)
            throws PfModelException;

    /**
     * Update a PDP.
     *
     * @param pdpGroupName the name of the PDP group of the PDP subgroup
     * @param pdpSubGroup the PDP subgroup to be updated
     * @param pdp the PDP to be updated
     * @throws PfModelException on errors updating PDP subgroups
     */
    public void updatePdp(@NonNull final String pdpGroupName, @NonNull final String pdpSubGroup, @NonNull final Pdp pdp)
            throws PfModelException;

    /**
     * Delete a PDP group.
     *
     * @param name the name of the policy to get, null to get all PDP groups
     * @return the PDP group deleted
     * @throws PfModelException on errors deleting PDP groups
     */
    public PdpGroup deletePdpGroup(@NonNull final String name) throws PfModelException;


    /**
     * Get filtered PdpStatistics.
     *
     * @param filterParams filter parameters
     * @return the PDP statistics found
     * @throws PfModelException on errors getting policies
     */
    public List<PdpStatistics> getFilteredPdpStatistics(PdpFilterParameters filterParams) throws PfModelException;

    /**
     * Creates PDP statistics.
     *
     * @param pdpStatisticsList a specification of the PDP statistics to create
     * @return the PDP statistics created
     * @throws PfModelException on errors creating PDP statistics
     */
    public List<PdpStatistics> createPdpStatistics(@NonNull List<PdpStatistics> pdpStatisticsList)
            throws PfModelException;

    /**
     * Updates PDP statistics.
     *
     * @param pdpStatisticsList a specification of the PDP statistics to update
     * @return the PDP statistics updated
     * @throws PfModelException on errors updating PDP statistics
     */
    public List<PdpStatistics> updatePdpStatistics(@NonNull List<PdpStatistics> pdpStatisticsList)
            throws PfModelException;

    /**
     * Delete a PDP statistics.
     *
     * @param name the name of the policy to get, null to get all PDP statistics
     * @param timestamp the timestamp of statistics to delete, null to delete all statistics record of given pdp
     * @return the PDP statistics deleted
     * @throws PfModelException on errors deleting PDP statistics
     */
    public List<PdpStatistics> deletePdpStatistics(@NonNull String name, Instant timestamp) throws PfModelException;

    /**
     * Gets all policy deployments.
     *
     * @return the deployments found
     * @throws PfModelException on errors getting PDP groups
     */
    public List<PdpPolicyStatus> getAllPolicyStatus() throws PfModelException;

    /**
     * Gets all deployments for a policy.
     *
     * @return the deployments found
     * @throws PfModelException on errors getting PDP groups
     */
    public List<PdpPolicyStatus> getAllPolicyStatus(@NonNull ToscaConceptIdentifierOptVersion policy)
            throws PfModelException;

    /**
     * Gets the policy deployments for a PDP group.
     *
     * @param groupName the name of the PDP group of interest, null to get results for all PDP groups
     * @return the deployments found
     * @throws PfModelException on errors getting PDP groups
     */
    public List<PdpPolicyStatus> getGroupPolicyStatus(@NonNull final String groupName) throws PfModelException;

    /**
     * Creates, updates, and deletes collections of policy status.
     *
     * @param createObjs the objects to create
     * @param updateObjs the objects to update
     * @param deleteObjs the objects to delete
     */
    public void cudPolicyStatus(Collection<PdpPolicyStatus> createObjs, Collection<PdpPolicyStatus> updateObjs,
            Collection<PdpPolicyStatus> deleteObjs);

    /**
     * Creates records for audit actions on policies.
     *
     * @param auditRecords the objects to create
     */
    public void createAuditRecords(@NonNull List<PolicyAudit> auditRecords);

    /**
     * Collect the audit records.
     * @param auditFilter filter for search
     * @return list of {@link PolicyAudit} or empty if none or not match with filter
     */
    public List<PolicyAudit> getAuditRecords(AuditFilter auditFilter);
}
