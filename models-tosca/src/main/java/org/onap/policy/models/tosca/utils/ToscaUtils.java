/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2023 Nordix Foundation.
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

package org.onap.policy.models.tosca.utils;

import jakarta.ws.rs.core.Response;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.apache.commons.collections4.CollectionUtils;
import org.onap.policy.common.parameters.BeanValidationResult;
import org.onap.policy.common.parameters.ValidationStatus;
import org.onap.policy.models.base.PfConcept;
import org.onap.policy.models.base.PfConceptContainer;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfKey;
import org.onap.policy.models.base.PfModelRuntimeException;
import org.onap.policy.models.base.PfNameVersion;
import org.onap.policy.models.base.Validated;
import org.onap.policy.models.tosca.authorative.concepts.ToscaEntity;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaEntityType;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaServiceTemplate;

/**
 * Utility class for TOSCA concepts.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ToscaUtils {
    private static final String ROOT_KEY_NAME_SUFFIX = ".Root";

    // @formatter:off
    private static final Set<PfConceptKey> PREDEFINED_TOSCA_DATA_TYPES = Set.of(
            new PfConceptKey("string",                       PfKey.NULL_KEY_VERSION),
            new PfConceptKey("integer",                      PfKey.NULL_KEY_VERSION),
            new PfConceptKey("float",                        PfKey.NULL_KEY_VERSION),
            new PfConceptKey("boolean",                      PfKey.NULL_KEY_VERSION),
            new PfConceptKey("timestamp",                    PfKey.NULL_KEY_VERSION),
            new PfConceptKey("null",                         PfKey.NULL_KEY_VERSION),
            new PfConceptKey("list",                         PfKey.NULL_KEY_VERSION),
            new PfConceptKey("map",                          PfKey.NULL_KEY_VERSION),
            new PfConceptKey("object",                       PfKey.NULL_KEY_VERSION),
            new PfConceptKey("scalar-unit.size",             PfKey.NULL_KEY_VERSION),
            new PfConceptKey("scalar-unit.time",             PfKey.NULL_KEY_VERSION),
            new PfConceptKey("scalar-unit.frequency",        PfKey.NULL_KEY_VERSION),
            new PfConceptKey("tosca.datatypes.TimeInterval", PfKey.NULL_KEY_VERSION)
        );
    // @formatter:on

    /**
     * Get the predefined policy types.
     *
     * @return the predefined policy types
     */
    public static Collection<PfConceptKey> getPredefinedDataTypes() {
        return PREDEFINED_TOSCA_DATA_TYPES;
    }

    /**
     * Assert that data types have been specified correctly.
     *
     * @param serviceTemplate the service template containing data types to be checked
     */
    public static void assertDataTypesExist(final JpaToscaServiceTemplate serviceTemplate) {
        assertExist(serviceTemplate, ToscaUtils::checkDataTypesExist);
    }

    /**
     * Assert that policy types have been specified correctly.
     *
     * @param serviceTemplate the service template containing policy types to be checked
     */
    public static void assertPolicyTypesExist(final JpaToscaServiceTemplate serviceTemplate) {
        assertExist(serviceTemplate, ToscaUtils::checkPolicyTypesExist);
    }

    /**
     * Assert that policies have been specified correctly.
     *
     * @param serviceTemplate the service template containing policy types to be checked
     */
    public static void assertPoliciesExist(final JpaToscaServiceTemplate serviceTemplate) {
        assertExist(serviceTemplate, ToscaUtils::checkPoliciesExist);
    }

    /**
     * Assert that node templates have been specified correctly.
     *
     * @param serviceTemplate the service template containing node templates to be checked
     */
    public static void assertNodeTemplatesExist(final JpaToscaServiceTemplate serviceTemplate) {
        assertExist(serviceTemplate, ToscaUtils::checkNodeTemplateExist);
    }

    /**
     * Check that data types have been specified correctly.
     *
     * @param serviceTemplate the service template containing data types to be checked
     */
    public static boolean doDataTypesExist(final JpaToscaServiceTemplate serviceTemplate) {
        return doExist(serviceTemplate, ToscaUtils::checkDataTypesExist);
    }

    /**
     * Check that policy types have been specified correctly.
     *
     * @param serviceTemplate the service template containing policy types to be checked
     */
    public static boolean doPolicyTypesExist(final JpaToscaServiceTemplate serviceTemplate) {
        return doExist(serviceTemplate, ToscaUtils::checkPolicyTypesExist);
    }

    /**
     * Check that policies have been specified correctly.
     *
     * @param serviceTemplate the service template containing policy types to be checked
     */
    public static boolean doPoliciesExist(final JpaToscaServiceTemplate serviceTemplate) {

        return doExist(serviceTemplate, ToscaUtils::checkPoliciesExist);
    }

    /**
     * Check that tosca node templates have been specified correctly.
     *
     * @param serviceTemplate the service template containing node templates to be checked
     */
    public static boolean doNodeTemplatesExist(final JpaToscaServiceTemplate serviceTemplate) {

        return doExist(serviceTemplate, ToscaUtils::checkNodeTemplateExist);
    }

    /**
     * Assert that something have been specified correctly.
     *
     * @param serviceTemplate the service template containing policy types to be checked
     */
    public static void assertExist(final JpaToscaServiceTemplate serviceTemplate,
            final Function<JpaToscaServiceTemplate, String> checkerFunction) {
        String message = checkerFunction.apply(serviceTemplate);
        if (message != null) {
            throw new PfModelRuntimeException(Response.Status.NOT_FOUND, message);
        }
    }

    /**
     * Check that something have been specified correctly.
     *
     * @param serviceTemplate the service template containing policy types to be checked
     */
    public static boolean doExist(final JpaToscaServiceTemplate serviceTemplate,
            final Function<JpaToscaServiceTemplate, String> checkerFunction) {
        return checkerFunction.apply(serviceTemplate) == null;
    }

    /**
     * Check if data types have been specified correctly.
     */
    public static String checkDataTypesExist(final JpaToscaServiceTemplate serviceTemplate) {
        if (serviceTemplate.getDataTypes() == null) {
            return "no data types specified on service template";
        }

        if (serviceTemplate.getDataTypes().getConceptMap().isEmpty()) {
            return "list of data types specified on service template is empty";
        }

        return null;
    }

    /**
     * Check if policy types have been specified correctly.
     */
    public static String checkPolicyTypesExist(final JpaToscaServiceTemplate serviceTemplate) {
        if (serviceTemplate.getPolicyTypes() == null) {
            return "no policy types specified on service template";
        }

        if (serviceTemplate.getPolicyTypes().getConceptMap().isEmpty()) {
            return "list of policy types specified on service template is empty";
        }

        return null;
    }

    /**
     * Check if policies have been specified correctly.
     */
    public static String checkPoliciesExist(final JpaToscaServiceTemplate serviceTemplate) {
        if (serviceTemplate.getTopologyTemplate() == null) {
            return "topology template not specified on service template";
        }

        if (serviceTemplate.getTopologyTemplate().getPolicies() == null) {
            return "no policies specified on topology template of service template";
        }

        if (serviceTemplate.getTopologyTemplate().getPolicies().getConceptMap().isEmpty()) {
            return "list of policies specified on topology template of service template is empty";
        }

        return null;
    }

    /**
     * Check if node templates have been specified correctly.
     */
    public static String checkNodeTemplateExist(final JpaToscaServiceTemplate serviceTemplate) {
        if (serviceTemplate.getTopologyTemplate().getNodeTemplates() == null) {
            return "node templates not present on the service template";
        }

        if (serviceTemplate.getTopologyTemplate().getNodeTemplates().getConceptMap().isEmpty()) {
            return "no parameters present on the node templates";
        }
        return null;
    }

    /**
     * getLatestPolicyTypeVersion Find all the ancestors of an entity type.
     *
     * @param entityTypes the set of entity types that exist
     * @param entityType the entity type for which to get the parents
     * @param result the result of the ancestor search with any warnings or errors
     * @return the entity set containing the ancestors of the incoming entity
     */
    public static Collection<JpaToscaEntityType<ToscaEntity>> getEntityTypeAncestors(
            @NonNull PfConceptContainer<? extends PfConcept, ? extends PfNameVersion> entityTypes,
            @NonNull JpaToscaEntityType<?> entityType, @NonNull final BeanValidationResult result) {

        PfConceptKey parentEntityTypeKey = entityType.getDerivedFrom();
        if (parentEntityTypeKey == null || parentEntityTypeKey.getName().endsWith(ROOT_KEY_NAME_SUFFIX)) {
            return CollectionUtils.emptyCollection();
        }

        if (entityType.getKey().equals(parentEntityTypeKey)) {
            result.addResult("entity type", entityType.getKey().getId(),
                            ValidationStatus.INVALID, "ancestor of itself");
            throw new PfModelRuntimeException(Response.Status.CONFLICT, result.getResult());
        }

        @SuppressWarnings("unchecked")
        Set<JpaToscaEntityType<ToscaEntity>> ancestorEntitySet = (Set<JpaToscaEntityType<ToscaEntity>>) entityTypes
                .getAll(parentEntityTypeKey.getName(), parentEntityTypeKey.getVersion());
        Set<JpaToscaEntityType<ToscaEntity>> ancestorEntitySetToReturn = new HashSet<>(ancestorEntitySet);
        if (ancestorEntitySet.isEmpty()) {
            result.addResult("parent", parentEntityTypeKey.getId(), ValidationStatus.INVALID, Validated.NOT_FOUND);
        } else {
            for (JpaToscaEntityType<?> filteredEntityType : ancestorEntitySet) {
                ancestorEntitySetToReturn.addAll(getEntityTypeAncestors(entityTypes, filteredEntityType, result));
            }
        }
        return ancestorEntitySetToReturn;
    }

    /**
     * Get the entity tree from a concept container for a given entity key.
     *
     * @param entityTypes the concept container containing entity types
     * @param entityName the name of the entity
     * @param entityVersion the version of the entity
     */
    public static void getEntityTree(
            @NonNull final PfConceptContainer<? extends PfConcept, ? extends PfNameVersion> entityTypes,
            final String entityName, final String entityVersion) {

        var result = new BeanValidationResult("entity", entityName);

        @SuppressWarnings("unchecked")
        Set<JpaToscaEntityType<?>> filteredEntitySet =
                (Set<JpaToscaEntityType<?>>) entityTypes.getAllNamesAndVersions(entityName, entityVersion);
        Set<JpaToscaEntityType<?>> filteredEntitySetToReturn = new HashSet<>(filteredEntitySet);
        for (JpaToscaEntityType<?> filteredEntityType : filteredEntitySet) {
            filteredEntitySetToReturn
                    .addAll(ToscaUtils.getEntityTypeAncestors(entityTypes, filteredEntityType, result));
        }

        if (!result.isValid()) {
            throw new PfModelRuntimeException(Response.Status.NOT_ACCEPTABLE, result.getResult());
        }

        entityTypes.getConceptMap().entrySet()
                .removeIf(entityEntry -> !filteredEntitySetToReturn.contains(entityEntry.getValue()));
    }
}
