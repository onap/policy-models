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

package org.onap.policy.models.dao;

import java.util.Collection;
import java.util.List;

import org.onap.policy.models.base.PfConcept;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.base.PfReferenceKey;

/**
 * The Interface PfDao describes the DAO interface for reading and writing Policy Framework {@link PfConcept} concepts
 * to and from databases using JDBC.
 */
public interface PfDao {

    /**
     * Initialize the Policy Framework DAO with the given parameters.
     *
     * @param daoParameters parameters to use to access the database
     * @throws PfModelException on initialization errors
     */
    void init(DaoParameters daoParameters) throws PfModelException;

    /**
     * Close the Policy Framework DAO.
     */
    void close();

    /**
     * Creates an Policy Framework concept on the database.
     *
     * @param <T> the type of the object to create, a subclass of {@link PfConcept}
     * @param obj the object to create
     */
    <T extends PfConcept> void create(T obj);

    /**
     * Delete an Policy Framework concept on the database.
     *
     * @param <T> the type of the object to delete, a subclass of {@link PfConcept}
     * @param obj the object to delete
     */
    <T extends PfConcept> void delete(T obj);

    /**
     * Delete an Policy Framework concept on the database.
     *
     * @param <T> the type of the object to delete, a subclass of {@link PfConcept}
     * @param someClass the class of the object to delete, a subclass of {@link PfConcept}
     * @param key the key of the object to delete
     */
    <T extends PfConcept> void delete(Class<T> someClass, PfConceptKey key);

    /**
     * Delete an Policy Framework concept on the database.
     *
     * @param <T> the type of the object to delete, a subclass of {@link PfConcept}
     * @param someClass the class of the object to delete, a subclass of {@link PfConcept}
     * @param key the key of the object to delete
     */
    <T extends PfConcept> void delete(Class<T> someClass, PfReferenceKey key);

    /**
     * Create a collection of objects in the database.
     *
     * @param <T> the type of the object to create, a subclass of {@link PfConcept}
     * @param objs the objects to create
     */
    <T extends PfConcept> void createCollection(Collection<T> objs);

    /**
     * Delete a collection of objects in the database.
     *
     * @param <T> the type of the objects to delete, a subclass of {@link PfConcept}
     * @param objs the objects to delete
     */
    <T extends PfConcept> void deleteCollection(Collection<T> objs);

    /**
     * Delete a collection of objects in the database referred to by concept key.
     *
     * @param <T> the type of the objects to delete, a subclass of {@link PfConcept}
     * @param someClass the class of the objects to delete, a subclass of {@link PfConcept}
     * @param keys the keys of the objects to delete
     * @return the number of objects deleted
     */
    <T extends PfConcept> int deleteByConceptKey(Class<T> someClass, Collection<PfConceptKey> keys);

    /**
     * policypolicypolicy Delete a collection of objects in the database referred to by reference key.
     *
     * @param <T> the type of the objects to delete, a subclass of {@link PfConcept}
     * @param someClass the class of the objects to delete, a subclass of {@link PfConcept}
     * @param keys the keys of the objects to delete
     * @return the number of objects deleted
     */
    <T extends PfConcept> int deleteByReferenceKey(Class<T> someClass, Collection<PfReferenceKey> keys);

    /**
     * Delete all objects of a given class in the database.
     *
     * @param <T> the type of the objects to delete, a subclass of {@link PfConcept}
     * @param someClass the class of the objects to delete, a subclass of {@link PfConcept}
     */
    <T extends PfConcept> void deleteAll(Class<T> someClass);

    /**
     * Get an object from the database, referred to by concept key.
     *
     * @param <T> the type of the object to get, a subclass of {@link PfConcept}
     * @param someClass the class of the object to get, a subclass of {@link PfConcept}, if name is null, all concepts
     *        of type T are returned, if name is not null and version is null, all versions of that concept matching the
     *        name are returned.
     * @param key the key of the object to get
     * @return the objects that was retrieved from the database
     */
    <T extends PfConcept> List<T> getFiltered(Class<T> someClass, PfConceptKey key);

    /**
     * Get an object from the database, referred to by concept key.
     *
     * @param <T> the type of the object to get, a subclass of {@link PfConcept}
     * @param someClass the class of the object to get, a subclass of {@link PfConcept}
     * @param key the key of the object to get
     * @return the object that was retrieved from the database
     */
    <T extends PfConcept> T get(Class<T> someClass, PfConceptKey key);

    /**
     * Get an object from the database, referred to by reference key.
     *
     * @param <T> the type of the object to get, a subclass of {@link PfConcept}
     * @param someClass the class of the object to get, a subclass of {@link PfConcept}
     * @param key the key of the object to get
     * @return the object that was retrieved from the database or null if the object was not retrieved
     */
    <T extends PfConcept> T get(Class<T> someClass, PfReferenceKey key);

    /**
     * Get all the objects in the database of a given type.
     *
     * @param <T> the type of the objects to get, a subclass of {@link PfConcept}
     * @param someClass the class of the objects to get, a subclass of {@link PfConcept}
     * @return the objects or null if no objects were retrieved
     */
    <T extends PfConcept> List<T> getAll(Class<T> someClass);

    /**
     * Get all the objects in the database of the given type with the given parent concept key.
     *
     * @param <T> the type of the objects to get, a subclass of {@link PfConcept}
     * @param someClass the class of the objects to get, a subclass of {@link PfConcept}
     * @param parentKey the parent key of the concepts to get
     * @return the all
     */
    <T extends PfConcept> List<T> getAll(Class<T> someClass, PfConceptKey parentKey);

    /**
     * Get all the objects in the database of a given type.
     *
     * @param <T> the type of the objects to get, a subclass of {@link PfConcept}
     * @param someClass the class of the objects to get, a subclass of {@link PfConcept}
     * @param name the name of the concepts for which to get all versions
     * @return the objects or null if no objects were retrieved
     */
    <T extends PfConcept> List<T> getAllVersions(Class<T> someClass, final String name);

    /**
     * Get latest version of objects in the database of a given type.
     *
     * @param <T> the type of the objects to get, a subclass of {@link PfConcept}
     * @param someClass the class of the objects to get, a subclass of {@link PfConcept}
     * @return the objects or null if no objects were retrieved
     */
    <T extends PfConcept> List<T> getLatestVersions(Class<T> someClass);

    /**
     * Get latest version of an object in the database of a given type.
     *
     * @param <T> the type of the objects to get, a subclass of {@link PfConcept}
     * @param someClass the class of the objects to get, a subclass of {@link PfConcept}
     * @param conceptName the name of the concept for which to get the latest version
     * @return the objects or null if no objects were retrieved
     */
    <T extends PfConcept> T getLatestVersion(Class<T> someClass, final String conceptName);

    /**
     * Get a concept from the database with the given concept key.
     *
     * @param <T> the type of the object to get, a subclass of {@link PfConcept}
     * @param someClass the class of the object to get, a subclass of {@link PfConcept}
     * @param conceptId the concept key of the concept to get
     * @return the concept that matches the key or null if the concept is not retrieved
     */
    <T extends PfConcept> T getConcept(Class<T> someClass, PfConceptKey conceptId);

    /**
     * Get a concept from the database with the given reference key.
     *
     * @param <T> the type of the object to get, a subclass of {@link PfConcept}
     * @param someClass the class of the object to get, a subclass of {@link PfConcept}
     * @param conceptId the concept key of the concept to get
     * @return the concept that matches the key or null if the concept is not retrieved
     */
    <T extends PfConcept> T getConcept(Class<T> someClass, PfReferenceKey conceptId);

    /**
     * Get the number of instances of a concept that exist in the database.
     *
     * @param <T> the type of the object to get, a subclass of {@link PfConcept}
     * @param someClass the class of the object to get, a subclass of {@link PfConcept}
     * @return the number of instances of the concept in the database
     */
    <T extends PfConcept> long size(Class<T> someClass);

    /**
     * Update a concept in the database.
     *
     * @param <T> the type of the object to update, a subclass of {@link PfConcept}
     * @param obj the object to update
     * @return the updated object
     */
    <T extends PfConcept> T update(T obj);
}
