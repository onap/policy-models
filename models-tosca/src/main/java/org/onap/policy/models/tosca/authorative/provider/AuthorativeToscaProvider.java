/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2020 Nordix Foundation.
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
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import lombok.NonNull;

import org.apache.commons.collections4.CollectionUtils;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.base.PfModelRuntimeException;
import org.onap.policy.models.dao.PfDao;
import org.onap.policy.models.tosca.authorative.concepts.ToscaEntity;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicy;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyFilter;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyType;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyTypeFilter;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaServiceTemplate;
import org.onap.policy.models.tosca.simple.provider.SimpleToscaProvider;
import org.onap.policy.models.tosca.utils.ToscaServiceTemplateUtils;
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

        JpaToscaServiceTemplate jpaServiceTemplate = new SimpleToscaProvider().getPolicyTypes(dao, name, version);

        ToscaServiceTemplate serviceTemplate = jpaServiceTemplate.toAuthorative();

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

        List<ToscaPolicyType> policyTypeList;

        try {
            policyTypeList = new ArrayList<>(new SimpleToscaProvider().getPolicyTypes(dao, name, version)
                    .toAuthorative().getPolicyTypes().values());
        } catch (PfModelRuntimeException pfme) {
            return handlePfModelRuntimeException(pfme);
        }

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
        SimpleToscaProvider simpleToscaProvider = new SimpleToscaProvider();

        final JpaToscaServiceTemplate dbServiceTemplate = simpleToscaProvider.getPolicyTypes(dao, null, null);

        List<ToscaPolicyType> filteredPolicyTypes =
                new ArrayList<>(dbServiceTemplate.toAuthorative().getPolicyTypes().values());
        filteredPolicyTypes = filter.filter(filteredPolicyTypes);

        if (CollectionUtils.isEmpty(filteredPolicyTypes)) {
            throw new PfModelRuntimeException(Response.Status.NOT_FOUND,
                    "policy types for filter " + filter.toString() + " do not exist");
        }

        JpaToscaServiceTemplate filteredServiceTemplate = new JpaToscaServiceTemplate();

        for (ToscaPolicyType policyType : filteredPolicyTypes) {
            JpaToscaServiceTemplate cascadedServiceTemplate = simpleToscaProvider
                    .getCascadedPolicyTypes(dbServiceTemplate, policyType.getName(), policyType.getVersion());

            filteredServiceTemplate =
                    ToscaServiceTemplateUtils.addFragment(filteredServiceTemplate, cascadedServiceTemplate);
        }

        ToscaServiceTemplate returnServiceTemplate = filteredServiceTemplate.toAuthorative();

        LOGGER.debug("<-getFilteredPolicyTypes: filter={}, serviceTemplate={}", filter, returnServiceTemplate);
        return returnServiceTemplate;
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

        List<ToscaPolicy> policyList;

        try {
            policyList = asConceptList(new SimpleToscaProvider().getPolicies(dao, name, version).toAuthorative()
                    .getToscaTopologyTemplate().getPolicies());
        } catch (PfModelRuntimeException pfme) {
            return handlePfModelRuntimeException(pfme);
        }

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

        SimpleToscaProvider simpleToscaProvider = new SimpleToscaProvider();
        final JpaToscaServiceTemplate dbServiceTemplate =
                simpleToscaProvider.getPolicies(dao, filter.getName(), version);

        List<ToscaPolicy> filteredPolicies =
                asConceptList(dbServiceTemplate.toAuthorative().getToscaTopologyTemplate().getPolicies());
        filteredPolicies = filter.filter(filteredPolicies);

        if (CollectionUtils.isEmpty(filteredPolicies)) {
            throw new PfModelRuntimeException(Response.Status.NOT_FOUND,
                    "policies for filter " + filter.toString() + " do not exist");
        }

        JpaToscaServiceTemplate filteredServiceTemplate = new JpaToscaServiceTemplate();

        for (ToscaPolicy policy : filteredPolicies) {
            JpaToscaServiceTemplate cascadedServiceTemplate =
                    simpleToscaProvider.getCascadedPolicies(dbServiceTemplate, policy.getName(), policy.getVersion());

            filteredServiceTemplate =
                    ToscaServiceTemplateUtils.addFragment(filteredServiceTemplate, cascadedServiceTemplate);
        }

        ToscaServiceTemplate returnServiceTemplate = filteredServiceTemplate.toAuthorative();

        LOGGER.debug("<-getFilteredPolicies: filter={}, serviceTemplate={}", filter, returnServiceTemplate);
        return returnServiceTemplate;
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
     * Handle a PfModelRuntimeException on a list call.
     *
     * @param pfme the model exception
     * @return an empty list on 404
     */
    private <T extends ToscaEntity> List<T> handlePfModelRuntimeException(final PfModelRuntimeException pfme) {
        if (Status.NOT_FOUND.equals(pfme.getErrorResponse().getResponseCode())) {
            LOGGER.trace("request did not find any results", pfme);
            return Collections.emptyList();
        } else {
            throw pfme;
        }
    }
}
