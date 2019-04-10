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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.NonNull;

import org.onap.policy.models.base.PfConcept;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.dao.PfDao;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaPolicies;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaPolicy;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaPolicyType;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaPolicyTypes;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaServiceTemplate;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaTopologyTemplate;
import org.onap.policy.models.tosca.utils.ToscaUtils;

/**
 * This class provides the provision of information on TOSCA concepts in the database to callers.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class SimpleToscaProvider {
    /**
     * Get policy types.
     *
     * @param dao the DAO to use to access the database
     * @param name the name of the policy type to get, set to null to get all policy types
     * @param version the version of the policy type to get, set to null to get all versions
     * @return the policy types found
     * @throws PfModelException on errors getting policy types
     */
    public JpaToscaServiceTemplate getPolicyTypes(@NonNull final PfDao dao, final String name, final String version)
            throws PfModelException {

        // Create the structure of the TOSCA service template to contain the policy type
        JpaToscaServiceTemplate serviceTemplate = new JpaToscaServiceTemplate();
        serviceTemplate.setPolicyTypes(new JpaToscaPolicyTypes());

        // Add the policy type to the TOSCA service template
        List<JpaToscaPolicyType> jpaPolicyTypeList = dao.getFiltered(JpaToscaPolicyType.class, name, version);
        serviceTemplate.getPolicyTypes().getConceptMap().putAll(asConceptMap(jpaPolicyTypeList));

        return serviceTemplate;
    }

    /**
     * Create policy types.
     *
     * @param dao the DAO to use to access the database
     * @param serviceTemplate the service template containing the definition of the policy types to be created
     * @return the TOSCA service template containing the created policy types
     * @throws PfModelException on errors creating policy types
     */
    public JpaToscaServiceTemplate createPolicyTypes(@NonNull final PfDao dao,
            @NonNull final JpaToscaServiceTemplate serviceTemplate) throws PfModelException {

        ToscaUtils.assertPolicyTypesExist(serviceTemplate);

        for (JpaToscaPolicyType policyType : serviceTemplate.getPolicyTypes().getAll(null)) {
            dao.create(policyType);
        }

        // Return the created policy types
        JpaToscaPolicyTypes returnPolicyTypes = new JpaToscaPolicyTypes();

        for (PfConceptKey policyTypeKey : serviceTemplate.getPolicyTypes().getConceptMap().keySet()) {
            returnPolicyTypes.getConceptMap().put(policyTypeKey, dao.get(JpaToscaPolicyType.class, policyTypeKey));
        }

        JpaToscaServiceTemplate returnServiceTemplate = new JpaToscaServiceTemplate();
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
    public JpaToscaServiceTemplate updatePolicyTypes(@NonNull final PfDao dao,
            @NonNull final JpaToscaServiceTemplate serviceTemplate) throws PfModelException {

        ToscaUtils.assertPolicyTypesExist(serviceTemplate);

        for (JpaToscaPolicyType policyType : serviceTemplate.getPolicyTypes().getAll(null)) {
            dao.update(policyType);
        }

        // Return the created policy types
        JpaToscaPolicyTypes returnPolicyTypes = new JpaToscaPolicyTypes();

        for (PfConceptKey policyTypeKey : serviceTemplate.getPolicyTypes().getConceptMap().keySet()) {
            returnPolicyTypes.getConceptMap().put(policyTypeKey, dao.get(JpaToscaPolicyType.class, policyTypeKey));
        }

        JpaToscaServiceTemplate returnServiceTemplate = new JpaToscaServiceTemplate();
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
    public JpaToscaServiceTemplate deletePolicyType(@NonNull final PfDao dao, @NonNull final PfConceptKey policyTypeKey)
            throws PfModelException {

        JpaToscaServiceTemplate serviceTemplate =
                getPolicyTypes(dao, policyTypeKey.getName(), policyTypeKey.getVersion());

        dao.delete(JpaToscaPolicyType.class, policyTypeKey);

        return serviceTemplate;
    }

    /**
     * Get policies.
     *
     * @param dao the DAO to use to access the database
     * @param name the name of the policy to get, set to null to get all policy types
     * @param version the version of the policy to get, set to null to get all versions
     * @return the policies found
     * @throws PfModelException on errors getting policies
     */
    public JpaToscaServiceTemplate getPolicies(@NonNull final PfDao dao, final String name, final String version)
            throws PfModelException {

        // Create the structure of the TOSCA service template to contain the policy type
        JpaToscaServiceTemplate serviceTemplate = new JpaToscaServiceTemplate();
        serviceTemplate.setTopologyTemplate(new JpaToscaTopologyTemplate());
        serviceTemplate.getTopologyTemplate().setPolicies(new JpaToscaPolicies());

        // Add the policy type to the TOSCA service template
        List<JpaToscaPolicy> jpaPolicyList = dao.getFiltered(JpaToscaPolicy.class, name, version);
        serviceTemplate.getTopologyTemplate().getPolicies().getConceptMap().putAll(asConceptMap(jpaPolicyList));
        return serviceTemplate;
    }

    /**
     * Create policies.
     *
     * @param dao the DAO to use to access the database
     * @param serviceTemplate the service template containing the definitions of the new policies to be created.
     * @return the TOSCA service template containing the policy types that were created
     * @throws PfModelException on errors creating policies
     */
    public JpaToscaServiceTemplate createPolicies(@NonNull final PfDao dao,
            @NonNull final JpaToscaServiceTemplate serviceTemplate) throws PfModelException {

        ToscaUtils.assertPoliciesExist(serviceTemplate);

        for (JpaToscaPolicy policy : serviceTemplate.getTopologyTemplate().getPolicies().getAll(null)) {
            dao.create(policy);
        }

        // Return the created policy types
        JpaToscaPolicies returnPolicies = new JpaToscaPolicies();
        returnPolicies.setKey(serviceTemplate.getTopologyTemplate().getPolicies().getKey());

        for (PfConceptKey policyKey : serviceTemplate.getTopologyTemplate().getPolicies().getConceptMap().keySet()) {
            returnPolicies.getConceptMap().put(policyKey, dao.get(JpaToscaPolicy.class, policyKey));
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
    public JpaToscaServiceTemplate updatePolicies(@NonNull final PfDao dao,
            @NonNull final JpaToscaServiceTemplate serviceTemplate) throws PfModelException {

        ToscaUtils.assertPoliciesExist(serviceTemplate);

        for (JpaToscaPolicy policy : serviceTemplate.getTopologyTemplate().getPolicies().getAll(null)) {
            dao.update(policy);
        }

        // Return the created policy types
        JpaToscaPolicies returnPolicies = new JpaToscaPolicies();
        returnPolicies.setKey(serviceTemplate.getTopologyTemplate().getPolicies().getKey());

        for (PfConceptKey policyKey : serviceTemplate.getTopologyTemplate().getPolicies().getConceptMap().keySet()) {
            returnPolicies.getConceptMap().put(policyKey, dao.get(JpaToscaPolicy.class, policyKey));
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
    public JpaToscaServiceTemplate deletePolicy(@NonNull final PfDao dao, @NonNull final PfConceptKey policyKey)
            throws PfModelException {

        JpaToscaServiceTemplate serviceTemplate = getPolicies(dao, policyKey.getName(), policyKey.getVersion());

        dao.delete(JpaToscaPolicy.class, policyKey);

        return serviceTemplate;
    }

    /**
     * Convert a list of concepts to a map of concepts.
     *
     * @param conceptList the concept list
     * @return the concept map
     */
    private <T extends PfConcept> Map<PfConceptKey, T> asConceptMap(List<T> conceptList) {
        Map<PfConceptKey, T> conceptMap = new LinkedHashMap<>();
        for (T concept : conceptList) {
            conceptMap.put((PfConceptKey) concept.getKey(), concept);
        }

        return conceptMap;
    }
}
