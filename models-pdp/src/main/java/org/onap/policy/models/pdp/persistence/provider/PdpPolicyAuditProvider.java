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

package org.onap.policy.models.pdp.persistence.provider;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.NonNull;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.dao.PfDao;
import org.onap.policy.models.pdp.concepts.PdpPolicyAudit;
import org.onap.policy.models.pdp.persistence.concepts.JpaPdpPolicyAudit;

/**
 * Provider for Policy Audit.
 *
 * @author Adheli Tavares (adheli.tavares@est.tech)
 *
 */
public class PdpPolicyAuditProvider {

    private static final Integer DEFAULT_MAX_RECORDS = 50;
    private static final String DESCENDING_ORDER = "DESC";

    /**
     * Collect the deployment history of policies in a Pdp Group.
     *
     * @param dao database access to be used.
     * @param groupName pdp group name to be audited.
     * @param startTime specified timestamp from where the audit starts.
     * @param maxRecords number max of records returned by query.
     * @return list of information about deployments acted upon the policies on pdp group.
     */
    public List<PdpPolicyAudit> auditPdpPolicyDeploymentByGroup(@NonNull final PfDao dao,
            @NonNull final String groupName, final Instant startTime, Integer maxRecords) {
        Map<String, Object> filter = Map.of("pdpGroup", groupName);
        maxRecords = (maxRecords == null) ? DEFAULT_MAX_RECORDS : maxRecords;

        return dao.getFiltered(JpaPdpPolicyAudit.class, null, null, startTime, Instant.now(), filter, DESCENDING_ORDER,
                maxRecords).stream().map(JpaPdpPolicyAudit::toAuthorative).collect(Collectors.toList());
    }

    /**
     * Collect the deployment history of policies based on name and version.
     *
     * @param dao database access to be used.
     * @param name policy name
     * @param version policy version
     * @param startTime specified timestamp from where the audit starts.
     * @param maxRecords number max of records returned by query.
     * @return list of information about deployments acted upon the policy with the name and version informed.
     */
    public List<PdpPolicyAudit> auditPdpPolicyDeploymentByPolicy(@NonNull final PfDao dao, @NonNull final String name,
            @NonNull final String version, final Instant startTime, Integer maxRecords) {
        maxRecords = (maxRecords == null) ? DEFAULT_MAX_RECORDS : maxRecords;

        return dao.getFiltered(JpaPdpPolicyAudit.class, name, version, startTime, Instant.now(), null, DESCENDING_ORDER,
                maxRecords).stream().map(JpaPdpPolicyAudit::toAuthorative).collect(Collectors.toList());
    }

    /**
     * Creates trackers for PDP policies' status which (deploy/undeploy) have changed.
     *
     * @param dao the DAO to use to access the database
     * @param trackers a specification of the PDP groups to create
     * @throws PfModelException on errors creating trackers
     */
    public void createPdpPolicyDeploymentAudit(@NonNull final PfDao dao, @NonNull final List<PdpPolicyAudit> trackers) {
        dao.createCollection(trackers.stream().map(JpaPdpPolicyAudit::new).collect(Collectors.toList()));
    }
}
