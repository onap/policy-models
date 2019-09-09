/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy Model
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.policy.models.tosca.authorative.concepts;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import java.util.Map;
import lombok.Data;

/**
 * Class to represent TOSCA property matching input/output from/to client.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 */
@Data
public class ToscaProperty {
    public enum Status {
        SUPPORTED, UNSUPPORTED, EXPERIMENTAL, DEPRECATED
    }

    private String name;

    private String type;

    private String typeVersion;

    private String description;

    @ApiModelProperty(name = "default")
    @SerializedName("default")
    private String defaultValue;

    private boolean required = false;

    private Status status;

    private List<ToscaConstraint> constraints;

    @ApiModelProperty(name = "entry_schema")
    @SerializedName("entry_schema")
    private ToscaEntrySchema entrySchema;

    private Map<String, String> metadata;
}
