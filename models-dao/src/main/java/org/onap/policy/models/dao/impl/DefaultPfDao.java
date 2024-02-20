/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2021, 2023-2024 Nordix Foundation.
 *  Modifications Copyright (C) 2019-2021 AT&T Intellectual Property. All rights reserved.
 *  Modifications Copyright (C) 2022 Bell Canada. All rights reserved.
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

package org.onap.policy.models.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;
import jakarta.ws.rs.core.Response;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.onap.policy.models.base.PfConcept;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.base.PfModelRuntimeException;
import org.onap.policy.models.base.PfReferenceKey;
import org.onap.policy.models.base.PfReferenceTimestampKey;
import org.onap.policy.models.base.PfTimestampKey;
import org.onap.policy.models.base.PfUtils;
import org.onap.policy.models.dao.DaoParameters;
import org.onap.policy.models.dao.PfDao;
import org.onap.policy.models.dao.PfFilter;
import org.onap.policy.models.dao.PfFilterFactory;
import org.onap.policy.models.dao.PfFilterParametersIntfc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class DefaultPfDao is an JPA implementation of the {@link PfDao} class for Policy Framework concepts
 * ({@link PfConcept}). It uses the default JPA implementation in the jakarta {@link Persistence} class.
 */
public class DefaultPfDao implements PfDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultPfDao.class);

    // Entity manager for JPA
    private EntityManagerFactory emf = null;

    @Override
    public void init(final DaoParameters daoParameters) throws PfModelException {
        if (daoParameters == null || daoParameters.getPersistenceUnit() == null) {
            LOGGER.error("Policy Framework persistence unit parameter not set");
            throw new PfModelException(Response.Status.INTERNAL_SERVER_ERROR,
                "Policy Framework persistence unit parameter not set");
        }

        LOGGER.debug("Creating Policy Framework persistence unit \"{}\" . . .", daoParameters.getPersistenceUnit());
        try {
            emf = Persistence.createEntityManagerFactory(daoParameters.getPersistenceUnit(),
                daoParameters.getJdbcProperties());
        } catch (final Exception ex) {
            String errorMessage = "Creation of Policy Framework persistence unit \""
                + daoParameters.getPersistenceUnit() + "\" failed";
            LOGGER.warn(errorMessage);
            throw new PfModelException(Response.Status.INTERNAL_SERVER_ERROR, errorMessage, ex);
        }
        LOGGER.debug("Created Policy Framework persistence unit \"{}\"", daoParameters.getPersistenceUnit());
    }

    /**
     * Gets the entity manager for this DAO.
     *
     * @return the entity manager
     */
    protected final synchronized EntityManager getEntityManager() {
        if (emf == null) {
            LOGGER.warn("Policy Framework DAO has not been initialized");
            throw new PfModelRuntimeException(Response.Status.INTERNAL_SERVER_ERROR,
                "Policy Framework DAO has not been initialized");
        }

        return emf.createEntityManager();
    }

    @Override
    public final void close() {
        if (emf != null) {
            emf.close();
        }
    }

    @Override
    public <T extends PfConcept> void create(final T obj) {
        if (obj == null) {
            return;
        }
        try (var mg = getEntityManager()) {
            mg.getTransaction().begin();
            mg.merge(obj);
            mg.getTransaction().commit();
        }
    }

    @Override
    public <T extends PfConcept> void delete(final T obj) {
        if (obj == null) {
            return;
        }
        try (var mg = getEntityManager()) {
            mg.getTransaction().begin();
            mg.remove(mg.contains(obj) ? obj : mg.merge(obj));
            mg.getTransaction().commit();
        }
    }

    @Override
    public <T extends PfConcept> void delete(final Class<T> someClass, final PfConceptKey key) {
        if (key == null) {
            return;
        }
        try (var mg = getEntityManager()) {
            // @formatter:off
            mg.getTransaction().begin();
            mg.createQuery(setQueryTable(DELETE_BY_CONCEPT_KEY, someClass))
                .setParameter(NAME,    key.getName())
                .setParameter(VERSION, key.getVersion())
                .executeUpdate();
            mg.getTransaction().commit();
            // @formatter:on
        }
    }

    @Override
    public <T extends PfConcept> void delete(final Class<T> someClass, final PfReferenceKey key) {
        if (key == null) {
            return;
        }
        try (var mg = getEntityManager()) {
            // @formatter:off
            mg.getTransaction().begin();
            mg.createQuery(setQueryTable(DELETE_BY_REFERENCE_KEY, someClass))
                .setParameter(PARENT_NAME,    key.getParentKeyName())
                .setParameter(PARENT_VERSION, key.getParentKeyVersion())
                .setParameter(LOCAL_NAME,     key.getLocalName())
                .executeUpdate();
            mg.getTransaction().commit();
            // @formatter:on
        }
    }

    @Override
    public <T extends PfConcept> void delete(final Class<T> someClass, final PfTimestampKey key) {
        if (key == null) {
            return;
        }
        try (var mg = getEntityManager()) {
            // @formatter:off
            mg.getTransaction().begin();
            mg.createQuery(setQueryTable(DELETE_BY_TIMESTAMP_KEY, someClass))
                    .setParameter(NAME,    key.getName())
                    .setParameter(VERSION, key.getVersion())
                    .setParameter(TIMESTAMP, key.getTimeStamp())
                    .executeUpdate();
            mg.getTransaction().commit();
            // @formatter:on
        }
    }

    @Override
    public <T extends PfConcept> void createCollection(final Collection<T> objs) {
        if (objs == null || objs.isEmpty()) {
            return;
        }
        try (var mg = getEntityManager()) {
            mg.getTransaction().begin();
            for (final T t : objs) {
                mg.merge(t);
            }
            mg.getTransaction().commit();
        }
    }

    @Override
    public <T extends PfConcept> void deleteCollection(final Collection<T> objs) {
        if (objs == null || objs.isEmpty()) {
            return;
        }
        try (var mg = getEntityManager()) {
            mg.getTransaction().begin();
            for (final T t : objs) {
                mg.remove(mg.contains(t) ? t : mg.merge(t));
            }
            mg.getTransaction().commit();
        }
    }

    @Override
    public <T extends PfConcept> int deleteByConceptKey(final Class<T> someClass, final Collection<PfConceptKey> keys) {
        if (keys == null || keys.isEmpty()) {
            return 0;
        }
        var deletedCount = 0;
        try (var mg = getEntityManager()) {
            // @formatter:off
            mg.getTransaction().begin();
            for (final PfConceptKey key : keys) {
                deletedCount += mg.createQuery(setQueryTable(DELETE_BY_CONCEPT_KEY, someClass))
                    .setParameter(NAME,    key.getName())
                    .setParameter(VERSION, key.getVersion())
                    .executeUpdate();
            }
            mg.getTransaction().commit();
            // @formatter:on
        }
        return deletedCount;
    }

    @Override
    public <T extends PfConcept> int deleteByReferenceKey(final Class<T> someClass,
                                                          final Collection<PfReferenceKey> keys) {
        if (keys == null || keys.isEmpty()) {
            return 0;
        }
        var deletedCount = 0;
        try (var mg = getEntityManager()) {
            // @formatter:off
            mg.getTransaction().begin();
            for (final PfReferenceKey key : keys) {
                deletedCount += mg.createQuery(setQueryTable(DELETE_BY_REFERENCE_KEY, someClass))
                    .setParameter(PARENT_NAME,    key.getParentKeyName())
                    .setParameter(PARENT_VERSION, key.getParentKeyVersion())
                    .setParameter(LOCAL_NAME,     key.getLocalName())
                    .executeUpdate();
            }
            mg.getTransaction().commit();
            // @formatter:on
        }
        return deletedCount;
    }

    @Override
    public <T extends PfConcept> void deleteAll(final Class<T> someClass) {
        try (var mg = getEntityManager()) {
            mg.getTransaction().begin();
            mg.createQuery(setQueryTable(DELETE_FROM_TABLE, someClass)).executeUpdate();
            mg.getTransaction().commit();
        }
    }

    @Override
    public <T extends PfConcept> List<T> getFiltered(final Class<T> someClass, final String name,
                                                     final String version) {
        if (name == null) {
            return getAll(someClass);
        }

        if (version == null) {
            return getAllVersions(someClass, name);
        }

        var foundConcept = get(someClass, new PfConceptKey(name, version));

        return (foundConcept == null ? Collections.emptyList() : Collections.singletonList(foundConcept));
    }

    @Override
    public <T extends PfConcept> List<T> getFiltered(final Class<T> someClass, PfFilterParametersIntfc filterParams) {

        try (var mg = getEntityManager()) {
            PfFilter filter = new PfFilterFactory().createFilter(someClass);
            var filterQueryString = SELECT_FROM_TABLE
                + filter.genWhereClause(filterParams)
                + filter.genOrderClause(filterParams);

            TypedQuery<T> query = mg.createQuery(setQueryTable(filterQueryString, someClass), someClass);
            filter.setParams(query, filterParams);

            LOGGER.debug("filterQueryString is  \"{}\"", filterQueryString);
            return query.getResultList();
        }
    }

    @Override
    public <T extends PfConcept> T get(final Class<T> someClass, final PfConceptKey key) {
        return genericGet(someClass, key);
    }

    @Override
    public <T extends PfConcept> T get(final Class<T> someClass, final PfReferenceKey key) {
        return genericGet(someClass, key);
    }

    @Override
    public <T extends PfConcept> T get(final Class<T> someClass, final PfTimestampKey key) {
        return genericGet(someClass, key);
    }

    @Override
    public <T extends PfConcept> T get(final Class<T> someClass, final PfReferenceTimestampKey key) {
        return genericGet(someClass, key);
    }

    private <T extends PfConcept> T genericGet(final Class<T> someClass, final Object key) {
        if (someClass == null) {
            return null;
        }
        try (var mg = getEntityManager()) {
            final var t = mg.find(someClass, key);
            if (t != null) {
                mg.refresh(t);
            }
            return checkAndReturn(someClass, t);
        }
    }

    @Override
    public <T extends PfConcept> List<T> getAll(final Class<T> someClass) {
        if (someClass == null) {
            return Collections.emptyList();
        }
        try (var mg = getEntityManager()) {
            return mg.createQuery(setQueryTable(SELECT_FROM_TABLE, someClass), someClass).getResultList();
        }
    }

    @Override
    public <T extends PfConcept> List<T> getAll(final Class<T> someClass, final PfConceptKey parentKey) {
        if (someClass == null) {
            return Collections.emptyList();
        }
        try (var mg = getEntityManager()) {
            // @formatter:off
            return mg.createQuery(setQueryTable(SELECT_ALL_FOR_PARENT, someClass), someClass)
                    .setParameter(PARENT_NAME,    parentKey.getName())
                    .setParameter(PARENT_VERSION, parentKey.getVersion())
                    .getResultList();
            // @formatter:on
        }
    }

    @Override
    public <T extends PfConcept> List<T> getAll(Class<T> someClass, String orderBy, Integer numRecords) {

        if (someClass == null) {
            return Collections.emptyList();
        }
        try (var mg = getEntityManager()) {
            String query = setQueryTable(SELECT_FROM_TABLE, someClass);

            if (StringUtils.isNotBlank(orderBy)) {
                query = query.concat(ORDER_BY).concat(orderBy);
            }

            return mg.createQuery(query, someClass).setMaxResults(numRecords)
                .getResultList();
        }
    }

    @Override
    public <T extends PfConcept> List<T> getAllVersionsByParent(final Class<T> someClass, final String parentKeyName) {
        if (someClass == null || parentKeyName == null) {
            return Collections.emptyList();
        }
        try (var mg = getEntityManager()) {
            // @formatter:off
            return mg.createQuery(setQueryTable(SELECT_ALL_VERSIONS_FOR_PARENT, someClass), someClass)
                    .setParameter(PARENT_NAME, parentKeyName)
                    .getResultList();
            // @formatter:on
        }
    }

    @Override
    public <T extends PfConcept> List<T> getAllVersions(final Class<T> someClass, final String conceptName) {
        if (someClass == null || conceptName == null) {
            return Collections.emptyList();
        }
        try (var mg = getEntityManager()) {
            // @formatter:off
            return mg.createQuery(setQueryTable(SELECT_ALL_VERSIONS, someClass), someClass)
                    .setParameter(NAME, conceptName)
                    .getResultList();
            // @formatter:on
        }
    }

    @Override
    public <T extends PfConcept> T getConcept(final Class<T> someClass, final PfConceptKey key) {
        if (someClass == null || key == null) {
            return null;
        }
        List<T> ret;
        try (var mg = getEntityManager()) {
            // @formatter:off
            ret = mg.createQuery(setQueryTable(SELECT_BY_CONCEPT_KEY, someClass), someClass)
                    .setParameter(NAME,    key.getName())
                    .setParameter(VERSION, key.getVersion())
                    .getResultList();
            // @formatter:on
        }

        return getSingleResult(someClass, key.getId(), ret);
    }

    @Override
    public <T extends PfConcept> T getConcept(final Class<T> someClass, final PfReferenceKey key) {
        if (someClass == null || key == null) {
            return null;
        }
        List<T> ret;
        try (var mg = getEntityManager()) {
            // @formatter:off
            ret = mg.createQuery(setQueryTable(SELECT_BY_REFERENCE_KEY, someClass), someClass)
                    .setParameter(PARENT_NAME,    key.getParentKeyName())
                    .setParameter(PARENT_VERSION, key.getParentKeyVersion())
                    .setParameter(LOCAL_NAME,     key.getLocalName())
                    .getResultList();
            // @formatter:on
        }

        return getSingleResult(someClass, key.getId(), ret);
    }

    @Override
    public <T extends PfConcept> T update(final T obj) {
        T ret;
        try (var mg = getEntityManager()) {
            mg.getTransaction().begin();
            ret = mg.merge(obj);
            mg.flush();
            mg.getTransaction().commit();
        }
        return ret;
    }

    @Override
    public <T extends PfConcept> long size(final Class<T> someClass) {
        if (someClass == null) {
            return 0;
        }
        long size;
        try (var mg = getEntityManager()) {
            /*
             * The invoking code only passes well-known classes into this method, thus
             * disabling the sonar about SQL injection.
             */
            size = mg.createQuery("SELECT COUNT(c) FROM " + someClass.getSimpleName() + " c", Long.class) // NOSONAR
                .getSingleResult();
        }
        return size;
    }

    /**
     * Add the table to a query string.
     *
     * @param queryString the query string
     * @param tableClass  the class name of the table
     * @return the updated query string
     */
    private <T extends PfConcept> String setQueryTable(final String queryString, final Class<T> tableClass) {
        return queryString.replace(TABLE_TOKEN, tableClass.getSimpleName());
    }

    /**
     * Check that a query returned one and only one entry and return that entry.
     *
     * @param someClass    the class being searched for
     * @param searchFilter the search filter
     * @param resultList   the result list returned by the query
     * @return the single unique result
     */
    private <T extends PfConcept> T getSingleResult(final Class<T> someClass, final String searchFilter,
                                                    List<T> resultList) {
        if (resultList == null || resultList.isEmpty()) {
            return null;
        }
        if (resultList.size() > 1) {
            throw new IllegalArgumentException("More than one result was returned query on " + someClass
                + " with filter " + searchFilter + ": " + resultList);
        }
        return resultList.get(0);
    }

    /**
     * check the result get from database and return the object.
     *
     * @param <T>        the type of the object to get, a subclass of {@link PfConcept}
     * @param someClass  the class of the object to get, a subclass of {@link PfConcept}
     * @param objToCheck the object that was retrieved from the database
     * @return the checked object or null
     */
    private <T extends PfConcept> T checkAndReturn(final Class<T> someClass, final T objToCheck) {
        if (objToCheck != null) {
            try {
                return PfUtils.makeCopy(objToCheck);
            } catch (final Exception e) {
                LOGGER.warn(CLONE_ERR_MSG, someClass.getName(), e);
            }
        }
        return null;
    }
}
