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

package org.onap.policy.models.tosca.legacy.concepts;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response;

import lombok.Data;

import org.onap.policy.models.base.PfModelRuntimeException;
import org.onap.policy.models.tosca.legacy.mapping.LegacyGuardPolicyMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Content object of a Legacy Guard Policy.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
@Data
public class LegacyGuardPolicyContent {
    private static final Logger LOGGER = LoggerFactory.getLogger(LegacyGuardPolicyMapper.class);

    private String actor;
    private String recipe;
    private String targets;
    private String clname;
    private String limit;
    private String timeWindow;
    private String timeUnits;
    private String min;
    private String max;
    private String guardActiveStart;
    private String guardActiveEnd;

    /**
     * Get contents as a map.
     *
     * @return the contents as a map.
     */
    public Map<String, String> getAsPropertyMap() {
        final Map<String, String> propertyMap = new HashMap<>();

        try {
            for (Field field : this.getClass().getDeclaredFields()) {
                if (field.get(this) != null && field.getType().equals(String.class)) {
                    propertyMap.put(field.getName(), (String)field.get(this));
                }
            }
        } catch (Exception exc) {
            String errorMessage = "could not convert content to a property map";
            LOGGER.warn(errorMessage, exc);
            throw new PfModelRuntimeException(Response.Status.BAD_REQUEST, errorMessage, exc);

        }

        return propertyMap;
    }
}
