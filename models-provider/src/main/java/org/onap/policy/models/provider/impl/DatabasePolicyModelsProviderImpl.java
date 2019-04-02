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
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import lombok.NonNull;

import org.apache.commons.lang3.tuple.Pair;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.base.PfModelRuntimeException;
import org.onap.policy.models.dao.DaoParameters;
import org.onap.policy.models.dao.PfDao;
import org.onap.policy.models.dao.PfDaoFactory;
import org.onap.policy.models.dao.impl.DefaultPfDao;
import org.onap.policy.models.pdp.concepts.PdpGroup;
import org.onap.policy.models.pdp.concepts.PdpStatistics;
import org.onap.policy.models.pdp.concepts.PdpSubGroup;
import org.onap.policy.models.pdp.persistence.provider.PdpProvider;
import org.onap.policy.models.provider.PolicyModelsProvider;
import org.onap.policy.models.provider.PolicyModelsProviderParameters;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicy;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyType;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;
import org.onap.policy.models.tosca.authorative.provider.AuthorativeToscaProvider;
import org.onap.policy.models.tosca.legacy.concepts.LegacyGuardPolicyInput;
import org.onap.policy.models.tosca.legacy.concepts.LegacyGuardPolicyOutput;
import org.onap.policy.models.tosca.legacy.concepts.LegacyOperationalPolicy;
import org.onap.policy.models.tosca.legacy.provider.LegacyProvider;
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

        if (connection != null || pfDao != null) {
            String errorMessage = "provider is already initialized";
            LOGGER.warn(errorMessage);
            throw new PfModelException(Response.Status.NOT_ACCEPTABLE, errorMessage);
        }

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
    public ToscaServiceTemplate getPolicyTypes(final String name, final String version) throws PfModelException {
        assertInitilized();
        return new AuthorativeToscaProvider().getPolicyTypes(pfDao, name, version);
    }

    @Override
    public List<ToscaPolicyType> getPolicyTypeList(final String name, final String version) throws PfModelException {
        assertInitilized();
        return new AuthorativeToscaProvider().getPolicyTypeList(pfDao, name, version);
    }

    @Override
    public ToscaServiceTemplate getLatestPolicyTypes(final String name) throws PfModelException {
        assertInitilized();
        return new AuthorativeToscaProvider().getLatestPolicyTypes(pfDao, name);
    }

    @Override
    public List<ToscaPolicyType> getLatestPolicyTypeList(final String name) throws PfModelException {
        assertInitilized();
        return new AuthorativeToscaProvider().getLatestPolicyTypeList(pfDao, name);
    }

    @Override
    public ToscaServiceTemplate createPolicyTypes(@NonNull final ToscaServiceTemplate serviceTemplate)
            throws PfModelException {
        assertInitilized();
        return new AuthorativeToscaProvider().createPolicyTypes(pfDao, serviceTemplate);
    }

    @Override
    public ToscaServiceTemplate updatePolicyTypes(@NonNull final ToscaServiceTemplate serviceTemplate)
            throws PfModelException {
        assertInitilized();
        return new AuthorativeToscaProvider().updatePolicyTypes(pfDao, serviceTemplate);
    }

    @Override
    public ToscaServiceTemplate deletePolicyType(@NonNull final String name, @NonNull final String version)
            throws PfModelException {
        assertInitilized();
        return new AuthorativeToscaProvider().deletePolicyType(pfDao, name, version);
    }

    @Override
    public ToscaServiceTemplate getPolicies(final String name, final String version) throws PfModelException {
        assertInitilized();
        return new AuthorativeToscaProvider().getPolicies(pfDao, name, version);
    }

    @Override
    public List<ToscaPolicy> getPolicyList(final String name, final String version) throws PfModelException {
        assertInitilized();
        return new AuthorativeToscaProvider().getPolicyList(pfDao, name, version);
    }

    @Override
    public List<ToscaPolicy> getPolicyList4PolicyType(@NonNull final String policyTypeName,
            final String policyTypeVersion) throws PfModelException {
        assertInitilized();
        return new AuthorativeToscaProvider().getPolicyList4PolicyType(pfDao, policyTypeName, policyTypeVersion);
    }

    @Override
    public ToscaServiceTemplate getLatestPolicies(final String name) throws PfModelException {
        assertInitilized();
        return new AuthorativeToscaProvider().getLatestPolicies(pfDao, name);
    }

    @Override
    public List<ToscaPolicy> getLatestPolicyList(final String name) throws PfModelException {
        assertInitilized();
        return new AuthorativeToscaProvider().getLatestPolicyList(pfDao, name);
    }

    @Override
    public ToscaServiceTemplate createPolicies(@NonNull final ToscaServiceTemplate serviceTemplate)
            throws PfModelException {
        assertInitilized();
        return new AuthorativeToscaProvider().createPolicies(pfDao, serviceTemplate);
    }

    @Override
    public ToscaServiceTemplate updatePolicies(@NonNull final ToscaServiceTemplate serviceTemplate)
            throws PfModelException {
        assertInitilized();
        return new AuthorativeToscaProvider().updatePolicies(pfDao, serviceTemplate);
    }

    @Override
    public ToscaServiceTemplate deletePolicy(@NonNull final String name, @NonNull final String version)
            throws PfModelException {
        assertInitilized();
        return new AuthorativeToscaProvider().deletePolicy(pfDao, name, version);
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
    public Map<String, LegacyGuardPolicyOutput> getGuardPolicy(@NonNull final String policyId) throws PfModelException {
        assertInitilized();
        return new LegacyProvider().getGuardPolicy(pfDao, policyId);
    }

    @Override
    public Map<String, LegacyGuardPolicyOutput> createGuardPolicy(
            @NonNull final LegacyGuardPolicyInput legacyGuardPolicy) throws PfModelException {
        assertInitilized();
        return new LegacyProvider().createGuardPolicy(pfDao, legacyGuardPolicy);
    }

    @Override
    public Map<String, LegacyGuardPolicyOutput> updateGuardPolicy(
            @NonNull final LegacyGuardPolicyInput legacyGuardPolicy) throws PfModelException {
        assertInitilized();
        return new LegacyProvider().updateGuardPolicy(pfDao, legacyGuardPolicy);
    }

    @Override
    public Map<String, LegacyGuardPolicyOutput> deleteGuardPolicy(@NonNull final String policyId)
            throws PfModelException {
        assertInitilized();
        return new LegacyProvider().deleteGuardPolicy(pfDao, policyId);
    }

    @Override
    public List<PdpGroup> getPdpGroups(final String name, final String version) throws PfModelException {
        assertInitilized();
        return new PdpProvider().getPdpGroups(pfDao, name, version);
    }

    @Override
    public List<PdpGroup> getLatestPdpGroups(final String name) throws PfModelException {
        assertInitilized();
        return new PdpProvider().getLatestPdpGroups(pfDao, name);
    }

    @Override
    public List<PdpGroup> getFilteredPdpGroups(final String pdpType,
            @NonNull final List<Pair<String, String>> supportedPolicyTypes) {
        assertInitilized();
        return new PdpProvider().getFilteredPdpGroups(pfDao, pdpType, supportedPolicyTypes);
    }

    @Override
    public List<PdpGroup> createPdpGroups(@NonNull final List<PdpGroup> pdpGroups) throws PfModelException {
        assertInitilized();
        return new PdpProvider().createPdpGroups(pfDao, pdpGroups);
    }

    @Override
    public List<PdpGroup> updatePdpGroups(@NonNull final List<PdpGroup> pdpGroups) throws PfModelException {
        assertInitilized();
        return new PdpProvider().updatePdpGroups(pfDao, pdpGroups);
    }

    @Override
    public void updatePdpSubGroup(@NonNull final String pdpGroupName, @NonNull final String pdpGroupVersion,
            @NonNull final PdpSubGroup pdpSubGroup) throws PfModelException {
        assertInitilized();
        new PdpProvider().updatePdpSubGroup(pfDao, pdpGroupName, pdpGroupVersion, pdpSubGroup);
    }

    @Override
    public PdpGroup deletePdpGroup(@NonNull final String name, @NonNull final String version) throws PfModelException {
        assertInitilized();
        return new PdpProvider().deletePdpGroup(pfDao, name, version);
    }

    @Override
    public List<PdpStatistics> getPdpStatistics(final String name, final String version) throws PfModelException {
        assertInitilized();
        return new PdpProvider().getPdpStatistics(pfDao, name, version);
    }

    @Override
    public void updatePdpStatistics(@NonNull final String pdpGroupName, @NonNull final String pdpGroupVersion,
            @NonNull final String pdpType, @NonNull final String pdpInstanceId,
            @NonNull final PdpStatistics pdppStatistics) throws PfModelException {
        assertInitilized();
        new PdpProvider().updatePdpStatistics(pfDao, pdpGroupName, pdpGroupVersion, pdpType, pdpInstanceId,
                pdppStatistics);
    }

    @Override
    public Map<Pair<String, String>, List<ToscaPolicy>> getDeployedPolicyList(final String name)
            throws PfModelException {
        assertInitilized();
        return new PdpProvider().getDeployedPolicyList(pfDao, name);
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
