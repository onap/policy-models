/*-
 * ============LICENSE_START=======================================================
 * sdc
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019 Nordix Foundation.
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

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.UUID;
import lombok.Data;

@Data
public class ResourceInstance implements Serializable {

    private static final long serialVersionUID = -5506162340393802424L;

    @SerializedName("ResourceUUID")
    private UUID resourceUuid;

    private String resourceInstanceName;
    private String resourceName;
    private String resourceVersion;
    private String resourceType;

    @SerializedName("ResourceInvariantUUID")
    private UUID resourceInvariantUuid;

    public ResourceInstance() {
        // Empty Constructor
    }

    /**
     * Constructor.
     *
     * @param instance copy object
     */
    public ResourceInstance(ResourceInstance instance) {
        if (instance == null) {
            return;
        }
        this.resourceInstanceName = instance.resourceInstanceName;
        this.resourceName = instance.resourceName;
        this.resourceInvariantUuid = instance.resourceInvariantUuid;
        this.resourceVersion = instance.resourceVersion;
        this.resourceType = instance.resourceType;
        this.resourceUuid = instance.resourceUuid;
    }
}
