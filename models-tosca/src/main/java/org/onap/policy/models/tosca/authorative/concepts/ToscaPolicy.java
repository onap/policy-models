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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

/**
 * Class to represent TOSCA policy matching input/output from/to client.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@ToString(callSuper = true)
public class ToscaPolicy extends ToscaEntity implements Comparable<ToscaPolicy> {

    public static final String TOSCA_POLICY_SERVICE = "service";
    public static final String TOSCA_POLICY_ONAP_NAME = "onapName";
    public static final String TOSCA_POLICY_NAME = "policyName";
    public static final String TOSCA_POLICY_DESCRIPTION = "description";
    public static final String TOSCA_POLICY_TEMPLATE_VERSION = "templateVersion";
    public static final String TOSCA_POLICY_VERSION = "version";
    public static final String TOSCA_POLICY_PRIORITY = "priority";
    public static final String TOSCA_POLICY_RISK_LEVEL = "riskLevel";
    public static final String TOSCA_POLICY_RISK_TYPE = "riskType";
    public static final String TOSCA_POLICY_GUARD = "guard";
    public static final String TOSCA_POLICY_CONTENT = "content";
    private String type;

    @ApiModelProperty(name = "type_version")
    @SerializedName("type_version")
    private String typeVersion;

    private Map<String, Object> properties;

    /**
     * Copy constructor.
     *
     * @param copyObject the obejct to copy from.
     */
    public ToscaPolicy(@NonNull ToscaPolicy copyObject) {
        super(copyObject);

        this.type = copyObject.type;
        this.typeVersion = copyObject.typeVersion;

        if (copyObject.properties != null) {
            properties = new LinkedHashMap<>();
            for (final Entry<String, Object> propertyEntry : copyObject.properties.entrySet()) {
                properties.put(propertyEntry.getKey(), propertyEntry.getValue());
            }
        }
    }

    /**
     * Getter method of properties.
     *
     * @return this policy's properties.
     */
    public Map<String, Object> getProperties() {
        if (properties == null) {
            properties = new LinkedHashMap<>();
        }
        return properties;
    }

    /**
     * Gets the identifier for this policy.
     *
     * @return this policy's identifier
     */
    public ToscaPolicyIdentifier getIdentifier() {
        return new ToscaPolicyIdentifier(getName(), getVersion());
    }

    /**
     * Gets the type identifier for this policy.
     *
     * @return this policy's type identifier
     */
    public ToscaPolicyTypeIdentifier getTypeIdentifier() {
        return new ToscaPolicyTypeIdentifier(getType(), getTypeVersion());
    }

    @Override
    public int compareTo(final ToscaPolicy other) {
        return compareNameVersion(this, other);
    }
}
