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

package org.onap.policy.models.pdp.persistence.provider;

import lombok.NonNull;

import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.dao.PfDao;
import org.onap.policy.models.pdp.concepts.PdpGroups;

/**
 * This class provides the provision of information on PAP concepts in the database to callers.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class PdpProvider {
    /**
     * Get PDP groups.
     *
     * @param dao the DAO to use to access the database
     * @param pdpGroupFilter a filter for the get
     * @return the PDP groups found
     * @throws PfModelException on errors getting PDP groups
     */
    public PdpGroups getPdpGroups(@NonNull final PfDao dao, @NonNull final String pdpGroupFilter)
            throws PfModelException {
        return new PdpGroups();
    }

    /**
     * Creates PDP groups.
     *
     * @param dao the DAO to use to access the database
     * @param pdpGroups a specification of the PDP groups to create
     * @return the PDP groups created
     * @throws PfModelException on errors creating PDP groups
     */
    public PdpGroups createPdpGroups(@NonNull final PfDao dao, @NonNull final PdpGroups pdpGroups)
            throws PfModelException {
        return new PdpGroups();
    }

    /**
     * Updates PDP groups.
     *
     * @param dao the DAO to use to access the database
     * @param pdpGroups a specification of the PDP groups to update
     * @return the PDP groups updated
     * @throws PfModelException on errors updating PDP groups
     */
    public PdpGroups updatePdpGroups(@NonNull final PfDao dao, @NonNull final PdpGroups pdpGroups)
            throws PfModelException {
        return new PdpGroups();
    }

    /**
     * Delete PDP groups.
     *
     * @param dao the DAO to use to access the database
     * @param pdpGroupFilter a filter for the get
     * @return the PDP groups deleted
     * @throws PfModelException on errors deleting PDP groups
     */
    public PdpGroups deletePdpGroups(@NonNull final PfDao dao, @NonNull final String pdpGroupFilter)
            throws PfModelException {
        return new PdpGroups();
    }
}
