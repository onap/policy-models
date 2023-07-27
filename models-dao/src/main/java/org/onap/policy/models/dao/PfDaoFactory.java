/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019, 2023 Nordix Foundation.
 *  Modifications Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.models.dao;

import jakarta.ws.rs.core.Response;
import java.lang.reflect.InvocationTargetException;
import org.onap.policy.common.utils.validation.Assertions;
import org.onap.policy.models.base.PfModelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This factory class returns a Policy Framework DAO for the configured persistence mechanism. The
 * factory uses the plugin class specified in {@link DaoParameters} to instantiate a DAO instance.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class PfDaoFactory {
    // Get a reference to the logger
    private static final Logger LOGGER = LoggerFactory.getLogger(PfDaoFactory.class);

    /**
     * Return a Policy Framework DAO for the required Policy Framework DAO plugin class.
     *
     * @param daoParameters parameters to use to read the database configuration information
     * @return the Policy Framework DAO
     * @throws PfModelException on invalid JPA plugins
     */
    public PfDao createPfDao(final DaoParameters daoParameters) throws PfModelException {
        Assertions.argumentOfClassNotNull(daoParameters, PfModelException.class,
                "Parameter \"daoParameters\" may not be null");

        // Get the class for the DAO using reflection
        Object pfDaoObject;
        try {
            pfDaoObject = Class.forName(daoParameters.getPluginClass()).getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | IllegalArgumentException
                        | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            String errorMessage =
                    "Policy Framework DAO class not found for DAO plugin \"" + daoParameters.getPluginClass() + "\"";
            LOGGER.error(errorMessage);
            throw new PfModelException(Response.Status.INTERNAL_SERVER_ERROR, errorMessage, e);
        }

        // Check the class is a Policy Framework DAO
        if (!(pfDaoObject instanceof PfDao)) {
            String errorMessage = "Specified DAO plugin class \"" + daoParameters.getPluginClass()
                    + "\" does not implement the PfDao interface";
            LOGGER.error(errorMessage);
            throw new PfModelException(Response.Status.INTERNAL_SERVER_ERROR, errorMessage);
        }

        return (PfDao) pfDaoObject;
    }
}
