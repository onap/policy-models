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

package org.onap.policy.models.base.testconcepts;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import org.apache.commons.lang3.ObjectUtils;
import org.onap.policy.models.base.PfNameVersion;

/**
 * Dummy object for filtering using the {@link DummyPfObjectFilter} interface.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
@Data
@RequiredArgsConstructor
public class DummyPfObject implements PfNameVersion, Comparable<DummyPfObject> {
    private String name;
    private String version;
    private String description;

    @Override
    public int compareTo(@NonNull final DummyPfObject otherObject) {
        int result = ObjectUtils.compare(this.name, otherObject.name);
        if (result != 0) {
            return result;
        }
        result = ObjectUtils.compare(this.version, otherObject.version);
        if (result != 0) {
            return result;
        }
        return ObjectUtils.compare(this.description, otherObject.description);
    }
}
