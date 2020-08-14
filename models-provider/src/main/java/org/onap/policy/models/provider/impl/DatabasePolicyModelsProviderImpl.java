/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2020 Nordix Foundation.
 *  Modifications Copyright (C) 2019-2020 AT&T Intellectual Property. All rights reserved.
 *  Modifications Copyright (C) 2020 Bell Canada. All rights reserved.
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

import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import javax.ws.rs.core.Response;
import lombok.NonNull;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.base.PfModelRuntimeException;
import org.onap.policy.models.dao.DaoParameters;
import org.onap.policy.models.dao.PfDao;
import org.onap.policy.models.dao.PfDaoFactory;
import org.onap.policy.models.dao.impl.DefaultPfDao;
import org.onap.policy.models.pdp.concepts.Pdp;
import org.onap.policy.models.pdp.concepts.PdpGroup;
import org.onap.policy.models.pdp.concepts.PdpGroupFilter;
import org.onap.policy.models.pdp.concepts.PdpStatistics;
import org.onap.policy.models.pdp.concepts.PdpSubGroup;
import org.onap.policy.models.pdp.persistence.provider.PdpProvider;
import org.onap.policy.models.pdp.persistence.provider.PdpStatisticsProvider;
import org.onap.policy.models.provider.PolicyModelsProvider;
import org.onap.policy.models.provider.PolicyModelsProviderParameters;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicy;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyFilter;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyIdentifier;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyType;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyTypeFilter;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyTypeIdentifier;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;
import org.onap.policy.models.tosca.authorative.provider.AuthorativeToscaProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides an implementation of the Policy Models Provider for the ONAP Policy Framework that works towards
 * a relational database.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class DatabasePolicyModelsProviderImpl implements PolicyModelsProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabasePolicyModelsProviderImpl.class);

    private final PolicyModelsProviderParameters parameters;

    // Database connection and the DAO for reading and writing Policy Framework concepts
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

        if (pfDao != null) {
            String errorMessage = "provider is already initialized";
            LOGGER.warn(errorMessage);
            throw new PfModelException(Response.Status.NOT_ACCEPTABLE, errorMessage);
        }

        // Parameters for the DAO
        final DaoParameters daoParameters = new DaoParameters();
        daoParameters.setPluginClass(DefaultPfDao.class.getName());
        daoParameters.setPersistenceUnit(parameters.getPersistenceUnit());

        // Decode the password using Base64
        String decodedPassword = new String(Base64.getDecoder().decode(getValue(parameters.getDatabasePassword())));

        // @formatter:off
        Properties jdbcProperties = new Properties();
        jdbcProperties.setProperty(PersistenceUnitProperties.JDBC_DRIVER,   parameters.getDatabaseDriver());
        jdbcProperties.setProperty(PersistenceUnitProperties.JDBC_URL,      parameters.getDatabaseUrl());
        jdbcProperties.setProperty(PersistenceUnitProperties.JDBC_USER,     parameters.getDatabaseUser());
        jdbcProperties.setProperty(PersistenceUnitProperties.JDBC_PASSWORD, decodedPassword);
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

    private String getValue(final String value) {
        if (value != null && value.startsWith("${") && value.endsWith("}")) {
            return System.getenv(value.substring(2, value.length() - 1));
        }
        return value;
    }

    @Override
    public void close() throws PfModelException {
        LOGGER.debug("closing the database connection to {} using persistence unit {}", parameters.getDatabaseUrl(),
                parameters.getPersistenceUnit());

        if (pfDao != null) {
            pfDao.close();
            pfDao = null;
        }

        LOGGER.debug("closed the database connection to {} using persistence unit {}", parameters.getDatabaseUrl(),
                parameters.getPersistenceUnit());
    }

    @Override
    public ToscaServiceTemplate getPolicyTypes(final String name, final String version) throws PfModelException {
        assertInitialized();
        return new AuthorativeToscaProvider().getPolicyTypes(pfDao, name, version);
    }

    @Override
    public List<ToscaPolicyType> getPolicyTypeList(final String name, final String version) throws PfModelException {
        assertInitialized();
        return new AuthorativeToscaProvider().getPolicyTypeList(pfDao, name, version);
    }

    @Override
    public ToscaServiceTemplate getFilteredPolicyTypes(@NonNull ToscaPolicyTypeFilter filter) throws PfModelException {
        assertInitialized();
        return new AuthorativeToscaProvider().getFilteredPolicyTypes(pfDao, filter);
    }

    @Override
    public List<ToscaPolicyType> getFilteredPolicyTypeList(@NonNull ToscaPolicyTypeFilter filter)
            throws PfModelException {
        assertInitialized();
        return new AuthorativeToscaProvider().getFilteredPolicyTypeList(pfDao, filter);
    }

    @Override
    public ToscaServiceTemplate createPolicyTypes(@NonNull final ToscaServiceTemplate serviceTemplate)
            throws PfModelException {
        assertInitialized();
        return new AuthorativeToscaProvider().createPolicyTypes(pfDao, serviceTemplate);
    }

    @Override
    public ToscaServiceTemplate updatePolicyTypes(@NonNull final ToscaServiceTemplate serviceTemplate)
            throws PfModelException {
        assertInitialized();
        return new AuthorativeToscaProvider().updatePolicyTypes(pfDao, serviceTemplate);
    }

    @Override
    public ToscaServiceTemplate deletePolicyType(@NonNull final String name, @NonNull final String version)
            throws PfModelException {
        assertInitialized();

        ToscaPolicyTypeIdentifier policyTypeIdentifier = new ToscaPolicyTypeIdentifier(name, version);
        assertPolicyTypeNotSupportedInPdpGroup(policyTypeIdentifier);

        return new AuthorativeToscaProvider().deletePolicyType(pfDao, name, version);
    }

    @Override
    public ToscaServiceTemplate getPolicies(final String name, final String version) throws PfModelException {
        assertInitialized();
        return new AuthorativeToscaProvider().getPolicies(pfDao, name, version);
    }

    @Override
    public List<ToscaPolicy> getPolicyList(final String name, final String version) throws PfModelException {
        assertInitialized();
        return new AuthorativeToscaProvider().getPolicyList(pfDao, name, version);
    }

    @Override
    public ToscaServiceTemplate getFilteredPolicies(@NonNull ToscaPolicyFilter filter) throws PfModelException {
        assertInitialized();
        return new AuthorativeToscaProvider().getFilteredPolicies(pfDao, filter);
    }

    @Override
    public List<ToscaPolicy> getFilteredPolicyList(@NonNull ToscaPolicyFilter filter) throws PfModelException {
        assertInitialized();
        return new AuthorativeToscaProvider().getFilteredPolicyList(pfDao, filter);
    }

    @Override
    public ToscaServiceTemplate createPolicies(@NonNull final ToscaServiceTemplate serviceTemplate)
            throws PfModelException {
        assertInitialized();
        return new AuthorativeToscaProvider().createPolicies(pfDao, serviceTemplate);
    }

    @Override
    public ToscaServiceTemplate updatePolicies(@NonNull final ToscaServiceTemplate serviceTemplate)
            throws PfModelException {
        assertInitialized();
        return new AuthorativeToscaProvider().updatePolicies(pfDao, serviceTemplate);
    }

    @Override
    public ToscaServiceTemplate deletePolicy(@NonNull final String name, @NonNull final String version)
            throws PfModelException {
        assertInitialized();

        ToscaPolicyIdentifier policyIdentifier = new ToscaPolicyIdentifier(name, version);
        assertPolicyNotDeployedInPdpGroup(policyIdentifier);

        return new AuthorativeToscaProvider().deletePolicy(pfDao, name, version);
    }

    @Override
    public List<PdpGroup> getPdpGroups(final String name) throws PfModelException {
        assertInitialized();
        return new PdpProvider().getPdpGroups(pfDao, name);
    }

    @Override
    public List<PdpGroup> getFilteredPdpGroups(@NonNull PdpGroupFilter filter) throws PfModelException {
        assertInitialized();
        return new PdpProvider().getFilteredPdpGroups(pfDao, filter);
    }

    @Override
    public List<PdpGroup> createPdpGroups(@NonNull final List<PdpGroup> pdpGroups) throws PfModelException {
        assertInitialized();
        return new PdpProvider().createPdpGroups(pfDao, pdpGroups);
    }

    @Override
    public List<PdpGroup> updatePdpGroups(@NonNull final List<PdpGroup> pdpGroups) throws PfModelException {
        assertInitialized();
        return new PdpProvider().updatePdpGroups(pfDao, pdpGroups);
    }

    @Override
    public void updatePdpSubGroup(@NonNull final String pdpGroupName, @NonNull final PdpSubGroup pdpSubGroup)
            throws PfModelException {
        assertInitialized();
        new PdpProvider().updatePdpSubGroup(pfDao, pdpGroupName, pdpSubGroup);
    }

    @Override
    public void updatePdp(@NonNull String pdpGroupName, @NonNull String pdpSubGroup, @NonNull Pdp pdp)
            throws PfModelException {
        new PdpProvider().updatePdp(pfDao, pdpGroupName, pdpSubGroup, pdp);
    }

    @Override
    public PdpGroup deletePdpGroup(@NonNull final String name) throws PfModelException {
        assertInitialized();
        return new PdpProvider().deletePdpGroup(pfDao, name);
    }

    @Override
    public List<PdpStatistics> getPdpStatistics(final String name, final Date timestamp) throws PfModelException {
        assertInitialized();
        return new PdpStatisticsProvider().getPdpStatistics(pfDao, name, timestamp);
    }

    @Override
    public List<PdpStatistics> getFilteredPdpStatistics(final String name, @NonNull final String pdpGroupName,
            final String pdpSubGroup, final Date startTimeStamp, final Date endTimeStamp, final String sortOrder,
            final int getRecordNum) throws PfModelException {
        assertInitialized();
        return new PdpStatisticsProvider().getFilteredPdpStatistics(pfDao, name, pdpGroupName, pdpSubGroup,
                startTimeStamp, endTimeStamp, sortOrder, getRecordNum);
    }

    @Override
    public List<PdpStatistics> createPdpStatistics(@NonNull final List<PdpStatistics> pdpStatisticsList)
            throws PfModelException {
        assertInitialized();
        return new PdpStatisticsProvider().createPdpStatistics(pfDao, pdpStatisticsList);
    }

    @Override
    public List<PdpStatistics> updatePdpStatistics(@NonNull final List<PdpStatistics> pdpStatisticsList)
            throws PfModelException {
        assertInitialized();
        return new PdpStatisticsProvider().updatePdpStatistics(pfDao, pdpStatisticsList);
    }

    @Override
    public List<PdpStatistics> deletePdpStatistics(@NonNull final String name, final Date timestamp)
            throws PfModelException {
        assertInitialized();
        return new PdpStatisticsProvider().deletePdpStatistics(pfDao, name, timestamp);
    }

    /**
     * Check if the model provider is initialized.
     */
    private void assertInitialized() {
        if (pfDao == null) {
            String errorMessage = "policy models provider is not initilaized";
            LOGGER.warn(errorMessage);
            throw new PfModelRuntimeException(Response.Status.BAD_REQUEST, errorMessage);
        }
    }

    /**
     * Assert that the policy type is not supported in any PDP group.
     *
     * @param policyTypeIdentifier the policy type identifier
     * @throws PfModelException if the policy type is supported in a PDP group
     */
    private void assertPolicyTypeNotSupportedInPdpGroup(ToscaPolicyTypeIdentifier policyTypeIdentifier)
            throws PfModelException {
        for (PdpGroup pdpGroup : getPdpGroups(null)) {
            for (PdpSubGroup pdpSubGroup : pdpGroup.getPdpSubgroups()) {
                if (pdpSubGroup.getSupportedPolicyTypes().contains(policyTypeIdentifier)) {
                    throw new PfModelRuntimeException(Response.Status.NOT_ACCEPTABLE,
                            "policy type is in use, it is referenced in PDP group " + pdpGroup.getName() + " subgroup "
                                    + pdpSubGroup.getPdpType());
                }
            }
        }
    }

    /**
     * Assert that the policy is not deployed in a PDP group.
     *
     * @param policyIdentifier the identifier of the policy
     * @throws PfModelException thrown if the policy is deployed in a PDP group
     */
    private void assertPolicyNotDeployedInPdpGroup(final ToscaPolicyIdentifier policyIdentifier)
            throws PfModelException {
        for (PdpGroup pdpGroup : getPdpGroups(null)) {
            for (PdpSubGroup pdpSubGroup : pdpGroup.getPdpSubgroups()) {
                if (pdpSubGroup.getPolicies().contains(policyIdentifier)) {
                    throw new PfModelRuntimeException(Response.Status.NOT_ACCEPTABLE,
                            "policy is in use, it is deployed in PDP group " + pdpGroup.getName() + " subgroup "
                                    + pdpSubGroup.getPdpType());
                }
            }
        }
    }
}
