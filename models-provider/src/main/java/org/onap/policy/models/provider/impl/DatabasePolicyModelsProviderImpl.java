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

package org.onap.policy.models.provider.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Base64;

import javax.ws.rs.core.Response;

import lombok.NonNull;

import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.base.PfModelRuntimeException;
import org.onap.policy.models.dao.DaoParameters;
import org.onap.policy.models.dao.PfDao;
import org.onap.policy.models.dao.PfDaoFactory;
import org.onap.policy.models.dao.impl.DefaultPfDao;
import org.onap.policy.models.pap.concepts.PdpGroups;
import org.onap.policy.models.pap.provider.PapProvider;
import org.onap.policy.models.provider.PolicyModelsProvider;
import org.onap.policy.models.provider.PolicyModelsProviderParameters;
import org.onap.policy.models.tosca.legacy.concepts.LegacyGuardPolicy;
import org.onap.policy.models.tosca.legacy.concepts.LegacyOperationalPolicy;
import org.onap.policy.models.tosca.legacy.provider.LegacyProvider;
import org.onap.policy.models.tosca.simple.concepts.ToscaServiceTemplate;
import org.onap.policy.models.tosca.simple.provider.SimpleToscaProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides an implementation of the Policy Models Provider for the ONAP Policy Framework that works towards
 * a relational database.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class DatabasePolicyModelsProviderImpl implements PolicyModelsProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultPfDao.class);

    private final PolicyModelsProviderParameters parameters;

    // Database connection and the DAO for reading and writing Policy Framework concepts
    private Connection connection;
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
    public void init() throws PfModelException {
        LOGGER.debug("opening the database connection to {} using persistence unit {}", parameters.getDatabaseUrl(),
                parameters.getPersistenceUnit());

        // Decode the password using Base64
        String decodedPassword = new String(Base64.getDecoder().decode(parameters.getDatabasePassword()));

        // Connect to the database, call does not implement AutoCloseable for try-with-resources
        try {
            connection = DriverManager.getConnection(parameters.getDatabaseUrl(), parameters.getDatabaseUser(),
                    decodedPassword);
        } catch (Exception exc) {
            String errorMessage = "could not connect to database with URL \"" + parameters.getDatabaseUrl() + "\"";
            LOGGER.warn(errorMessage, exc);
            throw new PfModelException(Response.Status.NOT_ACCEPTABLE, errorMessage, exc);
        }

        // Parameters for the DAO
        final DaoParameters daoParameters = new DaoParameters();
        daoParameters.setPluginClass(DefaultPfDao.class.getCanonicalName());
        daoParameters.setPersistenceUnit(parameters.getPersistenceUnit());

        try {
            pfDao = new PfDaoFactory().createPfDao(daoParameters);
            pfDao.init(daoParameters);
        } catch (Exception exc) {
            String errorMessage = "could not create Data Access Object (DAO) using url \"" + parameters.getDatabaseUrl()
                    + "\" and persistence unit \"" + parameters.getPersistenceUnit() + "\"";
            LOGGER.warn(errorMessage, exc);

            this.close();
            throw new PfModelException(Response.Status.NOT_ACCEPTABLE, errorMessage, exc);
        }
    }

    @Override
    public void close() throws PfModelException {
        LOGGER.debug("closing the database connection to {} using persistence unit {}", parameters.getDatabaseUrl(),
                parameters.getPersistenceUnit());

        if (pfDao != null) {
            pfDao.close();
            pfDao = null;
        }

        if (connection != null) {
            try {
                connection.close();
            } catch (Exception exc) {

                String errorMessage =
                        "could not close connection to database with URL \"" + parameters.getDatabaseUrl() + "\"";
                LOGGER.warn(errorMessage, exc);
                throw new PfModelException(Response.Status.INTERNAL_SERVER_ERROR, errorMessage, exc);
            } finally {
                connection = null;
            }
        }

        LOGGER.debug("closed the database connection to {} using persistence unit {}", parameters.getDatabaseUrl(),
                parameters.getPersistenceUnit());
    }

    @Override
    public ToscaServiceTemplate getPolicyTypes(@NonNull final PfConceptKey policyTypeKey) throws PfModelException {
        assertInitilized();
        return new SimpleToscaProvider().getPolicyTypes(pfDao, policyTypeKey);
    }

    @Override
    public ToscaServiceTemplate createPolicyTypes(@NonNull final ToscaServiceTemplate serviceTemplate)
            throws PfModelException {
        assertInitilized();
        return new SimpleToscaProvider().createPolicyTypes(pfDao, serviceTemplate);
    }

    @Override
    public ToscaServiceTemplate updatePolicyTypes(@NonNull final ToscaServiceTemplate serviceTemplate)
            throws PfModelException {
        assertInitilized();
        return new SimpleToscaProvider().updatePolicyTypes(pfDao, serviceTemplate);
    }

    @Override
    public ToscaServiceTemplate deletePolicyTypes(@NonNull final PfConceptKey policyTypeKey) throws PfModelException {
        assertInitilized();
        return new SimpleToscaProvider().deletePolicyTypes(pfDao, policyTypeKey);
    }

    @Override
    public ToscaServiceTemplate getPolicies(@NonNull final PfConceptKey policyKey) throws PfModelException {
        assertInitilized();
        return new SimpleToscaProvider().getPolicies(pfDao, policyKey);
    }

    @Override
    public ToscaServiceTemplate createPolicies(@NonNull final ToscaServiceTemplate serviceTemplate)
            throws PfModelException {
        assertInitilized();
        return new SimpleToscaProvider().createPolicies(pfDao, serviceTemplate);
    }

    @Override
    public ToscaServiceTemplate updatePolicies(@NonNull final ToscaServiceTemplate serviceTemplate)
            throws PfModelException {
        assertInitilized();
        return new SimpleToscaProvider().updatePolicies(pfDao, serviceTemplate);
    }

    @Override
    public ToscaServiceTemplate deletePolicies(@NonNull final PfConceptKey policyKey) throws PfModelException {
        assertInitilized();
        return new SimpleToscaProvider().deletePolicies(pfDao, policyKey);
    }

    @Override
    public LegacyOperationalPolicy getOperationalPolicy(@NonNull final String policyId) throws PfModelException {
        assertInitilized();
        return new LegacyProvider().getOperationalPolicy(pfDao, policyId);
    }

    @Override
    public LegacyOperationalPolicy createOperationalPolicy(
            @NonNull final LegacyOperationalPolicy legacyOperationalPolicy) throws PfModelException {
        assertInitilized();
        return new LegacyProvider().createOperationalPolicy(pfDao, legacyOperationalPolicy);
    }

    @Override
    public LegacyOperationalPolicy updateOperationalPolicy(
            @NonNull final LegacyOperationalPolicy legacyOperationalPolicy) throws PfModelException {
        assertInitilized();
        return new LegacyProvider().updateOperationalPolicy(pfDao, legacyOperationalPolicy);
    }

    @Override
    public LegacyOperationalPolicy deleteOperationalPolicy(@NonNull final String policyId) throws PfModelException {
        assertInitilized();
        return new LegacyProvider().deleteOperationalPolicy(pfDao, policyId);
    }

    @Override
    public LegacyGuardPolicy getGuardPolicy(@NonNull final String policyId) throws PfModelException {
        assertInitilized();
        return new LegacyProvider().getGuardPolicy(pfDao, policyId);
    }

    @Override
    public LegacyGuardPolicy createGuardPolicy(@NonNull final LegacyGuardPolicy legacyGuardPolicy)
            throws PfModelException {
        assertInitilized();
        return new LegacyProvider().createGuardPolicy(pfDao, legacyGuardPolicy);
    }

    @Override
    public LegacyGuardPolicy updateGuardPolicy(@NonNull final LegacyGuardPolicy legacyGuardPolicy)
            throws PfModelException {
        assertInitilized();
        return new LegacyProvider().updateGuardPolicy(pfDao, legacyGuardPolicy);
    }

    @Override
    public LegacyGuardPolicy deleteGuardPolicy(@NonNull final String policyId) throws PfModelException {
        assertInitilized();
        return new LegacyProvider().deleteGuardPolicy(pfDao, policyId);
    }

    @Override
    public PdpGroups getPdpGroups(@NonNull String pdpGroupFilter) throws PfModelException {
        assertInitilized();
        return new PapProvider().getPdpGroups(pfDao, pdpGroupFilter);
    }

    @Override
    public PdpGroups createPdpGroups(@NonNull PdpGroups pdpGroups) throws PfModelException {
        assertInitilized();
        return new PapProvider().createPdpGroups(pfDao, pdpGroups);
    }

    @Override
    public PdpGroups updatePdpGroups(@NonNull PdpGroups pdpGroups) throws PfModelException {
        assertInitilized();
        return new PapProvider().updatePdpGroups(pfDao, pdpGroups);
    }

    @Override
    public PdpGroups deletePdpGroups(@NonNull String pdpGroupFilter) throws PfModelException {
        assertInitilized();
        return new PapProvider().deletePdpGroups(pfDao, pdpGroupFilter);
    }

    /**
     * Check if the model provider is initialized.
     */
    private void assertInitilized() {
        if (pfDao == null) {
            String errorMessage = "policy models provider is not initilaized";
            LOGGER.warn(errorMessage);
            throw new PfModelRuntimeException(Response.Status.BAD_REQUEST, errorMessage);
        }
    }

    /**
     * Hook for unit test mocking of database connection.
     *
     * @param client the mocked client
     */
    protected void setConnection(final Connection connection) {
        this.connection = connection;
    }
}
