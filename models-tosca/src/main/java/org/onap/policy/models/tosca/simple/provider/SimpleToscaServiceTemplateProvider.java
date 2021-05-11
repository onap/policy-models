/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020-2021 Nordix Foundation.
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

package org.onap.policy.models.tosca.simple.provider;

import java.util.List;
import javax.ws.rs.core.Response.Status;
import lombok.NonNull;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.dao.PfDao;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaServiceTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides CRUD to and from the database en bloc using Service Template reads and writes.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class SimpleToscaServiceTemplateProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleToscaServiceTemplateProvider.class);

    // There is only one service template in the database becasue TOSCA does not specify names and versions on service
    // templates.
    private static final PfConceptKey DEFAULT_SERVICE_TEMPLATE_KEY =
        new PfConceptKey(JpaToscaServiceTemplate.DEFAULT_NAME, JpaToscaServiceTemplate.DEFAULT_VERSION);

    /**
     * Get a service template from the database.
     *
     * @param dao the DAO to use to access the database
     * @return the Service Template read from the database
     * @throws PfModelException on errors getting the service template
     */
    protected JpaToscaServiceTemplate read(@NonNull final PfDao dao) throws PfModelException {
        LOGGER.debug("->read");

        try {
            // Get the service template
            JpaToscaServiceTemplate serviceTemplate =
                dao.get(JpaToscaServiceTemplate.class, DEFAULT_SERVICE_TEMPLATE_KEY);

            LOGGER.debug("<-read: serviceTemplate={}", serviceTemplate);
            return serviceTemplate;
        } catch (Exception dbException) {
            throw new PfModelException(Status.INTERNAL_SERVER_ERROR, "database read error on service tempalate"
                + DEFAULT_SERVICE_TEMPLATE_KEY.getId() + "\n" + dbException.getMessage(), dbException);
        }
    }

    /**
     * Get a list of service templates with given conceptName from the database.
     *
     * @param dao         the DAO to use to access the database
     * @param conceptName the conceptName
     * @return list of Service Template read from the database
     * @throws PfModelException on errors getting the service template
     */
    protected List<JpaToscaServiceTemplate> readByName(@NonNull final PfDao dao, @NonNull final String conceptName)
        throws PfModelException {
        LOGGER.debug("->read for conceptName {}", conceptName);

        try {
            // Get list of service templates
            List<JpaToscaServiceTemplate> serviceTemplate =
                dao.getAllVersions(JpaToscaServiceTemplate.class, conceptName);

            LOGGER.debug("<-read: serviceTemplate={}", serviceTemplate);
            return serviceTemplate;
        } catch (Exception dbException) {
            throw new PfModelException(Status.INTERNAL_SERVER_ERROR,
                "database read error on service template with conceptName " + conceptName + "\n"
                    + dbException.getMessage(), dbException);
        }
    }

    /**
     * Get a list of service templates with given conceptName and version from the database.
     *
     * @param dao     the DAO to use to access the database
     * @param conceptName    the conceptName
     * @param version the version
     * @return list of Service Template read from the database
     * @throws PfModelException on errors getting the service template
     */
    protected List<JpaToscaServiceTemplate> readByNameAndVersion(@NonNull final PfDao dao,
                                                                 final String conceptName,
                                                                 final String version)
        throws PfModelException {
        LOGGER.debug("->read for conceptName {} and version {}", conceptName, version);

        try {
            // Get list of service templates
            List<JpaToscaServiceTemplate> serviceTemplate =
                dao.getFiltered(JpaToscaServiceTemplate.class, conceptName, version);
            LOGGER.debug("<-read: serviceTemplate={}", serviceTemplate);
            return serviceTemplate;
        } catch (Exception dbException) {
            throw new PfModelException(Status.INTERNAL_SERVER_ERROR,
                "database read error on service template with conceptName " + conceptName + " and version " + version
                    + "\n" + dbException.getMessage(), dbException);
        }
    }

    /**
     * Write a service template to the database.
     *
     * @param dao the DAO to use to access the database
     * @param serviceTemplate the service template to be written
     * @return the TOSCA service template overwritten by this method
     * @throws PfModelException on errors writing the service template
     */
    protected JpaToscaServiceTemplate write(@NonNull final PfDao dao,
        @NonNull final JpaToscaServiceTemplate serviceTemplate) throws PfModelException {

        try {
            LOGGER.debug("->write: serviceTempalate={}", serviceTemplate);
            JpaToscaServiceTemplate overwrittenServiceTemplate = dao.update(serviceTemplate);
            LOGGER.debug("<-write: overwrittenServiceTemplate={}", overwrittenServiceTemplate);
            return overwrittenServiceTemplate;
        } catch (Exception dbException) {
            throw new PfModelException(Status.INTERNAL_SERVER_ERROR, "database write error on service tempalate"
                + serviceTemplate.getKey().getId() + "\n" + dbException.getMessage(), dbException);
        }
    }

    /**
     * Delete a service template from the database.
     *
     * @param dao the DAO to use to access the database
     * @return the Service Template stored in the database
     * @throws PfModelException on errors getting the service template
     */
    protected JpaToscaServiceTemplate delete(@NonNull final PfDao dao) throws PfModelException {
        try {
            LOGGER.debug("->delete");

            JpaToscaServiceTemplate serviceTemplateToBeDeleted =
                dao.get(JpaToscaServiceTemplate.class, DEFAULT_SERVICE_TEMPLATE_KEY);

            dao.delete(serviceTemplateToBeDeleted);

            LOGGER.debug("<-delete: serviceTemplate={}", serviceTemplateToBeDeleted);
            return serviceTemplateToBeDeleted;
        } catch (Exception dbException) {
            throw new PfModelException(Status.INTERNAL_SERVER_ERROR, "database delete error on service tempalate"
                + DEFAULT_SERVICE_TEMPLATE_KEY.getId() + "\n" + dbException.getMessage(), dbException);
        }
    }

    /**
     * Delete a service template from the database.
     *
     * @param dao the DAO to use to access the database
     * @return the Service Template stored in the database
     * @throws PfModelException on errors getting the service template
     */
    protected List<JpaToscaServiceTemplate> delete(@NonNull final PfDao dao, @NonNull final String conceptName)
        throws PfModelException {
        try {
            LOGGER.debug("->delete");

            final var deleted = dao.deleteByConceptName(JpaToscaServiceTemplate.class, conceptName);

            LOGGER.debug("<-deleted {} services with concept name: {}", deleted, conceptName);
            return deleted;
        } catch (Exception dbException) {
            throw new PfModelException(Status.INTERNAL_SERVER_ERROR, "database delete error on service template"
                + DEFAULT_SERVICE_TEMPLATE_KEY.getId() + "\n" + dbException.getMessage(), dbException);
        }
    }

    /**
     * Delete a service template from the database.
     *
     * @param dao the DAO to use to access the database
     * @return the Service Template stored in the database
     * @throws PfModelException on errors getting the service template
     */
    protected JpaToscaServiceTemplate delete(@NonNull final PfDao dao, @NonNull final String conceptName,
                                             @NonNull final String version)
        throws PfModelException {
        try {
            LOGGER.debug("->delete");
            final var pfConceptKey = new PfConceptKey(conceptName, version);
            JpaToscaServiceTemplate serviceTemplateToBeDeleted = dao.get(JpaToscaServiceTemplate.class, pfConceptKey);

            dao.delete(serviceTemplateToBeDeleted);

            LOGGER.debug("<-delete: serviceTemplate={}", serviceTemplateToBeDeleted);
            return serviceTemplateToBeDeleted;
        } catch (Exception dbException) {
            throw new PfModelException(Status.INTERNAL_SERVER_ERROR, "database delete error on service tempalate"
                + DEFAULT_SERVICE_TEMPLATE_KEY.getId() + "\n" + dbException.getMessage(), dbException);
        }
    }
}
