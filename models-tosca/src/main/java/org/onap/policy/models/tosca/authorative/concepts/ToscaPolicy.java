/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy Model
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * Class to represent TOSCA policy matching input/output from/to client.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 */
@Data
@NoArgsConstructor
public class ToscaPolicy {

    private String type;

    private String version;

    private String description;

    private Map<String, String> metadata;

    private Map<String, Object> properties;

    /**
     * Copy constructor.
     *
     * @param copyObject the obejct to copy from.
     */
    public ToscaPolicy(@NonNull ToscaPolicy copyObject) {
        this.type = copyObject.type;
        this.version = copyObject.version;
        this.description = copyObject.description;
        this.metadata = (metadata != null ? new LinkedHashMap<>(copyObject.metadata) : null);
        this.properties = (properties != null ? new LinkedHashMap<>(copyObject.properties) : null);
    }
}
