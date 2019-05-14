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

package org.onap.policy.models.sim.pdp.comm;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.onap.policy.common.endpoints.event.comm.TopicSink;
import org.onap.policy.common.endpoints.event.comm.client.TopicSinkClient;
import org.onap.policy.models.pdp.concepts.PdpStatus;
import org.onap.policy.models.sim.pdp.handler.PdpMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used to send pdp status messages to pap using TopicSinkClient.
 *
 * @author Ajith Sreekumar (ajith.sreekumar@est.tech)
 */
public class PdpStatusPublisher extends TimerTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(PdpStatusPublisher.class);

    private TopicSinkClient topicSinkClient;
    private Timer timer;
    private long interval;

    /**
     * Constructor for instantiating PdpStatusPublisher.
     *
     * @param topicSinks the topic sinks
     * @param interval time interval to send pdp status
     */
    public PdpStatusPublisher(final List<TopicSink> topicSinks, final long interval) {
        this.topicSinkClient = new TopicSinkClient(topicSinks.get(0));
        this.interval = interval;
        timer = new Timer(false);
        timer.scheduleAtFixedRate(this, 0, interval);
    }

    @Override
    public void run() {
        final PdpStatus pdpStatus = new PdpMessageHandler().createPdpStatusFromContext();
        topicSinkClient.send(pdpStatus);
        LOGGER.debug("Sent heartbeat to PAP - {}", pdpStatus);
    }

    /**
     * Terminates the current timer.
     */
    public void terminate() {
        timer.cancel();
        timer.purge();
    }

    /**
     * Get the current time interval used by the timer task.
     *
     * @return interval the current time interval
     */
    public long getInterval() {
        return interval;
    }

    /**
     * Method to send pdp status message to pap on demand.
     *
     * @param pdpStatus the pdp status
     */
    public void send(final PdpStatus pdpStatus) {
        topicSinkClient.send(pdpStatus);
        LOGGER.debug("Sent pdp status message to PAP - {}", pdpStatus);
    }
}
