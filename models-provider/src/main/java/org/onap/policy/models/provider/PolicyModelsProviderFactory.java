/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
 *  Modifications Copyright (C) 2019-2020 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.models.provider;

import javax.ws.rs.core.Response;
import lombok.NonNull;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.dao.impl.ProxyDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A factory for creating PolicyModelsProvider objects using the default Policy Framework implementation.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class PolicyModelsProviderFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(PolicyModelsProviderFactory.class);

    /**
     * Create PolicyModelsProvider.
     *
     * @param pfDao the ProxyDao
     * @param parameters the PolicyModelsProviderParameters
     * @return the PolicyModelsProvider
     * @throws PfModelException on errors creating an implementation of the PolicyModelProvider
     */
    public PolicyModelsProvider createPolicyModelsProvider(@NonNull final ProxyDao pfDao,
            @NonNull final PolicyModelsProviderParameters parameters) throws PfModelException {
        // Get the class for the PolicyModelsProvider
        Class<?> implementationClass = null;
        try {
            // Check if the implementation class is on the classpath
            implementationClass = Class.forName(parameters.getImplementation());
        } catch (final Exception exc) {
            String errorMessage = "could not find implementation of the \"PolicyModelsProvider\" interface \""
                    + parameters.getImplementation() + "\"";
            LOGGER.warn(errorMessage);
            throw new PfModelException(Response.Status.NOT_FOUND, errorMessage, exc);
        }

        // It is, now check if it is a PolicyModelsProvider
        if (!PolicyModelsProvider.class.isAssignableFrom(implementationClass)) {
            String errorMessage = "the class \"" + implementationClass.getName()
                    + "\" is not an implementation of the \"PolicyModelsProvider\" interface";
            LOGGER.warn(errorMessage);
            throw new PfModelException(Response.Status.BAD_REQUEST, errorMessage);
        }

        try {
            return (PolicyModelsProvider) implementationClass.getConstructor(ProxyDao.class).newInstance(pfDao);
        } catch (Exception exc) {
            String errorMessage =
                    "could not create an instance of PolicyModelsProvider \"" + parameters.getImplementation() + "\"";
            LOGGER.warn(errorMessage);
            throw new PfModelException(Response.Status.INTERNAL_SERVER_ERROR, errorMessage, exc);
        }
    }

    /**
     * Creates a new PolicyModelsProvider object from its implementation.
     *
     * @param parameters The parameters for the implementation of the PolicyModelProvider
     * @throws PfModelException on errors creating an implementation of the PolicyModelProvider
     */
    public PolicyModelsProvider createPolicyModelsProvider(@NonNull final PolicyModelsProviderParameters parameters)
            throws PfModelException {
        // Get the class for the PolicyModelsProvider
        Class<?> implementationClass = null;
        try {
            // Check if the implementation class is on the classpath
            implementationClass = Class.forName(parameters.getImplementation());
        } catch (final Exception exc) {
            String errorMessage = "could not find implementation of the \"PolicyModelsProvider\" interface \""
                    + parameters.getImplementation() + "\"";
            LOGGER.warn(errorMessage);
            throw new PfModelException(Response.Status.NOT_FOUND, errorMessage, exc);
        }

        // It is, now check if it is a PolicyModelsProvider
        if (!PolicyModelsProvider.class.isAssignableFrom(implementationClass)) {
            String errorMessage = "the class \"" + implementationClass.getName()
                    + "\" is not an implementation of the \"PolicyModelsProvider\" interface";
            LOGGER.warn(errorMessage);
            throw new PfModelException(Response.Status.BAD_REQUEST, errorMessage);
        }

        try {
            PolicyModelsProvider provider = (PolicyModelsProvider) implementationClass
                    .getConstructor(PolicyModelsProviderParameters.class).newInstance(parameters);

            provider.init();

            return provider;
        } catch (Exception exc) {
            String errorMessage =
                    "could not create an instance of PolicyModelsProvider \"" + parameters.getImplementation() + "\"";
            LOGGER.warn(errorMessage);
            throw new PfModelException(Response.Status.INTERNAL_SERVER_ERROR, errorMessage, exc);
        }
    }
}
