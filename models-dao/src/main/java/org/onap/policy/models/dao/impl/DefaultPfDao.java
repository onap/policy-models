/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2021 Nordix Foundation.
 *  Modifications Copyright (C) 2019-2021 AT&T Intellectual Property. All rights reserved.
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

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import javax.ws.rs.core.Response;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class DefaultPfDao is an JPA implementation of the {@link PfDao} class for Policy Framework concepts
 * ({@link PfConcept}). It uses the default JPA implementation in the javax {@link Persistence} class.
 */
public class DefaultPfDao implements PfDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultPfDao.class);

    // @formatter:off
    private static final String NAME           = "name";
    private static final String VERSION        = "version";
    private static final String TIMESTAMP      = "timeStamp";
    private static final String PARENT_NAME    = "parentname";
    private static final String PARENT_VERSION = "parentversion";
    private static final String LOCAL_NAME     = "localname";

    private static final String TABLE_TOKEN = "__TABLE__";

    private static final String DELETE_FROM_TABLE = "DELETE FROM __TABLE__ c";

    private static final String SELECT_FROM_TABLE = "SELECT c FROM __TABLE__ c";

    private static final String WHERE      = " WHERE ";
    private static final String AND        = " AND ";
    private static final String ORDER      = " ORDER BY ";

    private static final String NAME_FILTER            = "c.key.name = :name";
    private static final String VERSION_FILTER         = "c.key.version = :version";
    private static final String TIMESTAMP_FILTER       = "c.key.timeStamp = :timeStamp";
    private static final String TIMESTAMP_START_FILTER = "c.key.timeStamp >= :startTime";
    private static final String TIMESTAMP_END_FILTER   = "c.key.timeStamp <= :endTime";
    private static final String PARENT_NAME_FILTER     = "c.key.parentKeyName = :parentname";
    private static final String PARENT_VERSION_FILTER  = "c.key.parentKeyVersion = :parentversion";
    private static final String LOCAL_NAME_FILTER      = "c.key.localName = :localname";

    private static final String PARENT_NAME_REF_FILTER     = "c.key.referenceKey.parentKeyName = :parentKeyName";

    private static final String CLONE_ERR_MSG = "Could not clone object of class \"{}\"";

    private static final String DELETE_BY_CONCEPT_KEY =
            DELETE_FROM_TABLE + WHERE + NAME_FILTER + AND + VERSION_FILTER;

    private static final String DELETE_BY_TIMESTAMP_KEY =
            DELETE_FROM_TABLE + WHERE + NAME_FILTER + AND + VERSION_FILTER  + AND + TIMESTAMP_FILTER;

    private static final String DELETE_BY_REFERENCE_KEY =
            DELETE_FROM_TABLE + WHERE + PARENT_NAME_FILTER + AND + PARENT_VERSION_FILTER + AND + LOCAL_NAME_FILTER;

    private static final String SELECT_ALL_FOR_PARENT =
            SELECT_FROM_TABLE + WHERE + PARENT_NAME_FILTER + AND + PARENT_VERSION_FILTER;

    private static final String SELECT_ALL_VERSIONS_FOR_PARENT =
            SELECT_FROM_TABLE + WHERE + PARENT_NAME_FILTER;

    private static final String SELECT_ALL_VERSIONS = SELECT_FROM_TABLE + WHERE + NAME_FILTER;

    private static final String SELECT_BY_CONCEPT_KEY =
            SELECT_FROM_TABLE + WHERE + NAME_FILTER + AND + VERSION_FILTER;

    private static final String SELECT_BY_REFERENCE_KEY =
            SELECT_FROM_TABLE + WHERE + PARENT_NAME_FILTER + AND + PARENT_VERSION_FILTER + AND + LOCAL_NAME_FILTER;
    // @formatter:on

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
        final EntityManager mg = getEntityManager();
        try {
            mg.getTransaction().begin();
            mg.merge(obj);
            mg.getTransaction().commit();
        } finally {
            mg.close();
        }
    }

    @Override
    public <T extends PfConcept> void delete(final T obj) {
        if (obj == null) {
            return;
        }
        final EntityManager mg = getEntityManager();
        try {
            mg.getTransaction().begin();
            mg.remove(mg.contains(obj) ? obj : mg.merge(obj));
            mg.getTransaction().commit();
        } finally {
            mg.close();
        }
    }

    @Override
    public <T extends PfConcept> void delete(final Class<T> someClass, final PfConceptKey key) {
        if (key == null) {
            return;
        }
        final EntityManager mg = getEntityManager();
        try {
            // @formatter:off
            mg.getTransaction().begin();
            mg.createQuery(setQueryTable(DELETE_BY_CONCEPT_KEY, someClass), someClass)
                .setParameter(NAME,    key.getName())
                .setParameter(VERSION, key.getVersion())
                .executeUpdate();
            mg.getTransaction().commit();
            // @formatter:on
        } finally {
            mg.close();
        }
    }

    @Override
    public <T extends PfConcept> void delete(final Class<T> someClass, final PfReferenceKey key) {
        if (key == null) {
            return;
        }
        final EntityManager mg = getEntityManager();
        try {
            // @formatter:off
            mg.getTransaction().begin();
            mg.createQuery(setQueryTable(DELETE_BY_REFERENCE_KEY, someClass), someClass)
                .setParameter(PARENT_NAME,    key.getParentKeyName())
                .setParameter(PARENT_VERSION, key.getParentKeyVersion())
                .setParameter(LOCAL_NAME,     key.getLocalName())
                .executeUpdate();
            mg.getTransaction().commit();
            // @formatter:on
        } finally {
            mg.close();
        }
    }

    @Override
    public <T extends PfConcept> void delete(final Class<T> someClass, final PfTimestampKey key) {
        if (key == null) {
            return;
        }
        final EntityManager mg = getEntityManager();
        try {
            // @formatter:off
            mg.getTransaction().begin();
            mg.createQuery(setQueryTable(DELETE_BY_TIMESTAMP_KEY, someClass), someClass)
                    .setParameter(NAME,    key.getName())
                    .setParameter(VERSION, key.getVersion())
                    .setParameter(TIMESTAMP, key.getTimeStamp())
                    .executeUpdate();
            mg.getTransaction().commit();
            // @formatter:on
        } finally {
            mg.close();
        }
    }

    @Override
    public <T extends PfConcept> void createCollection(final Collection<T> objs) {
        if (objs == null || objs.isEmpty()) {
            return;
        }
        final EntityManager mg = getEntityManager();
        try {
            mg.getTransaction().begin();
            for (final T t : objs) {
                mg.merge(t);
            }
            mg.getTransaction().commit();
        } finally {
            mg.close();
        }
    }

    @Override
    public <T extends PfConcept> void deleteCollection(final Collection<T> objs) {
        if (objs == null || objs.isEmpty()) {
            return;
        }
        final EntityManager mg = getEntityManager();
        try {
            mg.getTransaction().begin();
            for (final T t : objs) {
                mg.remove(mg.contains(t) ? t : mg.merge(t));
            }
            mg.getTransaction().commit();
        } finally {
            mg.close();
        }
    }

    @Override
    public <T extends PfConcept> int deleteByConceptKey(final Class<T> someClass, final Collection<PfConceptKey> keys) {
        if (keys == null || keys.isEmpty()) {
            return 0;
        }
        int deletedCount = 0;
        final EntityManager mg = getEntityManager();
        try {
            // @formatter:off
            mg.getTransaction().begin();
            for (final PfConceptKey key : keys) {
                deletedCount += mg.createQuery(setQueryTable(DELETE_BY_CONCEPT_KEY, someClass), someClass)
                    .setParameter(NAME,    key.getName())
                    .setParameter(VERSION, key.getVersion())
                    .executeUpdate();
            }
            mg.getTransaction().commit();
            // @formatter:on
        } finally {
            mg.close();
        }
        return deletedCount;
    }

    @Override
    public <T extends PfConcept> int deleteByReferenceKey(final Class<T> someClass,
            final Collection<PfReferenceKey> keys) {
        if (keys == null || keys.isEmpty()) {
            return 0;
        }
        int deletedCount = 0;
        final EntityManager mg = getEntityManager();
        try {
            // @formatter:off
            mg.getTransaction().begin();
            for (final PfReferenceKey key : keys) {
                deletedCount += mg.createQuery(setQueryTable(DELETE_BY_REFERENCE_KEY, someClass), someClass)
                    .setParameter(PARENT_NAME,    key.getParentKeyName())
                    .setParameter(PARENT_VERSION, key.getParentKeyVersion())
                    .setParameter(LOCAL_NAME,     key.getLocalName())
                    .executeUpdate();
            }
            mg.getTransaction().commit();
            // @formatter:on
        } finally {
            mg.close();
        }
        return deletedCount;
    }

    @Override
    public <T extends PfConcept> void deleteAll(final Class<T> someClass) {
        final EntityManager mg = getEntityManager();
        try {
            mg.getTransaction().begin();
            mg.createQuery(setQueryTable(DELETE_FROM_TABLE, someClass), someClass).executeUpdate();
            mg.getTransaction().commit();
        } finally {
            mg.close();
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

        T foundConcept = get(someClass, new PfConceptKey(name, version));

        return (foundConcept == null ? Collections.emptyList() : Collections.singletonList(foundConcept));
    }

    @Override
    public <T extends PfConcept> List<T> getFiltered(final Class<T> someClass, final String name, final String version,
            final Instant startTime, final Instant endTime, final Map<String, Object> filterMap, final String sortOrder,
            final int getRecordNum) {
        final EntityManager mg = getEntityManager();

        String filterQueryString = SELECT_FROM_TABLE + WHERE;

        try {
            if (filterMap != null) {
                filterQueryString = buildFilter(filterMap, filterQueryString);
            }
            filterQueryString = addKeyFilterString(filterQueryString, name, startTime, endTime,
                isRefTimestampKey(someClass));
            if (getRecordNum > 0) {
                filterQueryString += ORDER + " c.key.timeStamp " + sortOrder;
            }
            TypedQuery<T> query = mg.createQuery(setQueryTable(filterQueryString, someClass), someClass);

            if (filterMap != null) {
                for (Map.Entry<String, Object> entry : filterMap.entrySet()) {
                    query.setParameter(entry.getKey(), entry.getValue());
                }
            }
            if (name != null) {
                if (isRefTimestampKey(someClass)) {
                    query.setParameter("parentKeyName", name);
                } else {
                    query.setParameter("name", name);
                }
            }
            if (startTime != null) {
                if (endTime != null) {
                    query.setParameter("startTime", Timestamp.from(startTime));
                    query.setParameter("endTime", Timestamp.from(endTime));
                } else {
                    query.setParameter("startTime", Timestamp.from(startTime));
                }
            } else {
                if (endTime != null) {
                    query.setParameter("endTime", Timestamp.from(endTime));
                }
            }
            if (getRecordNum > 0) {
                query.setMaxResults(getRecordNum);
            }

            LOGGER.debug("filterQueryString is  \"{}\"", filterQueryString);
            return query.getResultList();
        }  finally {
            mg.close();
        }
    }

    /**
     * This method checks if the class invoking the DAO is using PfReferenceTimestamp Key.
     * @param someClass class that invoked Dao
     * @return true if the key is PfReferenceTimestampKey.
     */
    private <T extends PfConcept> boolean isRefTimestampKey(final Class<T> someClass) {
        try {
            return PfReferenceTimestampKey.class.isAssignableFrom(someClass.getDeclaredField("key").getType());
        } catch (NoSuchFieldException e) {
            LOGGER.error("Error verifying the key for reference timestamp:", e);
            return false;
        }
    }

    private String buildFilter(final Map<String, Object> filterMap, String filterQueryString) {
        StringBuilder bld = new StringBuilder(filterQueryString);
        for (String key : filterMap.keySet()) {
            bld.append("c." + key + "= :" + key + AND);
        }
        return bld.toString();
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
        final EntityManager mg = getEntityManager();
        try {
            final T t = mg.find(someClass, key);
            if (t != null) {
                mg.refresh(t);
            }
            return checkAndReturn(someClass, t);
        } finally {
            mg.close();
        }
    }

    @Override
    public <T extends PfConcept> List<T> getAll(final Class<T> someClass) {
        if (someClass == null) {
            return Collections.emptyList();
        }
        final EntityManager mg = getEntityManager();
        try {
            return mg.createQuery(setQueryTable(SELECT_FROM_TABLE, someClass), someClass).getResultList();
        } finally {
            mg.close();
        }
    }

    @Override
    public <T extends PfConcept> List<T> getAll(final Class<T> someClass, final PfConceptKey parentKey) {
        if (someClass == null) {
            return Collections.emptyList();
        }
        final EntityManager mg = getEntityManager();
        try {
            // @formatter:off
            return mg.createQuery(setQueryTable(SELECT_ALL_FOR_PARENT, someClass), someClass)
                    .setParameter(PARENT_NAME,    parentKey.getName())
                    .setParameter(PARENT_VERSION, parentKey.getVersion())
                    .getResultList();
            // @formatter:on
        } finally {
            mg.close();
        }
    }

    @Override
    public <T extends PfConcept> List<T> getAllVersionsByParent(final Class<T> someClass, final String parentKeyName) {
        if (someClass == null || parentKeyName == null) {
            return Collections.emptyList();
        }
        final EntityManager mg = getEntityManager();
        try {
            // @formatter:off
            return mg.createQuery(setQueryTable(SELECT_ALL_VERSIONS_FOR_PARENT, someClass), someClass)
                    .setParameter(PARENT_NAME, parentKeyName)
                    .getResultList();
            // @formatter:on
        } finally {
            mg.close();
        }
    }

    @Override
    public <T extends PfConcept> List<T> getAllVersions(final Class<T> someClass, final String conceptName) {
        if (someClass == null || conceptName == null) {
            return Collections.emptyList();
        }
        final EntityManager mg = getEntityManager();
        try {
            // @formatter:off
            return mg.createQuery(setQueryTable(SELECT_ALL_VERSIONS, someClass), someClass)
                    .setParameter(NAME, conceptName)
                    .getResultList();
            // @formatter:on
        } finally {
            mg.close();
        }
    }

    @Override
    public <T extends PfConcept> T getConcept(final Class<T> someClass, final PfConceptKey key) {
        if (someClass == null || key == null) {
            return null;
        }
        final EntityManager mg = getEntityManager();
        List<T> ret;
        try {
            // @formatter:off
            ret = mg.createQuery(setQueryTable(SELECT_BY_CONCEPT_KEY, someClass), someClass)
                    .setParameter(NAME,    key.getName())
                    .setParameter(VERSION, key.getVersion())
                    .getResultList();
            // @formatter:on
        } finally {
            mg.close();
        }

        return getSingleResult(someClass, key.getId(), ret);
    }

    @Override
    public <T extends PfConcept> T getConcept(final Class<T> someClass, final PfReferenceKey key) {
        if (someClass == null || key == null) {
            return null;
        }
        final EntityManager mg = getEntityManager();
        List<T> ret;
        try {
            // @formatter:off
            ret = mg.createQuery(setQueryTable(SELECT_BY_REFERENCE_KEY, someClass), someClass)
                    .setParameter(PARENT_NAME,    key.getParentKeyName())
                    .setParameter(PARENT_VERSION, key.getParentKeyVersion())
                    .setParameter(LOCAL_NAME,     key.getLocalName())
                    .getResultList();
            // @formatter:on
        } finally {
            mg.close();
        }

        return getSingleResult(someClass, key.getId(), ret);
    }

    @Override
    public <T extends PfConcept> T update(final T obj) {
        final EntityManager mg = getEntityManager();
        T ret;
        try {
            mg.getTransaction().begin();
            ret = mg.merge(obj);
            mg.flush();
            mg.getTransaction().commit();
        } finally {
            mg.close();
        }
        return ret;
    }

    @Override
    public <T extends PfConcept> long size(final Class<T> someClass) {
        if (someClass == null) {
            return 0;
        }
        final EntityManager mg = getEntityManager();
        long size = 0;
        try {
            size = mg.createQuery("SELECT COUNT(c) FROM " + someClass.getSimpleName() + " c", Long.class)
                    .getSingleResult();
        } finally {
            mg.close();
        }
        return size;
    }

    /**
     * Add the table to a query string.
     *
     * @param queryString the query string
     * @param tableClass the class name of the table
     * @return the updated query string
     */
    private <T extends PfConcept> String setQueryTable(final String queryString, final Class<T> tableClass) {
        return queryString.replace(TABLE_TOKEN, tableClass.getSimpleName());
    }

    /**
     * Check that a query returned one and only one entry and return that entry.
     *
     * @param someClass the class being searched for
     * @param searchFilter the concept name being searched for
     * @param ret the result list returned by the query
     * @return the single unique result
     */
    private <T extends PfConcept> T getSingleResult(final Class<T> someClass, final String searchFilter, List<T> ret) {
        if (ret == null || ret.isEmpty()) {
            return null;
        }
        if (ret.size() > 1) {
            throw new IllegalArgumentException("More than one result was returned query on " + someClass
                    + " with filter " + searchFilter + ": " + ret);
        }
        return ret.get(0);
    }

    /**
     * generate filter string with the filter value in TimestampKey.
     *
     * @param inputFilterString current filterString generated from FilterMap
     * @param name the pdp name the start timeStamp to filter from database, filter rule: startTime <= filteredRecord
     *        timeStamp <= endTime. null for ignore start time.
     * @param endTime the end timeStamp to filter from database, filter rule: startTime <= filteredRecord timeStamp <=
     *        endTime. null for ignore end time
     * @param isRefTimestampKey boolean value, set to true if the query invoked for pfReferenceTimestampKey
     * @return the filter string to query database
     */
    private String addKeyFilterString(String inputFilterString, final String name, final Instant startTime,
            final Instant endTime, final boolean isRefTimestampKey) {
        String filterQueryString;
        String inputFilter = inputFilterString;
        if (name != null) {
            if (isRefTimestampKey) {
                inputFilter += PARENT_NAME_REF_FILTER + AND;
            } else {
                inputFilter += NAME_FILTER + AND;
            }
        }
        if (startTime != null) {
            if (endTime != null) {
                filterQueryString = inputFilter + TIMESTAMP_START_FILTER + AND + TIMESTAMP_END_FILTER;
            } else {
                filterQueryString = inputFilter + TIMESTAMP_START_FILTER;
            }
        } else {
            if (endTime != null) {
                filterQueryString = inputFilter + TIMESTAMP_END_FILTER;
            } else {
                filterQueryString = inputFilter.substring(0, inputFilter.length() - AND.length());
            }
        }

        return filterQueryString;
    }

    /**
     * check the result get from database and return the object.
     *
     * @param <T> the type of the object to get, a subclass of {@link PfConcept}
     * @param someClass the class of the object to get, a subclass of {@link PfConcept}
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
