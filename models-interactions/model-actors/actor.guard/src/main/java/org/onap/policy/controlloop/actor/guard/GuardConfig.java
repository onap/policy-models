/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.controlloop.actor.guard;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import lombok.Getter;
import org.onap.policy.common.endpoints.http.client.HttpClient;
import org.onap.policy.common.endpoints.http.client.HttpClientFactory;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpConfig;

/**
 * Configuration for Guard Operators.
 */
public class GuardConfig extends HttpConfig {
    private final Map<String, Object> defaultRequest = new LinkedHashMap<>();

    /**
     * {@code True} if the associated guard operation is disabled.
     */
    @Getter
    private boolean disabled;

    /**
     * Constructs the object.
     *
     * @param blockingExecutor executor to be used for tasks that may perform blocking I/O
     * @param params operator parameters
     * @param clientFactory factory from which to obtain the {@link HttpClient}
     */
    public GuardConfig(Executor blockingExecutor, GuardParams params, HttpClientFactory clientFactory) {
        super(blockingExecutor, params, clientFactory);

        addProperty("ONAPComponent", params.getOnapComponent());
        addProperty("ONAPInstance", params.getOnapInstance());
        addProperty("ONAPName", params.getOnapName());
        addProperty("action", params.getAction());

        this.disabled = params.isDisabled();
    }

    /**
     * Adds a property to the default request, if the value is not {@code null}.
     *
     * @param key property key
     * @param value property value, or {@code null}
     */
    private void addProperty(String key, String value) {
        if (value != null) {
            defaultRequest.put(key, value);
        }
    }

    /**
     * Creates a new request, with the default values.
     *
     * @return a new request map
     */
    public Map<String, Object> makeRequest() {
        return new LinkedHashMap<>(defaultRequest);
    }
}
