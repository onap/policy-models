/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2020 Nordix Foundation.
 *  Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

import java.util.Map;

import javax.ws.rs.core.Response;

import lombok.NonNull;

import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.base.PfModelRuntimeException;
import org.onap.policy.models.dao.PfDao;
import org.onap.policy.models.tosca.legacy.concepts.LegacyOperationalPolicy;
import org.onap.policy.models.tosca.legacy.mapping.LegacyOperationalPolicyMapper;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaPolicy;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaServiceTemplate;
import org.onap.policy.models.tosca.simple.provider.SimpleToscaProvider;
import org.onap.policy.models.tosca.utils.ToscaUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides the provision of information on TOSCA concepts in the database to callers in legacy formats.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class LegacyProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(LegacyProvider.class);

    public static final String LEGACY_MINOR_PATCH_SUFFIX = ".0.0";

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

        LegacyOperationalPolicy legacyOperationalPolicy =
            new LegacyOperationalPolicyMapper().fromToscaServiceTemplate(getLegacyPolicy(dao, policyId, policyVersion));

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

        JpaToscaServiceTemplate legacyOperationalServiceTemplate =
            new LegacyOperationalPolicyMapper().toToscaServiceTemplate(legacyOperationalPolicy);

        new SimpleToscaProvider().createPolicies(dao, legacyOperationalServiceTemplate);

        LegacyOperationalPolicy createdLegacyOperationalPolicy =
            new LegacyOperationalPolicyMapper().fromToscaServiceTemplate(legacyOperationalServiceTemplate);

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

        JpaToscaServiceTemplate deleteServiceTemplate = new SimpleToscaProvider().deletePolicy(dao,
            new PfConceptKey(policyId, policyVersion + LEGACY_MINOR_PATCH_SUFFIX));
        LegacyOperationalPolicy legacyOperationalPolicy =
            new LegacyOperationalPolicyMapper().fromToscaServiceTemplate(deleteServiceTemplate);

        LOGGER.debug("<-deleteOperationalPolicy: policyId={}, policyVersion={}, legacyOperationalPolicy={}", policyId,
            policyVersion, legacyOperationalPolicy);
        return legacyOperationalPolicy;
    }

    /**
     * Get the JPA Policy for a policy ID and version.
     *
     * @param dao The DAO to search
     * @param policyId the policy ID to search for
     * @param policyVersion the policy version to search for
     * @return the JPA policy found
     * @throws PfModelException if a policy is not found
     */
    private JpaToscaServiceTemplate getLegacyPolicy(final PfDao dao, final String policyId, final String policyVersion)
        throws PfModelException {
        JpaToscaServiceTemplate foundPolicyServiceTemplate = null;
        if (policyVersion == null) {
            foundPolicyServiceTemplate = getLatestPolicy(dao, policyId);
        } else {
            foundPolicyServiceTemplate =
                new SimpleToscaProvider().getPolicies(dao, policyId, policyVersion + LEGACY_MINOR_PATCH_SUFFIX);
        }

        if (foundPolicyServiceTemplate == null) {
            String errorMessage = NO_POLICY_FOUND_FOR_POLICY + policyId + ':' + policyVersion;
            LOGGER.warn(errorMessage);
            throw new PfModelRuntimeException(Response.Status.BAD_REQUEST, errorMessage);
        }

        return foundPolicyServiceTemplate;
    }

    /**
     * Get the latest policy for a policy ID.
     *
     * @param dao The DAO to read from
     * @param policyId the ID of the policy
     * @return the policy
     * @throws PfModelException on exceptions getting the policies
     */
    private JpaToscaServiceTemplate getLatestPolicy(final PfDao dao, final String policyId) throws PfModelException {
        // Get all the policies in the database and check the policy ID against the policies returned
        JpaToscaServiceTemplate serviceTemplate = new SimpleToscaProvider().getPolicies(dao, policyId, null);

        if (!ToscaUtils.doPoliciesExist(serviceTemplate)) {
            return null;
        }

        // Find the latest policy that matches the ID
        final Map<PfConceptKey, JpaToscaPolicy> policyMap =
            serviceTemplate.getTopologyTemplate().getPolicies().getConceptMap();
        PfConceptKey newestPolicyKey = null;

        for (JpaToscaPolicy policy : policyMap.values()) {
            if (!policyId.equals(policy.getKey().getName())) {
                continue;
            }

            // We found a matching policy
            if (newestPolicyKey == null || policy.getKey().isNewerThan(newestPolicyKey)) {
                // First policy found
                newestPolicyKey = policy.getKey();
            }
        }

        final PfConceptKey newestPolicyFinalKey = newestPolicyKey;
        policyMap.keySet().removeIf(key -> !key.equals(newestPolicyFinalKey));

        return serviceTemplate;
    }
}
