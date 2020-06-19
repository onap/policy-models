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
import lombok.NoArgsConstructor;
import org.onap.policy.models.base.PfNameVersion;

/**
 * Dummy authorative concept.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
@Data
@NoArgsConstructor
public class DummyAuthorativeConcept implements PfNameVersion {
    private String name;
    private String version;
    private String description;

    /**
     * Constructor.
     *
     * @param name the name
     * @param version the version
     * @param description the description
     */
    public DummyAuthorativeConcept(final String name, final String version, final String description) {
        this.name = name;
        this.version = version;
        this.description = description;
    }
}
