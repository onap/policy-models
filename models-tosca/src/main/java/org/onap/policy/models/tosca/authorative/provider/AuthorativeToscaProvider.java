/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2023 Nordix Foundation.
 *  Modifications Copyright (C) 2019, 2021 AT&T Intellectual Property. All rights reserved.
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

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.NonNull;
import org.apache.commons.collections4.CollectionUtils;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.base.PfModelRuntimeException;
import org.onap.policy.models.dao.PfDao;
import org.onap.policy.models.tosca.authorative.concepts.ToscaEntity;
import org.onap.policy.models.tosca.authorative.concepts.ToscaEntityFilter;
import org.onap.policy.models.tosca.authorative.concepts.ToscaEntityKey;
import org.onap.policy.models.tosca.authorative.concepts.ToscaNodeTemplate;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicy;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyType;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;
import org.onap.policy.models.tosca.authorative.concepts.ToscaTypedEntityFilter;
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

    private static final Object providerLockObject = "providerLockObject";

    /**
     * Get service templates.
     *
     * @param dao the DAO to use to access the database
     * @param name the name of the service template to get.
     * @param version the version of the service template to get.
     * @return the service templates found
     * @throws PfModelException on errors getting service templates
     */
    public List<ToscaServiceTemplate> getServiceTemplateList(PfDao dao, String name, String version)
            throws PfModelException {

        synchronized (providerLockObject) {
            LOGGER.debug("->getServiceTemplateList: name={}, version={}", name, version);

            List<ToscaServiceTemplate> serviceTemplateList = new ArrayList<>();

            try {
                ToscaServiceTemplate serviceTemplate =
                        new SimpleToscaProvider().getServiceTemplate(dao).toAuthorative();
                serviceTemplateList.add(serviceTemplate);
            } catch (PfModelRuntimeException pfme) {
                return handlePfModelRuntimeException(pfme);
            }

            LOGGER.debug("<-getServiceTemplateList: name={}, version={}, serviceTemplateList={}", name, version,
                    serviceTemplateList);
            return serviceTemplateList;
        }
    }

    /**
     * Get filtered service templates.
     *
     * @param pfDao the DAO to use to access the database
     * @param filter the filter for the service templates to get
     * @return the service templates found
     * @throws PfModelException on errors getting service templates
     */
    public List<ToscaServiceTemplate> getFilteredServiceTemplateList(PfDao pfDao,
            @NonNull ToscaEntityFilter<ToscaServiceTemplate> filter) throws PfModelException {

        LOGGER.debug("->getFilteredServiceTemplateList: filter={}", filter);

        List<ToscaServiceTemplate> filteredServiceTemplateList =
                filter.filter(getServiceTemplateList(pfDao, null, null));

        LOGGER.debug("<-getFilteredServiceTemplateList: filter={}, filteredServiceTemplateList={}", filter,
                filteredServiceTemplateList);

        return filteredServiceTemplateList;
    }

    /**
     * Create a service template.
     *
     * @param dao the DAO to use to access the database
     * @param serviceTemplate the service template to be created.
     * @return the TOSCA service template that was created
     * @throws PfModelException on errors creating the service template
     */
    public ToscaServiceTemplate createServiceTemplate(@NonNull final PfDao dao,
            @NonNull final ToscaServiceTemplate serviceTemplate) throws PfModelException {

        synchronized (providerLockObject) {
            LOGGER.debug("->createServiceTemplate: serviceTemplate={}", serviceTemplate);

            ToscaServiceTemplate createdServiceTemplate = new SimpleToscaProvider()
                    .appendToServiceTemplate(dao, new JpaToscaServiceTemplate(serviceTemplate)).toAuthorative();

            LOGGER.debug("<-createServiceTemplate: createdServiceTemplate={}", createdServiceTemplate);
            return createdServiceTemplate;
        }
    }

    /**
     * Update a service template.
     *
     * @param dao the DAO to use to access the database
     * @param serviceTemplate the service template to be updated.
     * @return the TOSCA service template that was updated
     * @throws PfModelException on errors updating the service template
     */
    public ToscaServiceTemplate updateServiceTemplate(@NonNull final PfDao dao,
            @NonNull final ToscaServiceTemplate serviceTemplate) throws PfModelException {

        synchronized (providerLockObject) {
            LOGGER.debug("->updateServiceTemplate: serviceTemplate={}", serviceTemplate);

            ToscaServiceTemplate updatedServiceTemplate = new SimpleToscaProvider()
                    .appendToServiceTemplate(dao, new JpaToscaServiceTemplate(serviceTemplate)).toAuthorative();

            LOGGER.debug("<-updateServiceTemplate: updatedServiceTemplate={}", updatedServiceTemplate);
            return updatedServiceTemplate;
        }
    }

    /**
     * Delete a service template.
     *
     * @param dao the DAO to use to access the database
     * @param name the name of the service template to delete.
     * @param version the version of the service template to delete.
     * @return the TOSCA service template that was deleted
     * @throws PfModelException on errors deleting the control loop
     */
    public ToscaServiceTemplate deleteServiceTemplate(@NonNull final PfDao dao, @NonNull final String name,
            @NonNull final String version) throws PfModelException {

        synchronized (providerLockObject) {
            LOGGER.debug("->deleteServiceTemplate: name={}, version={}", name, version);

            ToscaServiceTemplate deletedServiceTemplate =
                    new SimpleToscaProvider().deleteServiceTemplate(dao).toAuthorative();

            LOGGER.debug("<-deleteServiceTemplate: name={}, version={}, deletedServiceTemplate={}", name, version,
                    deletedServiceTemplate);
            return deletedServiceTemplate;
        }
    }

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

        synchronized (providerLockObject) {
            LOGGER.debug("->getPolicyTypes: name={}, version={}", name, version);

            JpaToscaServiceTemplate jpaServiceTemplate = new SimpleToscaProvider().getPolicyTypes(dao, name, version);

            ToscaServiceTemplate serviceTemplate = jpaServiceTemplate.toAuthorative();

            LOGGER.debug("<-getPolicyTypes: name={}, version={}, serviceTemplate={}", name, version, serviceTemplate);
            return serviceTemplate;
        }
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

        synchronized (providerLockObject) {
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
            @NonNull final ToscaEntityFilter<ToscaPolicyType> filter) throws PfModelException {

        synchronized (providerLockObject) {
            LOGGER.debug("->getFilteredPolicyTypes: filter={}", filter);
            var simpleToscaProvider = new SimpleToscaProvider();

            final JpaToscaServiceTemplate dbServiceTemplate = simpleToscaProvider.getPolicyTypes(dao, null, null);

            List<ToscaPolicyType> filteredPolicyTypes = dbServiceTemplate.getPolicyTypes().toAuthorativeList();
            filteredPolicyTypes = filter.filter(filteredPolicyTypes);

            if (CollectionUtils.isEmpty(filteredPolicyTypes)) {
                throw new PfModelRuntimeException(Response.Status.NOT_FOUND,
                        "policy types for filter " + filter.toString() + " do not exist");
            }

            var filteredServiceTemplate = new JpaToscaServiceTemplate();

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
            @NonNull final ToscaEntityFilter<ToscaPolicyType> filter) throws PfModelException {

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

        synchronized (providerLockObject) {
            LOGGER.debug("->createPolicyTypes: serviceTemplate={}", serviceTemplate);

            ToscaServiceTemplate createdServiceTemplate = new SimpleToscaProvider()
                    .createPolicyTypes(dao, new JpaToscaServiceTemplate(serviceTemplate)).toAuthorative();

            LOGGER.debug("<-createPolicyTypes: createdServiceTemplate={}", createdServiceTemplate);
            return createdServiceTemplate;
        }
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

        synchronized (providerLockObject) {
            LOGGER.debug("->updatePolicyTypes: serviceTemplate={}", serviceTemplate);

            ToscaServiceTemplate updatedServiceTemplate = new SimpleToscaProvider()
                    .updatePolicyTypes(dao, new JpaToscaServiceTemplate(serviceTemplate)).toAuthorative();

            LOGGER.debug("<-updatePolicyTypes: updatedServiceTemplate={}", updatedServiceTemplate);
            return updatedServiceTemplate;
        }
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

        synchronized (providerLockObject) {
            LOGGER.debug("->deletePolicyType: name={}, version={}", name, version);

            ToscaServiceTemplate deletedServiceTemplate =
                    new SimpleToscaProvider().deletePolicyType(dao, new PfConceptKey(name, version)).toAuthorative();

            LOGGER.debug("<-deletePolicyType: name={}, version={}, deletedServiceTemplate={}", name, version,
                    deletedServiceTemplate);
            return deletedServiceTemplate;
        }
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

        synchronized (providerLockObject) {
            LOGGER.debug("->getPolicies: name={}, version={}", name, version);

            ToscaServiceTemplate gotServiceTemplate =
                    new SimpleToscaProvider().getPolicies(dao, name, version).toAuthorative();

            LOGGER.debug("<-getPolicies: name={}, version={}, gotServiceTemplate={}", name, version,
                    gotServiceTemplate);
            return gotServiceTemplate;
        }
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

        synchronized (providerLockObject) {
            LOGGER.debug("->getPolicyList: name={}, version={}", name, version);

            List<ToscaPolicy> policyList;

            try {
                policyList = asConceptList(new SimpleToscaProvider().getPolicies(dao, name, version).toAuthorative()
                        .getToscaTopologyTemplate().getPolicies());
            } catch (PfModelRuntimeException pfme) {
                return handlePfModelRuntimeException(pfme);
            }

            LOGGER.debug("<-getPolicyList: name={}, version={}, policyList={}", name, version, policyList);
            return policyList;
        }
    }

    /**
     * Get filtered policies.
     *
     * @param dao the DAO to use to access the database
     * @param filter the filter for the policies to get
     * @return the policies found
     * @throws PfModelException on errors getting policies
     */
    public ToscaServiceTemplate getFilteredPolicies(@NonNull final PfDao dao,
            @NonNull final ToscaTypedEntityFilter<ToscaPolicy> filter) throws PfModelException {

        synchronized (providerLockObject) {
            LOGGER.debug("->getFilteredPolicies: filter={}", filter);
            String version =
                    ToscaTypedEntityFilter.LATEST_VERSION.equals(filter.getVersion()) ? null : filter.getVersion();

            var simpleToscaProvider = new SimpleToscaProvider();
            final JpaToscaServiceTemplate dbServiceTemplate =
                    simpleToscaProvider.getPolicies(dao, filter.getName(), version);

            List<ToscaPolicy> filteredPolicies =
                    dbServiceTemplate.getTopologyTemplate().getPolicies().toAuthorativeList();
            filteredPolicies = filter.filter(filteredPolicies);

            if (CollectionUtils.isEmpty(filteredPolicies)) {
                throw new PfModelRuntimeException(Response.Status.NOT_FOUND,
                        "policies for filter " + filter.toString() + " do not exist");
            }

            var filteredServiceTemplate = new JpaToscaServiceTemplate();

            for (ToscaPolicy policy : filteredPolicies) {
                JpaToscaServiceTemplate cascadedServiceTemplate = simpleToscaProvider
                        .getCascadedPolicies(dbServiceTemplate, policy.getName(), policy.getVersion());

                filteredServiceTemplate =
                        ToscaServiceTemplateUtils.addFragment(filteredServiceTemplate, cascadedServiceTemplate);
            }

            ToscaServiceTemplate returnServiceTemplate = filteredServiceTemplate.toAuthorative();

            LOGGER.debug("<-getFilteredPolicies: filter={}, serviceTemplate={}", filter, returnServiceTemplate);
            return returnServiceTemplate;
        }
    }

    /**
     * Get filtered policies.
     *
     * @param dao the DAO to use to access the database
     * @param filter the filter for the policies to get
     * @return the policies found
     * @throws PfModelException on errors getting policies
     */
    public List<ToscaPolicy> getFilteredPolicyList(@NonNull final PfDao dao,
            @NonNull final ToscaTypedEntityFilter<ToscaPolicy> filter) throws PfModelException {

        LOGGER.debug("->getFilteredPolicyList: filter={}", filter);
        String version = ToscaTypedEntityFilter.LATEST_VERSION.equals(filter.getVersion()) ? null : filter.getVersion();

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

        synchronized (providerLockObject) {
            LOGGER.debug("->createPolicies: serviceTemplate={}", serviceTemplate);

            ToscaServiceTemplate createdServiceTemplate = new SimpleToscaProvider()
                    .createPolicies(dao, new JpaToscaServiceTemplate(serviceTemplate)).toAuthorative();

            LOGGER.debug("<-createPolicies: createdServiceTemplate={}", createdServiceTemplate);
            return createdServiceTemplate;
        }
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

        synchronized (providerLockObject) {
            LOGGER.debug("->updatePolicies: serviceTemplate={}", serviceTemplate);

            ToscaServiceTemplate updatedServiceTemplate = new SimpleToscaProvider()
                    .updatePolicies(dao, new JpaToscaServiceTemplate(serviceTemplate)).toAuthorative();

            LOGGER.debug("<-updatePolicies: updatedServiceTemplate={}", updatedServiceTemplate);
            return updatedServiceTemplate;
        }
    }

    /**
     * Delete policy.
     *
     * @param dao the DAO to use to access the database
     * @param name the name of the policy to delete.
     * @param version the version of the policy to delete.
     * @return the TOSCA service template containing the policy that was deleted
     * @throws PfModelException on errors deleting policies
     */
    public ToscaServiceTemplate deletePolicy(@NonNull final PfDao dao, @NonNull final String name,
            @NonNull final String version) throws PfModelException {

        synchronized (providerLockObject) {
            LOGGER.debug("->deletePolicy: name={}, version={}", name, version);

            ToscaServiceTemplate deletedServiceTemplate =
                    new SimpleToscaProvider().deletePolicy(dao, new PfConceptKey(name, version)).toAuthorative();

            LOGGER.debug("<-deletePolicy: name={}, version={}, deletedServiceTemplate={}", name, version,
                    deletedServiceTemplate);
            return deletedServiceTemplate;
        }
    }

    /**
     * Create tosca node templates.
     *
     * @param dao the DAO to use to access the database
     * @param toscaServiceTemplate the template with node templates entities to be created.
     * @return the toscaServiceTemplate with node templates that were created
     * @throws PfModelException on errors creating node templates
     */
    public ToscaServiceTemplate createToscaNodeTemplates(@NonNull final PfDao dao,
                                                         @NonNull final ToscaServiceTemplate toscaServiceTemplate)
        throws PfModelException {
        LOGGER.debug("createToscaNodeTemplates ={}", toscaServiceTemplate);
        ToscaServiceTemplate createdServiceTemplate;

        synchronized (providerLockObject) {
            createdServiceTemplate = new SimpleToscaProvider()
                .createToscaNodeTemplates(dao, new JpaToscaServiceTemplate(toscaServiceTemplate)).toAuthorative();
        }

        LOGGER.debug("<-createToscaNodeTemplates: createdServiceTemplate={}", createdServiceTemplate);
        return createdServiceTemplate;
    }

    /**
     * Update tosca node templates.
     *
     * @param dao the DAO to use to access the database
     * @param serviceTemplate the service template containing the definitions of the nodeTemplates to be updated.
     * @return the TOSCA service template containing the nodeTemplates that were updated
     * @throws PfModelRuntimeException on errors updating node templates
     */
    public ToscaServiceTemplate updateToscaNodeTemplates(@NonNull final PfDao dao,
                                                         @NonNull final ToscaServiceTemplate serviceTemplate)
        throws PfModelException {
        LOGGER.debug("->updateToscaNodeTemplates: serviceTemplate={}", serviceTemplate);
        ToscaServiceTemplate updatedServiceTemplate;

        synchronized (providerLockObject) {
            updatedServiceTemplate = new SimpleToscaProvider()
                .updateToscaNodeTemplates(dao, new JpaToscaServiceTemplate(serviceTemplate)).toAuthorative();
        }

        LOGGER.debug("<-updateToscaNodeTemplates: updatedServiceTemplate={}", updatedServiceTemplate);
        return updatedServiceTemplate;
    }


    /**
     * Delete a tosca node template.
     *
     * @param dao the DAO to use to access the database
     * @param name the name of the node template to delete.
     * @param version the version of the node template to delete.
     * @return the TOSCA service template containing the node template that was deleted
     * @throws PfModelException on errors deleting node template
     */
    public ToscaServiceTemplate deleteToscaNodeTemplate(@NonNull final PfDao dao, @NonNull final String name,
                                                        @NonNull final String version) throws PfModelException {
        LOGGER.debug("->deleteToscaNodeTemplate: name={}, version={}", name, version);
        ToscaServiceTemplate deletedServiceTemplate;

        synchronized (providerLockObject) {
            deletedServiceTemplate =
                new SimpleToscaProvider().deleteToscaNodeTemplate(dao, new PfConceptKey(name, version)).toAuthorative();
        }
        LOGGER.debug("<-deleteToscaNodeTemplate: name={}, version={}, deletedServiceTemplate={}", name, version,
            deletedServiceTemplate);
        return deletedServiceTemplate;
    }

    /**
     * Get node template metadataSet.
     *
     * @param dao the DAO to use to access the database
     * @param name the name of the metadataSet to get, null to get all metadataSets
     * @param version the version of the metadataSet to get, null to get all versions of a metadataSets
     * @return the metadataSets found
     * @throws PfModelException on errors getting policy metadataSet
     */
    public List<Map<ToscaEntityKey, Map<String, Object>>> getNodeTemplateMetadataSet(
        @NonNull final PfDao dao, final String name, final String version)
        throws PfModelException {
        LOGGER.debug("->getNodeTemplateMetadataSet: name={}, version={}", name, version);
        List<Map<ToscaEntityKey, Map<String, Object>>> metadataSets;

        synchronized (providerLockObject) {
            metadataSets = new SimpleToscaProvider().getNodeTemplateMetadata(dao, name, version);
        }
        LOGGER.debug("<-getNodeTemplateMetadataSet: name={}, version={}, metadataSets={}", name, version,
            metadataSets);
        return metadataSets;
    }

    /**
     * Get tosca node templates.
     *
     * @param dao the DAO to use to access the database
     * @param name the name of the node template to get, null to get all node templates
     * @param version the version of the node template to get, null to get all versions of node template
     * @return the node templates found
     * @throws PfModelException on errors getting tosca node templates
     */
    public List<ToscaNodeTemplate> getToscaNodeTemplate(
        @NonNull final PfDao dao, final String name, final String version)
        throws PfModelException {
        LOGGER.debug("->getNodeTemplate: name={}, version={}", name, version);
        List<ToscaNodeTemplate> nodeTemplates = new ArrayList<>();

        synchronized (providerLockObject) {
            new SimpleToscaProvider().getToscaNodeTemplates(dao, name, version)
                .getConceptMap().forEach((key, value) -> nodeTemplates.add(value.toAuthorative()));
        }

        LOGGER.debug("<-getNodeTemplate: name={}, version={}, nodeTemplates={}", name, version,
            nodeTemplates);
        return nodeTemplates;
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
            returnList.addAll(conceptMap.values());
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
