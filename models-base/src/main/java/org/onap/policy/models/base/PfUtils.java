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
}
