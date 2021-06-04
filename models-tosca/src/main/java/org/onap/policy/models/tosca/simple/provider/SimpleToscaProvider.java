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

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.ws.rs.core.Response;
import lombok.NonNull;
import org.onap.policy.common.parameters.BeanValidationResult;
import org.onap.policy.models.base.PfConcept;
import org.onap.policy.models.base.PfConceptContainer;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfKey;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.base.PfModelRuntimeException;
import org.onap.policy.models.base.PfNameVersion;
import org.onap.policy.models.dao.PfDao;
import org.onap.policy.models.tosca.authorative.concepts.ToscaEntityFilter;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicy;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyType;
import org.onap.policy.models.tosca.authorative.concepts.ToscaTypedEntityFilter;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaDataType;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaDataTypes;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaEntityType;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaPolicies;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaPolicy;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaPolicyType;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaPolicyTypes;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaServiceTemplate;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaTopologyTemplate;
import org.onap.policy.models.tosca.simple.provider.EntityKey.NodeType;
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
    private static final String POLICY = "policy ";
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

        var serviceTemplate = new JpaToscaServiceTemplate(dbServiceTemplate);

        EntityTree tree = makeTree(serviceTemplate);

        serviceTemplate.setPolicyTypes(null);
        serviceTemplate.setTopologyTemplate(null);

        // load up data types
        var dataTypes = filter(serviceTemplate.getDataTypes(), NodeType.DATA_TYPE, name, version);
        var dataKeys = getCascadedDataTypes(serviceTemplate, tree, dataTypes);

        if (dataKeys.isEmpty()) {
            throw new PfModelRuntimeException(Response.Status.NOT_FOUND,
                "data types for " + name + ":" + version + DO_NOT_EXIST);
        }

        LOGGER.debug("<-getDataTypes: name={}, version={}, serviceTemplate={}", name, version, serviceTemplate);
        return serviceTemplate;
    }

    private EntityTree makeTree(JpaToscaServiceTemplate serviceTemplate) {
        EntityTree tree = new EntityTree();

        loadPolicies(serviceTemplate, tree);
        loadPolicyTypes(serviceTemplate, tree);
        loadDataTypes(serviceTemplate, tree);

        return tree;
    }

    private void loadPolicies(JpaToscaServiceTemplate serviceTemplate, EntityTree tree) {
        if (serviceTemplate.getTopologyTemplate() == null
                        || serviceTemplate.getTopologyTemplate().getPolicies() == null) {
            return;
        }

        for (JpaToscaPolicy data : serviceTemplate.getTopologyTemplate().getPolicies().getConceptMap().values()) {
            EntityKey dataKey = new EntityKey(NodeType.POLICY, data.getKey());
            tree.add(dataKey, data);

            addReference(tree, dataKey, NodeType.POLICY, data.getDerivedFrom());
            addReferences(tree, dataKey, NodeType.POLICY_TYPE, Collections.singleton(data.getType()));
        }
    }

    private void loadPolicyTypes(JpaToscaServiceTemplate serviceTemplate, EntityTree tree) {
        if (serviceTemplate.getPolicyTypes() == null) {
            return;
        }

        for (JpaToscaPolicyType data : serviceTemplate.getPolicyTypes().getConceptMap().values()) {
            EntityKey dataKey = new EntityKey(NodeType.POLICY_TYPE, data.getKey());
            tree.add(dataKey, data);

            addReference(tree, dataKey, NodeType.POLICY_TYPE, data.getDerivedFrom());
            addReferences(tree, dataKey, NodeType.DATA_TYPE, data.getReferencedDataTypes());
        }
    }

    private void loadDataTypes(JpaToscaServiceTemplate serviceTemplate, EntityTree tree) {
        if (serviceTemplate.getDataTypes() == null) {
            return;
        }

        for (JpaToscaDataType data : serviceTemplate.getDataTypes().getConceptMap().values()) {
            EntityKey dataKey = new EntityKey(NodeType.DATA_TYPE, data.getKey());
            tree.add(dataKey, data);

            addReference(tree, dataKey, NodeType.DATA_TYPE, data.getDerivedFrom());
            addReferences(tree, dataKey, NodeType.DATA_TYPE, data.getReferencedDataTypes());
        }
    }

    private void addReferences(EntityTree tree, EntityKey key, NodeType refType,
                    Collection<PfConceptKey> referencedKeys) {
        for (var refKey : referencedKeys) {
            addReference(tree, key, refType, refKey);
        }
    }

    private void addReference(EntityTree tree, EntityKey key, NodeType refType, PfConceptKey refKey) {
        if (refKey == null || refKey.getName().endsWith(ToscaUtils.ROOT_KEY_NAME_SUFFIX)) {
            return;
        }

        EntityKey refEntityKey = new EntityKey(refType, refKey);

        tree.addReference(key, refEntityKey);
    }

    private <T extends JpaToscaEntityType<?>> Collection<EntityKey> filter(
                    PfConceptContainer<T, ? extends PfNameVersion> entityTypes, NodeType type, String name,
                    String version) {

        Predicate<PfConcept> theFilter;
        if (name == null) {
            theFilter = null;
        } else if (version == null || PfKey.NULL_KEY_VERSION.equals(version)) {
            theFilter = key -> name.equals(key.getName());
        } else {
            theFilter = key -> name.equals(key.getName()) && version.contentEquals(key.getVersion());
        }

        Stream<T> stream = entityTypes.getConceptMap().values().stream();
        if (theFilter != null) {
            stream = stream.filter(theFilter);
        }

        return stream.map(jpa -> new EntityKey(type, jpa.getKey())).collect(Collectors.toList());
    }

    private <T extends JpaToscaEntityType<?>> Map<PfConceptKey, T> cascade(EntityTree tree,
                    NodeType type, Collection<EntityKey> data) {

        Map<PfConceptKey, T> map = new TreeMap<>();
        Queue<EntityKey> queue = new ArrayDeque<>(data);

        while (!queue.isEmpty()) {
            EntityKey entKey = queue.remove();

            if (entKey.getType() == type) {
                map.put(entKey.getIdent(), tree.get(entKey));
            }

            for (EntityKey refKey : tree.getUses(entKey, type)) {
                map.computeIfAbsent(refKey.getIdent(), refId -> {
                    queue.add(refKey);
                    T refData = tree.get(refKey);
                    map.put(refId, refData);
                    return refData;
                });
            }
        }

        return map;
    }

    private List<EntityKey> getCascadedDataTypes(JpaToscaServiceTemplate serviceTemplate, EntityTree tree,
                    Collection<EntityKey> keys) {

        Map<PfConceptKey, JpaToscaDataType> dataTypeMap = cascade(tree, NodeType.DATA_TYPE, keys);

        JpaToscaDataTypes jpaDataTypes = new JpaToscaDataTypes();
        jpaDataTypes.setConceptMap(dataTypeMap);

        serviceTemplate.setDataTypes(jpaDataTypes);

        return dataTypeMap.keySet().stream().map(key -> new EntityKey(NodeType.DATA_TYPE, key))
                        .collect(Collectors.toList());
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

        JpaToscaServiceTemplate dbServiceTemplate = getServiceTemplate(dao);
        if (dbServiceTemplate == null) {
            throw new PfModelRuntimeException(Response.Status.NOT_FOUND, "service template " + NOT_FOUND);
        }

        if (dbServiceTemplate.getDataTypes() == null) {
            dbServiceTemplate.setDataTypes(new JpaToscaDataTypes());
        }

        Map<PfConceptKey, JpaToscaDataType> dbDataTypes = dbServiceTemplate.getDataTypes().getConceptMap();

        for (JpaToscaDataType dataType : serviceTemplate.getDataTypes().getAll(null)) {
            dbDataTypes.put(dataType.getKey(), dataType);
        }

        new SimpleToscaServiceTemplateProvider().write(dao, dbServiceTemplate);

        // Return the created data types
        var returnDataTypes = new JpaToscaDataTypes();

        for (PfConceptKey dataTypeKey : dbDataTypes.keySet()) {
            returnDataTypes.getConceptMap().put(dataTypeKey, dbDataTypes.get(dataTypeKey));
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

        EntityTree tree = makeTree(serviceTemplate);
        EntityKey key = new EntityKey(NodeType.DATA_TYPE, dataTypeKey);

        JpaToscaDataType dataType4Deletion = tree.get(key);
        if (dataType4Deletion == null) {
            throw new PfModelRuntimeException(Response.Status.NOT_FOUND, DATA_TYPE + dataTypeKey.getId() + NOT_FOUND);
        }

        Set<EntityKey> refKeys = tree.getUsedBy(key, NodeType.POLICY_TYPE, NodeType.DATA_TYPE);
        if (!refKeys.isEmpty()) {
            var refKey = refKeys.iterator().next();
            String msg;
            switch (refKey.getType()) {
                case DATA_TYPE:
                    msg = "data type";
                    break;
                case POLICY_TYPE:
                    msg = "policy type";
                    break;
                default:
                    msg = "unknown";
                    break;
            }
            throw new PfModelRuntimeException(Response.Status.NOT_ACCEPTABLE, DATA_TYPE + dataTypeKey.getId()
                            + " is in use, it is referenced in " + msg + " " + refKey.getIdent().getId());
        }

        serviceTemplate.getDataTypes().getConceptMap().remove(dataTypeKey);
        new SimpleToscaServiceTemplateProvider().write(dao, serviceTemplate);

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

        var serviceTemplate = new JpaToscaServiceTemplate(dbServiceTemplate);

        EntityTree tree = makeTree(serviceTemplate);

        serviceTemplate.setTopologyTemplate(null);

        // load up policy types
        var policyTypes = filter(serviceTemplate.getPolicyTypes(), NodeType.POLICY_TYPE, name, version);
        var typeKeys = getCascadedPolicyTypes(serviceTemplate, tree, policyTypes);

        if (typeKeys.isEmpty()) {
            throw new PfModelRuntimeException(Response.Status.NOT_FOUND,
                "policy types for " + name + ":" + version + DO_NOT_EXIST);
        }

        getCascadedDataTypes(serviceTemplate, tree, typeKeys);

        LOGGER.debug("<-getPolicyTypes: name={}, version={}, serviceTemplate={}", name, version, serviceTemplate);
        return serviceTemplate;
    }

    /**
     * Get policy types.
     *
     * @param dao the DAO to use to access the database
     * @param filter the filter for the policy types to get
     * @return the policy types found
     * @throws PfModelException on errors getting policy types
     */
    public JpaToscaServiceTemplate getFilteredPolicyTypes(@NonNull final PfDao dao,
                    @NonNull final ToscaEntityFilter<ToscaPolicyType> filter)
        throws PfModelException {
        LOGGER.debug("->getFilteredPolicyTypes: filter={}", filter);

        final JpaToscaServiceTemplate dbServiceTemplate = getServiceTemplate(dao);

        if (!ToscaUtils.doPolicyTypesExist(dbServiceTemplate)) {
            throw new PfModelRuntimeException(Response.Status.NOT_FOUND,
                            "policy types for filter " + filter + DO_NOT_EXIST);
        }

        var serviceTemplate = new JpaToscaServiceTemplate(dbServiceTemplate);

        EntityTree tree = makeTree(serviceTemplate);

        serviceTemplate.setTopologyTemplate(null);

        // load up policy types of interest
        var authorativeTypes = serviceTemplate.getPolicyTypes().getConceptMap().values().stream()
                        .map(JpaToscaPolicyType::toAuthorative).collect(Collectors.toList());
        authorativeTypes = filter.filter(authorativeTypes);

        var typeKeys = authorativeTypes.stream()
                        .map(ToscaPolicyType::getKey)
                        .map(key -> new PfConceptKey(key.getName(), key.getVersion()))
                        .map(key -> new EntityKey(NodeType.POLICY_TYPE, key))
                        .collect(Collectors.toList());

        typeKeys = getCascadedPolicyTypes(serviceTemplate, tree, typeKeys);

        if (typeKeys.isEmpty()) {
            throw new PfModelRuntimeException(Response.Status.NOT_FOUND,
                            "policy types for filter " + filter + DO_NOT_EXIST);
        }

        getCascadedDataTypes(serviceTemplate, tree, typeKeys);

        LOGGER.debug("<-getFilteredPolicyTypes: filter={}, serviceTemplate={}", filter, serviceTemplate);
        return serviceTemplate;
    }

    private List<EntityKey> getCascadedPolicyTypes(JpaToscaServiceTemplate serviceTemplate,
                    EntityTree tree, Collection<EntityKey> keys) {

        Map<PfConceptKey, JpaToscaPolicyType> policyTypeMap = cascade(tree, NodeType.POLICY_TYPE, keys);

        JpaToscaPolicyTypes jpaPolicyTypes = new JpaToscaPolicyTypes();
        jpaPolicyTypes.setConceptMap(policyTypeMap);

        serviceTemplate.setPolicyTypes(jpaPolicyTypes);

        return policyTypeMap.keySet().stream().map(key -> new EntityKey(NodeType.POLICY_TYPE, key))
                        .collect(Collectors.toList());
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

        JpaToscaServiceTemplate dbServiceTemplate = getServiceTemplate(dao);
        if (dbServiceTemplate == null) {
            throw new PfModelRuntimeException(Response.Status.NOT_FOUND, "service template " + NOT_FOUND);
        }

        if (dbServiceTemplate.getPolicyTypes() == null) {
            dbServiceTemplate.setPolicyTypes(new JpaToscaPolicyTypes());
        }

        Map<PfConceptKey, JpaToscaPolicyType> dbPolicyTypes = dbServiceTemplate.getPolicyTypes().getConceptMap();

        for (JpaToscaPolicyType policyType : serviceTemplate.getPolicyTypes().getAll(null)) {
            dbPolicyTypes.put(policyType.getKey(), policyType);
        }

        new SimpleToscaServiceTemplateProvider().write(dao, dbServiceTemplate);

        // Return the created policy types
        var returnPolicyTypes = new JpaToscaPolicyTypes();

        for (PfConceptKey policyTypeKey : serviceTemplate.getPolicyTypes().getConceptMap().keySet()) {
            returnPolicyTypes.getConceptMap().put(policyTypeKey, dbPolicyTypes.get(policyTypeKey));
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

        EntityTree tree = makeTree(serviceTemplate);
        EntityKey key = new EntityKey(NodeType.POLICY_TYPE, policyTypeKey);

        JpaToscaPolicyType policyType4Deletion = tree.get(key);
        if (policyType4Deletion == null) {
            throw new PfModelRuntimeException(Response.Status.NOT_FOUND,
                POLICY_TYPE + policyTypeKey.getId() + NOT_FOUND);
        }

        Set<EntityKey> refKeys = tree.getUsedBy(key, NodeType.POLICY, NodeType.POLICY_TYPE);
        if (!refKeys.isEmpty()) {
            var refKey = refKeys.iterator().next();
            String msg;
            switch (refKey.getType()) {
                case POLICY_TYPE:
                    msg = "policy type";
                    break;
                case POLICY:
                    msg = "policy";
                    break;
                default:
                    msg = "unknown";
                    break;
            }
            throw new PfModelRuntimeException(Response.Status.NOT_ACCEPTABLE, POLICY_TYPE + policyTypeKey.getId()
                            + " is in use, it is referenced in " + msg + " " + refKey.getIdent().getId());
        }

        serviceTemplate.getPolicyTypes().getConceptMap().remove(policyTypeKey);
        new SimpleToscaServiceTemplateProvider().write(dao, serviceTemplate);

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

        var serviceTemplate = new JpaToscaServiceTemplate(dbServiceTemplate);

        EntityTree tree = makeTree(serviceTemplate);

        serviceTemplate.setPolicyTypes(null);
        serviceTemplate.setDataTypes(null);

        // load up policies
        var policies = filter(serviceTemplate.getTopologyTemplate().getPolicies(), NodeType.POLICY, name, version);
        var policyKeys = getCascadedPolicies(serviceTemplate, tree, policies);

        if (policyKeys.isEmpty()) {
            throw new PfModelRuntimeException(Response.Status.NOT_FOUND,
                "policies for " + name + ":" + version + DO_NOT_EXIST);
        }

        var typeKeys = getCascadedPolicyTypes(serviceTemplate, tree, policyKeys);

        getCascadedDataTypes(serviceTemplate, tree, typeKeys);

        LOGGER.debug("<-getPolicies: name={}, version={}, serviceTemplate={}", name, version, serviceTemplate);
        return serviceTemplate;
    }

    /**
     * Get policies.
     *
     * @param dao the DAO to use to access the database
     * @param filter the filter for the policies to get
     * @return the policies found
     * @throws PfModelException on errors getting policies
     */
    public JpaToscaServiceTemplate getFilteredPolicies(@NonNull final PfDao dao,
                    @NonNull final ToscaTypedEntityFilter<ToscaPolicy> filter) throws PfModelException {
        LOGGER.debug("->getFilteredPolicies: filter={}", filter);

        JpaToscaServiceTemplate dbServiceTemplate = getServiceTemplate(dao);

        if (!ToscaUtils.doPoliciesExist(dbServiceTemplate)) {
            throw new PfModelRuntimeException(Response.Status.NOT_FOUND,
                "policies for " + filter + DO_NOT_EXIST);
        }

        var serviceTemplate = new JpaToscaServiceTemplate(dbServiceTemplate);

        final EntityTree tree = makeTree(serviceTemplate);

        serviceTemplate.setPolicyTypes(null);
        serviceTemplate.setDataTypes(null);

        // load up policies of interest
        var authorativePolicies = serviceTemplate.getTopologyTemplate().getPolicies().getConceptMap().values().stream()
                        .map(JpaToscaPolicy::toAuthorative).collect(Collectors.toList());
        authorativePolicies = filter.filter(authorativePolicies);

        var policyEntityKeys = authorativePolicies.stream()
                        .map(ToscaPolicy::getKey)
                        .map(key -> new PfConceptKey(key.getName(), key.getVersion()))
                        .map(key -> new EntityKey(NodeType.POLICY, key))
                        .collect(Collectors.toList());

        var policyKeys = getCascadedPolicies(serviceTemplate, tree, policyEntityKeys);

        if (policyKeys.isEmpty()) {
            throw new PfModelRuntimeException(Response.Status.NOT_FOUND, "policies for " + filter + DO_NOT_EXIST);
        }

        var typeKeys = getCascadedPolicyTypes(serviceTemplate, tree, policyKeys);

        getCascadedDataTypes(serviceTemplate, tree, typeKeys);

        LOGGER.debug("<-getFilteredPolicies: filter={}, serviceTemplate={}", filter, serviceTemplate);
        return serviceTemplate;
    }

    private List<EntityKey> getCascadedPolicies(JpaToscaServiceTemplate serviceTemplate, EntityTree tree,
                    Collection<EntityKey> keys) {

        Map<PfConceptKey, JpaToscaPolicy> policyMap = cascade(tree, NodeType.POLICY, keys);

        JpaToscaPolicies jpaPolicies = new JpaToscaPolicies();
        jpaPolicies.setConceptMap(policyMap);

        serviceTemplate.getTopologyTemplate().setPolicies(jpaPolicies);

        return policyMap.keySet().stream().map(key -> new EntityKey(NodeType.POLICY, key)).collect(Collectors.toList());
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

        JpaToscaServiceTemplate dbServiceTemplate = getServiceTemplate(dao);
        if (dbServiceTemplate == null) {
            throw new PfModelRuntimeException(Response.Status.NOT_FOUND, "service template " + NOT_FOUND);
        }

        if (dbServiceTemplate.getTopologyTemplate() == null) {
            dbServiceTemplate.setTopologyTemplate(new JpaToscaTopologyTemplate());
        }
        if (serviceTemplate.getTopologyTemplate().getPolicies() == null) {
            serviceTemplate.getTopologyTemplate().setPolicies(new JpaToscaPolicies());
        }
        Map<PfConceptKey, JpaToscaPolicy> dbPolicies =
                        serviceTemplate.getTopologyTemplate().getPolicies().getConceptMap();

        for (JpaToscaPolicy policy : serviceTemplate.getTopologyTemplate().getPolicies().getAll(null)) {
            verifyPolicyTypeForPolicy(dbServiceTemplate.getPolicyTypes(), policy);
            dbPolicies.put(policy.getKey(), policy);
        }

        new SimpleToscaServiceTemplateProvider().write(dao, dbServiceTemplate);

        // Return the created policies
        var returnPolicies = new JpaToscaPolicies();
        returnPolicies.setKey(serviceTemplate.getTopologyTemplate().getPolicies().getKey());

        for (PfConceptKey policyKey : serviceTemplate.getTopologyTemplate().getPolicies().getConceptMap().keySet()) {
            returnPolicies.getConceptMap().put(policyKey, dbPolicies.get(policyKey));
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

        EntityTree tree = makeTree(serviceTemplate);
        EntityKey key = new EntityKey(NodeType.POLICY, policyKey);

        JpaToscaPolicy policy4Deletion = tree.get(key);
        if (policy4Deletion == null) {
            throw new PfModelRuntimeException(Response.Status.NOT_FOUND, POLICY + policyKey.getId() + NOT_FOUND);
        }

        Set<EntityKey> refKeys = tree.getUsedBy(key, NodeType.POLICY);
        if (!refKeys.isEmpty()) {
            var refKey = refKeys.iterator().next();
            throw new PfModelRuntimeException(Response.Status.NOT_ACCEPTABLE, POLICY + policyKey.getId()
                            + " is in use, it is referenced in policy " + refKey.getIdent().getId());
        }

        serviceTemplate.getTopologyTemplate().getPolicies().getConceptMap().remove(policyKey);
        new SimpleToscaServiceTemplateProvider().write(dao, serviceTemplate);

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
     * @param policyTypes the available policy types in the service template
     * @param policy the policy to check the policy type for
     */
    private void verifyPolicyTypeForPolicy(final JpaToscaPolicyTypes policyTypes, final JpaToscaPolicy policy) {
        if (policyTypes == null) {
            String errorMessage = "No policy types for policy " + policy.getId();
            throw new PfModelRuntimeException(Response.Status.NOT_ACCEPTABLE, errorMessage);
        }

        PfConceptKey policyTypeKey = policy.getType();

        JpaToscaPolicyType policyType = null;

        if (PfKey.NULL_KEY_VERSION.equals(policyTypeKey.getVersion())) {
            policyType = policyTypes.get(policyTypeKey.getName());

            if (policyType != null) {
                policy.getType().setVersion(policyType.getKey().getVersion());
            }
        } else {
            policyType = policyTypes.get(policyTypeKey);
        }

        if (policyType == null) {
            String errorMessage =
                POLICY_TYPE + policyTypeKey.getId() + " for policy " + policy.getId() + " does not exist";
            throw new PfModelRuntimeException(Response.Status.NOT_ACCEPTABLE, errorMessage);
        }
    }
}
