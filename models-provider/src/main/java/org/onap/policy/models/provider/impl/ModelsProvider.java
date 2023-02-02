/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation.
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

import java.util.Properties;
import javax.ws.rs.core.Response;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.dao.DaoParameters;
import org.onap.policy.models.dao.PfDao;
import org.onap.policy.models.dao.PfDaoFactory;
import org.onap.policy.models.dao.impl.DefaultPfDao;
import org.onap.policy.models.provider.PolicyModelsProviderParameters;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ModelsProvider {

    /**
     * Initialise the provider.
     *
     * @throws PfModelException in initialisation errors
     */
    public static PfDao init(PolicyModelsProviderParameters parameters) throws PfModelException {
        // Parameters for the DAO
        final var daoParameters = new DaoParameters();
        daoParameters.setPluginClass(DefaultPfDao.class.getName());
        daoParameters.setPersistenceUnit(parameters.getPersistenceUnit());

        // @formatter:off
        var jdbcProperties = new Properties();
        jdbcProperties.setProperty("javax.persistence.jdbc.driver",   parameters.getDatabaseDriver());
        jdbcProperties.setProperty("javax.persistence.jdbc.url",      parameters.getDatabaseUrl());
        jdbcProperties.setProperty("javax.persistence.jdbc.user",     parameters.getDatabaseUser());
        jdbcProperties.setProperty("javax.persistence.jdbc.password", parameters.getDatabasePassword());
        jdbcProperties.setProperty("hibernate.dialect",
            (parameters.getDatabaseType() == null
                ? "org.hibernate.dialect.MariaDBDialect"
                    : parameters.getDatabaseType()));        // @formatter:on

        daoParameters.setJdbcProperties(jdbcProperties);

        PfDao pfDao = null;
        try {
            pfDao = new PfDaoFactory().createPfDao(daoParameters);
            pfDao.init(daoParameters);
        } catch (Exception exc) {
            String errorMessage = "could not create Data Access Object (DAO) using url \"" + parameters.getDatabaseUrl()
                    + "\" and persistence unit \"" + parameters.getPersistenceUnit() + "\"";
            if (pfDao != null) {
                pfDao.close();
            }

            throw new PfModelException(Response.Status.NOT_ACCEPTABLE, errorMessage, exc);
        }
        return pfDao;
    }

}
