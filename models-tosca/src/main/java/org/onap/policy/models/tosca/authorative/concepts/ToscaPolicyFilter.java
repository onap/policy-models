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

package org.onap.policy.models.tosca.authorative.concepts;

import java.util.List;
import java.util.stream.Collectors;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import org.onap.policy.models.base.PfObjectFiler;

/**
 * Filter class for searches for {@link ToscaPolicy} instances.
 * If any fields are null, they are ignored.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
@Builder
@Data
public class ToscaPolicyFilter implements PfObjectFiler<ToscaPolicy> {
    public static final String LATEST_VERSION = "LATEST";

    // Regular expression
    private String name;

    // Regular Expression, set to LATEST_VERRSION to get the latest version
    private String version;

    // Regular expression
    private String policyTypeName;

    // Regular Expression, set to LATEST_VERRSION to get the latest version
    private String policyTypeVersion;

    @Override
    public List<ToscaPolicy> filter(@NonNull final List<ToscaPolicy> originalList) {

        // @formatter:off
        return originalList.stream()
                .filter(p -> name              != null && p.getName()       .matches(name))
                .filter(p -> version           != null && p.getVersion()    .matches(version))
                .filter(p -> policyTypeName    != null && p.getType()       .matches(policyTypeName))
                .filter(p -> policyTypeVersion != null && p.getTypeVersion().matches(policyTypeVersion))
                .collect(Collectors.toList());
        // @formatter:off
    }
}
