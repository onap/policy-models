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

import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import lombok.NonNull;

import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.base.PfModelRuntimeException;
import org.onap.policy.models.dao.PfDao;
import org.onap.policy.models.tosca.legacy.concepts.LegacyGuardPolicyInput;
import org.onap.policy.models.tosca.legacy.concepts.LegacyGuardPolicyOutput;
import org.onap.policy.models.tosca.legacy.concepts.LegacyOperationalPolicy;
import org.onap.policy.models.tosca.legacy.mapping.LegacyGuardPolicyMapper;
import org.onap.policy.models.tosca.legacy.mapping.LegacyOperationalPolicyMapper;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaPolicies;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaPolicy;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaServiceTemplate;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaTopologyTemplate;
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
    private static final String LEGACY_MINOR_PATCH_SUFFIX = ".0.0";

    // Recurring constants
    private static final String NO_POLICY_FOUND_FOR_POLICY = "no policy found for policy: ";

    /**
     * Get legacy operational policy.
     *
     * @param dao the DAO to use to access the database
     * @param policyId ID of the policy.
     * @param policyVersion version of the policy.
     * @return the policies found
     * @throws PfModelException on errors getting policies
     */
    public LegacyOperationalPolicy getOperationalPolicy(@NonNull final PfDao dao, @NonNull final String policyId,
            final String policyVersion) throws PfModelException {

        LOGGER.debug("->getOperationalPolicy: policyId={}, policyVersion={}", policyId, policyVersion);

        LegacyOperationalPolicy legacyOperationalPolicy = new LegacyOperationalPolicyMapper()
                .fromToscaServiceTemplate(getLegacyPolicy(dao, policyId, policyVersion));

        LOGGER.debug("<-getOperationalPolicy: policyId={}, policyVersion={}, legacyOperationalPolicy={}", policyId,
                policyVersion, legacyOperationalPolicy);
        return legacyOperationalPolicy;
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

        LOGGER.debug("->createOperationalPolicy: legacyOperationalPolicy={}", legacyOperationalPolicy);

        // We need to find the latest policy and update the major version, if there is no policy with this ID, then
        // we set it to the first version
        JpaToscaPolicy newestPolicy = getLatestPolicy(dao, legacyOperationalPolicy.getPolicyId());

        if (newestPolicy == null) {
            legacyOperationalPolicy.setPolicyVersion(FIRST_POLICY_VERSION);
        } else {
            legacyOperationalPolicy.setPolicyVersion(Integer.toString(newestPolicy.getKey().getMajorVersion() + 1));
        }

        JpaToscaServiceTemplate incomingServiceTemplate =
                new LegacyOperationalPolicyMapper().toToscaServiceTemplate(legacyOperationalPolicy);
        JpaToscaServiceTemplate outgoingingServiceTemplate =
                new SimpleToscaProvider().createPolicies(dao, incomingServiceTemplate);

        LegacyOperationalPolicy createdLegacyOperationalPolicy =
                new LegacyOperationalPolicyMapper().fromToscaServiceTemplate(outgoingingServiceTemplate);

        LOGGER.debug("<-createOperationalPolicy: createdLegacyOperationalPolicy={}", createdLegacyOperationalPolicy);
        return createdLegacyOperationalPolicy;
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

        LOGGER.debug("->updateOperationalPolicy: legacyOperationalPolicy={}", legacyOperationalPolicy);
        JpaToscaServiceTemplate incomingServiceTemplate =
                new LegacyOperationalPolicyMapper().toToscaServiceTemplate(legacyOperationalPolicy);
        JpaToscaServiceTemplate outgoingingServiceTemplate =
                new SimpleToscaProvider().updatePolicies(dao, incomingServiceTemplate);

        LegacyOperationalPolicy updatedLegacyOperationalPolicy =
                new LegacyOperationalPolicyMapper().fromToscaServiceTemplate(outgoingingServiceTemplate);

        LOGGER.debug("<-updateOperationalPolicy: updatedLegacyOperationalPolicy={}", updatedLegacyOperationalPolicy);
        return updatedLegacyOperationalPolicy;
    }

    /**
     * Delete legacy operational policy.
     *
     * @param dao the DAO to use to access the database
     * @param policyId ID of the policy.
     * @param policyVersion version of the policy.
     * @return the deleted policy
     * @throws PfModelException on errors deleting policies
     */
    public LegacyOperationalPolicy deleteOperationalPolicy(@NonNull final PfDao dao, @NonNull final String policyId,
            @NonNull final String policyVersion) throws PfModelException {

        LOGGER.debug("->deleteOperationalPolicy: policyId={}, policyVersion={}", policyId, policyVersion);

        LegacyOperationalPolicy legacyOperationalPolicy = new LegacyOperationalPolicyMapper()
                .fromToscaServiceTemplate(deleteLegacyPolicy(dao, policyId, policyVersion));

        LOGGER.debug("<-deleteOperationalPolicy: policyId={}, policyVersion={}, legacyOperationalPolicy={}", policyId,
                policyVersion, legacyOperationalPolicy);
        return legacyOperationalPolicy;
    }

    /**
     * Get legacy guard policy.
     *
     * @param dao the DAO to use to access the database
     * @param policyId ID of the policy.
     * @param policyVersion version of the policy.
     * @return the policies found
     * @throws PfModelException on errors getting policies
     */
    public Map<String, LegacyGuardPolicyOutput> getGuardPolicy(@NonNull final PfDao dao, @NonNull final String policyId,
            final String policyVersion) throws PfModelException {

        LOGGER.debug("->getGuardPolicy: policyId={}, policyVersion={}", policyId, policyVersion);

        Map<String, LegacyGuardPolicyOutput> legacyGuardPolicyMap =
                new LegacyGuardPolicyMapper().fromToscaServiceTemplate(getLegacyPolicy(dao, policyId, policyVersion));

        LOGGER.debug("<-getGuardPolicy: policyId={}, policyVersion={}, legacyGuardPolicyMap={}", policyId,
                policyVersion, legacyGuardPolicyMap);
        return legacyGuardPolicyMap;
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

        LOGGER.debug("->createGuardPolicy: legacyGuardPolicy={}", legacyGuardPolicy);

        JpaToscaServiceTemplate incomingServiceTemplate =
                new LegacyGuardPolicyMapper().toToscaServiceTemplate(legacyGuardPolicy);
        JpaToscaServiceTemplate outgoingingServiceTemplate =
                new SimpleToscaProvider().createPolicies(dao, incomingServiceTemplate);

        Map<String, LegacyGuardPolicyOutput> createdLegacyGuardPolicyMap =
                new LegacyGuardPolicyMapper().fromToscaServiceTemplate(outgoingingServiceTemplate);

        LOGGER.debug("<-createGuardPolicy: createdLegacyGuardPolicyMap={}", createdLegacyGuardPolicyMap);
        return createdLegacyGuardPolicyMap;
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

        LOGGER.debug("->updateGuardPolicy: legacyGuardPolicy={}", legacyGuardPolicy);

        JpaToscaServiceTemplate incomingServiceTemplate =
                new LegacyGuardPolicyMapper().toToscaServiceTemplate(legacyGuardPolicy);
        JpaToscaServiceTemplate outgoingingServiceTemplate =
                new SimpleToscaProvider().updatePolicies(dao, incomingServiceTemplate);

        Map<String, LegacyGuardPolicyOutput> updatedLegacyGuardPolicyMap =
                new LegacyGuardPolicyMapper().fromToscaServiceTemplate(outgoingingServiceTemplate);

        LOGGER.debug("<-updateGuardPolicy: updatedLegacyGuardPolicyMap={}", updatedLegacyGuardPolicyMap);
        return updatedLegacyGuardPolicyMap;
    }


    /**
     * Delete legacy guard policy.
     *
     * @param dao the DAO to use to access the database
     * @param policyId ID of the policy.
     * @param policyVersion version of the policy.
     * @return the deleted policy
     * @throws PfModelException on errors deleting policies
     */
    public Map<String, LegacyGuardPolicyOutput> deleteGuardPolicy(@NonNull final PfDao dao,
            @NonNull final String policyId, @NonNull final String policyVersion) throws PfModelException {

        LOGGER.debug("->deleteGuardPolicy: policyId={}, policyVersion={}", policyId, policyVersion);
        Map<String, LegacyGuardPolicyOutput> legacyGuardPolicyMap = new LegacyGuardPolicyMapper()
                .fromToscaServiceTemplate(deleteLegacyPolicy(dao, policyId, policyVersion));

        LOGGER.debug("<-deleteGuardPolicy: policyId={}, policyVersion={}, legacyGuardPolicyMap={}", policyId,
                policyVersion, legacyGuardPolicyMap);
        return legacyGuardPolicyMap;
    }

    /**
     * Get the JPA Policy for a policy ID and version.
     *
     * @param dao The DAO to search
     * @param policyId the policy ID to search for
     * @param policyVersion the policy version to search for
     * @return the JPA policy found
     * @throws PfModelRuntimeException if a policy is not found
     */
    private JpaToscaServiceTemplate getLegacyPolicy(final PfDao dao, final String policyId,
            final String policyVersion) {
        JpaToscaPolicy foundPolicy = null;
        if (policyVersion == null) {
            foundPolicy = getLatestPolicy(dao, policyId);
        } else {
            foundPolicy = dao.get(JpaToscaPolicy.class,
                    new PfConceptKey(policyId, policyVersion + LEGACY_MINOR_PATCH_SUFFIX));
        }

        if (foundPolicy == null) {
            String errorMessage = NO_POLICY_FOUND_FOR_POLICY + policyId + ':' + policyVersion;
            LOGGER.warn(errorMessage);
            throw new PfModelRuntimeException(Response.Status.BAD_REQUEST, errorMessage);
        }

        // Create the structure of the TOSCA service template to contain the policy type
        JpaToscaServiceTemplate serviceTemplate = new JpaToscaServiceTemplate();
        serviceTemplate.setTopologyTemplate(new JpaToscaTopologyTemplate());
        serviceTemplate.getTopologyTemplate().setPolicies(new JpaToscaPolicies());
        serviceTemplate.getTopologyTemplate().getPolicies().getConceptMap().put(foundPolicy.getKey(), foundPolicy);

        return serviceTemplate;
    }

    /**
     * Delete a legacy policy.
     *
     * @param dao the DAO to use for the deletion
     * @param policyId the policy ID
     * @param policyVersion the policy version
     * @return a service template containing the policy that has been deleted
     */
    private JpaToscaServiceTemplate deleteLegacyPolicy(final PfDao dao, final String policyId,
            final String policyVersion) {

        final JpaToscaPolicy deletePolicy =
                dao.get(JpaToscaPolicy.class, new PfConceptKey(policyId, policyVersion + LEGACY_MINOR_PATCH_SUFFIX));

        if (deletePolicy == null) {
            String errorMessage = NO_POLICY_FOUND_FOR_POLICY + policyId + ':' + policyVersion;
            LOGGER.warn(errorMessage);
            throw new PfModelRuntimeException(Response.Status.BAD_REQUEST, errorMessage);
        }

        // Delete the policy
        dao.delete(deletePolicy);

        // Create the structure of the TOSCA service template to contain the policy type
        JpaToscaServiceTemplate serviceTemplate = new JpaToscaServiceTemplate();
        serviceTemplate.setTopologyTemplate(new JpaToscaTopologyTemplate());
        serviceTemplate.getTopologyTemplate().setPolicies(new JpaToscaPolicies());
        serviceTemplate.getTopologyTemplate().getPolicies().getConceptMap().put(deletePolicy.getKey(), deletePolicy);

        return serviceTemplate;
    }

    /**
     * Get the latest policy for a policy ID.
     *
     * @param dao The DAO to read from
     * @param policyId the ID of the policy
     * @return the policy
     */
    private JpaToscaPolicy getLatestPolicy(final PfDao dao, final String policyId) {
        // Get all the policies in the database and check the policy ID against the policies returned
        List<JpaToscaPolicy> policyList = dao.getAll(JpaToscaPolicy.class);

        // Find the latest policy that matches the ID
        JpaToscaPolicy newestPolicy = null;

        for (JpaToscaPolicy policy : policyList) {
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
