/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2021 Nordix Foundation.
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
import java.util.Arrays;
import java.util.Map;
import org.onap.policy.models.base.PfConcept;
import org.onap.policy.models.base.PfReferenceTimestampKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This factory class returns a query string for a class with a timeStamp field.
 * The format of the query depends on the type of class being used.
 *
 */
public class PfFilterFactory {
    // Get a reference to the logger
    private static final Logger LOGGER = LoggerFactory.getLogger(PfFilterFactory.class);
    private static final String NAME_FILTER                  = "c.key.name = :name";
    private static final String PARENT_NAME_REF_FILTER       = "c.key.referenceKey.parentKeyName = :parentKeyName";
    private static final String TIMESTAMP_START_FILTER       = "c.key.timeStamp >= :startTime";
    private static final String TIMESTAMP_END_FILTER         = "c.key.timeStamp <= :endTime";
    private static final String TIMESTAMP_START_FILTER_NOKEY = "c.timeStamp >= :startTime";
    private static final String TIMESTAMP_END_FILTER_NOKEY   = "c.timeStamp <= :endTime";

    private static final String AND        = " AND ";
    private static final String ORDER      = " ORDER BY ";

    public enum FilterType {
          KEY_TIMESTAMP, REF_KEY_TIMESTAMP, NON_KEY_TIMESTAMP
    }

    /**
     * generate filter string with the filter values.
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
     * @param getRecordNum Total query count from database
     * @return the filter string to query database
     */
    public <T extends PfConcept> String createFilter(final Class<T> someClass, final String inputFilterString,
        final String name, final Instant startTime, final Instant endTime,
        final Map<String, Object> filterMap, final String sortOrder, final int getRecordNum) {

        String queryString = "";
        FilterType filterType = getFilterType(someClass);

        switch (filterType) {
            case NON_KEY_TIMESTAMP:
                queryString = addTimestampNonKeyFilter(inputFilterString, name, startTime,
                     endTime, filterMap, sortOrder, getRecordNum);
                LOGGER.debug("Key Type is NON_KEY_TIMESTAMP");
                break;
            case REF_KEY_TIMESTAMP:
                queryString = addReferenceTimestampFilter(inputFilterString, name, startTime,
                  endTime, filterMap, sortOrder, getRecordNum);
                LOGGER.debug("Key Type is REF_KEY_TIMESTAMP");
                break;
            default:
                queryString = addTimestampKeyFilter(inputFilterString, name, startTime,
                  endTime, filterMap, sortOrder, getRecordNum);
                LOGGER.debug("Key Type is KEY_TIMESTAMP");
        }
        return queryString;
    }

    /**
     * generate filter string with the filter value for keys without timeStamps.
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
     * @return the filter string to query database
     */
    public String addTimestampNonKeyFilter(final String inputFilterString,
        final String name, final Instant startTime, final Instant endTime,
        final Map<String, Object> filterMap, final String sortOrder, final int getRecordNum) {
        StringBuilder filterQueryString = new StringBuilder(inputFilterString);
        if (filterMap != null) {
            for (String key : filterMap.keySet()) {
                filterQueryString.append("c." + key + "= :" + key + AND);
            }
        }

        if (name != null) {
            filterQueryString.append(NAME_FILTER + AND);
        }

        if (startTime != null) {
            if (endTime != null) {
                filterQueryString.append(TIMESTAMP_START_FILTER_NOKEY);
                filterQueryString.append(AND);
                filterQueryString.append(TIMESTAMP_END_FILTER_NOKEY);
            } else {
                filterQueryString.append(TIMESTAMP_START_FILTER_NOKEY);
            }
        } else {
            if (endTime != null) {
                filterQueryString.append(TIMESTAMP_END_FILTER_NOKEY);
            } else {
                filterQueryString.delete(filterQueryString.length() - AND.length(), filterQueryString.length());
            }
        }

        if (getRecordNum > 0) {
            filterQueryString.append(ORDER + " c.timeStamp " + sortOrder);
        }
        return filterQueryString.toString();
    }

    /**
     * generate filter string with the filter value in TimestampKey.
     *
     * @param inputFilterString current filterString generated from FilterMap
     * @param name the pdpInstance name for the PDP statistics to get
     * @param startTime the start timeStamp to filter from database, filter rule:
     *     startTime <= filteredRecord timeStamp <= endTime. null for ignore end time
     * @param endTime the end timeStamp to filter from database, filter rule:
     *     startTime <= filteredRecord timeStamp <= endTime. null for ignore end time
     * @param filterMap Map store extra key/value used to filter from database, can be null
     * @param sortOrder sortOrder to query database
     * @param getRecordNum Total query count from database
     * @return the filter string to query database
     */
    public String addTimestampKeyFilter(final String inputFilterString,
        final String name, final Instant startTime, final Instant endTime,
        final Map<String, Object> filterMap, final String sortOrder, final int getRecordNum) {
        StringBuilder filterQueryString = new StringBuilder(inputFilterString);
        if (filterMap != null) {
            for (String key : filterMap.keySet()) {
                filterQueryString.append("c." + key + "= :" + key + AND);
            }
        }

        if (name != null) {
            filterQueryString.append(NAME_FILTER + AND);
        }

        if (startTime != null) {
            if (endTime != null) {
                filterQueryString.append(TIMESTAMP_START_FILTER);
                filterQueryString.append(AND);
                filterQueryString.append(TIMESTAMP_END_FILTER);
            } else {
                filterQueryString.append(TIMESTAMP_START_FILTER);
            }
        } else {
            if (endTime != null) {
                filterQueryString.append(TIMESTAMP_END_FILTER);
            } else {
                filterQueryString.delete(filterQueryString.length() - AND.length(), filterQueryString.length());
            }
        }

        if (getRecordNum > 0) {
            filterQueryString.append(ORDER + " c.key.timeStamp " + sortOrder);
        }
        return filterQueryString.toString();
    }

    /**
     * generate filter string with the filter value in ReferenceTimestampKey.
     *
     * @param inputFilterString current filterString generated from FilterMap
     * @param name the pdpInstance name for the PDP statistics to get
     * @param startTime the start timeStamp to filter from database, filter rule:
     *     startTime <= filteredRecord timeStamp <= endTime. null for ignore end time
     * @param endTime the end timeStamp to filter from database, filter rule:
     *     startTime <= filteredRecord timeStamp <= endTime. null for ignore end time
     * @param filterMap Map store extra key/value used to filter from database, can be null
     * @param sortOrder sortOrder to query database
     * @param getRecordNum Total query count from database
     * @return the filter string to query database
     */
    public String addReferenceTimestampFilter(final String inputFilterString,
        final String name, final Instant startTime, final Instant endTime,
        final Map<String, Object> filterMap, final String sortOrder, final int getRecordNum) {
        StringBuilder filterQueryString = new StringBuilder(inputFilterString);
        if (filterMap != null) {
            for (String key : filterMap.keySet()) {
                filterQueryString.append("c.key.referenceKey." + key + "= :" + key + AND);
            }
        }

        if (name != null) {
            filterQueryString.append(PARENT_NAME_REF_FILTER + AND);
        }

        if (startTime != null) {
            if (endTime != null) {
                filterQueryString.append(TIMESTAMP_START_FILTER);
                filterQueryString.append(AND);
                filterQueryString.append(TIMESTAMP_END_FILTER);
            } else {
                filterQueryString.append(TIMESTAMP_START_FILTER);
            }
        } else {
            if (endTime != null) {
                filterQueryString.append(TIMESTAMP_END_FILTER);
            } else {
                filterQueryString.delete(filterQueryString.length() - AND.length(), filterQueryString.length());
            }
        }

        if (getRecordNum > 0) {
            filterQueryString.append(ORDER + " c.key.timeStamp " + sortOrder);
        }
        return filterQueryString.toString();
    }

    /**
     * This method checks the type of key the class invoking the DAO is using.
     * @param someClass class that invoked Dao
     * @return enum value of key.
     */
    public <T extends PfConcept> FilterType getFilterType(final Class<T> someClass) {
        if (isRefTimestampKey(someClass)) {
            return FilterType.REF_KEY_TIMESTAMP;
        } else if (isTimestampKey(someClass)) {
            return FilterType.KEY_TIMESTAMP;
        }
        return FilterType.NON_KEY_TIMESTAMP;
    }

    /**
     * This method checks if the class invoking the DAO is using a Key with a timeStamp field.
     * @param someClass class that invoked Dao
     * @return true if the key is Key that contains timestamp field.
     */
    private <T extends PfConcept> boolean isTimestampKey(final Class<T> someClass) {
        try {
            return Arrays.stream(someClass.getDeclaredField("key").getType().getDeclaredFields())
                         .anyMatch(f -> f.getName().equals("timeStamp"));
        } catch (NoSuchFieldException e) {
            LOGGER.error("Error verifying the key with timestamp:", e);
            return false;
        }
    }

    /**
     * This method checks if the class invoking the DAO is using PfReferenceTimestamp Key.
     * @param someClass class that invoked Dao
     * @return true if the key is PfReferenceTimestampKey.
     */
    private <T extends PfConcept> boolean isRefTimestampKey(final Class<T> someClass) {
        try {
            return PfReferenceTimestampKey.class.isAssignableFrom(someClass.getDeclaredField("key").getType());
        } catch (NoSuchFieldException e) {
            LOGGER.error("Error verifying the key for reference timestamp:", e);
            return false;
        }
    }
}
