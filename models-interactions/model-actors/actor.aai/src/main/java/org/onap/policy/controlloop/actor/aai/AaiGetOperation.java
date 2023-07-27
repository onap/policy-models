/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2023 Nordix Foundation.
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

package org.onap.policy.controlloop.actor.aai;

import jakarta.ws.rs.client.Invocation.Builder;
import jakarta.ws.rs.client.WebTarget;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.onap.policy.common.utils.coder.StandardCoderObject;
import org.onap.policy.controlloop.actorserviceprovider.OperationProperties;
import org.onap.policy.controlloop.actorserviceprovider.impl.HttpOperation;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpConfig;

/**
 * Superclass of A&AI operators that use "get" to perform their request and store their
 * response within the context as a {@link StandardCoderObject}. The property name under
 * which they are stored is ${actor}.${operation}.${targetEntity}.
 */
public class AaiGetOperation extends HttpOperation<StandardCoderObject> {
    public static final int DEFAULT_RETRY = 3;

    private static final List<String> PROPERTY_NAMES = List.of(OperationProperties.AAI_TARGET_ENTITY);

    /**
     * Constructs the object.
     *
     * @param params operation parameters
     * @param config configuration for this operation
     */
    public AaiGetOperation(ControlLoopOperationParams params, HttpConfig config) {
        super(params, config, StandardCoderObject.class, PROPERTY_NAMES);
    }

    @Override
    public void generateSubRequestId(int attempt) {
        setSubRequestId(String.valueOf(attempt));
    }

    /**
     * Adds a query parameter to a web target.
     *
     * @param web target to which the parameter should be added
     * @param str the separator and parameter are appended here, for logging purposes
     * @param separator separator to be added to "str"; that's its only use
     * @param name parameter name
     * @param value parameter value
     * @return "web"
     */
    protected WebTarget addQuery(WebTarget web, StringBuilder str, String separator, String name, String value) {
        str.append(separator);
        str.append(name);
        str.append('=');
        str.append(value);

        return web.queryParam(name, value);
    }

    /**
     * Adds headers to the web builder.
     *
     * @param webldr builder to which the headers should be added
     * @param headers headers to be added
     */
    protected void addHeaders(Builder webldr, Map<String, Object> headers) {
        for (Entry<String, Object> header : headers.entrySet()) {
            webldr.header(header.getKey(), header.getValue());
        }
    }

    @Override
    protected Map<String, Object> makeHeaders() {
        return AaiUtil.makeHeaders(params);
    }

    /**
     * Provides a default retry value, if none specified.
     */
    @Override
    protected int getRetry(Integer retry) {
        return (retry == null ? DEFAULT_RETRY : retry);
    }
}
