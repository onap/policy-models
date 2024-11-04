/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2020, 2024 Nordix Foundation.
 *  Modifications Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.models.sim.pdp.parameters;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.onap.policy.common.parameters.ParameterGroup;
import org.onap.policy.common.parameters.topic.TopicParameters;
import org.onap.policy.common.utils.coder.Coder;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;

/**
 * Class to hold/create all parameters for test cases.
 *
 * @author Ajith Sreekumar (ajith.sreekumar@est.tech)
 */
public class CommonTestData {

    public static final String PDP_SIMULATOR_GROUP_NAME = "PdpSimulatorParameterGroup";
    public static final long TIME_INTERVAL = 2000;
    public static final String PDP_NAME = "apex-pdp";
    public static final String VERSION = "0.0.1";
    public static final String PDP_TYPE = "apex";
    public static final String PDP_GROUP = "defaultGroup";
    public static final String DESCRIPTION = "Pdp status for HealthCheck";
    public static final String POLICY_NAME = "onap.controllloop.operational.apex.BBS";
    public static final String POLICY_VERSION = "0.0.1";
    protected static final List<ToscaPolicyTypeIdentifierParameters> SUPPORTED_POLICY_TYPES =
        List.of(getSupportedPolicyTypes(POLICY_NAME, POLICY_VERSION));
    public static final List<TopicParameters> TOPIC_PARAMS = List.of(getTopicParams());
    private static final String REST_SERVER_PASS = "zb!XztG34";
    private static final String REST_SERVER_USER = "healthcheck";
    private static final int REST_SERVER_PORT = 6969;
    private static final String REST_SERVER_HOST = "0.0.0.0";
    private static final boolean REST_SERVER_HTTPS = true;

    public static final Coder coder = new StandardCoder();

    /**
     * Returns supported policy types for test cases.
     *
     * @return supported policy types
     */
    public static ToscaPolicyTypeIdentifierParameters getSupportedPolicyTypes(final String name, final String version) {
        final ToscaPolicyTypeIdentifierParameters policyTypeIdentParameters = new ToscaPolicyTypeIdentifierParameters();
        policyTypeIdentParameters.setName(name);
        policyTypeIdentParameters.setVersion(version);
        return policyTypeIdentParameters;
    }

    /**
     * Returns topic parameters for test cases.
     *
     * @return topic parameters
     */
    public static TopicParameters getTopicParams() {
        final TopicParameters topicParams = new TopicParameters();
        topicParams.setTopic("POLICY-PDP-PAP");
        topicParams.setTopicCommInfrastructure("kafka");
        topicParams.setServers(List.of("localhost:9092"));
        return topicParams;
    }

    /**
     * Converts the contents of a map to a parameter class.
     *
     * @param source property map
     * @param clazz class of object to be created from the map
     * @return a new object represented by the map
     */
    public <T extends ParameterGroup> T toObject(final Map<String, Object> source, final Class<T> clazz) {
        try {
            return coder.decode(coder.encode(source), clazz);

        } catch (final CoderException e) {
            throw new RuntimeException("cannot create " + clazz.getName() + " from map", e);
        }
    }

    /**
     * Returns a property map for a PdpSimulatorParameterGroup map for test cases.
     *
     * @param name name of the parameters
     *
     * @return a property map suitable for constructing an object
     */
    public Map<String, Object> getPdpSimulatorParameterGroupMap(final String name) {
        final Map<String, Object> map = new TreeMap<>();

        map.put("name", name);
        map.put("restServerParameters", getRestServerParametersMap(false));
        map.put("pdpStatusParameters", getPdpStatusParametersMap(false));
        map.put("topicParameterGroup", getTopicParametersMap(false));
        return map;
    }

    /**
     * Returns a property map for a RestServerParameters map for test cases.
     *
     * @param isEmpty boolean value to represent that object created should be empty or not
     * @return a property map suitable for constructing an object
     */
    public Map<String, Object> getRestServerParametersMap(final boolean isEmpty) {
        final Map<String, Object> map = new TreeMap<>();
        map.put("https", REST_SERVER_HTTPS);

        if (!isEmpty) {
            map.put("host", REST_SERVER_HOST);
            map.put("port", REST_SERVER_PORT);
            map.put("userName", REST_SERVER_USER);
            map.put("password", REST_SERVER_PASS);
        }

        return map;
    }

    /**
     * Returns a property map for a PdpStatusParameters map for test cases.
     *
     * @param isEmpty boolean value to represent that object created should be empty or not
     * @return a property map suitable for constructing an object
     */
    public Map<String, Object> getPdpStatusParametersMap(final boolean isEmpty) {
        final Map<String, Object> map = new TreeMap<>();
        if (!isEmpty) {
            map.put("timeIntervalMs", TIME_INTERVAL);
            map.put("pdpName", PDP_NAME);
            map.put("version", VERSION);
            map.put("pdpType", PDP_TYPE);
            map.put("pdpGroup", PDP_GROUP);
            map.put("description", DESCRIPTION);
            map.put("supportedPolicyTypes", SUPPORTED_POLICY_TYPES);
        }

        return map;
    }

    /**
     * Returns a property map for a TopicParameters map for test cases.
     *
     * @param isEmpty boolean value to represent that object created should be empty or not
     * @return a property map suitable for constructing an object
     */
    public Map<String, Object> getTopicParametersMap(final boolean isEmpty) {
        final Map<String, Object> map = new TreeMap<>();
        if (!isEmpty) {
            map.put("topicSources", TOPIC_PARAMS);
            map.put("topicSinks", TOPIC_PARAMS);
        }
        return map;
    }
}
