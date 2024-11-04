/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020-2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2024 Nordix Foundation
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

package org.onap.policy.controlloop.actor.test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileNotFoundException;
import java.util.Map;
import lombok.Getter;
import org.onap.policy.common.endpoints.http.client.HttpClientConfigException;
import org.onap.policy.common.endpoints.http.client.HttpClientFactoryInstance;
import org.onap.policy.common.message.bus.event.TopicEndpointManager;
import org.onap.policy.common.parameters.BeanValidationResult;
import org.onap.policy.common.parameters.BeanValidator;
import org.onap.policy.common.parameters.annotations.NotNull;
import org.onap.policy.common.parameters.topic.BusTopicParams;
import org.onap.policy.common.parameters.topic.TopicParameterGroup;
import org.onap.policy.common.utils.coder.Coder;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardYamlCoder;
import org.onap.policy.common.utils.resources.ResourceUtils;
import org.onap.policy.controlloop.actorserviceprovider.ActorService;
import org.onap.policy.controlloop.actorserviceprovider.Operator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Superclass for various Actor tests.
 */
public class BasicActor {
    private static final Logger logger = LoggerFactory.getLogger(BasicActor.class);
    private static final Coder yamlCoder = new StandardYamlCoder();

    /**
     * Reads a YAML configuration file, configures the specified topics and HTTP clients,
     * and then runs the specified actor through its paces: configure(), start(), stop(),
     * and shutdown(). Finally, it destroys the topics and HTTP clients.
     *
     * @param actorName name of the actor to be tested.
     * @param yamlConfigFile YAML configuration file name
     * @throws IllegalArgumentException if an error occurs
     */
    protected void verifyActorService(String actorName, String yamlConfigFile) {
        ActorService service = new ActorService() {};

        // ensure the actor was loaded
        assertNotNull(service.getActor(actorName));

        try {
            MyConfig config = readConfig(yamlConfigFile);
            config.validate();

            startOtherServices(config);

            // configure and verify
            service.configure(config.getActors());
            for (Operator operator : service.getActor(actorName).getOperators()) {
                assertTrue(operator.isConfigured());
            }

            // start and verify
            service.start();
            for (Operator operator : service.getActor(actorName).getOperators()) {
                assertTrue(operator.isAlive());
            }

            // stop and verify
            service.stop();
            for (Operator operator : service.getActor(actorName).getOperators()) {
                assertFalse(operator.isAlive());
            }

            // shut down and verify
            service.shutdown();
            for (Operator operator : service.getActor(actorName).getOperators()) {
                assertFalse(operator.isAlive());
            }

        } catch (HttpClientConfigException e) {
            logger.error("failed to configure HTTP client(s) for actor: {}", actorName);
            throw new IllegalArgumentException(e);

        } finally {
            stopOtherServices();
        }
    }

    /**
     * Reads a YAML configuration from a file.
     *
     * @param yamlConfigFile YAML configuration file name
     * @return the configuration that was read from the file
     * @throws AssertionError if an error occurs
     */
    private MyConfig readConfig(String yamlConfigFile) {
        try {
            var yaml = ResourceUtils.getResourceAsString(yamlConfigFile);
            if (yaml == null) {
                throw new FileNotFoundException(yamlConfigFile);
            }

            return yamlCoder.decode(yaml, MyConfig.class);

        } catch (CoderException | FileNotFoundException e) {
            logger.error("cannot decode YAML file {}", yamlConfigFile);
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Starts the Topic and HTTP clients.
     *
     * @param config configuration
     * @throws HttpClientConfigException if an error occurs
     */
    private void startOtherServices(MyConfig config) throws HttpClientConfigException {
        stopOtherServices();

        if (config.getHttpClients() != null) {
            var factory = HttpClientFactoryInstance.getClientFactory();
            for (BusTopicParams params : config.getHttpClients()) {
                factory.build(params);
            }
        }

        if (config.getTopics() != null) {
            TopicEndpointManager.getManager().addTopics(config.getTopics());
        }
    }

    /**
     * Stops the Topic and HTTP clients.
     */
    private void stopOtherServices() {
        TopicEndpointManager.getManager().shutdown();
        HttpClientFactoryInstance.getClientFactory().destroy();
    }

    @Getter
    public static class MyConfig {
        private BusTopicParams[] httpClients;
        private TopicParameterGroup topics;

        @NotNull
        private Map<String, Object> actors;

        /**
         * Validates the config.
         */
        public void validate() {
            BeanValidationResult result = new BeanValidator().validateTop(BasicActor.class.getSimpleName(), this);
            if (topics != null) {
                result.addResult(topics.validate());
            }
            if (!result.isValid()) {
                throw new IllegalArgumentException(result.getResult());
            }
        }
    }
}
