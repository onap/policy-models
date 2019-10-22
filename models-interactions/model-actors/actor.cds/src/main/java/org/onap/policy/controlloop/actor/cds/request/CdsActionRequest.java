/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2019 Bell Canada. All rights reserved.
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

package org.onap.policy.controlloop.actor.cds.request;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.Serializable;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CdsActionRequest implements Serializable {

    private static final long serialVersionUID = -4172157702597791493L;

    private String actionName;
    private String resolutionKey;
    private Map<String, String> aaiProperties;
    private Map<String, String> policyPayload;

    @Override
    public String toString() {
        Gson gson = new GsonBuilder().registerTypeAdapter(CdsActionRequest.class, new CdsRequestGenerator()).create();
        return gson.toJson(this);
    }
}
