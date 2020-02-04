/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2020 Nordix Foundation.
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

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

import javax.ws.rs.core.Response;

import lombok.NonNull;

import org.apache.commons.collections4.CollectionUtils;
import org.onap.policy.models.base.PfConcept;
import org.onap.policy.models.base.PfConceptContainer;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfModelRuntimeException;
import org.onap.policy.models.base.PfNameVersion;
import org.onap.policy.models.base.PfValidationMessage;
import org.onap.policy.models.base.PfValidationResult;
import org.onap.policy.models.base.PfValidationResult.ValidationResult;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaEntityType;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaServiceTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for TOSCA concepts.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public final class ToscaUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(ToscaUtils.class);

    private static final String ROOT_KEY_NAME_SUFFIX = ".Root";

    /**
     * Private constructor to prevent subclassing.
     */
    private ToscaUtils() {
        // Private constructor to prevent subclassing
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
     * Assert that something have been specified correctly.
     *
     * @param serviceTemplate the service template containing policy types to be checked
     */
    public static void assertExist(final JpaToscaServiceTemplate serviceTemplate,
            final Function<JpaToscaServiceTemplate, String> checkerFunction) {
        String message = checkerFunction.apply(serviceTemplate);
        if (message != null) {
            LOGGER.warn(message);
            throw new PfModelRuntimeException(Response.Status.BAD_REQUEST, message);
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
     * Find all the ancestors of an entity type.
     *
     * @param entityTypes the set of entity types that exist
     * @param entityType the entity type for which to get the parents
     * @param result the result of the ancestor search with any warnings or errors
     * @return
     */
    public static Collection<? extends JpaToscaEntityType<?>> getEntityTypeAncestors(
            @NonNull PfConceptContainer<? extends PfConcept, ? extends PfNameVersion> entityTypes,
            @NonNull JpaToscaEntityType<?> entityType, @NonNull final PfValidationResult result) {

        PfConceptKey parentEntityTypeKey = entityType.getDerivedFrom();
        if (parentEntityTypeKey == null || parentEntityTypeKey.getName().endsWith(ROOT_KEY_NAME_SUFFIX)) {
            return CollectionUtils.emptyCollection();
        }

        @SuppressWarnings("unchecked")
        Set<JpaToscaEntityType<?>> ancestorEntitySet = (Set<JpaToscaEntityType<?>>) entityTypes
                .getAll(parentEntityTypeKey.getName(), parentEntityTypeKey.getVersion());

        if (ancestorEntitySet.isEmpty()) {
            result.addValidationMessage(new PfValidationMessage(entityType.getKey(), ToscaUtils.class,
                    ValidationResult.INVALID, "parent " + parentEntityTypeKey.getId() + " of entity not found"));
        } else {
            for (JpaToscaEntityType<?> filteredEntityType : ancestorEntitySet) {
                ancestorEntitySet.addAll(getEntityTypeAncestors(entityTypes, filteredEntityType, result));
            }
        }
        return ancestorEntitySet;
    }
}
