/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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
     * @param regexp the regular expression to check against
     * @return match or not
     */
    public default boolean filterOnRegexp(@NonNull final String value, @NonNull final String regexp) {
        return value.matches(regexp);
    }

    /**
     * Sort an incoming list and remove all but the latest version of each concept.
     *
     * @param originalList the incoming list
     * @return the filtered list
     */
    public default List<T> latestVersionFilter(final List<T> originalList) {
        List<T> filteredList = new ArrayList<>();
        Collections.sort(filteredList);

        List<T> filteredOutList = new ArrayList<>();

        for (int i = 1; i < filteredList.size(); i++) {
            // Get the current and last element
            T thisElement = filteredList.get(i);
            T lastElement = filteredList.get(i - 1);

            // The list is sorted so if the last element name is the same as the current element name, the last element
            // should be removed
            if (((PfNameVersion)thisElement).getName().equals(((PfNameVersion)lastElement).getName())) {
                filteredOutList.add(lastElement);
            }
        }

        // We can now remove these elements
        filteredList.removeAll(filteredOutList);

        return filteredList;
    }
}
