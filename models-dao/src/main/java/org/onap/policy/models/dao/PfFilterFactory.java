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
import org.onap.policy.models.base.PfConcept;
import org.onap.policy.models.dao.impl.PfNonTimestampKeyFilter;
import org.onap.policy.models.dao.impl.PfReferenceTimestampKeyFilter;
import org.onap.policy.models.dao.impl.PfTimestampKeyFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This factory class creates a filter class for a key type.
 * It sets the nameParameter and queryString variables using the filter
 * The format of the query depends on the type of class being used.
 *
 */
@Data
public class PfFilterFactory {
    // Get a reference to the logger
    private static final Logger LOGGER = LoggerFactory.getLogger(PfFilterFactory.class);

    private String keyName;
    private String nameParameter;
    private String queryString;

    /**
     * Generates filter string with the filter values.
     *
     * @param <T> the type of the object to get, a subclass of {@link PfConcept}*
     * @param inputFilterString current filterString generated from FilterMap
     * @param name the pdpInstance name for the PDP statistics to get
     * @param startTime the start timeStamp to filter from database, filter rule:
     *     startTime <= filteredRecord timeStamp <= endTime. null for ignore end time
     * @param endTime the end timeStamp to filter from database, filter rule:
     *     startTime <= filteredRecord timeStamp <= endTime. null for ignore end time
     * @param filterMap Map store extra key/value used to filter from database, can be null     *
     * @param sortOrder sortOrder to query database
     * @param getRecordNum Total query count from database     *
     */
    public <T extends PfConcept> void createFilter(final Class<T> someClass, final String inputFilterString,
        final String name, final Instant startTime, final Instant endTime,
        final Map<String, Object> filterMap, final String sortOrder, final int getRecordNum) {

        setKeyName(someClass);
        LOGGER.debug("Key Type is {}", getKeyName());

        PfFilter filter = null;

        switch (keyName) {
            case "PfTimestampKey":
                filter = new PfTimestampKeyFilter();
                setNameParameter(filter.getNameParameter());
                setQueryString(filter.addFilter(inputFilterString,
                    name, startTime, endTime, filterMap, sortOrder, getRecordNum));
                break;
            case "PfReferenceTimestampKey":
                filter = new PfReferenceTimestampKeyFilter();
                setNameParameter(filter.getNameParameter());
                setQueryString(filter.addFilter(inputFilterString,
                    name, startTime, endTime, filterMap, sortOrder, getRecordNum));
                break;
            default:
                filter = new PfNonTimestampKeyFilter();
                setNameParameter(filter.getNameParameter());
                setQueryString(filter.addFilter(inputFilterString,
                    name, startTime, endTime, filterMap, sortOrder, getRecordNum));
        }
    }

    /**
     * Sets the name of the key class of the class invoking the DAO.
     * @param someClass class that invoked Dao
     */
    private <T extends PfConcept> void setKeyName(final Class<T> someClass) {
        try {
            String fullClassName = someClass.getDeclaredField("key").getType().toString();
            this.keyName = fullClassName.substring(fullClassName.lastIndexOf('.') + 1);
        } catch (NoSuchFieldException e) {
            LOGGER.error("Error setting the key:", e);
        }
    }
}
