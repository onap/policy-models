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

package org.onap.policy.models.tosca.legacy.provider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import lombok.NonNull;

import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.base.PfModelRuntimeException;
import org.onap.policy.models.dao.PfDao;
import org.onap.policy.models.tosca.legacy.concepts.LegacyGuardPolicyInput;
import org.onap.policy.models.tosca.legacy.concepts.LegacyGuardPolicyOutput;
import org.onap.policy.models.tosca.legacy.concepts.LegacyOperationalPolicy;
import org.onap.policy.models.tosca.legacy.mapping.LegacyGuardPolicyMapper;
import org.onap.policy.models.tosca.legacy.mapping.LegacyOperationalPolicyMapper;
import org.onap.policy.models.tosca.simple.concepts.ToscaPolicies;
import org.onap.policy.models.tosca.simple.concepts.ToscaPolicy;
import org.onap.policy.models.tosca.simple.concepts.ToscaServiceTemplate;
import org.onap.policy.models.tosca.simple.concepts.ToscaTopologyTemplate;
import org.onap.policy.models.tosca.simple.provider.SimpleToscaProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides the provision of information on TOSCA concepts in the database to callers in legacy formats.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class LegacyProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(LegacyProvider.class);

    private static final String FIRST_POLICY_VERSION = "1";

    // Recurring constants
    private static final String NO_POLICY_FOUND_FOR_POLICY_ID = "no policy found for policy ID: ";

    /**
     * Get legacy operational policy.
     *
     * @param dao the DAO to use to access the database
     * @param policyId ID of the policy.
     * @return the policies found
     * @throws PfModelException on errors getting policies
     */
    public LegacyOperationalPolicy getOperationalPolicy(@NonNull final PfDao dao, @NonNull final String policyId)
            throws PfModelException {

        ToscaPolicy newestPolicy = getLatestPolicy(dao, policyId);

        if (newestPolicy == null) {
            String errorMessage = NO_POLICY_FOUND_FOR_POLICY_ID + policyId;
            LOGGER.warn(errorMessage);
            throw new PfModelRuntimeException(Response.Status.BAD_REQUEST, errorMessage);
        }

        // Create the structure of the TOSCA service template to contain the policy type
        ToscaServiceTemplate serviceTemplate = new ToscaServiceTemplate();
        serviceTemplate.setTopologyTemplate(new ToscaTopologyTemplate());
        serviceTemplate.getTopologyTemplate().setPolicies(new ToscaPolicies());
        serviceTemplate.getTopologyTemplate().getPolicies().getConceptMap().put(newestPolicy.getKey(), newestPolicy);

        return new LegacyOperationalPolicyMapper().fromToscaServiceTemplate(serviceTemplate);
    }

    /**
     * Create legacy operational policy.
     *
     * @param dao the DAO to use to access the database
     * @param legacyOperationalPolicy the definition of the policy to be created.
     * @return the created policy
     * @throws PfModelException on errors creating policies
     */
    public LegacyOperationalPolicy createOperationalPolicy(@NonNull final PfDao dao,
            @NonNull final LegacyOperationalPolicy legacyOperationalPolicy) throws PfModelException {

        // We need to find the latest policy and update the major version, if there is no policy with this ID, then
        // we set it to the first version
        ToscaPolicy newestPolicy = getLatestPolicy(dao, legacyOperationalPolicy.getPolicyId());

        if (newestPolicy == null) {
            legacyOperationalPolicy.setPolicyVersion(FIRST_POLICY_VERSION);
        } else {
            legacyOperationalPolicy.setPolicyVersion(Integer.toString(newestPolicy.getKey().getMajorVersion() + 1));
        }

        ToscaServiceTemplate incomingServiceTemplate =
                new LegacyOperationalPolicyMapper().toToscaServiceTemplate(legacyOperationalPolicy);
        ToscaServiceTemplate outgoingingServiceTemplate =
                new SimpleToscaProvider().createPolicies(dao, incomingServiceTemplate);

        return new LegacyOperationalPolicyMapper().fromToscaServiceTemplate(outgoingingServiceTemplate);
    }

    /**
     * Update legacy operational policy.
     *
     * @param dao the DAO to use to access the database
     * @param legacyOperationalPolicy the definition of the policy to be updated
     * @return the updated policy
     * @throws PfModelException on errors updating policies
     */
    public LegacyOperationalPolicy updateOperationalPolicy(@NonNull final PfDao dao,
            @NonNull final LegacyOperationalPolicy legacyOperationalPolicy) throws PfModelException {

        // We need to find the latest policy and use the major version, if there is no policy with this ID, then
        // we have an error
        ToscaPolicy newestPolicy = getLatestPolicy(dao, legacyOperationalPolicy.getPolicyId());

        if (newestPolicy == null) {
            String errorMessage = NO_POLICY_FOUND_FOR_POLICY_ID + legacyOperationalPolicy.getPolicyId();
            LOGGER.warn(errorMessage);
            throw new PfModelRuntimeException(Response.Status.BAD_REQUEST, errorMessage);
        } else {
            legacyOperationalPolicy.setPolicyVersion(Integer.toString(newestPolicy.getKey().getMajorVersion()));
        }

        ToscaServiceTemplate incomingServiceTemplate =
                new LegacyOperationalPolicyMapper().toToscaServiceTemplate(legacyOperationalPolicy);
        ToscaServiceTemplate outgoingingServiceTemplate =
                new SimpleToscaProvider().createPolicies(dao, incomingServiceTemplate);

        return new LegacyOperationalPolicyMapper().fromToscaServiceTemplate(outgoingingServiceTemplate);
    }

    /**
     * Delete legacy operational policy.
     *
     * @param dao the DAO to use to access the database
     * @param policyId ID of the policy.
     * @return the deleted policy
     * @throws PfModelException on errors deleting policies
     */
    public LegacyOperationalPolicy deleteOperationalPolicy(@NonNull final PfDao dao, @NonNull final String policyId)
            throws PfModelException {

        // Get all the policies in the database and check the policy ID against the policies returned
        List<ToscaPolicy> policyList = dao.getAll(ToscaPolicy.class);

        // Find the latest policy that matches the ID
        List<ToscaPolicy> policyDeleteList = new ArrayList<>();

        for (ToscaPolicy policy : policyList) {
            if (policyId.equals(policy.getKey().getName())) {
                policyDeleteList.add(policy);
            }
        }

        if (policyDeleteList.isEmpty()) {
            String errorMessage = NO_POLICY_FOUND_FOR_POLICY_ID + policyId;
            LOGGER.warn(errorMessage);
            throw new PfModelRuntimeException(Response.Status.BAD_REQUEST, errorMessage);
        }

        // Create the structure of the TOSCA service template to contain the policy type
        ToscaServiceTemplate serviceTemplate = new ToscaServiceTemplate();
        serviceTemplate.setTopologyTemplate(new ToscaTopologyTemplate());
        serviceTemplate.getTopologyTemplate().setPolicies(new ToscaPolicies());

        for (ToscaPolicy deletePolicy : policyDeleteList) {
            dao.delete(deletePolicy);
            serviceTemplate.getTopologyTemplate().getPolicies().getConceptMap().put(deletePolicy.getKey(),
                    deletePolicy);
        }

        return new LegacyOperationalPolicyMapper().fromToscaServiceTemplate(serviceTemplate);
    }

    /**
     * Get legacy guard policy.
     *
     * @param dao the DAO to use to access the database
     * @param policyId ID of the policy.
     * @return the policies found
     * @throws PfModelException on errors getting policies
     */
    public Map<String, LegacyGuardPolicyOutput> getGuardPolicy(@NonNull final PfDao dao, @NonNull final String policyId)
            throws PfModelException {

        ToscaPolicy newestPolicy = getLatestPolicy(dao, policyId);

        if (newestPolicy == null) {
            String errorMessage = NO_POLICY_FOUND_FOR_POLICY_ID + policyId;
            LOGGER.warn(errorMessage);
            throw new PfModelRuntimeException(Response.Status.BAD_REQUEST, errorMessage);
        }

        // Create the structure of the TOSCA service template to contain the policy type
        ToscaServiceTemplate serviceTemplate = new ToscaServiceTemplate();
        serviceTemplate.setTopologyTemplate(new ToscaTopologyTemplate());
        serviceTemplate.getTopologyTemplate().setPolicies(new ToscaPolicies());
        serviceTemplate.getTopologyTemplate().getPolicies().getConceptMap().put(newestPolicy.getKey(), newestPolicy);

        return new LegacyGuardPolicyMapper().fromToscaServiceTemplate(serviceTemplate);
    }

    /**
     * Create legacy guard policy.
     *
     * @param dao the DAO to use to access the database
     * @param legacyGuardPolicy the definition of the policy to be created.
     * @return the created policy
     * @throws PfModelException on errors creating policies
     */
    public Map<String, LegacyGuardPolicyOutput> createGuardPolicy(@NonNull final PfDao dao,
            @NonNull final LegacyGuardPolicyInput legacyGuardPolicy) throws PfModelException {

        ToscaServiceTemplate incomingServiceTemplate =
                new LegacyGuardPolicyMapper().toToscaServiceTemplate(legacyGuardPolicy);
        ToscaServiceTemplate outgoingingServiceTemplate =
                new SimpleToscaProvider().createPolicies(dao, incomingServiceTemplate);

        return new LegacyGuardPolicyMapper().fromToscaServiceTemplate(outgoingingServiceTemplate);
    }

    /**
     * Update legacy guard policy.
     *
     * @param dao the DAO to use to access the database
     * @param legacyGuardPolicy the definition of the policy to be updated
     * @return the updated policy
     * @throws PfModelException on errors updating policies
     */
    public Map<String, LegacyGuardPolicyOutput> updateGuardPolicy(@NonNull final PfDao dao,
            @NonNull final LegacyGuardPolicyInput legacyGuardPolicy) throws PfModelException {

        ToscaServiceTemplate incomingServiceTemplate =
                new LegacyGuardPolicyMapper().toToscaServiceTemplate(legacyGuardPolicy);
        ToscaServiceTemplate outgoingingServiceTemplate =
                new SimpleToscaProvider().createPolicies(dao, incomingServiceTemplate);

        return new LegacyGuardPolicyMapper().fromToscaServiceTemplate(outgoingingServiceTemplate);
    }


    /**
     * Delete legacy guard policy.
     *
     * @param dao the DAO to use to access the database
     * @param policyId ID of the policy.
     * @return the deleted policy
     * @throws PfModelException on errors deleting policies
     */
    public Map<String, LegacyGuardPolicyOutput> deleteGuardPolicy(@NonNull final PfDao dao,
            @NonNull final String policyId) throws PfModelException {

        // Get all the policies in the database and check the policy ID against the policies returned
        List<ToscaPolicy> policyList = dao.getAll(ToscaPolicy.class);

        // Find the latest policy that matches the ID
        List<ToscaPolicy> policyDeleteList = new ArrayList<>();

        for (ToscaPolicy policy : policyList) {
            if (policyId.equals(policy.getKey().getName())) {
                policyDeleteList.add(policy);
            }
        }

        if (policyDeleteList.isEmpty()) {
            String errorMessage = NO_POLICY_FOUND_FOR_POLICY_ID + policyId;
            LOGGER.warn(errorMessage);
            throw new PfModelRuntimeException(Response.Status.BAD_REQUEST, errorMessage);
        }

        // Create the structure of the TOSCA service template to contain the policy type
        ToscaServiceTemplate serviceTemplate = new ToscaServiceTemplate();
        serviceTemplate.setTopologyTemplate(new ToscaTopologyTemplate());
        serviceTemplate.getTopologyTemplate().setPolicies(new ToscaPolicies());

        for (ToscaPolicy deletePolicy : policyDeleteList) {
            dao.delete(deletePolicy);
            serviceTemplate.getTopologyTemplate().getPolicies().getConceptMap().put(deletePolicy.getKey(),
                    deletePolicy);
        }

        return new LegacyGuardPolicyMapper().fromToscaServiceTemplate(serviceTemplate);
    }

    /**
     * Get the latest policy for a policy ID.
     *
     * @param dao The DAO to read from
     * @param policyId the ID of the policy
     * @return the policy
     */
    private ToscaPolicy getLatestPolicy(final PfDao dao, final String policyId) {
        // Get all the policies in the database and check the policy ID against the policies returned
        List<ToscaPolicy> policyList = dao.getAll(ToscaPolicy.class);

        // Find the latest policy that matches the ID
        ToscaPolicy newestPolicy = null;

        for (ToscaPolicy policy : policyList) {
            if (!policyId.equals(policy.getKey().getName())) {
                continue;
            }

            // We found a matching policy
            if (newestPolicy == null || policy.getKey().isNewerThan(newestPolicy.getKey())) {
                // First policy found
                newestPolicy = policy;
            }
        }
        return newestPolicy;
    }
}
