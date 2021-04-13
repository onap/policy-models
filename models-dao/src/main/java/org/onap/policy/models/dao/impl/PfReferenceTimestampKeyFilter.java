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

package org.onap.policy.models.dao.impl;

import org.onap.policy.models.dao.PfFilter;

/**
 * This class is used to set the values for a reference timeStamp key query.
 *
 */
public class PfReferenceTimestampKeyFilter extends PfFilter {
    private static final String PARENT_NAME_REF_FILTER       = "c.key.referenceKey.parentKeyName = :parentKeyName";
    private static final String TIMESTAMP_START_FILTER       = "c.key.timeStamp >= :startTime";
    private static final String TIMESTAMP_END_FILTER         = "c.key.timeStamp <= :endTime";
    private static final String TIMESTAMP_FILTER             = " c.key.timeStamp ";
    private static final String NAME_PARAMETER               = "parentKeyName";

    /**
     * The default constructor injects query strings.
     */
    public PfReferenceTimestampKeyFilter() {
        setNameFilter(PARENT_NAME_REF_FILTER);
        setTimeStampStartFilter(TIMESTAMP_START_FILTER);
        setTimeStampEndFilter(TIMESTAMP_END_FILTER);
        setTimeStampFilter(TIMESTAMP_FILTER);
        setNameParameter(NAME_PARAMETER);
    }
}
