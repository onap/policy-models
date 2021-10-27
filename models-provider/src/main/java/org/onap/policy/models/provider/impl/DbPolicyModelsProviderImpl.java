/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation.
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

import lombok.Getter;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.dao.impl.ProxyDao;

public class DbPolicyModelsProviderImpl extends AbstractPolicyModelsProvider {

    // Database connection and the DAO for reading and writing Policy Framework concepts
    @Getter
    private final ProxyDao pfDao;

    /**
     * Constructor.
     *
     * @param pfDao the ProxyDao
     */
    public DbPolicyModelsProviderImpl(ProxyDao pfDao) {
        this.pfDao = pfDao;
    }

    @Override
    public void init() throws PfModelException {
        // Not needs
    }

    @Override
    public void close() throws PfModelException {
        // Not needs
    }
}
