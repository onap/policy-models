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

package org.onap.policy.models.tosca;

import com.google.gson.annotations.SerializedName;
import java.util.List;

import javax.persistence.Column;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.onap.policy.models.base.PfConceptKey;

/**
 * Class to represent the property in TOSCA definition.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 *
 */
@ToString
public class ToscaProperty {

    @Getter
    @Setter
    @SerializedName("type")
    @Column(name = "derivedFrom")
    private PfConceptKey type;

    @Getter
    @Setter
    @SerializedName("description")
    private String description;

    @Getter
    @Setter
    @SerializedName("required")
    private boolean required;

    @Getter
    @Setter
    @SerializedName("default_value")
    private Object defaultValue;

    @Getter
    @Setter
    @SerializedName("status")
    private String status;

    @Getter
    @Setter
    @SerializedName("constraints")
    private List<ToscaConstraint> constraints;

    @Getter
    @Setter
    @SerializedName("entry_schema")
    private ToscaEntrySchema entrySchema;
}