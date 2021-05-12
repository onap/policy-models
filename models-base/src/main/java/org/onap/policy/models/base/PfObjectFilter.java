/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2020 Nordix Foundation.
 *  Modifications Copyright (C) 2019, 2021 AT&T Intellectual Property. All rights reserved.
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

import com.google.re2j.Pattern;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Interface for filtering a list of concepts.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
@FunctionalInterface
public interface PfObjectFilter<T> {
    /**
     * Filter an incoming list, removing items that do not match the filter.
     *
     * @param originalList the original list
     * @return the filtered list
     */
    public List<T> filter(final List<T> originalList);

    /**
     * Check if a value exactly equals some text.
     *
     * @param value the incoming value to check
     * @param text the desired text to check against
     * @return match or not
     */
    public default boolean filterString(final String value, final String text) {
        return value == null || text == null || value.equals(text);
    }

    /**
     * Gets a predicate used to filter an item in a list by exactly matching an extracted value with some text.
     *
     * @param text the desired text to check against, or {@code null} if to accept everything
     * @param extractor function to extract the value, to be matched, from a list item
     * @return a predicate to match a value from a list item
     */
    public default Predicate<T> filterStringPred(final String text, Function<T, String> extractor) {
        // if null text, then everything matches
        if (text == null) {
            return item -> true;
        }

        return item -> text.equals(extractor.apply(item));
    }

    /**
     * Gets a predicate used to filter an item in a list by comparing the start of an extracted value with a prefix.
     *
     * @param prefix the desired prefix to check against, or {@code null} if to accept everything
     * @param extractor function to extract the value, to be matched, from a list item
     * @return a predicate to match a prefix with a value from a list item
     */
    public default Predicate<T> filterPrefixPred(final String prefix, Function<T, String> extractor) {
        // if null prefix, then everything matches
        if (prefix == null) {
            return item -> true;
        }

        return item -> {
            String value = extractor.apply(item);
            return (value != null && value.startsWith(prefix));
        };
    }

    /**
     * Gets a predicate used to filter an item in a list by matching an extracted value with a regular expression.
     *
     * @param pattern regular expression to match, or {@code null} if to accept everything
     * @param extractor function to extract the value, to be matched, from a list item
     * @return a predicate to match a value from a list item using a regular expression
     */
    public default Predicate<T> filterRegexpPred(final String pattern, Function<T, String> extractor) {
        // if null pattern, then everything matches
        if (pattern == null) {
            return item -> true;
        }

        var pat = Pattern.compile(pattern);

        return item -> {
            String value = extractor.apply(item);
            return (value != null && pat.matcher(value).matches());
        };
    }

    /**
     * Sort an incoming list and remove all but the latest version of each concept.
     *
     * @param originalList the incoming list
     * @param versionComparator the comparator to use to order versions of the incoming object
     * @return the filtered list
     */
    public default List<T> latestVersionFilter(final List<T> originalList, final Comparator<T> versionComparator) {
        if (originalList.size() <= 1) {
            return originalList;
        }

        List<T> filteredList = new ArrayList<>(originalList);
        Collections.sort(filteredList, versionComparator);

        var icur = 0;
        for (var j = 1; j < filteredList.size(); j++) {
            // Get the current and last element
            var curElement = filteredList.get(icur);
            var lastElement = filteredList.get(j);

            /*
             * The list is sorted so if the last element name is the same as the current element name, the current
             * element should be removed.
             */
            if (PfUtils.compareObjects(((PfNameVersion) curElement).getName(),
                    ((PfNameVersion) lastElement).getName()) != 0) {
                // have a new name - done comparing with the old "current"
                ++icur;
            }

            filteredList.set(icur, lastElement);
        }

        return new ArrayList<>(filteredList.subList(0, icur + 1));
    }
}
