/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2020 Nordix Foundation.
 *  Modifications Copyright (C) 2020-2021 AT&T Intellectual Property. All rights reserved.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.ws.rs.core.Response;
import lombok.NonNull;
import org.apache.commons.collections4.CollectionUtils;
import org.onap.policy.common.parameters.BeanValidationResult;
import org.onap.policy.models.base.PfConcept;
import org.onap.policy.models.base.PfConceptFilter;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfKey;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.base.PfModelRuntimeException;
import org.onap.policy.models.dao.PfDao;
import org.onap.policy.models.tosca.authorative.concepts.ToscaEntity;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaDataType;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaDataTypes;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaEntityType;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaPolicies;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaPolicy;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaPolicyType;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaPolicyTypes;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaServiceTemplate;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaTopologyTemplate;
import org.onap.policy.models.tosca.utils.ToscaServiceTemplateUtils;
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

    // Recurring string constants
    private static final String DATA_TYPE = "data type ";
    private static final String POLICY_TYPE = "policy type ";
    private static final String SERVICE_TEMPLATE_NOT_FOUND_IN_DATABASE = "service template not found in database";
    private static final String DO_NOT_EXIST = " do not exist";
    private static final String NOT_FOUND = " not found";

    /**
     * Get Service Template.
     *
     * @param dao the DAO to use to access the database
     * @return the service template
     * @throws PfModelException on errors getting the service template
     */
    public JpaToscaServiceTemplate getServiceTemplate(@NonNull final PfDao dao) throws PfModelException {
        LOGGER.debug("->getServiceTemplate");

        JpaToscaServiceTemplate serviceTemplate = new SimpleToscaServiceTemplateProvider().read(dao);
        if (serviceTemplate == null) {
            throw new PfModelRuntimeException(Response.Status.NOT_FOUND, SERVICE_TEMPLATE_NOT_FOUND_IN_DATABASE);
        }

        LOGGER.debug("<-getServiceTemplate: serviceTemplate={}", serviceTemplate);
        return serviceTemplate;
    }

    /**
     * Append a service template fragment to the service template in the database.
     *
     * @param dao the DAO to use to access the database
     * @param incomingServiceTemplateFragment the service template containing the definition of the entities to be
     *        created
     * @return the TOSCA service template in the database after the operation
     * @throws PfModelException on errors appending a service template to the template in the database
     */
    public JpaToscaServiceTemplate appendToServiceTemplate(@NonNull final PfDao dao,
        @NonNull final JpaToscaServiceTemplate incomingServiceTemplateFragment) throws PfModelException {
        LOGGER.debug("->appendServiceTemplateFragment: incomingServiceTemplateFragment={}",
            incomingServiceTemplateFragment);

        JpaToscaServiceTemplate dbServiceTemplate = new SimpleToscaServiceTemplateProvider().read(dao);

        JpaToscaServiceTemplate serviceTemplateToWrite;
        if (dbServiceTemplate == null) {
            serviceTemplateToWrite = incomingServiceTemplateFragment;
        } else {
            serviceTemplateToWrite =
                ToscaServiceTemplateUtils.addFragment(dbServiceTemplate, incomingServiceTemplateFragment);
        }

        BeanValidationResult result = serviceTemplateToWrite.validate("service template");
        if (!result.isValid()) {
            throw new PfModelRuntimeException(Response.Status.NOT_ACCEPTABLE, result.getResult());
        }

        new SimpleToscaServiceTemplateProvider().write(dao, serviceTemplateToWrite);

        LOGGER.debug("<-appendServiceTemplateFragment: returnServiceTempalate={}", serviceTemplateToWrite);
        return serviceTemplateToWrite;
    }

    /**
     * Delete service template.
     *
     * @param dao the DAO to use to access the database
     * @return the TOSCA service template that was deleted
     * @throws PfModelException on errors deleting the service template
     */
    public JpaToscaServiceTemplate deleteServiceTemplate(@NonNull final PfDao dao) throws PfModelException {
        LOGGER.debug("->deleteServiceTemplate");

        JpaToscaServiceTemplate serviceTemplate = getServiceTemplate(dao);

        dao.delete(serviceTemplate);

        LOGGER.debug("->deleteServiceTemplate: serviceTemplate={}", serviceTemplate);
        return serviceTemplate;
    }

    /**
     * Get data types.
     *
     * @param dao the DAO to use to access the database
     * @param name the name of the data type to get, set to null to get all policy types
     * @param version the version of the data type to get, set to null to get all versions
     * @return the data types found
     * @throws PfModelException on errors getting data types
     */
    public JpaToscaServiceTemplate getDataTypes(@NonNull final PfDao dao, final String name, final String version)
        throws PfModelException {
        LOGGER.debug("->getDataTypes: name={}, version={}", name, version);

        final JpaToscaServiceTemplate dbServiceTemplate = getServiceTemplate(dao);

        if (!ToscaUtils.doDataTypesExist(dbServiceTemplate)) {
            throw new PfModelRuntimeException(Response.Status.NOT_FOUND,
                "data types for " + name + ":" + version + DO_NOT_EXIST);
        }

        JpaToscaServiceTemplate serviceTemplate = getCascadedDataTypes(dbServiceTemplate, name, version);

        LOGGER.debug("<-getDataTypes: name={}, version={}, serviceTemplate={}", name, version, serviceTemplate);
        return serviceTemplate;
    }

    /**
     * Get the cascaded data types for a data type name and version.
     *
     * @param dbServiceTemplate the service template to search for the cascaded data types
     * @param name the data type name we are searching for
     * @param version the data type version we are searching for
     * @return a service template containing the cascaded data types
     * @throws PfModelException on errors getting the data types
     */
    public JpaToscaServiceTemplate getCascadedDataTypes(@NonNull final JpaToscaServiceTemplate dbServiceTemplate,
        final String name, final String version) throws PfModelException {

        var serviceTemplate = new JpaToscaServiceTemplate(dbServiceTemplate);
        serviceTemplate.setPolicyTypes(null);
        serviceTemplate.setTopologyTemplate(null);

        ToscaUtils.getEntityTree(serviceTemplate.getDataTypes(), name, version);

        if (!ToscaUtils.doDataTypesExist(serviceTemplate)) {
            throw new PfModelRuntimeException(Response.Status.NOT_FOUND,
                "data types for " + name + ":" + version + DO_NOT_EXIST);
        }

        for (JpaToscaDataType dataType : serviceTemplate.getDataTypes().getConceptMap().values()) {
            Collection<PfConceptKey> referencedDataTypeKeys = dataType.getReferencedDataTypes();

            for (PfConceptKey referencedDataTypeKey : referencedDataTypeKeys) {
                JpaToscaServiceTemplate dataTypeEntityTreeServiceTemplate = getCascadedDataTypes(dbServiceTemplate,
                    referencedDataTypeKey.getName(), referencedDataTypeKey.getVersion());

                serviceTemplate =
                    ToscaServiceTemplateUtils.addFragment(serviceTemplate, dataTypeEntityTreeServiceTemplate);
            }
        }
        return serviceTemplate;
    }

    /**
     * Create data types.
     *
     * @param dao the DAO to use to access the database
     * @param incomingServiceTemplate the service template containing the definition of the data types to be created
     * @return the TOSCA service template containing the created data types
     * @throws PfModelException on errors creating data types
     */
    public JpaToscaServiceTemplate createDataTypes(@NonNull final PfDao dao,
        @NonNull final JpaToscaServiceTemplate incomingServiceTemplate) throws PfModelException {
        LOGGER.debug("->createDataTypes: incomingServiceTemplate={}", incomingServiceTemplate);

        ToscaUtils.assertDataTypesExist(incomingServiceTemplate);

        JpaToscaServiceTemplate writtenServiceTemplate = appendToServiceTemplate(dao, incomingServiceTemplate);

        LOGGER.debug("<-createDataTypes: writtenServiceTemplate={}", writtenServiceTemplate);
        return incomingServiceTemplate;
    }

    /**
     * Update Data types.
     *
     * @param dao the DAO to use to access the database
     * @param serviceTemplate the service template containing the definition of the data types to be modified
     * @return the TOSCA service template containing the modified data types
     * @throws PfModelException on errors updating Data types
     */
    public JpaToscaServiceTemplate updateDataTypes(@NonNull final PfDao dao,
        @NonNull final JpaToscaServiceTemplate serviceTemplate) throws PfModelException {
        LOGGER.debug("->updateDataTypes: serviceTempalate={}", serviceTemplate);

        ToscaUtils.assertDataTypesExist(serviceTemplate);

        for (JpaToscaDataType dataType : serviceTemplate.getDataTypes().getAll(null)) {
            dao.update(dataType);
        }

        // Return the created data types
        var returnDataTypes = new JpaToscaDataTypes();

        for (PfConceptKey dataTypeKey : serviceTemplate.getDataTypes().getConceptMap().keySet()) {
            returnDataTypes.getConceptMap().put(dataTypeKey, dao.get(JpaToscaDataType.class, dataTypeKey));
        }

        var returnServiceTemplate = new JpaToscaServiceTemplate();
        returnServiceTemplate.setDataTypes(returnDataTypes);

        LOGGER.debug("<-updateDataTypes: returnServiceTempalate={}", returnServiceTemplate);
        return returnServiceTemplate;
    }

    /**
     * Delete Data types.
     *
     * @param dao the DAO to use to access the database
     * @param dataTypeKey the data type key for the Data types to be deleted, if the version of the key is null, all
     *        versions of the data type are deleted.
     * @return the TOSCA service template containing the data types that were deleted
     * @throws PfModelException on errors deleting data types
     */
    public JpaToscaServiceTemplate deleteDataType(@NonNull final PfDao dao, @NonNull final PfConceptKey dataTypeKey)
        throws PfModelException {
        LOGGER.debug("->deleteDataType: key={}", dataTypeKey);

        JpaToscaServiceTemplate serviceTemplate = getServiceTemplate(dao);

        if (!ToscaUtils.doDataTypesExist(serviceTemplate)) {
            throw new PfModelRuntimeException(Response.Status.NOT_FOUND, "no data types found");
        }

        JpaToscaDataType dataType4Deletion = serviceTemplate.getDataTypes().get(dataTypeKey);
        if (dataType4Deletion == null) {
            throw new PfModelRuntimeException(Response.Status.NOT_FOUND, DATA_TYPE + dataTypeKey.getId() + NOT_FOUND);
        }

        for (JpaToscaDataType dataType : serviceTemplate.getDataTypes().getAll(null)) {
            if (dataType.getReferencedDataTypes().contains(dataTypeKey)) {
                throw new PfModelRuntimeException(Response.Status.NOT_ACCEPTABLE,
                    DATA_TYPE + dataTypeKey.getId() + " is in use, it is referenced in data type " + dataType.getId());
            }
        }

        if (ToscaUtils.doPolicyTypesExist(serviceTemplate)) {
            for (JpaToscaPolicyType policyType : serviceTemplate.getPolicyTypes().getAll(null)) {
                if (policyType.getReferencedDataTypes().contains(dataTypeKey)) {
                    throw new PfModelRuntimeException(Response.Status.NOT_ACCEPTABLE, DATA_TYPE + dataTypeKey.getId()
                        + " is in use, it is referenced in policy type " + policyType.getId());
                }
            }
        }

        serviceTemplate.getDataTypes().getConceptMap().remove(dataTypeKey);
        new SimpleToscaServiceTemplateProvider().write(dao, serviceTemplate);
        dao.delete(dataType4Deletion);

        var deletedServiceTemplate = new JpaToscaServiceTemplate();
        deletedServiceTemplate.setDataTypes(new JpaToscaDataTypes());
        deletedServiceTemplate.getDataTypes().getConceptMap().put(dataTypeKey, dataType4Deletion);

        LOGGER.debug("<-deleteDataType: key={}, serviceTempalate={}", dataTypeKey, deletedServiceTemplate);
        return deletedServiceTemplate;
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
    public JpaToscaServiceTemplate getPolicyTypes(@NonNull final PfDao dao, final String name, final String version)
        throws PfModelException {
        LOGGER.debug("->getPolicyTypes: name={}, version={}", name, version);

        final JpaToscaServiceTemplate dbServiceTemplate = getServiceTemplate(dao);

        if (!ToscaUtils.doPolicyTypesExist(dbServiceTemplate)) {
            throw new PfModelRuntimeException(Response.Status.NOT_FOUND,
                "policy types for " + name + ":" + version + DO_NOT_EXIST);
        }

        JpaToscaServiceTemplate serviceTemplate = getCascadedPolicyTypes(dbServiceTemplate, name, version);

        LOGGER.debug("<-getPolicyTypes: name={}, version={}, serviceTemplate={}", name, version, serviceTemplate);
        return serviceTemplate;
    }

    /**
     * Get the cascaded policy types for a policy type name and version.
     *
     * @param dbServiceTemplate the service template to search for the cascaded policy types
     * @param name the policy type name we are searching for
     * @param version the policy type version we are searching for
     * @return a service template containing the cascaded policy types
     * @throws PfModelException on errors getting the policy types
     */
    public JpaToscaServiceTemplate getCascadedPolicyTypes(final JpaToscaServiceTemplate dbServiceTemplate,
        final String name, final String version) throws PfModelException {

        var serviceTemplate = new JpaToscaServiceTemplate(dbServiceTemplate);

        serviceTemplate.setDataTypes(null);
        serviceTemplate.setTopologyTemplate(null);

        ToscaUtils.getEntityTree(serviceTemplate.getPolicyTypes(), name, version);

        if (!ToscaUtils.doPolicyTypesExist(serviceTemplate)) {
            throw new PfModelRuntimeException(Response.Status.NOT_FOUND,
                "policy types for " + name + ":" + version + DO_NOT_EXIST);
        }

        var dataTypeServiceTemplate = new JpaToscaServiceTemplate(serviceTemplate);
        dataTypeServiceTemplate.setPolicyTypes(null);

        for (JpaToscaPolicyType policyType : serviceTemplate.getPolicyTypes().getConceptMap().values()) {
            Collection<PfConceptKey> referencedDataTypeKeys = policyType.getReferencedDataTypes();

            for (PfConceptKey referencedDataTypeKey : referencedDataTypeKeys) {
                JpaToscaServiceTemplate dataTypeEntityTreeServiceTemplate = getCascadedDataTypes(dbServiceTemplate,
                    referencedDataTypeKey.getName(), referencedDataTypeKey.getVersion());

                dataTypeServiceTemplate =
                    ToscaServiceTemplateUtils.addFragment(dataTypeServiceTemplate, dataTypeEntityTreeServiceTemplate);
            }
        }

        serviceTemplate = ToscaServiceTemplateUtils.addFragment(serviceTemplate, dataTypeServiceTemplate);
        return serviceTemplate;
    }

    /**
     * Create policy types.
     *
     * @param dao the DAO to use to access the database
     * @param incomingServiceTemplate the service template containing the definition of the policy types to be created
     * @return the TOSCA service template containing the created policy types
     * @throws PfModelException on errors creating policy types
     */
    public JpaToscaServiceTemplate createPolicyTypes(@NonNull final PfDao dao,
        @NonNull final JpaToscaServiceTemplate incomingServiceTemplate) throws PfModelException {
        LOGGER.debug("->createPolicyTypes: serviceTempalate={}", incomingServiceTemplate);

        ToscaUtils.assertPolicyTypesExist(incomingServiceTemplate);

        JpaToscaServiceTemplate writtenServiceTemplate = appendToServiceTemplate(dao, incomingServiceTemplate);

        LOGGER.debug("<-createPolicyTypes: writtenServiceTemplate={}", writtenServiceTemplate);
        return incomingServiceTemplate;
    }

    /**
     * Update policy types.
     *
     * @param dao the DAO to use to access the database
     * @param serviceTemplate the service template containing the definition of the policy types to be modified
     * @return the TOSCA service template containing the modified policy types
     * @throws PfModelException on errors updating policy types
     */
    public JpaToscaServiceTemplate updatePolicyTypes(@NonNull final PfDao dao,
        @NonNull final JpaToscaServiceTemplate serviceTemplate) throws PfModelException {
        LOGGER.debug("->updatePolicyTypes: serviceTempalate={}", serviceTemplate);

        ToscaUtils.assertPolicyTypesExist(serviceTemplate);

        // Update the data types on the policy type
        if (ToscaUtils.doDataTypesExist(serviceTemplate)) {
            updateDataTypes(dao, serviceTemplate);
        }

        for (JpaToscaPolicyType policyType : serviceTemplate.getPolicyTypes().getAll(null)) {
            dao.update(policyType);
        }

        // Return the created policy types
        var returnPolicyTypes = new JpaToscaPolicyTypes();

        for (PfConceptKey policyTypeKey : serviceTemplate.getPolicyTypes().getConceptMap().keySet()) {
            returnPolicyTypes.getConceptMap().put(policyTypeKey, dao.get(JpaToscaPolicyType.class, policyTypeKey));
        }

        var returnServiceTemplate = new JpaToscaServiceTemplate();
        returnServiceTemplate.setPolicyTypes(returnPolicyTypes);

        LOGGER.debug("<-updatePolicyTypes: returnServiceTempalate={}", returnServiceTemplate);
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
        LOGGER.debug("->deletePolicyType: key={}", policyTypeKey);

        JpaToscaServiceTemplate serviceTemplate = getServiceTemplate(dao);

        if (!ToscaUtils.doPolicyTypesExist(serviceTemplate)) {
            throw new PfModelRuntimeException(Response.Status.NOT_FOUND, "no policy types found");
        }

        JpaToscaPolicyType policyType4Deletion = serviceTemplate.getPolicyTypes().get(policyTypeKey);
        if (policyType4Deletion == null) {
            throw new PfModelRuntimeException(Response.Status.NOT_FOUND,
                POLICY_TYPE + policyTypeKey.getId() + NOT_FOUND);
        }

        var result = new BeanValidationResult("policy types", serviceTemplate);

        for (JpaToscaPolicyType policyType : serviceTemplate.getPolicyTypes().getAll(null)) {
            Collection<JpaToscaEntityType<ToscaEntity>> ancestorList = ToscaUtils
                .getEntityTypeAncestors(serviceTemplate.getPolicyTypes(), policyType, result);

            if (ancestorList.contains(policyType4Deletion)) {
                throw new PfModelRuntimeException(Response.Status.NOT_ACCEPTABLE, POLICY_TYPE + policyTypeKey.getId()
                    + " is in use, it is referenced in policy type " + policyType.getId());
            }
        }

        if (ToscaUtils.doPoliciesExist(serviceTemplate)) {
            for (JpaToscaPolicy policy : serviceTemplate.getTopologyTemplate().getPolicies().getAll(null)) {
                if (policyTypeKey.equals(policy.getType())) {
                    throw new PfModelRuntimeException(Response.Status.NOT_ACCEPTABLE, POLICY_TYPE
                        + policyTypeKey.getId() + " is in use, it is referenced in policy " + policy.getId());
                }
            }
        }

        serviceTemplate.getPolicyTypes().getConceptMap().remove(policyTypeKey);
        new SimpleToscaServiceTemplateProvider().write(dao, serviceTemplate);
        dao.delete(policyType4Deletion);

        var deletedServiceTemplate = new JpaToscaServiceTemplate();
        deletedServiceTemplate.setPolicyTypes(new JpaToscaPolicyTypes());
        deletedServiceTemplate.getPolicyTypes().getConceptMap().put(policyTypeKey, policyType4Deletion);

        LOGGER.debug("<-deletePolicyType: key={}, serviceTempalate={}", policyTypeKey, deletedServiceTemplate);
        return deletedServiceTemplate;
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
        LOGGER.debug("->getPolicies: name={}, version={}", name, version);

        JpaToscaServiceTemplate dbServiceTemplate = getServiceTemplate(dao);

        if (!ToscaUtils.doPoliciesExist(dbServiceTemplate)) {
            throw new PfModelRuntimeException(Response.Status.NOT_FOUND,
                "policies for " + name + ":" + version + DO_NOT_EXIST);
        }

        JpaToscaServiceTemplate returnServiceTemplate = getCascadedPolicies(dbServiceTemplate, name, version);

        LOGGER.debug("<-getPolicies: name={}, version={}, serviceTemplate={}", name, version, returnServiceTemplate);
        return returnServiceTemplate;
    }

    /**
     * Get the cascaded policies for a policy name and version.
     *
     * @param dbServiceTemplate the service template to search for the cascaded policy
     * @param name the policy name we are searching for
     * @param version the policy version we are searching for
     * @return a service template containing the cascaded policy
     * @throws PfModelException on errors getting the policy
     */
    public JpaToscaServiceTemplate getCascadedPolicies(final JpaToscaServiceTemplate dbServiceTemplate,
        final String name, final String version) throws PfModelException {

        var serviceTemplate = new JpaToscaServiceTemplate(dbServiceTemplate);
        serviceTemplate.setDataTypes(new JpaToscaDataTypes());
        serviceTemplate.setPolicyTypes(new JpaToscaPolicyTypes());

        ToscaUtils.getEntityTree(serviceTemplate.getTopologyTemplate().getPolicies(), name, version);

        if (!ToscaUtils.doPoliciesExist(serviceTemplate)) {
            throw new PfModelRuntimeException(Response.Status.NOT_FOUND,
                "policies for " + name + ":" + version + DO_NOT_EXIST);
        }

        var returnServiceTemplate = new JpaToscaServiceTemplate(serviceTemplate);
        returnServiceTemplate.getTopologyTemplate().setPolicies(new JpaToscaPolicies());

        for (JpaToscaPolicy policy : serviceTemplate.getTopologyTemplate().getPolicies().getConceptMap().values()) {
            JpaToscaServiceTemplate referencedEntitiesServiceTemplate =
                getCascadedPolicyTypes(dbServiceTemplate, policy.getType().getName(), policy.getType().getVersion());

            returnServiceTemplate.getTopologyTemplate().getPolicies().getConceptMap().put(policy.getKey(), policy);
            returnServiceTemplate =
                ToscaServiceTemplateUtils.addFragment(returnServiceTemplate, referencedEntitiesServiceTemplate);
        }

        return returnServiceTemplate;
    }

    /**
     * Create policies.
     *
     * @param dao the DAO to use to access the database
     * @param incomingServiceTemplate the service template containing the definitions of the new policies to be created.
     * @return the TOSCA service template containing the policy types that were created
     * @throws PfModelException on errors creating policies
     */
    public JpaToscaServiceTemplate createPolicies(@NonNull final PfDao dao,
        @NonNull final JpaToscaServiceTemplate incomingServiceTemplate) throws PfModelException {
        LOGGER.debug("->createPolicies: incomingServiceTemplate={}", incomingServiceTemplate);

        ToscaUtils.assertPoliciesExist(incomingServiceTemplate);

        JpaToscaServiceTemplate writtenServiceTemplate = appendToServiceTemplate(dao, incomingServiceTemplate);

        LOGGER.debug("<-createPolicies: writtenServiceTemplate={}", writtenServiceTemplate);
        return incomingServiceTemplate;
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
        LOGGER.debug("->updatePolicies: serviceTempalate={}", serviceTemplate);

        ToscaUtils.assertPoliciesExist(serviceTemplate);

        for (JpaToscaPolicy policy : serviceTemplate.getTopologyTemplate().getPolicies().getAll(null)) {
            verifyPolicyTypeForPolicy(dao, policy);
            dao.update(policy);
        }

        // Return the created policy types
        var returnPolicies = new JpaToscaPolicies();
        returnPolicies.setKey(serviceTemplate.getTopologyTemplate().getPolicies().getKey());

        for (PfConceptKey policyKey : serviceTemplate.getTopologyTemplate().getPolicies().getConceptMap().keySet()) {
            returnPolicies.getConceptMap().put(policyKey, dao.get(JpaToscaPolicy.class, policyKey));
        }

        serviceTemplate.getTopologyTemplate().setPolicies(returnPolicies);

        LOGGER.debug("<-updatePolicies: serviceTemplate={}", serviceTemplate);
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
        LOGGER.debug("->deletePolicy: key={}", policyKey);

        JpaToscaServiceTemplate serviceTemplate = getServiceTemplate(dao);

        if (!ToscaUtils.doPoliciesExist(serviceTemplate)) {
            throw new PfModelRuntimeException(Response.Status.NOT_FOUND, "no policies found");
        }

        JpaToscaPolicy policy4Deletion = serviceTemplate.getTopologyTemplate().getPolicies().get(policyKey);
        if (policy4Deletion == null) {
            throw new PfModelRuntimeException(Response.Status.NOT_FOUND, "policy " + policyKey.getId() + NOT_FOUND);
        }

        serviceTemplate.getTopologyTemplate().getPolicies().getConceptMap().remove(policyKey);
        new SimpleToscaServiceTemplateProvider().write(dao, serviceTemplate);
        dao.delete(policy4Deletion);

        var deletedServiceTemplate = new JpaToscaServiceTemplate();
        deletedServiceTemplate.setTopologyTemplate(new JpaToscaTopologyTemplate());
        deletedServiceTemplate.getTopologyTemplate().setPolicies(new JpaToscaPolicies());
        deletedServiceTemplate.getTopologyTemplate().getPolicies().getConceptMap().put(policyKey, policy4Deletion);

        LOGGER.debug("<-deletePolicy: key={}, serviceTempalate={}", policyKey, deletedServiceTemplate);
        return deletedServiceTemplate;
    }

    /**
     * Verify the policy type for a policy exists.
     *
     * @param dao the DAO to use to access policy types in the database
     * @param policy the policy to check the policy type for
     */
    private void verifyPolicyTypeForPolicy(final PfDao dao, final JpaToscaPolicy policy) {
        PfConceptKey policyTypeKey = policy.getType();

        JpaToscaPolicyType policyType = null;

        if (PfKey.NULL_KEY_VERSION.equals(policyTypeKey.getVersion())) {
            policyType = getLatestPolicyTypeVersion(dao, policyTypeKey.getName());

            if (policyType != null) {
                policy.getType().setVersion(policyType.getKey().getVersion());
            }
        } else {
            policyType = dao.get(JpaToscaPolicyType.class, policyTypeKey);
        }

        if (policyType == null) {
            String errorMessage =
                POLICY_TYPE + policyTypeKey.getId() + " for policy " + policy.getId() + " does not exist";
            throw new PfModelRuntimeException(Response.Status.NOT_ACCEPTABLE, errorMessage);
        }
    }

    /**
     * Get the latest version of the policy type for the given policy type name.
     *
     * @param dao the DAO to use to access policy types in the database
     * @param policyTypeName the name of the policy type
     * @return the latest policy type
     */
    private JpaToscaPolicyType getLatestPolicyTypeVersion(final PfDao dao, final String policyTypeName) {
        // Policy type version is not specified, get the latest version from the database
        List<JpaToscaPolicyType> jpaPolicyTypeList = dao.getFiltered(JpaToscaPolicyType.class, policyTypeName, null);

        if (CollectionUtils.isEmpty(jpaPolicyTypeList)) {
            return null;
        }

        // Create a filter to get the latest version of the policy type
        var pfConceptFilter = PfConceptFilter.builder().version(PfConceptFilter.LATEST_VERSION).build();

        // FIlter the returned policy type list
        List<PfConcept> policyTypeKeyList = new ArrayList<>(jpaPolicyTypeList);
        List<PfConcept> filterdPolicyTypeList = pfConceptFilter.filter(policyTypeKeyList);

        // We should have one and only one returned entry
        if (filterdPolicyTypeList.size() != 1) {
            String errorMessage = "search for latest policy type " + policyTypeName + " returned more than one entry";
            throw new PfModelRuntimeException(Response.Status.CONFLICT, errorMessage);
        }

        return (JpaToscaPolicyType) filterdPolicyTypeList.get(0);
    }
}
