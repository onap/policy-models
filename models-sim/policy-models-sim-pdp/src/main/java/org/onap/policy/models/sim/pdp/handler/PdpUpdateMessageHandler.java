/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2021 Nordix Foundation.
 *  Modifications Copyright (C) 2019, 2021 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.models.sim.pdp.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.onap.policy.common.endpoints.event.comm.TopicSink;
import org.onap.policy.common.utils.services.Registry;
import org.onap.policy.models.pdp.concepts.PdpResponseDetails;
import org.onap.policy.models.pdp.concepts.PdpStatus;
import org.onap.policy.models.pdp.concepts.PdpUpdate;
import org.onap.policy.models.pdp.enums.PdpResponseStatus;
import org.onap.policy.models.pdp.enums.PdpState;
import org.onap.policy.models.sim.pdp.PdpSimulatorConstants;
import org.onap.policy.models.sim.pdp.comm.PdpStatusPublisher;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicy;

/**
 * This class supports the handling of pdp update messages.
 *
 * @author Ajith Sreekumar (ajith.sreekumar@est.tech)
 */
public class PdpUpdateMessageHandler {

    /**
     * Method which handles a pdp update event from PAP.
     *
     * @param pdpUpdateMsg pdp update message
     */
    public void handlePdpUpdateEvent(final PdpUpdate pdpUpdateMsg) {
        final var pdpMessageHandler = new PdpMessageHandler();
        final var pdpStatusContext = Registry.get(PdpSimulatorConstants.REG_PDP_STATUS_OBJECT, PdpStatus.class);
        PdpResponseDetails pdpResponseDetails = null;
        if (pdpUpdateMsg.appliesTo(pdpStatusContext.getName(), pdpStatusContext.getPdpGroup(),
                        pdpStatusContext.getPdpSubgroup())) {
            final var pdpStatusPublisher =
                            Registry.get(PdpSimulatorConstants.REG_PDP_STATUS_PUBLISHER, PdpStatusPublisher.class);
            if (checkIfAlreadyHandled(pdpUpdateMsg, pdpStatusContext)) {
                pdpResponseDetails = pdpMessageHandler.createPdpResponseDetails(pdpUpdateMsg.getRequestId(),
                        PdpResponseStatus.SUCCESS, "Pdp already updated");
            } else {
                if (null != pdpUpdateMsg.getPdpHeartbeatIntervalMs() && pdpUpdateMsg.getPdpHeartbeatIntervalMs() > 0
                        && pdpStatusPublisher.getInterval() != pdpUpdateMsg.getPdpHeartbeatIntervalMs()) {
                    updateInterval(pdpUpdateMsg.getPdpHeartbeatIntervalMs());
                }
                pdpStatusContext.setPdpGroup(pdpUpdateMsg.getPdpGroup());
                pdpStatusContext.setPdpSubgroup(pdpUpdateMsg.getPdpSubgroup());
                @SuppressWarnings("unchecked")
                List<ToscaPolicy> policies = Registry.getOrDefault(PdpSimulatorConstants.REG_PDP_TOSCA_POLICY_LIST,
                        List.class, new ArrayList<>());
                policies.addAll(pdpUpdateMsg.getPoliciesToBeDeployed());
                policies.removeIf(policy -> pdpUpdateMsg.getPoliciesToBeUndeployed().contains(policy.getIdentifier()));
                pdpStatusContext.setPolicies(policies.stream().map(ToscaPolicy::getIdentifier)
                        .collect(Collectors.toList()));
                if (pdpStatusContext.getState().equals(PdpState.ACTIVE)
                        && !pdpUpdateMsg.getPoliciesToBeDeployed().isEmpty()) {
                    pdpResponseDetails = pdpMessageHandler.createPdpResponseDetails(pdpUpdateMsg.getRequestId(),
                            PdpResponseStatus.SUCCESS, "Pdp engine started and policies are running.");
                }
                Registry.registerOrReplace(PdpSimulatorConstants.REG_PDP_TOSCA_POLICY_LIST, policies);
                if (null == pdpResponseDetails) {
                    pdpResponseDetails = pdpMessageHandler.createPdpResponseDetails(pdpUpdateMsg.getRequestId(),
                            PdpResponseStatus.SUCCESS, "Pdp update successful.");
                }
            }
            final var pdpStatusPublisherTemp =
                    Registry.get(PdpSimulatorConstants.REG_PDP_STATUS_PUBLISHER, PdpStatusPublisher.class);
            final var pdpStatus = pdpMessageHandler.createPdpStatusFromContext();
            pdpStatus.setResponse(pdpResponseDetails);
            pdpStatus.setDescription("Pdp status response message for PdpUpdate");
            pdpStatusPublisherTemp.send(pdpStatus);
        }
    }


    /**
     * Method checks if the Pdp update message is already handled by checking the values in the context.
     *
     * @param pdpUpdateMsg pdp update message received from pap
     * @param pdpStatusContext values saved in context memory
     * @return boolean flag which tells if the information is same or not
     */
    private boolean checkIfAlreadyHandled(final PdpUpdate pdpUpdateMsg, final PdpStatus pdpStatusContext) {
        if (pdpStatusContext.getPdpGroup() == null
                || !pdpStatusContext.getPdpGroup().equals(pdpUpdateMsg.getPdpGroup())) {
            return false;
        }

        if (pdpStatusContext.getPdpSubgroup() == null
                || !pdpStatusContext.getPdpSubgroup().equals(pdpUpdateMsg.getPdpSubgroup())) {
            return false;
        }

        return null != pdpStatusContext.getPolicies() && (pdpStatusContext.getPolicies()
                .containsAll(new PdpMessageHandler().getToscaPolicyIdentifiers(
                        pdpUpdateMsg.getPoliciesToBeDeployed()))) && pdpStatusContext.getPolicies()
                .stream().noneMatch(pdpUpdateMsg.getPoliciesToBeUndeployed()::contains);
    }

    /**
     * Method to update the time interval used by the timer task.
     *
     * @param interval time interval received in the pdp update message from pap
     */
    public void updateInterval(final long interval) {
        final var pdpStatusPublisher =
                        Registry.get(PdpSimulatorConstants.REG_PDP_STATUS_PUBLISHER, PdpStatusPublisher.class);
        pdpStatusPublisher.terminate();
        final List<TopicSink> topicSinks = Registry.get(PdpSimulatorConstants.REG_PDP_TOPIC_SINKS);
        Registry.registerOrReplace(PdpSimulatorConstants.REG_PDP_STATUS_PUBLISHER,
                new PdpStatusPublisher(topicSinks, interval));
    }
}
