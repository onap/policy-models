/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
 *  Modifications Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.models.base;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.NonNull;

/**
 * Interface for filtering a list of concepts.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
@FunctionalInterface
public interface PfObjectFilter<T extends Comparable<T>> {
    /**
     * Filter an incoming list, removing items that do not match the filter.
     *
     * @param originalList the original list
     * @return the filtered list
     */
    public List<T> filter(final List<T> originalList);

    /**
     * Check if a value matches a regular expression.
     *
     * @param value the incoming value to check
     * @param pattern the pattern to check against
     * @return match or not
     */
    public default boolean filterString(@NonNull final String value, final String pattern) {
        return pattern == null || value.equals(pattern);
    }

    /**
     * Sort an incoming list and remove all but the latest version of each concept.
     *
     * @param originalList the incoming list
     * @return the filtered list
     */
    public default List<T> latestVersionFilter(final List<T> originalList) {
        if (originalList.size() <= 1) {
            return originalList;
        }

        List<T> filteredList = new ArrayList<>(originalList);
        Collections.sort(filteredList);

        int icur = 0;
        for (int j = 1; j < filteredList.size(); j++) {
            // Get the current and last element
            T curElement = filteredList.get(icur);
            T lastElement = filteredList.get(j);

            /*
             * The list is sorted so if the last element name is the same as the current element name, the current
             * element should be removed.
             */
            if (!((PfNameVersion) curElement).getName().equals(((PfNameVersion) lastElement).getName())) {
                // have a new name - done comparing with the old "current"
                ++icur;
            }

            filteredList.set(icur, lastElement);
        }

        return new ArrayList<>(filteredList.subList(0, icur + 1));
    }
}
