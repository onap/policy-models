/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
 *  Modifications Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.models.tosca.authorative.provider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.NonNull;

import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.dao.PfDao;
import org.onap.policy.models.tosca.authorative.concepts.ToscaEntity;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicy;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyFilter;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyType;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyTypeFilter;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaServiceTemplate;
import org.onap.policy.models.tosca.simple.provider.SimpleToscaProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides the provision of information on TOSCA concepts in the database to callers.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class AuthorativeToscaProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthorativeToscaProvider.class);

    /**
     * Get policy types.
     *
     * @param dao the DAO to use to access the database
     * @param name the name of the policy type to get.
     * @param version the version of the policy type to get.
     * @return the policy types found
     * @throws PfModelException on errors getting policy types
     */
    public ToscaServiceTemplate getPolicyTypes(@NonNull final PfDao dao, final String name, final String version)
            throws PfModelException {

        LOGGER.debug("->getPolicyTypes: name={}, version={}", name, version);

        ToscaServiceTemplate serviceTemplate =
                new SimpleToscaProvider().getPolicyTypes(dao, name, version).toAuthorative();

        LOGGER.debug("<-getPolicyTypes: name={}, version={}, serviceTemplate={}", name, version, serviceTemplate);
        return serviceTemplate;
    }

    /**
     * Get policy types.
     *
     * @param dao the DAO to use to access the database
     * @param name the name of the policy type to get, set to null to get all policy types
     * @param version the version of the policy type to get, set to null to get all versions
     * @return the policy types found
     * @throws PfModelException on errors getting policy types
     */
    public List<ToscaPolicyType> getPolicyTypeList(@NonNull final PfDao dao, final String name, final String version)
            throws PfModelException {

        LOGGER.debug("->getPolicyTypeList: name={}, version={}", name, version);

        List<ToscaPolicyType> policyTypeList = asConceptList(
                new SimpleToscaProvider().getPolicyTypes(dao, name, version).toAuthorative().getPolicyTypes());

        LOGGER.debug("<-getPolicyTypeList: name={}, version={}, policyTypeList={}", name, version, policyTypeList);
        return policyTypeList;
    }

    /**
     * Get filtered policy types.
     *
     * @param dao the DAO to use to access the database
     * @param filter the filter for the policy types to get
     * @return the policy types found
     * @throws PfModelException on errors getting policy types
     */
    public ToscaServiceTemplate getFilteredPolicyTypes(@NonNull final PfDao dao,
            @NonNull final ToscaPolicyTypeFilter filter) throws PfModelException {

        LOGGER.debug("->getFilteredPolicyTypes: filter={}", filter);

        ToscaServiceTemplate serviceTemplate =
                new SimpleToscaProvider().getPolicyTypes(dao, null, null).toAuthorative();

        List<ToscaPolicyType> filteredPolicyTypes = asConceptList(serviceTemplate.getPolicyTypes());
        filteredPolicyTypes = filter.filter(filteredPolicyTypes);

        serviceTemplate.setPolicyTypes(asConceptMap(filteredPolicyTypes));

        LOGGER.debug("<-getFilteredPolicyTypes: filter={}, serviceTemplate={}", filter, serviceTemplate);
        return serviceTemplate;
    }

    /**
     * Get filtered policy types.
     *
     * @param dao the DAO to use to access the database
     * @param filter the filter for the policy types to get
     * @return the policy types found
     * @throws PfModelException on errors getting policy types
     */
    public List<ToscaPolicyType> getFilteredPolicyTypeList(@NonNull final PfDao dao,
            @NonNull final ToscaPolicyTypeFilter filter) throws PfModelException {

        LOGGER.debug("->getFilteredPolicyTypeList: filter={}", filter);

        List<ToscaPolicyType> filteredPolicyTypeList = filter.filter(getPolicyTypeList(dao, null, null));

        LOGGER.debug("<-getFilteredPolicyTypeList: filter={}, filteredPolicyTypeList={}", filter,
                filteredPolicyTypeList);
        return filteredPolicyTypeList;
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

        LOGGER.debug("->createPolicyTypes: serviceTemplate={}", serviceTemplate);

        ToscaServiceTemplate createdServiceTempalate = new SimpleToscaProvider()
                .createPolicyTypes(dao, new JpaToscaServiceTemplate(serviceTemplate)).toAuthorative();

        LOGGER.debug("<-createPolicyTypes: createdServiceTempalate={}", createdServiceTempalate);
        return createdServiceTempalate;
    }

    /**
     * Update policy types.
     *
     * @param dao the DAO to use to access the database
     * @param serviceTemplate the service template containing the definition of the policy types to be modified
     * @return the TOSCA service template containing the modified policy types
     * @throws PfModelException on errors updating policy types
     */
    public ToscaServiceTemplate updatePolicyTypes(@NonNull final PfDao dao,
            @NonNull final ToscaServiceTemplate serviceTemplate) throws PfModelException {

        LOGGER.debug("->updatePolicyTypes: serviceTempalate={}", serviceTemplate);

        ToscaServiceTemplate updatedServiceTempalate = new SimpleToscaProvider()
                .updatePolicyTypes(dao, new JpaToscaServiceTemplate(serviceTemplate)).toAuthorative();

        LOGGER.debug("<-updatePolicyTypes: updatedServiceTempalate={}", updatedServiceTempalate);
        return updatedServiceTempalate;
    }

    /**
     * Delete policy type.
     *
     * @param dao the DAO to use to access the database
     * @param name the name of the policy type to delete.
     * @param version the version of the policy type to delete.
     * @return the TOSCA service template containing the policy type that was deleted
     * @throws PfModelException on errors deleting policy types
     */
    public ToscaServiceTemplate deletePolicyType(@NonNull final PfDao dao, @NonNull final String name,
            @NonNull final String version) throws PfModelException {

        LOGGER.debug("->deletePolicyType: name={}, version={}", name, version);

        ToscaServiceTemplate deletedServiceTempalate =
                new SimpleToscaProvider().deletePolicyType(dao, new PfConceptKey(name, version)).toAuthorative();

        LOGGER.debug("<-deletePolicyType: name={}, version={}, deletedServiceTempalate={}", name, version,
                deletedServiceTempalate);
        return deletedServiceTempalate;
    }

    /**
     * Get policies.
     *
     * @param dao the DAO to use to access the database
     * @param name the name of the policy to get.
     * @param version the version of the policy to get.
     * @return the policies found
     * @throws PfModelException on errors getting policies
     */
    public ToscaServiceTemplate getPolicies(@NonNull final PfDao dao, final String name, final String version)
            throws PfModelException {
        LOGGER.debug("->getPolicies: name={}, version={}", name, version);

        ToscaServiceTemplate gotServiceTempalate =
                new SimpleToscaProvider().getPolicies(dao, name, version).toAuthorative();

        LOGGER.debug("<-getPolicies: name={}, version={}, gotServiceTempalate={}", name, version, gotServiceTempalate);
        return gotServiceTempalate;
    }

    /**
     * Get policies.
     *
     * @param dao the DAO to use to access the database
     * @param name the name of the policy to get, null to get all policies
     * @param version the version of the policy to get, null to get all versions of a policy
     * @return the policies found
     * @throws PfModelException on errors getting policies
     */
    public List<ToscaPolicy> getPolicyList(@NonNull final PfDao dao, final String name, final String version)
            throws PfModelException {
        LOGGER.debug("->getPolicyList: name={}, version={}", name, version);

        List<ToscaPolicy> policyList = asConceptList(new SimpleToscaProvider().getPolicies(dao, name, version)
                .toAuthorative().getToscaTopologyTemplate().getPolicies());

        LOGGER.debug("<-getPolicyList: name={}, version={}, policyTypeList={}", name, version, policyList);
        return policyList;
    }

    /**
     * Get filtered policies.
     *
     * @param dao the DAO to use to access the database
     * @param filter the filter for the policies to get
     * @return the policies found
     * @throws PfModelException on errors getting policies
     */
    public ToscaServiceTemplate getFilteredPolicies(@NonNull final PfDao dao, @NonNull final ToscaPolicyFilter filter)
            throws PfModelException {

        LOGGER.debug("->getFilteredPolicies: filter={}", filter);
        String version = ToscaPolicyFilter.LATEST_VERSION.equals(filter.getVersion()) ? null : filter.getVersion();

        ToscaServiceTemplate serviceTemplate =
                new SimpleToscaProvider().getPolicies(dao, filter.getName(), version).toAuthorative();

        List<ToscaPolicy> filteredPolicies = asConceptList(serviceTemplate.getToscaTopologyTemplate().getPolicies());
        filteredPolicies = filter.filter(filteredPolicies);

        serviceTemplate.getToscaTopologyTemplate().setPolicies(asConceptMap(filteredPolicies));

        LOGGER.debug("<-getFilteredPolicies: filter={}, serviceTemplate={}", filter, serviceTemplate);
        return serviceTemplate;
    }

    /**
     * Get filtered policies.
     *
     * @param dao the DAO to use to access the database
     * @param filter the filter for the policies to get
     * @return the policies found
     * @throws PfModelException on errors getting policies
     */
    public List<ToscaPolicy> getFilteredPolicyList(@NonNull final PfDao dao, @NonNull final ToscaPolicyFilter filter)
            throws PfModelException {

        LOGGER.debug("->getFilteredPolicyList: filter={}", filter);
        String version = ToscaPolicyFilter.LATEST_VERSION.equals(filter.getVersion()) ? null : filter.getVersion();

        List<ToscaPolicy> policyList = filter.filter(getPolicyList(dao, filter.getName(), version));

        LOGGER.debug("<-getFilteredPolicyList: filter={}, policyList={}", filter, policyList);
        return policyList;
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

        LOGGER.debug("->createPolicies: serviceTempalate={}", serviceTemplate);

        ToscaServiceTemplate createdServiceTempalate = new SimpleToscaProvider()
                .createPolicies(dao, new JpaToscaServiceTemplate(serviceTemplate)).toAuthorative();

        LOGGER.debug("<-createPolicies: createdServiceTempalate={}", createdServiceTempalate);
        return createdServiceTempalate;
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

        LOGGER.debug("->updatePolicies: serviceTempalate={}", serviceTemplate);

        ToscaServiceTemplate updatedServiceTempalate = new SimpleToscaProvider()
                .updatePolicies(dao, new JpaToscaServiceTemplate(serviceTemplate)).toAuthorative();

        LOGGER.debug("<-updatePolicies: updatedServiceTempalate={}", updatedServiceTempalate);
        return updatedServiceTempalate;
    }

    /**
     * Delete policy.
     *
     * @param dao the DAO to use to access the database
     * @param name the name of the policy to delete.
     * @param version the version of the policy to delete.
     * @return the TOSCA service template containing the policy that weas deleted
     * @throws PfModelException on errors deleting policies
     */
    public ToscaServiceTemplate deletePolicy(@NonNull final PfDao dao, @NonNull final String name,
            @NonNull final String version) throws PfModelException {

        LOGGER.debug("->deletePolicy: name={}, version={}", name, version);

        ToscaServiceTemplate deletedServiceTempalate =
                new SimpleToscaProvider().deletePolicy(dao, new PfConceptKey(name, version)).toAuthorative();

        LOGGER.debug("<-deletePolicy: name={}, version={}, deletedServiceTempalate={}", name, version,
                deletedServiceTempalate);
        return deletedServiceTempalate;
    }

    /**
     * Return the contents of a list of maps as a plain list.
     *
     * @param listOfMaps the list of maps
     * @return the plain list
     */
    private <T> List<T> asConceptList(final List<Map<String, T>> listOfMaps) {
        List<T> returnList = new ArrayList<>();
        for (Map<String, T> conceptMap : listOfMaps) {
            for (T concept : conceptMap.values()) {
                returnList.add(concept);
            }
        }

        return returnList;
    }

    /**
     * Return the contents of a list of concepts as a list of maps of concepts.
     *
     * @param comceptList the concept list
     * @return the concept map
     */
    private <T extends ToscaEntity> List<Map<String, T>> asConceptMap(List<T> conceptList) {
        Map<String, T> conceptMap = new LinkedHashMap<>();
        for (T concept : conceptList) {
            conceptMap.put(concept.getName(), concept);
        }

        return Collections.singletonList(conceptMap);
    }
}
