/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix Foundation.
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

package org.onap.policy.models.tosca.authorative.concepts;

import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import org.onap.policy.models.base.PfObjectFilter;

/**
 * Filter class for searches for {@link ToscaServiceTemplate} instances. If any fields are null, they are ignored.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
@Builder
@Data
public class ToscaServiceTemplateFilter implements PfObjectFilter<ToscaServiceTemplate> {
    public static final String LATEST_VERSION = "LATEST";

    // Regular expression
    private String name;

    // Regular Expression, set to LATEST_VERRSION to get the latest version
    private String version;

    @Override
    public List<ToscaServiceTemplate> filter(@NonNull final List<ToscaServiceTemplate> originalList) {

        // @formatter:off
        List<ToscaServiceTemplate> returnList = originalList.stream()
                .filter(p -> filterString(p.getName(), name))
                .filter(p -> LATEST_VERSION.equals(version)
                        || filterString(p.getVersion(), version))
                .collect(Collectors.toList());
        // @formatter:off

        if (LATEST_VERSION.equals(version)) {
            return this.latestVersionFilter(returnList, new ToscaServiceTemplateComparator());
        } else  {
            return returnList;
        }
    }
}
