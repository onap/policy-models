/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021, 2023 Nordix Foundation.
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
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.onap.policy.models.base.PfConcept;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfModelException;
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
 * The Class ProxyDao is an JPA implementation of the {@link ProxyDao} class for Policy Framework concepts
 * ({@link PfConcept}). It uses the default JPA implementation in the jakarta {@link Persistence} class.
 */
@RequiredArgsConstructor
public class ProxyDao implements PfDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyDao.class);

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
    private static final String ORDER_BY        = " ORDER BY c.";

    private static final String NAME_FILTER            = "c.key.name = :name";
    private static final String VERSION_FILTER         = "c.key.version = :version";
    private static final String TIMESTAMP_FILTER       = "c.key.timeStamp = :timeStamp";
    private static final String PARENT_NAME_FILTER     = "c.key.parentKeyName = :parentname";
    private static final String PARENT_VERSION_FILTER  = "c.key.parentKeyVersion = :parentversion";
    private static final String LOCAL_NAME_FILTER      = "c.key.localName = :localname";

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
    private final EntityManager mg;

    @Override
    public void init(final DaoParameters daoParameters) throws PfModelException {
        // Entity manager for JPA should be created at Service level
    }

    @Override
    public final void close() {
        // Entity manager for JPA should be close at Service level
    }

    @Override
    public <T extends PfConcept> void create(final T obj) {
        if (obj == null) {
            return;
        }
        mg.merge(obj);
        mg.flush();
    }

    @Override
    public <T extends PfConcept> void delete(final T obj) {
        if (obj == null) {
            return;
        }
        mg.remove(mg.contains(obj) ? obj : mg.merge(obj));
    }

    @Override
    public <T extends PfConcept> void delete(final Class<T> someClass, final PfConceptKey key) {
        if (key == null) {
            return;
        }
        // @formatter:off
        mg.createQuery(setQueryTable(DELETE_BY_CONCEPT_KEY, someClass), someClass)
            .setParameter(NAME,    key.getName())
            .setParameter(VERSION, key.getVersion())
            .executeUpdate();
        // @formatter:on
    }

    @Override
    public <T extends PfConcept> void delete(final Class<T> someClass, final PfReferenceKey key) {
        if (key == null) {
            return;
        }
        // @formatter:off
        mg.createQuery(setQueryTable(DELETE_BY_REFERENCE_KEY, someClass), someClass)
            .setParameter(PARENT_NAME,    key.getParentKeyName())
            .setParameter(PARENT_VERSION, key.getParentKeyVersion())
            .setParameter(LOCAL_NAME,     key.getLocalName())
            .executeUpdate();
        // @formatter:on
    }

    @Override
    public <T extends PfConcept> void delete(final Class<T> someClass, final PfTimestampKey key) {
        if (key == null) {
            return;
        }

        // @formatter:off
        mg.createQuery(setQueryTable(DELETE_BY_TIMESTAMP_KEY, someClass), someClass)
                .setParameter(NAME,    key.getName())
                .setParameter(VERSION, key.getVersion())
                .setParameter(TIMESTAMP, key.getTimeStamp())
                .executeUpdate();
        // @formatter:on
    }

    @Override
    public <T extends PfConcept> void createCollection(final Collection<T> objs) {
        if (objs == null || objs.isEmpty()) {
            return;
        }

        for (final T t : objs) {
            mg.merge(t);
        }
    }

    @Override
    public <T extends PfConcept> void deleteCollection(final Collection<T> objs) {
        if (objs == null || objs.isEmpty()) {
            return;
        }

        for (final T t : objs) {
            mg.remove(mg.contains(t) ? t : mg.merge(t));
        }
    }

    @Override
    public <T extends PfConcept> int deleteByConceptKey(final Class<T> someClass, final Collection<PfConceptKey> keys) {
        if (keys == null || keys.isEmpty()) {
            return 0;
        }
        var deletedCount = 0;

        for (final PfConceptKey key : keys) {
            // @formatter:off
            deletedCount += mg.createQuery(setQueryTable(DELETE_BY_CONCEPT_KEY, someClass), someClass)
                .setParameter(NAME,    key.getName())
                .setParameter(VERSION, key.getVersion())
                .executeUpdate();
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

        for (final PfReferenceKey key : keys) {
            // @formatter:off
            deletedCount += mg.createQuery(setQueryTable(DELETE_BY_REFERENCE_KEY, someClass), someClass)
                .setParameter(PARENT_NAME,    key.getParentKeyName())
                .setParameter(PARENT_VERSION, key.getParentKeyVersion())
                .setParameter(LOCAL_NAME,     key.getLocalName())
                .executeUpdate();
            // @formatter:on
        }
        return deletedCount;
    }

    @Override
    public <T extends PfConcept> void deleteAll(final Class<T> someClass) {
        mg.createQuery(setQueryTable(DELETE_FROM_TABLE, someClass), someClass).executeUpdate();
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

        PfFilter filter = new PfFilterFactory().createFilter(someClass);
        var filterQueryString =
                SELECT_FROM_TABLE + filter.genWhereClause(filterParams) + filter.genOrderClause(filterParams);

        TypedQuery<T> query = mg.createQuery(setQueryTable(filterQueryString, someClass), someClass);
        filter.setParams(query, filterParams);

        LOGGER.debug("filterQueryString is  \"{}\"", filterQueryString);
        return query.getResultList();
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

        final var t = mg.find(someClass, key);
        return checkAndReturn(someClass, t);
    }

    @Override
    public <T extends PfConcept> List<T> getAll(final Class<T> someClass) {
        if (someClass == null) {
            return Collections.emptyList();
        }

        return mg.createQuery(setQueryTable(SELECT_FROM_TABLE, someClass), someClass).getResultList();
    }

    @Override
    public <T extends PfConcept> List<T> getAll(final Class<T> someClass, final PfConceptKey parentKey) {
        if (someClass == null) {
            return Collections.emptyList();
        }

        // @formatter:off
        return mg.createQuery(setQueryTable(SELECT_ALL_FOR_PARENT, someClass), someClass)
                .setParameter(PARENT_NAME,    parentKey.getName())
                .setParameter(PARENT_VERSION, parentKey.getVersion())
                .getResultList();
        // @formatter:on
    }

    @Override
    public <T extends PfConcept> List<T> getAll(Class<T> someClass, String orderBy, Integer numRecords) {

        if (someClass == null) {
            return Collections.emptyList();
        }

        String query = setQueryTable(SELECT_FROM_TABLE, someClass);

        if (StringUtils.isNotBlank(orderBy)) {
            query = query.concat(ORDER_BY).concat(orderBy);
        }

        return mg.createQuery(query, someClass).setMaxResults(numRecords).getResultList();
    }

    @Override
    public <T extends PfConcept> List<T> getAllVersionsByParent(final Class<T> someClass, final String parentKeyName) {
        if (someClass == null || parentKeyName == null) {
            return Collections.emptyList();
        }

        // @formatter:off
        return mg.createQuery(setQueryTable(SELECT_ALL_VERSIONS_FOR_PARENT, someClass), someClass)
                .setParameter(PARENT_NAME, parentKeyName)
                .getResultList();
        // @formatter:on
    }

    @Override
    public <T extends PfConcept> List<T> getAllVersions(final Class<T> someClass, final String conceptName) {
        if (someClass == null || conceptName == null) {
            return Collections.emptyList();
        }

        // @formatter:off
        return mg.createQuery(setQueryTable(SELECT_ALL_VERSIONS, someClass), someClass)
                .setParameter(NAME, conceptName)
                .getResultList();
        // @formatter:on
    }

    @Override
    public <T extends PfConcept> T getConcept(final Class<T> someClass, final PfConceptKey key) {
        if (someClass == null || key == null) {
            return null;
        }

        // @formatter:off
        var ret = mg.createQuery(setQueryTable(SELECT_BY_CONCEPT_KEY, someClass), someClass)
                .setParameter(NAME,    key.getName())
                .setParameter(VERSION, key.getVersion())
                .getResultList();
        // @formatter:on

        return getSingleResult(someClass, key.getId(), ret);
    }

    @Override
    public <T extends PfConcept> T getConcept(final Class<T> someClass, final PfReferenceKey key) {
        if (someClass == null || key == null) {
            return null;
        }

        // @formatter:off
        var ret = mg.createQuery(setQueryTable(SELECT_BY_REFERENCE_KEY, someClass), someClass)
                .setParameter(PARENT_NAME,    key.getParentKeyName())
                .setParameter(PARENT_VERSION, key.getParentKeyVersion())
                .setParameter(LOCAL_NAME,     key.getLocalName())
                .getResultList();
        // @formatter:on

        return getSingleResult(someClass, key.getId(), ret);
    }

    @Override
    public <T extends PfConcept> T update(final T obj) {
        var ret = mg.merge(obj);
        mg.flush();
        return ret;
    }

    @Override
    public <T extends PfConcept> long size(final Class<T> someClass) {
        if (someClass == null) {
            return 0;
        }

        long size = 0;
        /*
         * The invoking code only passes well-known classes into this method, thus
         * disabling the sonar about SQL injection.
         */
        size = mg.createQuery("SELECT COUNT(c) FROM " + someClass.getSimpleName() + " c", Long.class) // NOSONAR
                .getSingleResult();
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
     * @param searchFilter the search filter
     * @param resultList the result list returned by the query
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
