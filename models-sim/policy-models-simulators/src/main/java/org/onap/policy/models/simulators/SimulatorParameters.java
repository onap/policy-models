/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2020 Bell Canada. All rights reserved.
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

package org.onap.policy.models.simulators;

import java.util.LinkedList;
import java.util.List;
import lombok.Getter;
import org.onap.policy.common.endpoints.parameters.TopicParameters;
import org.onap.policy.common.parameters.BeanValidationResult;
import org.onap.policy.common.parameters.BeanValidator;
import org.onap.policy.common.parameters.annotations.NotNull;
import org.onap.policy.models.sim.dmaap.parameters.DmaapSimParameterGroup;

/**
 * Simulator parameters.
 */
@Getter
@NotNull
public class SimulatorParameters {

    /**
     * Note: this is only used to capture the provider's parameters; the rest server
     * parameters that it contains are ignored. Instead, the parameters for the rest
     * server are contained within the {@link #restServers} entry having the same name as
     * the provider parameters.
     */
    private DmaapSimParameterGroup dmaapProvider;

    private CdsServerParameters grpcServer;

    /**
     * Parameters for the REST server simulators that are to be started.
     */
    private List<ClassRestServerParameters> restServers = new LinkedList<>();

    /**
     * Topic sinks that are used by {@link #topicServers}.
     */
    private List<TopicParameters> topicSinks = new LinkedList<>();

    /**
     * Topic sources that are used by {@link #topicServers}.
     */
    private List<TopicParameters> topicSources = new LinkedList<>();

    /**
     * Parameters for the TOPIC server simulators that are to be started.
     */
    private List<TopicServerParameters> topicServers = new LinkedList<>();


    /**
     * Validates the parameters.
     *
     * @param containerName name of the parameter container
     * @return the validation result
     */
    public BeanValidationResult validate(String containerName) {
        BeanValidationResult result = new BeanValidator().validateTop(containerName, this);

        result.validateList("restServers", restServers, params -> params.validate("restServers"));
        result.validateList("topicServers", topicServers, params -> params.validate("topicServers"));

        return result;
    }
}
