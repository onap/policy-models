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

package org.onap.policy.models.tosca.utils;

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
    }

    /**
     * Check if policy types have been specified is initialized.
     */
    public static void assertPolicyTypesExist(final JpaToscaServiceTemplate serviceTemplate) {
        if (serviceTemplate.getPolicyTypes() == null) {
            String errorMessage = "no policy types specified on service template";
            LOGGER.warn(errorMessage);
            throw new PfModelRuntimeException(Response.Status.BAD_REQUEST, errorMessage);
        }

        if (serviceTemplate.getPolicyTypes().getConceptMap().isEmpty()) {
            String errorMessage = "list of policy types specified on service template is empty";
            LOGGER.warn(errorMessage);
            throw new PfModelRuntimeException(Response.Status.BAD_REQUEST, errorMessage);
        }
    }

    /**
     * Check if policy types have been specified is initialized.
     */
    public static void assertPoliciesExist(final JpaToscaServiceTemplate serviceTemplate) {
        if (serviceTemplate.getTopologyTemplate() == null) {
            String errorMessage = "topology template not specified on service template";
            LOGGER.warn(errorMessage);
            throw new PfModelRuntimeException(Response.Status.BAD_REQUEST, errorMessage);
        }

        if (serviceTemplate.getTopologyTemplate().getPolicies() == null) {
            String errorMessage = "no policies specified on topology template of service template";
            LOGGER.warn(errorMessage);
            throw new PfModelRuntimeException(Response.Status.BAD_REQUEST, errorMessage);
        }

        if (serviceTemplate.getTopologyTemplate().getPolicies().getConceptMap().isEmpty()) {
            String errorMessage = "list of policies specified on topology template of service template is empty";
            LOGGER.warn(errorMessage);
            throw new PfModelRuntimeException(Response.Status.BAD_REQUEST, errorMessage);
        }
    }


}
