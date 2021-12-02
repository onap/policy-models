/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation.
 *
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
import java.util.Map;
import javax.ws.rs.core.Response;
import lombok.NonNull;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.dao.PfDao;
import org.onap.policy.models.dao.PfFilterParameters;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaPolicyTypeImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PolicyTypeImplProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(PolicyTypeImplProvider.class);


    /**
     * Get all policy type impl entities from the database.
     *
     * @param dao the DAO to use to access the database
     * @return the list of policyy type impl entities read from the database
     * @throws PfModelException on errors getting the data
     */
    public List<JpaToscaPolicyTypeImpl> getAllPolicyTypeImpl(@NonNull final PfDao dao)
        throws PfModelException {
        LOGGER.debug("->read policyTypeImpl");

        try {
            List<JpaToscaPolicyTypeImpl> policyImpls =
                dao.getAll(JpaToscaPolicyTypeImpl.class);

            LOGGER.debug("<-read: policyTypeImpl={}", policyImpls);
            return policyImpls;
        } catch (Exception dbException) {
            throw new PfModelException(Response.Status.INTERNAL_SERVER_ERROR, "database read error on policy type impl"
               + "\n" + dbException.getMessage(), dbException);
        }
    }

    /**
     * Get filtered policy type impl entities from the database.
     *
     * @param dao the DAO to use to access the database
     * @return the list of policy type impl entities read from the database
     * @throws PfModelException on errors getting the data
     */
    public List<JpaToscaPolicyTypeImpl> getFilteredPolicyTypeImpl(@NonNull final PfDao dao,
                                                                  Map<String, Object> filterMap)
        throws PfModelException {
        LOGGER.debug("->read policyTypeImpl");

        try {
            // @formatter:off
            PfFilterParameters filterParams = PfFilterParameters
                .builder()
                .filterMap(filterMap)
                .build();
            // @formatter:on

            List<JpaToscaPolicyTypeImpl> policyImpls = dao.getFiltered(JpaToscaPolicyTypeImpl.class, filterParams);

            LOGGER.debug("<-read: policyTypeImpl={}", policyImpls);
            return policyImpls;
        } catch (Exception dbException) {
            throw new PfModelException(Response.Status.INTERNAL_SERVER_ERROR, "database read error on policy type impl"
                + "\n" + dbException.getMessage(), dbException);
        }
    }


    /**
     * Get a policy type impl from the database.
     *
     * @param dao the DAO to use to access the database
     * @return the policy type impl read from the database
     * @throws PfModelException on errors getting the data
     */
    public JpaToscaPolicyTypeImpl getPolicyTypeImpl(@NonNull final PfDao dao, @NonNull PfConceptKey key)
        throws PfModelException {
        LOGGER.debug("->read");

        try {
            // Get the policy type impl
            JpaToscaPolicyTypeImpl policyImpl =
                dao.get(JpaToscaPolicyTypeImpl.class, key);

            LOGGER.debug("<-read: policyImpl={}", policyImpl);
            return policyImpl;
        } catch (Exception dbException) {
            throw new PfModelException(Response.Status.INTERNAL_SERVER_ERROR, "database read error on policy type impl "
                + key.getName() + "\n" + dbException.getMessage(), dbException);
        }
    }

    /**
     * Write a policy type impl to the database.
     *
     * @param dao the DAO to use to access the database
     * @param policyImpl the service template to be written
     * @return the policy type impl created by this method
     * @throws PfModelException on errors writing the policyTypeImpl
     */
    public JpaToscaPolicyTypeImpl createPolicyTypeImpl(@NonNull final PfDao dao,
                                            @NonNull final JpaToscaPolicyTypeImpl policyImpl) throws PfModelException {

        try {
            LOGGER.debug("->write: policyTypeImpl={}", policyImpl);
            dao.create(policyImpl);
            JpaToscaPolicyTypeImpl createdPolicyTypeImpl = dao.get(JpaToscaPolicyTypeImpl.class, policyImpl.getKey());
            LOGGER.debug("<-write: writtenPolicyTypeImpl={}", createdPolicyTypeImpl);
            return createdPolicyTypeImpl;
        } catch (Exception dbException) {
            throw new PfModelException(Response.Status.INTERNAL_SERVER_ERROR,
                "database write error on policy type impl "
                + policyImpl.getKey().getName() + "\n" + dbException.getMessage(), dbException);
        }
    }

    /**
     * Update a policy type impl to the database.
     *
     * @param dao the DAO to use to access the database
     * @param policyImpl the service template to be written
     * @return the policy type impl updated by this method
     * @throws PfModelException on errors writing the policyTypeImpl
     */
    public JpaToscaPolicyTypeImpl updatePolicyTypeImpl(@NonNull final PfDao dao,
                                                       @NonNull final JpaToscaPolicyTypeImpl policyImpl)
        throws PfModelException {

        try {
            LOGGER.debug("->update: policyTypeImpl={}", policyImpl);
            JpaToscaPolicyTypeImpl updatedPolicyTypeImpl = dao.update(policyImpl);
            LOGGER.debug("<-update: updatedPolicyTypeImpl={}", updatedPolicyTypeImpl);
            return updatedPolicyTypeImpl;
        } catch (Exception dbException) {
            throw new PfModelException(Response.Status.INTERNAL_SERVER_ERROR,
                "database write error on policy type impl "
                    + policyImpl.getKey().getName() + "\n" + dbException.getMessage(), dbException);
        }
    }

    /**
     * Delete a policy type impl from the database.
     *
     * @param dao the DAO to use to access the database
     * @return the policy type impl deleted in the database
     * @throws PfModelException on errors getting the policyTypeImpl
     */
    public JpaToscaPolicyTypeImpl deletePolicyTypeImpl(@NonNull final PfDao dao, @NonNull PfConceptKey key)
        throws PfModelException {
        try {
            LOGGER.debug("->delete policyTypeImpl");

            JpaToscaPolicyTypeImpl policyImplToBeDeleted =
                dao.get(JpaToscaPolicyTypeImpl.class, key);

            dao.delete(policyImplToBeDeleted);

            LOGGER.debug("<-delete: deleted policyTypeImpl={}", policyImplToBeDeleted);
            return policyImplToBeDeleted;
        } catch (Exception dbException) {
            throw new PfModelException(Response.Status.INTERNAL_SERVER_ERROR,
                "database delete error on policy type impl "
                + key.getName() + "\n" + dbException.getMessage(), dbException);
        }
    }


}
