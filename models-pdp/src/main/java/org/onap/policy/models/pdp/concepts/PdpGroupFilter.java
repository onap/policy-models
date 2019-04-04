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

package org.onap.policy.models.pdp.concepts;

import java.util.List;
import java.util.stream.Collectors;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import org.onap.policy.models.base.PfObjectFiler;
import org.onap.policy.models.pdp.enums.PdpState;

/**
 * Filter class for searches for {@link PdpGroup} instances.
 * If any fields are null, they are ignored.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
@Builder
@Data
public class PdpGroupFilter implements PfObjectFiler<PdpGroup> {
    public static final String LATEST_VERSION = "LATEST";

    // Regular expression
    private String name;

    // Regular Expression, set to LATEST_VERRSION to get the latest version
    private String version;

    private PdpState groupState;

    // Regular expression
    private String pdpType;

    // Set regular expressions on fields to match policy type names and versions
    private ToscaPolicyTypeIdentifier policyType;

    // Set regular expressions on fields to match policy names and versions
    private ToscaPolicyIdentifier policy;

    @Override
    public List<PdpGroup> filter(@NonNull final List<PdpGroup> originalList) {

        // @formatter:off
        return originalList.stream()
                .filter(p -> name       != null && p.getName()   .matches(name))
                .filter(p -> version    != null && p.getVersion().matches(version))
                .filter(p -> groupState != null && p.getPdpGroupState().equals(groupState))
                .collect(Collectors.toList());
        // @formatter:off
    }
}
