/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix Foundation.
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
    public JpaToscaServiceTemplate read(@NonNull final PfDao dao) throws PfModelException {
        LOGGER.debug("->read");

        // Get the service template
        JpaToscaServiceTemplate serviceTemplate = dao.get(JpaToscaServiceTemplate.class, DEFAULT_SERVICE_TEMPLATE_KEY);

        LOGGER.debug("<-read: serviceTemplate={}", serviceTemplate);
        return serviceTemplate;
    }

    /**
     * Write a service template to the database.
     *
     * @param dao the DAO to use to access the database
     * @param serviceTemplate the service template to be written
     * @return the TOSCA service template overwritten by this method
     * @throws PfModelException on errors writing the service template
     */
    public JpaToscaServiceTemplate write(@NonNull final PfDao dao,
        @NonNull final JpaToscaServiceTemplate serviceTemplate) throws PfModelException {
        LOGGER.debug("->write: serviceTempalate={}", serviceTemplate);

        JpaToscaServiceTemplate overwrittenServiceTemplate = dao.update(serviceTemplate);

        LOGGER.debug("<-write: overwrittenServiceTemplate={}", overwrittenServiceTemplate);
        return overwrittenServiceTemplate;
    }

    /**
     * Delete a service template from the database.
     *
     * @param dao the DAO to use to access the database
     * @return the Service Template stored in the database
     * @throws PfModelException on errors getting the service template
     */
    public JpaToscaServiceTemplate delete(@NonNull final PfDao dao) throws PfModelException {
        LOGGER.debug("->delete");

        JpaToscaServiceTemplate serviceTemplateToBeDeleted =
            dao.get(JpaToscaServiceTemplate.class, DEFAULT_SERVICE_TEMPLATE_KEY);

        dao.delete(serviceTemplateToBeDeleted);

        LOGGER.debug("<-delete: serviceTemplate={}", serviceTemplateToBeDeleted);
        return serviceTemplateToBeDeleted;
    }

}
