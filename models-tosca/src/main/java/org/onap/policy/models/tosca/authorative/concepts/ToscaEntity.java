/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy Model
 * ================================================================================
 * Copyright (C) 2019 Nordix Foundation.
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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.core.Response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import org.onap.policy.models.base.PfModelRuntimeException;
import org.onap.policy.models.base.PfNameVersion;

/**
 * Class to represent TOSCA data type matching input/output from/to client.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 */
@Data
@NoArgsConstructor
public class ToscaEntity implements PfNameVersion {
    private String name;

    private String version;

    @ApiModelProperty(name = "derived_from")
    @SerializedName("derived_from")
    private String derivedFrom;

    private Map<String, String> metadata;

    private String description;

    /**
     * Copy Constructor.
     *
     * @param copyObject object to copy from
     */
    public ToscaEntity(@NonNull ToscaEntity copyObject) {
        this.name = copyObject.name;
        this.version = copyObject.version;
        this.derivedFrom = copyObject.derivedFrom;
        this.description = copyObject.description;

        if (copyObject.metadata != null) {
            metadata = new LinkedHashMap<>();
            for (final Entry<String, String> metadataEntry : copyObject.metadata.entrySet()) {
                metadata.put(metadataEntry.getKey(), metadataEntry.getValue());
            }
        }
    }

    /**
     * Get a key for this entity.
     *
     * @return a ToscaEntityKey for this entry
     */
    public ToscaEntityKey getKey() {
        return new ToscaEntityKey(name, version);
    }

    /**
     * Convert a list of maps of TOSCA entities into a regular map.
     *
     * @param listOfMapsOfEntities the incoming list of maps of entities
     * @return The entities on a regular map
     * @throws PfModelException on duplicate entity entries
     */
    public static <T extends ToscaEntity> Map<ToscaEntityKey, T> getEntityListMapAsMap(
            List<Map<String, T>> listOfMapsOfEntities) {
        // Declare the return map
        Map<ToscaEntityKey, T> entityMap = new LinkedHashMap<>();

        for (Map<String, T> mapOfEntities : listOfMapsOfEntities) {
            for (T entityEntry : mapOfEntities.values()) {
                if (entityMap.containsKey(entityEntry.getKey())) {
                    throw new PfModelRuntimeException(Response.Status.INTERNAL_SERVER_ERROR,
                            "list of map of entities contains more than one entity with key " + entityEntry.getKey());
                }

                entityMap.put(entityEntry.getKey(), entityEntry);
            }
        }

        return entityMap;
    }
}
