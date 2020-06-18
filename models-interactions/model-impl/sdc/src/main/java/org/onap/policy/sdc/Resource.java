/*-
 * ============LICENSE_START=======================================================
 * sdc
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019-2020 Nordix Foundation.
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
 * ============LICENSE_END=========================================================
 */

package org.onap.policy.sdc;

import java.io.Serializable;
import java.util.UUID;
import lombok.Data;

@Data
public class Resource implements Serializable {

    private static final long serialVersionUID = -913729158733348027L;

    private UUID resourceUuid;
    private UUID resourceInvariantUuid;
    private String resourceName;
    private String resourceVersion;
    private String resourceType;

    public Resource() {
        // Empty Constructor
    }

    /**
     * Constructor.
     *
     * @param resource copy object
     */
    public Resource(Resource resource) {
        this.resourceUuid = resource.resourceUuid;
        this.resourceInvariantUuid = resource.resourceInvariantUuid;
        this.resourceName = resource.resourceName;
        this.resourceVersion = resource.resourceVersion;
        this.resourceType = resource.resourceType;
    }

    public Resource(UUID uuid) {
        this.resourceUuid = uuid;
    }

    public Resource(String name, String type) {
        this.resourceName = name;
        this.resourceType = type;
    }

    /**
     * Constructor.
     *
     * @param uuid uuid
     * @param invariantUuid invariant uuid
     * @param name name
     * @param version version
     * @param type type
     */
    public Resource(UUID uuid, UUID invariantUuid, String name, String version, String type) {
        this.resourceUuid = uuid;
        this.resourceInvariantUuid = invariantUuid;
        this.resourceName = name;
        this.resourceVersion = version;
        this.resourceType = type;
    }
}
