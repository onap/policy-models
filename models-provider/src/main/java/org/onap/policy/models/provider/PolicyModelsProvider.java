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

package org.onap.policy.models.provider;

import java.util.List;
import java.util.Map;

import lombok.NonNull;

import org.apache.commons.lang3.tuple.Pair;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.pdp.concepts.PdpGroup;
import org.onap.policy.models.pdp.concepts.PdpGroups;
import org.onap.policy.models.pdp.concepts.PdpStatistics;
import org.onap.policy.models.pdp.concepts.PdpSubGroup;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicy;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyType;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;
import org.onap.policy.models.tosca.legacy.concepts.LegacyGuardPolicyInput;
import org.onap.policy.models.tosca.legacy.concepts.LegacyGuardPolicyOutput;
import org.onap.policy.models.tosca.legacy.concepts.LegacyOperationalPolicy;

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
     * Get latest policy types.
     *
     * @param name the name of the policy type to get, set to null to get all policy types
     * @return the policy types found
     * @throws PfModelException on errors getting policy types
     */
    public ToscaServiceTemplate getLatestPolicyTypes(final String name) throws PfModelException;

    /**
     * Get latest policy types.
     *
     * @param name the name of the policy type to get, set to null to get all policy types
     * @return the policy types found
     * @throws PfModelException on errors getting policy types
     */
    public List<ToscaPolicyType> getLatestPolicyTypeList(final String name) throws PfModelException;

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
     * Create policy types.
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
     * @throws PfModelException on errors deleting policy types
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
     * Get policies for a policy type name.
     *
     * @param policyTypeName the name of the policy type for which to get policies
     * @return the policies found
     * @throws PfModelException on errors getting policies
     */
    public List<ToscaPolicy> getPolicyList4PolicyType(@NonNull final String policyTypeName) throws PfModelException;

    /**
     * Get latest policies.
     *
     * @param name the name of the policy to get, null to get all policies
     * @return the policies found
     * @throws PfModelException on errors getting policies
     */
    public ToscaServiceTemplate getLatestPolicies(final String name) throws PfModelException;

    /**
     * Get latest policies.
     *
     * @param name the name of the policy to get, null to get all policies
     * @return the policies found
     * @throws PfModelException on errors getting policies
     */
    public List<ToscaPolicy> getLatestPolicyList(final String name) throws PfModelException;

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
     * Get legacy operational policy.
     *
     * @param policyId ID of the policy.
     * @return the policies found
     * @throws PfModelException on errors getting policies
     */
    public LegacyOperationalPolicy getOperationalPolicy(@NonNull final String policyId) throws PfModelException;

    /**
     * Create legacy operational policy.
     *
     * @param legacyOperationalPolicy the definition of the policy to be created.
     * @return the created policy
     * @throws PfModelException on errors creating policies
     */
    public LegacyOperationalPolicy createOperationalPolicy(
            @NonNull final LegacyOperationalPolicy legacyOperationalPolicy) throws PfModelException;

    /**
     * Update legacy operational policy.
     *
     * @param legacyOperationalPolicy the definition of the policy to be updated
     * @return the updated policy
     * @throws PfModelException on errors updating policies
     */
    public LegacyOperationalPolicy updateOperationalPolicy(
            @NonNull final LegacyOperationalPolicy legacyOperationalPolicy) throws PfModelException;

    /**
     * Delete legacy operational policy.
     *
     * @param policyId ID of the policy.
     * @return the deleted policy
     * @throws PfModelException on errors deleting policies
     */
    public LegacyOperationalPolicy deleteOperationalPolicy(@NonNull final String policyId) throws PfModelException;

    /**
     * Get legacy guard policy.
     *
     * @param policyId ID of the policy.
     * @return the policies found
     * @throws PfModelException on errors getting policies
     */
    public Map<String, LegacyGuardPolicyOutput> getGuardPolicy(@NonNull final String policyId) throws PfModelException;

    /**
     * Create legacy guard policy.
     *
     * @param legacyGuardPolicy the definition of the policy to be created.
     * @return the created policy
     * @throws PfModelException on errors creating policies
     */
    public Map<String, LegacyGuardPolicyOutput> createGuardPolicy(
            @NonNull final LegacyGuardPolicyInput legacyGuardPolicy) throws PfModelException;

    /**
     * Update legacy guard policy.
     *
     * @param legacyGuardPolicy the definition of the policy to be updated
     * @return the updated policy
     * @throws PfModelException on errors updating policies
     */
    public Map<String, LegacyGuardPolicyOutput> updateGuardPolicy(
            @NonNull final LegacyGuardPolicyInput legacyGuardPolicy) throws PfModelException;

    /**
     * Delete legacy guard policy.
     *
     * @param policyId ID of the policy.
     * @return the deleted policy
     * @throws PfModelException on errors deleting policies
     */
    public Map<String, LegacyGuardPolicyOutput> deleteGuardPolicy(@NonNull final String policyId)
            throws PfModelException;

    /**
     * Get PDP groups.
     *
     * @param name the name of the policy to get, null to get all PDP groups
     * @param version the version of the policy to get, null to get all versions of a PDP group
     * @return the PDP groups found
     * @throws PfModelException on errors getting PDP groups
     */
    public PdpGroups getPdpGroups(final String name, final String version) throws PfModelException;

    /**
     * Get latest PDP Groups.
     *
     * @param name the name of the PDP group to get, null to get all PDP groups
     * @return the PDP groups found
     * @throws PfModelException on errors getting policies
     */
    public PdpGroups getLatestPdpGroups(final String name) throws PfModelException;

    /**
     * Get a filtered list of PDP groups.
     *
     * @param pdpType The PDP type filter for the returned PDP groups
     * @param supportedPolicyTypes a list of policy type name/version pairs that the PDP groups must support.
     * @return the PDP groups found
     */
    public PdpGroups getFilteredPdpGroups(@NonNull final String pdpType,
            @NonNull final List<Pair<String, String>> supportedPolicyTypes);

    /**
     * Creates PDP groups.
     *
     * @param pdpGroups a specification of the PDP groups to create
     * @return the PDP groups created
     * @throws PfModelException on errors creating PDP groups
     */
    public PdpGroups createPdpGroups(@NonNull final PdpGroups pdpGroups) throws PfModelException;

    /**
     * Updates PDP groups.
     *
     * @param pdpGroups a specification of the PDP groups to update
     * @return the PDP groups updated
     * @throws PfModelException on errors updating PDP groups
     */
    public PdpGroups updatePdpGroups(@NonNull final PdpGroups pdpGroups) throws PfModelException;


    /**
     * Update a PDP subgroup.
     *
     * @param pdpGroupName the name of the PDP group of the PDP subgroup
     * @param pdpGroupVersion the version of the PDP group of the PDP subgroup
     * @param pdpSubGroup the PDP subgroup to be updated
     * @throws PfModelException on errors updating PDP subgroups
     */
    public void updatePdpSubGroup(@NonNull final String pdpGroupName, @NonNull final String pdpGroupVersion,
            @NonNull final PdpSubGroup pdpSubGroup) throws PfModelException;

    /**
     * Delete a PDP group.
     *
     * @param name the name of the policy to get, null to get all PDP groups
     * @param version the version of the policy to get, null to get all versions of a PDP group
     * @return the PDP group deleted
     * @throws PfModelException on errors deleting PDP groups
     */
    public PdpGroup deletePdpGroup(@NonNull final String name, @NonNull final String version) throws PfModelException;

    /**
     * Get PDP statistics.
     *
     * @param name the name of the PDP group to get statistics for, null to get all PDP groups
     * @param version the version of the PDP group to get statistics for, null to get all versions of a PDP group
     * @return the statistics found
     * @throws PfModelException on errors getting statistics
     */
    public List<PdpStatistics> getPdpStatistics(final String name, final String version) throws PfModelException;

    /**
     * Update PDP statistics for a PDP.
     *
     * @param pdpGroupName the name of the PDP group containing the PDP that the statistics are for
     * @param pdpGroupVersion the version of the PDP group containing the PDP that the statistics are for
     * @param pdpType the PDP type of the subgroup containing the PDP that the statistics are for
     * @param pdpInstanceId the instance ID of the PDP to update statistics for
     * @throws PfModelException on errors updating statistics
     */
    public void updatePdpStatistics(@NonNull final String pdpGroupName, @NonNull final String pdpGroupVersion,
            @NonNull final String pdpType, @NonNull final String pdpInstanceId,
            @NonNull final PdpStatistics pdppStatistics) throws PfModelException;

    /**
     * Get deployed policies.
     *
     * @param name the name of the policy to get, null to get all policies
     * @return the policies deployed as a map of policy lists keyed by PDP group
     * @throws PfModelException on errors getting policies
     */
    public Map<PdpGroup, List<ToscaPolicy>> getDeployedPolicyList(final String name) throws PfModelException;
}
