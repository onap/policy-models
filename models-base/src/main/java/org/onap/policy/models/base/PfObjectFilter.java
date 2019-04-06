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
import java.util.Iterator;
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
        if (originalList.size() < 2) {
            // TODO ok to return the original list?  Or do we need to return a copy?
            return originalList;
        }

        List<T> filteredList = new ArrayList<>(originalList);
        Collections.sort(filteredList);

        // reverse so we work from higher to lower versions
        Collections.reverse(filteredList);

        /*
         * The list is sorted so if the next element name is the same as the current
         * element name, the next element should be removed
         */
        Iterator<T> iter = filteredList.iterator();
        T current = iter.next();

        while (iter.hasNext()) {
            T next = iter.next();

            if (((PfNameVersion) next).getName().equals(((PfNameVersion) current).getName())) {
                iter.remove();
            }

            current = next;
        }

        // TODO is this necessary?
        Collections.reverse(filteredList);

        return filteredList;
    }
}
