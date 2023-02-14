/*
 * ============LICENSE_START=======================================================
 * Copyright (C) 2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2021 Nordix Foundation.
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
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

/**
 * Class to represent TOSCA classes containing property maps whose values are generic
 * Objects.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@ToString
public class ToscaWithTypeAndObjectProperties extends ToscaEntity {
    private String type;

    @SerializedName("type_version")
    private String typeVersion;

    private Map<String, Object> properties;

    /**
     * Copy constructor.
     *
     * @param copyObject object to copy
     */
    public ToscaWithTypeAndObjectProperties(@NonNull ToscaWithTypeAndObjectProperties copyObject) {
        super(copyObject);

        this.type = copyObject.type;
        this.typeVersion = copyObject.typeVersion;

        if (copyObject.properties != null) {
            properties = new LinkedHashMap<>(copyObject.properties);
        }
    }

    /**
     * Gets the identifier for this policy.
     *
     * @return this policy's identifier
     */
    public ToscaConceptIdentifier getIdentifier() {
        return new ToscaConceptIdentifier(getName(), getVersion());
    }

    /**
     * Gets the type identifier for this policy.
     *
     * @return this policy's type identifier
     */
    public ToscaConceptIdentifier getTypeIdentifier() {
        return new ToscaConceptIdentifier(getType(), getTypeVersion());
    }
}
