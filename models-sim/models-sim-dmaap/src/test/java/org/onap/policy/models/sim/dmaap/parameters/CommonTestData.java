/*-
 * ============LICENSE_START=======================================================
 *  Modifications Copyright (C) 2019 AT&T Intellectual Property.
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

package org.onap.policy.models.sim.dmaap.parameters;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.onap.policy.common.utils.coder.Coder;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.models.sim.dmaap.DmaapSimRuntimeException;

/**
 * Class to hold/create all parameters for test cases.
 */
public class CommonTestData {
    public static final String SIM_GROUP_NAME = "DMaapSim";

    private static final Coder coder = new StandardCoder();

    /**
     * Gets the standard simulator parameters.
     *
     * @param port port to be inserted into the parameters
     * @return the standard simulator parameters
     */
    public DmaapSimParameterGroup getParameterGroup(int port) {
        try {
            return coder.decode(getParameterGroupAsString(port), DmaapSimParameterGroup.class);

        } catch (CoderException e) {
            throw new DmaapSimRuntimeException("cannot read simulator parameters", e);
        }
    }

    /**
     * Gets the standard simulator parameters, as a String.
     *
     * @param port port to be inserted into the parameters
     * @return the standard simulator parameters
     */
    public String getParameterGroupAsString(int port) {

        try {
            File file = new File("src/test/resources/parameters/NormalParameters.json");
            String json = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);

            json = json.replace("6845", String.valueOf(port));

            return json;

        } catch (IOException e) {
            throw new DmaapSimRuntimeException("cannot read simulator parameters", e);
        }
    }

    /**
     * Nulls out a field within a JSON string. It does it by adding a field with the same
     * name, having a null value, and then prefixing the original field name with "Xxx",
     * thus causing the original field and value to be ignored.
     *
     * @param json JSON string
     * @param field field to be nulled out
     * @return a new JSON string with the field nulled out
     */
    public String nullifyField(String json, String field) {
        return json.replace(field + "\"", field + "\":null, \"" + field + "Xxx\"");
    }
}
