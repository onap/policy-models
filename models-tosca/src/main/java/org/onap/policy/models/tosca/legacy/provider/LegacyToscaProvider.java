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

package org.onap.policy.models.tosca.legacy.provider;

import lombok.NonNull;

import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.dao.PfDao;
import org.onap.policy.models.tosca.legacy.concepts.LegacyGuardPolicy;
import org.onap.policy.models.tosca.legacy.concepts.LegacyOperationalPolicy;

/**
 * This class provides the provision of information on TOSCA concepts in the database to callers in legacy formats.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class LegacyToscaProvider {
    /**
     * Get legacy operational policy.
     *
     * @param dao the DAO to use to access the database
     * @param policyId ID of the policy.
     * @return the policies found
     * @throws PfModelException on errors getting policies
     */
    public LegacyOperationalPolicy getOperationalPolicy(@NonNull final PfDao dao, @NonNull final String policyId)
            throws PfModelException {
        return null;
    }

    /**
     * Create legacy operational policy.
     *
     * @param dao the DAO to use to access the database
     * @param legacyOperationalPolicy the definition of the policy to be created.
     * @return the created policy
     * @throws PfModelException on errors creating policies
     */
    public LegacyOperationalPolicy createOperationalPolicy(@NonNull final PfDao dao,
            @NonNull final LegacyOperationalPolicy legacyOperationalPolicy) throws PfModelException {
        return null;
    }

    /**
     * Update legacy operational policy.
     *
     * @param dao the DAO to use to access the database
     * @param legacyOperationalPolicy the definition of the policy to be updated
     * @return the updated policy
     * @throws PfModelException on errors updating policies
     */
    public LegacyOperationalPolicy updateOperationalPolicy(@NonNull final PfDao dao,
            @NonNull final LegacyOperationalPolicy legacyOperationalPolicy) throws PfModelException {
        return null;
    }

    /**
     * Delete legacy operational policy.
     *
     * @param dao the DAO to use to access the database
     * @param policyId ID of the policy.
     * @return the deleted policy
     * @throws PfModelException on errors deleting policies
     */
    public LegacyOperationalPolicy deleteOperationalPolicy(@NonNull final PfDao dao, @NonNull final String policyId)
            throws PfModelException {
        return null;
    }

    /**
     * Get legacy guard policy.
     *
     * @param dao the DAO to use to access the database
     * @param policyId ID of the policy.
     * @return the policies found
     * @throws PfModelException on errors getting policies
     */
    public LegacyGuardPolicy getGuardPolicy(@NonNull final PfDao dao, @NonNull final String policyId)
            throws PfModelException {
        return null;
    }

    /**
     * Create legacy guard policy.
     *
     * @param dao the DAO to use to access the database
     * @param legacyGuardPolicy the definition of the policy to be created.
     * @return the created policy
     * @throws PfModelException on errors creating policies
     */
    public LegacyGuardPolicy createGuardPolicy(@NonNull final PfDao dao,
            @NonNull final LegacyGuardPolicy legacyGuardPolicy) throws PfModelException {
        return null;
    }

    /**
     * Update legacy guard policy.
     *
     * @param dao the DAO to use to access the database
     * @param legacyGuardPolicy the definition of the policy to be updated
     * @return the updated policy
     * @throws PfModelException on errors updating policies
     */
    public LegacyGuardPolicy updateGuardPolicy(@NonNull final PfDao dao,
            @NonNull final LegacyGuardPolicy legacyGuardPolicy) throws PfModelException {
        return null;
    }

    /**
     * Delete legacy guard policy.
     *
     * @param dao the DAO to use to access the database
     * @param policyId ID of the policy.
     * @return the deleted policy
     * @throws PfModelException on errors deleting policies
     */
    public LegacyGuardPolicy deleteGuardPolicy(@NonNull final PfDao dao, @NonNull final String policyId)
            throws PfModelException {
        return null;
    }
}
