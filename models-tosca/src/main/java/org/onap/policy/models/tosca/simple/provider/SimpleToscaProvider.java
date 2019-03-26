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

package org.onap.policy.models.tosca.simple.provider;

import javax.ws.rs.core.Response;

import lombok.NonNull;

import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.base.PfModelRuntimeException;
import org.onap.policy.models.dao.PfDao;
import org.onap.policy.models.tosca.simple.concepts.ToscaPolicies;
import org.onap.policy.models.tosca.simple.concepts.ToscaPolicy;
import org.onap.policy.models.tosca.simple.concepts.ToscaPolicyType;
import org.onap.policy.models.tosca.simple.concepts.ToscaPolicyTypes;
import org.onap.policy.models.tosca.simple.concepts.ToscaServiceTemplate;
import org.onap.policy.models.tosca.simple.concepts.ToscaTopologyTemplate;
import org.onap.policy.models.tosca.utils.ToscaUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides the provision of information on TOSCA concepts in the database to callers.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class SimpleToscaProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleToscaProvider.class);

    /**
     * Get policy types.
     *
     * @param dao the DAO to use to access the database
     * @param policyTypeKey the policy type key for the policy types to be retrieved. A null key name returns all policy
     *        types. A null key version returns all versions of the policy type name specified in the key.
     * @return the policy types found
     * @throws PfModelException on errors getting policy types
     */
    public ToscaServiceTemplate getPolicyTypes(@NonNull final PfDao dao, @NonNull final PfConceptKey policyTypeKey)
            throws PfModelException {

        // Create the structure of the TOSCA service template to contain the policy type
        ToscaServiceTemplate serviceTemplate = new ToscaServiceTemplate();
        serviceTemplate.setPolicyTypes(new ToscaPolicyTypes());

        // Add the policy type to the TOSCA service template
        ToscaPolicyType policyType = dao.get(ToscaPolicyType.class, policyTypeKey);
        if (policyType != null) {
            serviceTemplate.getPolicyTypes().getConceptMap().put(policyTypeKey, policyType);
            return serviceTemplate;
        } else {
            String errorMessage = "policy type not found: " + policyTypeKey.getId();
            LOGGER.warn(errorMessage);
            throw new PfModelRuntimeException(Response.Status.BAD_REQUEST, errorMessage);
        }
    }

    /**
     * Create policy types.
     *
     * @param dao the DAO to use to access the database
     * @param serviceTemplate the service template containing the definition of the policy types to be created
     * @return the TOSCA service template containing the created policy types
     * @throws PfModelException on errors creating policy types
     */
    public ToscaServiceTemplate createPolicyTypes(@NonNull final PfDao dao,
            @NonNull final ToscaServiceTemplate serviceTemplate) throws PfModelException {

        ToscaUtils.assertPolicyTypesExist(serviceTemplate);

        for (ToscaPolicyType policyType : serviceTemplate.getPolicyTypes().getAll(null)) {
            dao.create(policyType);
        }

        // Return the created policy types
        ToscaPolicyTypes returnPolicyTypes = new ToscaPolicyTypes();

        for (PfConceptKey policyTypeKey : serviceTemplate.getPolicyTypes().getConceptMap().keySet()) {
            returnPolicyTypes.getConceptMap().put(policyTypeKey, dao.get(ToscaPolicyType.class, policyTypeKey));
        }

        ToscaServiceTemplate returnServiceTemplate = new ToscaServiceTemplate();
        returnServiceTemplate.setPolicyTypes(returnPolicyTypes);

        return returnServiceTemplate;
    }

    /**
     * Create policy types.
     *
     * @param dao the DAO to use to access the database
     * @param serviceTemplate the service template containing the definition of the policy types to be modified
     * @return the TOSCA service template containing the modified policy types
     * @throws PfModelException on errors updating policy types
     */
    public ToscaServiceTemplate updatePolicyTypes(@NonNull final PfDao dao,
            @NonNull final ToscaServiceTemplate serviceTemplate) throws PfModelException {

        ToscaUtils.assertPolicyTypesExist(serviceTemplate);

        for (ToscaPolicyType policyType : serviceTemplate.getPolicyTypes().getAll(null)) {
            dao.update(policyType);
        }

        // Return the created policy types
        ToscaPolicyTypes returnPolicyTypes = new ToscaPolicyTypes();

        for (PfConceptKey policyTypeKey : serviceTemplate.getPolicyTypes().getConceptMap().keySet()) {
            returnPolicyTypes.getConceptMap().put(policyTypeKey, dao.get(ToscaPolicyType.class, policyTypeKey));
        }

        ToscaServiceTemplate returnServiceTemplate = new ToscaServiceTemplate();
        returnServiceTemplate.setPolicyTypes(returnPolicyTypes);

        return returnServiceTemplate;
    }

    /**
     * Delete policy types.
     *
     * @param dao the DAO to use to access the database
     * @param policyTypeKey the policy type key for the policy types to be deleted, if the version of the key is null,
     *        all versions of the policy type are deleted.
     * @return the TOSCA service template containing the policy types that were deleted
     * @throws PfModelException on errors deleting policy types
     */
    public ToscaServiceTemplate deletePolicyTypes(@NonNull final PfDao dao, @NonNull final PfConceptKey policyTypeKey)
            throws PfModelException {

        ToscaServiceTemplate serviceTemplate = getPolicyTypes(dao, policyTypeKey);

        dao.delete(ToscaPolicyType.class, policyTypeKey);

        return serviceTemplate;
    }

    /**
     * Get policies.
     *
     * @param dao the DAO to use to access the database
     * @param policyKey the policy key for the policies to be retrieved. The parent name and version must be specified.
     *        A null local name returns all policies for a parent policy type.
     * @return the policies found
     * @throws PfModelException on errors getting policies
     */
    public ToscaServiceTemplate getPolicies(@NonNull final PfDao dao, @NonNull final PfConceptKey policyKey)
            throws PfModelException {

        // Create the structure of the TOSCA service template to contain the policy type
        ToscaServiceTemplate serviceTemplate = new ToscaServiceTemplate();
        serviceTemplate.setTopologyTemplate(new ToscaTopologyTemplate());
        serviceTemplate.getTopologyTemplate().setPolicies(new ToscaPolicies());

        // Add the policy to the TOSCA service template
        ToscaPolicy policy = dao.get(ToscaPolicy.class, policyKey);
        if (policy != null) {
            serviceTemplate.getTopologyTemplate().getPolicies().getConceptMap().put(policyKey, policy);
            return serviceTemplate;
        } else {
            String errorMessage = "policy not found: " + policyKey.getId();
            LOGGER.warn(errorMessage);
            throw new PfModelRuntimeException(Response.Status.BAD_REQUEST, errorMessage);
        }
    }

    /**
     * Create policies.
     *
     * @param dao the DAO to use to access the database
     * @param serviceTemplate the service template containing the definitions of the new policies to be created.
     * @return the TOSCA service template containing the policy types that were created
     * @throws PfModelException on errors creating policies
     */
    public ToscaServiceTemplate createPolicies(@NonNull final PfDao dao,
            @NonNull final ToscaServiceTemplate serviceTemplate) throws PfModelException {

        ToscaUtils.assertPoliciesExist(serviceTemplate);

        for (ToscaPolicy policy : serviceTemplate.getTopologyTemplate().getPolicies().getAll(null)) {
            dao.create(policy);
        }

        // Return the created policy types
        ToscaPolicies returnPolicies = new ToscaPolicies();
        returnPolicies.setKey(serviceTemplate.getTopologyTemplate().getPolicies().getKey());

        for (PfConceptKey policyKey : serviceTemplate.getTopologyTemplate().getPolicies().getConceptMap().keySet()) {
            returnPolicies.getConceptMap().put(policyKey, dao.get(ToscaPolicy.class, policyKey));
        }

        serviceTemplate.getTopologyTemplate().setPolicies(returnPolicies);

        return serviceTemplate;
    }

    /**
     * Update policies.
     *
     * @param dao the DAO to use to access the database
     * @param serviceTemplate the service template containing the definitions of the policies to be updated.
     * @return the TOSCA service template containing the policies that were updated
     * @throws PfModelException on errors updating policies
     */
    public ToscaServiceTemplate updatePolicies(@NonNull final PfDao dao,
            @NonNull final ToscaServiceTemplate serviceTemplate) throws PfModelException {

        ToscaUtils.assertPoliciesExist(serviceTemplate);

        for (ToscaPolicy policy : serviceTemplate.getTopologyTemplate().getPolicies().getAll(null)) {
            dao.update(policy);
        }

        // Return the created policy types
        ToscaPolicies returnPolicies = new ToscaPolicies();
        returnPolicies.setKey(serviceTemplate.getTopologyTemplate().getPolicies().getKey());

        for (PfConceptKey policyKey : serviceTemplate.getTopologyTemplate().getPolicies().getConceptMap().keySet()) {
            returnPolicies.getConceptMap().put(policyKey, dao.get(ToscaPolicy.class, policyKey));
        }

        serviceTemplate.getTopologyTemplate().setPolicies(returnPolicies);

        return serviceTemplate;
    }

    /**
     * Delete policies.
     *
     * @param dao the DAO to use to access the database
     * @param policyKey the policy key
     * @return the TOSCA service template containing the policies that were deleted
     * @throws PfModelException on errors deleting policies
     */
    public ToscaServiceTemplate deletePolicies(@NonNull final PfDao dao, @NonNull final PfConceptKey policyKey)
            throws PfModelException {

        ToscaServiceTemplate serviceTemplate = getPolicies(dao, policyKey);

        dao.delete(ToscaPolicy.class, policyKey);

        return serviceTemplate;
    }
}
