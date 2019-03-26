/*
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
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Utility class for Policy Framework concept utilities.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public final class PfUtils {
    private PfUtils() {
        // Cannot be subclassed
    }

    /**
     * Compare two objects using their equals methods, nulls are allowed.
     *
     * @param leftObject the first object
     * @param rightObject the second object
     * @return a measure of the comparison
     */
    public static int compareObjects(final Object leftObject, final Object rightObject) {
        if (leftObject == null && rightObject == null) {
            return 0;
        }

        if (leftObject == null) {
            return 1;
        }

        if (rightObject == null) {
            return -1;
        }

        if (!leftObject.equals(rightObject)) {
            return leftObject.hashCode() - rightObject.hashCode();
        }

        return 0;
    }

    /**
     * Convenience method to apply a mapping function to all of the elements of a list,
     * generating a new list.
     *
     * @param source list whose elements are to be mapped, or {@code null}
     * @param mapFunc mapping function
     * @return a new list, containing mappings of all of the items in the original list
     */
    public static <T> List<T> mapList(List<T> source, Function<T, T> mapFunc) {
        if (source == null) {
            return new ArrayList<>();
        }

        return source.stream().map(mapFunc).collect(Collectors.toList());
    }
}
