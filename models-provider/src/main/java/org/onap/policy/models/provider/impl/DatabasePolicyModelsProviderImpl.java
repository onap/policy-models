/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2021 Nordix Foundation.
 *  Modifications Copyright (C) 2019-2021 AT&T Intellectual Property. All rights reserved.
 *  Modifications Copyright (C) 2020, 2022 Bell Canada. All rights reserved.
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

import javax.ws.rs.core.Response;
import lombok.Getter;
import lombok.NonNull;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.dao.PfDao;
import org.onap.policy.models.provider.PolicyModelsProviderParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides an implementation of the Policy Models Provider for the ONAP Policy Framework that works towards
 * a relational database.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class DatabasePolicyModelsProviderImpl extends AbstractPolicyModelsProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabasePolicyModelsProviderImpl.class);

    private final PolicyModelsProviderParameters parameters;

    // Database connection and the DAO for reading and writing Policy Framework concepts
    @Getter
    private PfDao pfDao;

    /**
     * Constructor that takes the parameters.
     *
     * @param parameters the parameters for the provider
     */
    public DatabasePolicyModelsProviderImpl(@NonNull final PolicyModelsProviderParameters parameters) {
        this.parameters = parameters;
    }

    @Override
    public synchronized void init() throws PfModelException {
        LOGGER.debug("opening the database connection to {} using persistence unit {}", parameters.getDatabaseUrl(),
                parameters.getPersistenceUnit());

        if (pfDao != null) {
            var errorMessage = "provider is already initialized";
            throw new PfModelException(Response.Status.NOT_ACCEPTABLE, errorMessage);
        }

        pfDao = ModelsProvider.init(parameters);
    }

    @Override
    public synchronized void close() throws PfModelException {
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
