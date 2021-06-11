/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation.
<<<<<<< HEAD
 *  Modifications Copyright (C) 2021 AT&T Intellectual Property. All rights reserved.
=======
 *  Modifications Copyright (C) 2021 Bell Canada. All rights reserved.
>>>>>>> Fix sonar in policy/models
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

package org.onap.policy.models.pap.persistence.provider;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.ws.rs.core.Response;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.onap.policy.common.parameters.BeanValidationResult;
import org.onap.policy.models.base.PfModelRuntimeException;
import org.onap.policy.models.dao.PfDao;
import org.onap.policy.models.dao.PfFilterParametersIntfc;
import org.onap.policy.models.pap.concepts.PolicyAudit;
import org.onap.policy.models.pap.concepts.PolicyAudit.AuditAction;
import org.onap.policy.models.pap.persistence.concepts.JpaPolicyAudit;

/**
 * Provider for Policy Audit.
 *
 * @author Adheli Tavares (adheli.tavares@est.tech)
 *
 */
public class PolicyAuditProvider {

    private static final Integer DEFAULT_MAX_RECORDS = 100;

    /**
     * Create audit records.
     *
     * @param audits list of policy audit
     */
    public void createAuditRecords(@NonNull PfDao dao, @NonNull final List<PolicyAudit> audits) {
        List<JpaPolicyAudit> jpaAudits = audits.stream().map(JpaPolicyAudit::new).collect(Collectors.toList());

        var result = new BeanValidationResult("createAuditRecords", jpaAudits);

        var count = 0;
        for (JpaPolicyAudit jpaAudit : jpaAudits) {
            result.addResult(jpaAudit.validate(String.valueOf(count++)));
        }

        if (!result.isValid()) {
            throw new PfModelRuntimeException(Response.Status.BAD_REQUEST, result.getResult());
        }

        dao.createCollection(jpaAudits);
    }

    /**
     * Collect all audit records.
     *
     * @param numRecords number of records to be collected
     * @return list of {@link PolicyAudit} records
     */
    public List<PolicyAudit> getAuditRecords(@NonNull PfDao dao, @NonNull Integer numRecords) {
        numRecords = numRecords > DEFAULT_MAX_RECORDS ? DEFAULT_MAX_RECORDS : numRecords;

        // @formatter:off
        return dao.getAll(JpaPolicyAudit.class, "timeStamp DESC", numRecords)
                .stream()
                .map(JpaPolicyAudit::toAuthorative)
                .collect(Collectors.toList());
        // @formatter:on
    }

    /**
     * Collect audit records based on filters at {@link AuditFilter}.
     *
     * @param auditFilter {@link AuditFilter} object with filters for search
     * @param numRecords number of records to be collected
     * @return list of {@link PolicyAudit} records
     */
    public List<PolicyAudit> getAuditRecords(@NonNull PfDao dao, @NonNull AuditFilter auditFilter,
            @NonNull Integer numRecords) {

        auditFilter.setRecordNum(Math.min(numRecords, DEFAULT_MAX_RECORDS));

        return getAuditRecords(dao, auditFilter);
    }

    /**
     * Collect audit records based on filters at {@link AuditFilter}.
     *
     * @param auditFilter {@link AuditFilter} object with filters for search
     * @return list of {@link PolicyAudit} records
     */
    public List<PolicyAudit> getAuditRecords(@NonNull PfDao dao, @NonNull AuditFilter auditFilter) {

        return dao.getFiltered(JpaPolicyAudit.class, auditFilter)
                    .stream().map(JpaPolicyAudit::toAuthorative).collect(Collectors.toList());
    }

    /**
     * Create a filter for looking for audit records.
     * name - policy name
     * version - policy version
     * pdpGroup - PDP group that policy might be related
     * action - type of action/operation realized on policy
     * fromDate - start of period in case of time interval search
     */
    @Data
    @Builder
    public static class AuditFilter implements PfFilterParametersIntfc {
        private String name;
        private String version;
        private AuditAction action;
        private String pdpGroup;
        private Instant fromDate;
        private Instant toDate;
        private int recordNum;
        @Builder.Default
        private String sortOrder = "DESC";

        // initialized lazily, if not set via the builder
        private Map<String, Object> filterMap;

        /**
         * Check if even still using build(), none of the params were provided.
         *
         * @return {@code true} if all empty/null; {@code false} otherwise.
         */
        public boolean isEmpty() {
            return StringUtils.isAllEmpty(name, version, pdpGroup) && action == null && fromDate == null
                    && toDate == null;
        }

        @Override
        public Instant getStartTime() {
            return fromDate;
        }

        @Override
        public Instant getEndTime() {
            return toDate;
        }

        @Override
        public Map<String, Object> getFilterMap() {
            if (filterMap != null) {
                return filterMap;
            }

            filterMap = new HashMap<>();

            if (StringUtils.isNotBlank(pdpGroup)) {
                filterMap.put("pdpGroup", pdpGroup);
            }

            if (action != null) {
                filterMap.put("action", action);
            }

            return filterMap;
        }
    }
}
