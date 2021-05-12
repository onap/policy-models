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

import org.onap.policy.models.base.PfConcept;
import org.onap.policy.models.dao.impl.PfNonTimestampKeyFilter;
import org.onap.policy.models.dao.impl.PfReferenceTimestampKeyFilter;
import org.onap.policy.models.dao.impl.PfTimestampKeyFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This factory class creates a filter class for a key type.
 * The filter returned depends on the type of class being used.
 *
 */
public class PfFilterFactory {
    // Get a reference to the logger
    private static final Logger LOGGER = LoggerFactory.getLogger(PfFilterFactory.class);

    /**
     * Return a filter class for the input class.
     *
     * @param <T> the type of the object to get, a subclass of {@link PfConcept}*
     * @return the filter type for the input class   *
     */
    public <T extends PfConcept> PfFilter createFilter(final Class<T> someClass) {
        PfFilter filter = null;

        switch (getKeyName(someClass)) {
            case "PfTimestampKey":
                filter = new PfTimestampKeyFilter();
                break;
            case "PfReferenceTimestampKey":
                filter = new PfReferenceTimestampKeyFilter();
                break;
            default:
                filter = new PfNonTimestampKeyFilter();
        }
        return filter;
    }

    /**
     * Gets the name of the key class of the class invoking the DAO.
     * @param someClass class that invoked Dao
     * @return the name of the key class
     */
    private <T extends PfConcept> String getKeyName(final Class<T> someClass) {
        try {
            var fullClassName = someClass.getDeclaredField("key").getType().toString();
            return fullClassName.substring(fullClassName.lastIndexOf('.') + 1);
        } catch (NoSuchFieldException e) {
            LOGGER.error("Error getting the key:", e);
            return "NON_TIMESTAMP_KEY";
        }
    }
}
