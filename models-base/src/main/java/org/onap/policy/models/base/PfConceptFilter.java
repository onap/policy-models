/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2020, 2024 Nordix Foundation.
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

import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

/**
 * Filter class for searches for {@link ToscaPolicy} instances. If any fields are null, they are ignored.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
@Builder
@Data
public class PfConceptFilter implements PfObjectFilter<PfConcept> {
    public static final String LATEST_VERSION = "LATEST";

    // Exact expression
    private String name;

    // Exact match, set to LATEST_VERSION to get the latest version
    private String version;

    // version prefix
    private String versionPrefix;

    @Override
    public List<PfConcept> filter(@NonNull final List<PfConcept> originalList) {

        // @formatter:off
        List<PfConcept> returnList = originalList.stream()
                .filter(filterStringPred(name, PfConcept::getName))
                .filter(filterStringPred((LATEST_VERSION.equals(version) ? null : version), PfConcept::getVersion))
                .filter(filterPrefixPred(versionPrefix, PfConcept::getVersion))
                .collect(Collectors.toList()); //NOSONAR
        // @formatter:off

        if (LATEST_VERSION.equals(version)) {
            return this.latestVersionFilter(returnList, new PfConceptComparator());
        } else  {
            return returnList;
        }
    }
}
