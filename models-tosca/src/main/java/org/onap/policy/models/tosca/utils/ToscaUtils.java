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

import java.util.function.Function;

import javax.ws.rs.core.Response;

import org.onap.policy.models.base.PfModelRuntimeException;
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
        assertExist(serviceTemplate, checkDataTypesExist());
    }

    /**
     * Assert that policy types have been specified correctly.
     *
     * @param serviceTemplate the service template containing policy types to be checked
     */
    public static void assertPolicyTypesExist(final JpaToscaServiceTemplate serviceTemplate) {
        assertExist(serviceTemplate, checkPolicyTypesExist());
    }

    /**
     * Assert that policies have been specified correctly.
     *
     * @param serviceTemplate the service template containing policy types to be checked
     */
    public static void assertPoliciesExist(final JpaToscaServiceTemplate serviceTemplate) {
        assertExist(serviceTemplate, checkPoliciesExist());
    }

    /**
     * Check that data types have been specified correctly.
     *
     * @param serviceTemplate the service template containing data types to be checked
     */
    public static boolean doDataTypesExist(final JpaToscaServiceTemplate serviceTemplate) {
        return doExist(serviceTemplate, checkDataTypesExist());
    }

    /**
     * Check that policy types have been specified correctly.
     *
     * @param serviceTemplate the service template containing policy types to be checked
     */
    public static boolean doPolicyTypesExist(final JpaToscaServiceTemplate serviceTemplate) {
        return doExist(serviceTemplate, checkPolicyTypesExist());
    }

    /**
     * Check that policies have been specified correctly.
     *
     * @param serviceTemplate the service template containing policy types to be checked
     */
    public static boolean doPoliciesExist(final JpaToscaServiceTemplate serviceTemplate) {
        return doExist(serviceTemplate, checkPoliciesExist());
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
    public static Function<JpaToscaServiceTemplate, String> checkDataTypesExist() {
        return serviceTemplate -> {
            if (serviceTemplate.getDataTypes() == null) {
                return "no data types specified on service template";
            }

            if (serviceTemplate.getDataTypes().getConceptMap().isEmpty()) {
                return "list of data types specified on service template is empty";
            }

            return null;
        };
    }

    /**
     * Check if policy types have been specified correctly.
     */
    public static Function<JpaToscaServiceTemplate, String> checkPolicyTypesExist() {
        return serviceTemplate -> {
            if (serviceTemplate.getPolicyTypes() == null) {
                return "no policy types specified on service template";
            }

            if (serviceTemplate.getPolicyTypes().getConceptMap().isEmpty()) {
                return "list of policy types specified on service template is empty";
            }

            return null;
        };
    }

    /**
     * Check if policies have been specified correctly.
     */
    public static Function<JpaToscaServiceTemplate, String> checkPoliciesExist() {
        return serviceTemplate -> {
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
        };
    }
}
