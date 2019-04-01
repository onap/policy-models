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

package org.onap.policy.models.pdp.parameters;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.onap.policy.common.parameters.ParameterGroup;
import org.onap.policy.common.utils.coder.Coder;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;

/**
 * Class to hold/create all parameters for test cases.
 *
 * @author Ajith Sreekumar (ajith.sreekumar@est.tech)
 */
public class CommonTestData {

    public static final String APEX_STARTER_GROUP_NAME = "ApexStarterParameterGroup";
    public static final int TIME_INTERVAL = 2;
    public static final String PDP_NAME = "apex-pdp";
    public static final String VERSION = "0.0.1";
    public static final String PDP_TYPE = "apex";
    public static final String DESCRIPTION = "Pdp status for HealthCheck";
    public static final String POLICY_NAME = "onap.controllloop.operational.apex.BBS";
    public static final String POLICY_VERSION = "0.0.1";
    public static final List<PolicyTypeIdentParameters> SUPPORTED_POLICY_TYPES =
            Arrays.asList(getSupportedPolicyTypes(POLICY_NAME, POLICY_VERSION));

    public static final Coder coder = new StandardCoder();

    /**
     * Returns supported policy types for test cases.
     *
     * @return supported policy types
     */
    private static PolicyTypeIdentParameters getSupportedPolicyTypes(final String name, final String version) {
        final PolicyTypeIdentParameters policyTypeIdentParameters = new PolicyTypeIdentParameters();
        policyTypeIdentParameters.setName(name);
        policyTypeIdentParameters.setVersion(version);
        return policyTypeIdentParameters;
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
     * Returns a property map for a PdpStatusParameters map for test cases.
     *
     * @param isEmpty boolean value to represent that object created should be empty or not
     * @return a property map suitable for constructing an object
     */
    public Map<String, Object> getPdpStatusParametersMap(final boolean isEmpty) {
        final Map<String, Object> map = new TreeMap<>();
        if (!isEmpty) {
            map.put("timeInterval", TIME_INTERVAL);
            map.put("pdpName", PDP_NAME);
            map.put("version", VERSION);
            map.put("pdpType", PDP_TYPE);
            map.put("description", DESCRIPTION);
            map.put("supportedPolicyTypes", SUPPORTED_POLICY_TYPES);
        }

        return map;
    }
}
