/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
 *  Modifications Copyright (C) 2019-2021 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.models.sim.pdp;

import java.security.SecureRandom;
import java.util.List;
import java.util.Random;
import lombok.Getter;
import lombok.Setter;
import org.onap.policy.common.endpoints.event.comm.TopicEndpointManager;
import org.onap.policy.common.endpoints.event.comm.TopicSink;
import org.onap.policy.common.endpoints.event.comm.TopicSource;
import org.onap.policy.common.endpoints.listeners.MessageTypeDispatcher;
import org.onap.policy.common.utils.services.Registry;
import org.onap.policy.common.utils.services.ServiceManager;
import org.onap.policy.common.utils.services.ServiceManagerException;
import org.onap.policy.models.pdp.enums.PdpMessageType;
import org.onap.policy.models.sim.pdp.comm.PdpStateChangeListener;
import org.onap.policy.models.sim.pdp.comm.PdpStatusPublisher;
import org.onap.policy.models.sim.pdp.comm.PdpUpdateListener;
import org.onap.policy.models.sim.pdp.exception.PdpSimulatorException;
import org.onap.policy.models.sim.pdp.exception.PdpSimulatorRunTimeException;
import org.onap.policy.models.sim.pdp.handler.PdpMessageHandler;
import org.onap.policy.models.sim.pdp.parameters.PdpSimulatorParameterGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class activates the PdpSimulator as a complete service together with all its handlers.
 *
 * @author Ajith Sreekumar (ajith.sreekumar@est.tech)
 */
public class PdpSimulatorActivator {

    private static final Logger LOGGER = LoggerFactory.getLogger(PdpSimulatorActivator.class);
    private final PdpSimulatorParameterGroup pdpSimulatorParameterGroup;
    private final List<TopicSink> topicSinks; // topics to which pdp sends pdp status
    private final List<TopicSource> topicSources; // topics to which pdp listens to for messages from pap.
    private static final String[] MSG_TYPE_NAMES = { "messageName" };

    /*
     * This simulator is only used for testing. Consequently, it is safe to use a simple
     * random number generator, thus the sonar is disabled.
     */
    private static final Random RANDOM = new SecureRandom();      // NOSONAR

    /**
     * Listens for messages on the topic, decodes them into a message, and then dispatches them.
     */
    private final MessageTypeDispatcher msgDispatcher;

    /**
     * Used to manage the services.
     */
    private final ServiceManager manager;


    @Getter
    @Setter(lombok.AccessLevel.PRIVATE)
    private volatile boolean alive = false;

    /**
     * Instantiate the activator for onappf PDP-A.
     *
     * @param pdpSimulatorParameterGroup the parameters for the onappf PDP-A service
     */
    public PdpSimulatorActivator(final PdpSimulatorParameterGroup pdpSimulatorParameterGroup) {

        topicSinks = TopicEndpointManager.getManager()
                        .addTopicSinks(pdpSimulatorParameterGroup.getTopicParameterGroup().getTopicSinks());
        topicSources = TopicEndpointManager.getManager()
                        .addTopicSources(pdpSimulatorParameterGroup.getTopicParameterGroup().getTopicSources());

        final var random = RANDOM.nextInt();
        final String instanceId = "pdp_" + random;
        LOGGER.debug("PdpSimulatorActivator initializing with instance id: {}", instanceId);
        try {
            this.pdpSimulatorParameterGroup = pdpSimulatorParameterGroup;
            this.msgDispatcher = new MessageTypeDispatcher(MSG_TYPE_NAMES);
        } catch (final RuntimeException e) {
            throw new PdpSimulatorRunTimeException(e);
        }

        final var pdpUpdateListener = new PdpUpdateListener();
        final var pdpStateChangeListener = new PdpStateChangeListener();
        // @formatter:off
        this.manager = new ServiceManager()
            .addAction("topics",
                TopicEndpointManager.getManager()::start,
                TopicEndpointManager.getManager()::shutdown)
            .addAction("set alive",
                () -> setAlive(true),
                () -> setAlive(false))
            .addAction("register pdp status context object",
                () -> Registry.register(PdpSimulatorConstants.REG_PDP_STATUS_OBJECT,
                        new PdpMessageHandler().createPdpStatusFromParameters(instanceId,
                                pdpSimulatorParameterGroup.getPdpStatusParameters())),
                () -> Registry.unregister(PdpSimulatorConstants.REG_PDP_STATUS_OBJECT))
            .addAction("topic sinks",
                () -> Registry.register(PdpSimulatorConstants.REG_PDP_TOPIC_SINKS, topicSinks),
                () -> Registry.unregister(PdpSimulatorConstants.REG_PDP_TOPIC_SINKS))
            .addAction("Pdp Status publisher",
                () -> Registry.register(PdpSimulatorConstants.REG_PDP_STATUS_PUBLISHER,
                        new PdpStatusPublisher(topicSinks,
                                pdpSimulatorParameterGroup.getPdpStatusParameters().getTimeIntervalMs())),
                this::stopAndRemovePdpStatusPublisher)
            .addAction("Register pdp update listener",
                () -> msgDispatcher.register(PdpMessageType.PDP_UPDATE.name(), pdpUpdateListener),
                () -> msgDispatcher.unregister(PdpMessageType.PDP_UPDATE.name()))
            .addAction("Register pdp state change request dispatcher",
                () -> msgDispatcher.register(PdpMessageType.PDP_STATE_CHANGE.name(), pdpStateChangeListener),
                () -> msgDispatcher.unregister(PdpMessageType.PDP_STATE_CHANGE.name()))
            .addAction("Message Dispatcher",
                this::registerMsgDispatcher,
                this::unregisterMsgDispatcher);

        // @formatter:on
    }

    /**
     * Method to stop and unregister the pdp status publisher.
     */
    private void stopAndRemovePdpStatusPublisher() {
        final var pdpStatusPublisher =
                Registry.get(PdpSimulatorConstants.REG_PDP_STATUS_PUBLISHER, PdpStatusPublisher.class);
        pdpStatusPublisher.terminate();
        Registry.unregister(PdpSimulatorConstants.REG_PDP_STATUS_PUBLISHER);
    }

    /**
     * Initialize PdpSimulator service.
     *
     * @throws PdpSimulatorException on errors in initializing the service
     */
    public void initialize() throws PdpSimulatorException {
        if (isAlive()) {
            throw new IllegalStateException("activator already initialized");
        }

        try {
            LOGGER.debug("PdpSimulator starting as a service . . .");
            manager.start();
            LOGGER.debug("PdpSimulator started as a service");
        } catch (final ServiceManagerException exp) {
            LOGGER.error("PdpSimulator service startup failed");
            throw new PdpSimulatorException(exp.getMessage(), exp);
        }
    }

    /**
     * Terminate PdpSimulator.
     *
     * @throws PdpSimulatorException on errors in terminating the service
     */
    public void terminate() throws PdpSimulatorException {
        if (!isAlive()) {
            throw new IllegalStateException("activator is not running");
        }
        try {
            final var pdpStatusPublisher =
                    Registry.get(PdpSimulatorConstants.REG_PDP_STATUS_PUBLISHER, PdpStatusPublisher.class);
            // send a final heartbeat with terminated status
            pdpStatusPublisher.send(new PdpMessageHandler().getTerminatedPdpStatus());
            manager.stop();
            Registry.unregister(PdpSimulatorConstants.REG_PDP_SIMULATOR_ACTIVATOR);
        } catch (final ServiceManagerException exp) {
            LOGGER.error("PdpSimulator termination failed");
            throw new PdpSimulatorException(exp.getMessage(), exp);
        }
    }

    /**
     * Get the parameters used by the activator.
     *
     * @return pdpSimulatorParameterGroup the parameters of the activator
     */
    public PdpSimulatorParameterGroup getParameterGroup() {
        return pdpSimulatorParameterGroup;
    }

    /**
     * Registers the dispatcher with the topic source(s).
     */
    private void registerMsgDispatcher() {
        for (final TopicSource source : topicSources) {
            source.register(msgDispatcher);
        }
    }

    /**
     * Unregisters the dispatcher from the topic source(s).
     */
    private void unregisterMsgDispatcher() {
        for (final TopicSource source : topicSources) {
            source.unregister(msgDispatcher);
        }
    }
}
