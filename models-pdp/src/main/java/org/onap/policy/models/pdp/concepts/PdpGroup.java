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

package org.onap.policy.models.pdp.concepts;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.NoArgsConstructor;

import org.onap.policy.models.base.PfNameVersion;
import org.onap.policy.models.base.PfUtils;
import org.onap.policy.models.pdp.enums.PdpState;

/**
 * Class to represent a PDPGroup, which groups multiple PDPSubGroup entities together for
 * a particular domain.
 *
 * @author Ram Krishna Verma (ram.krishna.verma@est.tech)
 */
@Data
@NoArgsConstructor
public class PdpGroup implements PfNameVersion, Comparable<PdpGroup> {
    private String name;
    private String version;
    private String description;
    private PdpState pdpGroupState;
    private Map<String, String> properties;
    private List<PdpSubGroup> pdpSubgroups;

    /*
     * Note: removed "@NotNull" annotation from the constructor argument, because it
     * cannot be covered by a junit test, as the superclass does the check and throws an
     * exception first.
     */

    /**
     * Constructs the object, making a deep copy from the source.
     *
     * @param source source from which to copy fields
     */
    public PdpGroup(PdpGroup source) {
        this.name = source.name;
        this.version = source.version;
        this.description = source.description;
        this.pdpGroupState = source.pdpGroupState;
        this.properties = (source.properties == null ? null : new LinkedHashMap<>(source.properties));
        this.pdpSubgroups = PfUtils.mapList(source.pdpSubgroups, PdpSubGroup::new);
    }

    @Override
    public int compareTo(final PdpGroup other) {
        return compareNameVersion(this, other);
    }
}
