/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation.
 *  Modifications Copyright (C) 2021 AT&T Intellectual Property. All rights reserved.
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

import java.sql.Timestamp;
import java.util.Map;
import javax.persistence.TypedQuery;
import lombok.Data;
import org.onap.policy.models.base.PfConcept;
/**
 * This abstract class is used as a base for the filter implementations.
 *
 */

@Data
public abstract class PfFilter {
    private static final String WHERE      = " WHERE ";
    private static final String AND        = " AND ";
    private static final String ORDER      = " ORDER BY ";

    private String nameFilter;
    private String timeStampStartFilter;
    private String timeStampEndFilter;
    private String timeStampFilter;
    private String nameParameter;
    private String keyPrefix;

    /**
     * Generates the "WHERE" (and "ORDER BY") clause for a JPA query.
     */
    public String genWhereClause(PfFilterParametersIntfc parameters) {
        var filterQueryString = new StringBuilder(WHERE);
        if (parameters.getFilterMap() != null) {
            for (String key : parameters.getFilterMap().keySet()) {
                filterQueryString.append(getKeyPrefix() + key + "= :" + key + AND);
            }
        }

        if (parameters.getName() != null) {
            filterQueryString.append(getNameFilter() + AND);
        }

        if (parameters.getStartTime() != null) {
            if (parameters.getEndTime() != null) {
                filterQueryString.append(getTimeStampStartFilter());
                filterQueryString.append(AND);
                filterQueryString.append(getTimeStampEndFilter());
            } else {
                filterQueryString.append(getTimeStampStartFilter());
            }
        } else {
            if (parameters.getEndTime() != null) {
                filterQueryString.append(getTimeStampEndFilter());
            } else {
                filterQueryString.delete(filterQueryString.length() - AND.length(), filterQueryString.length());
            }
        }

        if (parameters.getRecordNum() > 0) {
            filterQueryString.append(ORDER + getTimeStampFilter() + parameters.getSortOrder());
        }
        return filterQueryString.toString();
    }

    /**
     * Sets the JPA query parameters, based on the filter parameters.
     * @param query query to populate
     */
    public <T extends PfConcept> void setParams(TypedQuery<T> query, PfFilterParametersIntfc parameters) {

        if (parameters.getFilterMap() != null) {
            for (Map.Entry<String, Object> entry : parameters.getFilterMap().entrySet()) {
                query.setParameter(entry.getKey(), entry.getValue());
            }
        }
        if (parameters.getName() != null) {
            query.setParameter(this.getNameParameter(), parameters.getName());
        }
        if (parameters.getStartTime() != null) {
            if (parameters.getEndTime() != null) {
                query.setParameter("startTime", Timestamp.from(parameters.getStartTime()));
                query.setParameter("endTime", Timestamp.from(parameters.getEndTime()));
            } else {
                query.setParameter("startTime", Timestamp.from(parameters.getStartTime()));
            }
        } else {
            if (parameters.getEndTime() != null) {
                query.setParameter("endTime", Timestamp.from(parameters.getEndTime()));
            }
        }
        if (parameters.getRecordNum() > 0) {
            query.setMaxResults(parameters.getRecordNum());
        }
    }
}
