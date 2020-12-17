/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2020 Nordix Foundation.
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

import org.apache.commons.lang3.ObjectUtils;
import org.onap.policy.common.utils.validation.Version;

/**
 * An interface that forces a POJO to have getName() and getVersion() methods.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public interface PfNameVersion {
    public String getName();

    public void setName(final String name);

    public String getVersion();

    public void setVersion(final String version);

    /**
     * Get the defined name for a concept, return null if no name is defined.
     *
     * @return the defined name
     */
    public default String getDefinedName() {
        return getName();
    }

    /**
     * Get the defined version for a concept, return null if no version is defined.
     *
     * @return the defined version
     */
    public default String getDefinedVersion() {
        return getVersion();
    }

    /**
     * Compare two name version implementation objects.
     *
     * @param left the left name/version implementation
     * @param right the right name/version implementation
     * @return the comparison resilt
     */
    public default int compareNameVersion(final PfNameVersion left, final PfNameVersion right) {
        if (left == null && right == null) {
            return 0;
        }

        if (left == null) {
            return 1;
        }

        if (right == null) {
            return -1;
        }

        int result = ObjectUtils.compare(left.getName(), right.getName());
        if (result != 0) {
            return result;
        }

        if (left.getVersion() == null && right.getVersion() == null) {
            return 0;
        }

        if (left.getVersion() == null) {
            return 1;
        }

        if (right.getVersion() == null) {
            return -1;
        }
        return new Version(left.getVersion()).compareTo(new Version(right.getVersion()));
    }
}
