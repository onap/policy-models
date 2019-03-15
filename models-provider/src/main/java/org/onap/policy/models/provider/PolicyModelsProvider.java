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

import lombok.NonNull;

import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.tosca.concepts.ToscaServiceTemplate;
import org.onap.policy.models.tosca.serialization.legacy.LegacyGuardPolicy;
import org.onap.policy.models.tosca.serialization.legacy.LegacyOperationalPolicy;

/**
 * This interface describes the operations that are provided to users and components for reading
 * objects from and writing objects to the database.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public interface PolicyModelsProvider {

    /**
     * Get policy types.
     *
     * @param policyTypeKey the policy type key for the policy types to be retrieved. A null key
     *        name returns all policy types. A null key version returns all versions of the policy
     *        type name specified in the key.
     * @return the policy types found
     * @throws PfModelException on errors getting policy types
     */
    public ToscaServiceTemplate getPolicyTypes(@NonNull final PfConceptKey policyTypeKey) throws PfModelException;

    /**
     * Create policy types.
     *
     * @param serviceTemplate the service template containing the definition of the policy types to
     *        be created
     * @return the TOSCA service template containing the created policy types
     * @throws PfModelException on errors creating policy types
     */
    public ToscaServiceTemplate createPolicyTypes(@NonNull final ToscaServiceTemplate serviceTemplate)
            throws PfModelException;

    /**
     * Create policy types.
     *
     * @param serviceTemplate the service template containing the definition of the policy types to
     *        be modified
     * @return the TOSCA service template containing the modified policy types
     * @throws PfModelException on errors updating policy types
     */
    public ToscaServiceTemplate updatePolicyTypes(@NonNull final ToscaServiceTemplate serviceTemplate)
            throws PfModelException;

    /**
     * Delete policy types.
     *
     * @param policyTypeKey the policy type key for the policy types to be deleted, if the version
     *        of the key is null, all versions of the policy type are deleted.
     * @return the TOSCA service template containing the policy types that were deleted
     * @throws PfModelException on errors deleting policy types
     */
    public ToscaServiceTemplate deletePolicyTypes(@NonNull final PfConceptKey policyTypeKey) throws PfModelException;

    /**
     * Get policies.
     *
     * @param policyKey the policy key for the policies to be retrieved. The parent name and version
     *        must be specified. A null local name returns all policies for a parent policy type.
     * @return the policies found
     * @throws PfModelException on errors getting policies
     */
    public ToscaServiceTemplate getPolicies(@NonNull final PfConceptKey policyKey) throws PfModelException;

    /**
     * Create policies.
     *
     * @param serviceTemplate the service template containing the definitions of the new policies to
     *        be created.
     * @return the TOSCA service template containing the policy types that were created
     * @throws PfModelException on errors creating policies
     */
    public ToscaServiceTemplate createPolicies(@NonNull final ToscaServiceTemplate serviceTemplate)
            throws PfModelException;


    /**
     * Update policies.
     *
     * @param serviceTemplate the service template containing the definitions of the policies to be
     *        updated.
     * @return the TOSCA service template containing the policies that were updated
     * @throws PfModelException on errors updating policies
     */
    public ToscaServiceTemplate updatePolicies(@NonNull final ToscaServiceTemplate serviceTemplate)
            throws PfModelException;

    /**
     * Delete policies.
     *
     * @param policyKey the policy key
     * @return the TOSCA service template containing the policy types that were deleted
     * @throws PfModelException on errors deleting policies
     */
    public ToscaServiceTemplate deletePolicies(@NonNull final PfConceptKey policyKey) throws PfModelException;

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
    public LegacyGuardPolicy getGuardPolicy(@NonNull final String policyId) throws PfModelException;

    /**
     * Create legacy guard policy.
     *
     * @param legacyGuardPolicy the definition of the policy to be created.
     * @return the created policy
     * @throws PfModelException on errors creating policies
     */
    public LegacyGuardPolicy createGuardPolicy(@NonNull final LegacyGuardPolicy legacyGuardPolicy)
            throws PfModelException;

    /**
     * Update legacy guard policy.
     *
     * @param legacyGuardPolicy the definition of the policy to be updated
     * @return the updated policy
     * @throws PfModelException on errors updating policies
     */
    public LegacyGuardPolicy updateGuardPolicy(@NonNull final LegacyGuardPolicy legacyGuardPolicy)
            throws PfModelException;

    /**
     * Delete legacy guard policy.
     *
     * @param policyId ID of the policy.
     * @return the deleted policy
     * @throws PfModelException on errors deleting policies
     */
    public LegacyGuardPolicy deleteGuardPolicy(@NonNull final String policyId) throws PfModelException;

    /**
     * Get PDP groups.
     *
     * @param somePdpGroupFilter a filter for the get
     * @return the PDP groups found
     * @throws PfModelException on errors getting PDP groups
     */
    public Object getPdpGroups(@NonNull final Object somePdpGroupFilter) throws PfModelException;

    /**
     * Creates PDP groups.
     *
     * @param somePdpGroupSpecification a specification for the PDP group
     * @throws PfModelException on errors creating PDP groups
     */
    public Object createPdpGroups(@NonNull final Object somePdpGroupSpecification) throws PfModelException;


    /**
     * Updates PDP groups.
     *
     * @param somePdpGroupSpecification a specification for the PDP group
     * @throws PfModelException on errors updating PDP groups
     */
    public Object updatePdpGroups(@NonNull final Object somePdpGroupSpecification) throws PfModelException;

    /**
     * Delete PDP groups.
     *
     * @param somePdpGroupFilter a filter for the get
     * @throws PfModelException on errors deleting PDP groups
     */
    public Object deletePdpGroups(@NonNull final Object somePdpGroupFilter) throws PfModelException;
}
