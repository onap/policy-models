/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation.
 *  Modifications Copyright (C) 2021 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.models.provider.impl;

import java.io.Closeable;
import java.util.Properties;
import javax.ws.rs.core.Response;
import lombok.Getter;
import lombok.NonNull;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.dao.DaoParameters;
import org.onap.policy.models.dao.PfDao;
import org.onap.policy.models.dao.PfDaoFactory;
import org.onap.policy.models.dao.impl.DefaultPfDao;
import org.onap.policy.models.provider.PolicyModelsProviderParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides an abstract models provider that can be used to initialise the database for any specialisation of
 * the class.
 */
public abstract class AbstractModelsProvider implements Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractModelsProvider.class);

    private final PolicyModelsProviderParameters parameters;

    // Database connection and the DAO for reading and writing Policy Framework concepts
    @Getter
    private PfDao pfDao;

    /**
     * Constructor that takes the parameters.
     *
     * @param parameters the parameters for the provider
     */
    protected AbstractModelsProvider(@NonNull final PolicyModelsProviderParameters parameters) {
        this.parameters = parameters;
    }

    /**
     * Initialise the provider.
     *
     * @throws PfModelException in initialisation errors
     */
    public synchronized void init() throws PfModelException {
        LOGGER.debug("opening the database connection to {} using persistence unit {}", parameters.getDatabaseUrl(),
                parameters.getPersistenceUnit());

        if (pfDao != null) {
            var errorMessage = "provider is already initialized";
            throw new PfModelException(Response.Status.NOT_ACCEPTABLE, errorMessage);
        }

        // Parameters for the DAO
        final var daoParameters = new DaoParameters();
        daoParameters.setPluginClass(DefaultPfDao.class.getName());
        daoParameters.setPersistenceUnit(parameters.getPersistenceUnit());

        // @formatter:off
        var jdbcProperties = new Properties();
        jdbcProperties.setProperty(PersistenceUnitProperties.JDBC_DRIVER,   parameters.getDatabaseDriver());
        jdbcProperties.setProperty(PersistenceUnitProperties.JDBC_URL,      parameters.getDatabaseUrl());
        jdbcProperties.setProperty(PersistenceUnitProperties.JDBC_USER,     parameters.getDatabaseUser());
        jdbcProperties.setProperty(PersistenceUnitProperties.JDBC_PASSWORD, parameters.getDatabasePassword());
        jdbcProperties.setProperty(PersistenceUnitProperties.TARGET_DATABASE,
                        (parameters.getDatabaseType() == null ? "MySQL" : parameters.getDatabaseType()));
        // @formatter:on

        daoParameters.setJdbcProperties(jdbcProperties);

        try {
            pfDao = new PfDaoFactory().createPfDao(daoParameters);
            pfDao.init(daoParameters);
        } catch (Exception exc) {
            String errorMessage = "could not create Data Access Object (DAO) using url \"" + parameters.getDatabaseUrl()
                    + "\" and persistence unit \"" + parameters.getPersistenceUnit() + "\"";

            this.close();
            throw new PfModelException(Response.Status.NOT_ACCEPTABLE, errorMessage, exc);
        }
    }

    @Override
    public synchronized void close() {
        LOGGER.debug("closing the database connection to {} using persistence unit {}", parameters.getDatabaseUrl(),
                parameters.getPersistenceUnit());

        if (pfDao != null) {
            pfDao.close();
            pfDao = null;
        }

        LOGGER.debug("closed the database connection to {} using persistence unit {}", parameters.getDatabaseUrl(),
                parameters.getPersistenceUnit());
    }
}
