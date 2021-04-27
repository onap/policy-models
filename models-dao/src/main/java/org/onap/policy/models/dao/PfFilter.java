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

package org.onap.policy.models.dao;

import java.time.Instant;
import java.util.Map;
import lombok.Data;
/**
 * This abstract class is used as a base for the filter implementations.
 *
 */

@Data
public abstract class PfFilter {
    private static final String AND        = " AND ";
    private static final String ORDER      = " ORDER BY ";

    private String nameFilter;
    private String timeStampStartFilter;
    private String timeStampEndFilter;
    private String timeStampFilter;
    private String nameParameter;
    private String keyPrefix;

    /**
     * Generates filter string.
     *
     * @param inputFilterString current filterString generated from FilterMap
     * @param name the pdpInstance name for the PDP statistics to get
     * @param startTime the start timeStamp to filter from database, filter rule:
     *     startTime <= filteredRecord timeStamp <= endTime. null for ignore end time
     * @param endTime the end timeStamp to filter from database, filter rule:
     *     startTime <= filteredRecord timeStamp <= endTime. null for ignore end time
     * @param filterMap Map store extra key/value used to filter from database, can be null     *
     * @param sortOrder sortOrder to query database
     * @param getRecordNum Total query count from database

     */
    public String addFilter(final String inputFilterString,
        final String name, final Instant startTime, final Instant endTime,
        final Map<String, Object> filterMap, final String sortOrder, final int getRecordNum) {
        StringBuilder filterQueryString = new StringBuilder(inputFilterString);
        if (filterMap != null) {
            for (String key : filterMap.keySet()) {
                filterQueryString.append(getKeyPrefix() + key + "= :" + key + AND);
            }
        }

        if (name != null) {
            filterQueryString.append(getNameFilter() + AND);
        }

        if (startTime != null) {
            if (endTime != null) {
                filterQueryString.append(getTimeStampStartFilter());
                filterQueryString.append(AND);
                filterQueryString.append(getTimeStampEndFilter());
            } else {
                filterQueryString.append(getTimeStampStartFilter());
            }
        } else {
            if (endTime != null) {
                filterQueryString.append(getTimeStampEndFilter());
            } else {
                filterQueryString.delete(filterQueryString.length() - AND.length(), filterQueryString.length());
            }
        }

        if (getRecordNum > 0) {
            filterQueryString.append(ORDER + getTimeStampFilter() + sortOrder);
        }
        return filterQueryString.toString();
    }
}
