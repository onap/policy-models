/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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

import java.util.List;
import org.onap.policy.common.utils.services.Registry;
import org.onap.policy.models.pdp.concepts.PdpResponseDetails;
import org.onap.policy.models.pdp.concepts.PdpStateChange;
import org.onap.policy.models.pdp.concepts.PdpStatus;
import org.onap.policy.models.pdp.enums.PdpResponseStatus;
import org.onap.policy.models.pdp.enums.PdpState;
import org.onap.policy.models.sim.pdp.PdpSimulatorConstants;
import org.onap.policy.models.sim.pdp.comm.PdpStatusPublisher;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicy;

/**
 * This class supports the handling of pdp state change messages.
 *
 * @author Ajith Sreekumar (ajith.sreekumar@est.tech)
 */
public class PdpStateChangeMessageHandler {

    /**
     * Method which handles a pdp state change event from PAP.
     *
     * @param pdpStateChangeMsg pdp state change message
     */
    public void handlePdpStateChangeEvent(final PdpStateChange pdpStateChangeMsg) {
        final var pdpStatusContext = Registry.get(PdpSimulatorConstants.REG_PDP_STATUS_OBJECT, PdpStatus.class);
        final var pdpStatusPublisher =
                        Registry.get(PdpSimulatorConstants.REG_PDP_STATUS_PUBLISHER, PdpStatusPublisher.class);
        final var pdpMessageHandler = new PdpMessageHandler();
        PdpResponseDetails pdpResponseDetails = null;
        if (pdpStateChangeMsg.appliesTo(pdpStatusContext.getName(), pdpStatusContext.getPdpGroup(),
                pdpStatusContext.getPdpSubgroup())) {
            switch (pdpStateChangeMsg.getState()) {
                case PASSIVE:
                    pdpResponseDetails = handlePassiveState(pdpStateChangeMsg, pdpStatusContext, pdpMessageHandler);
                    break;
                case ACTIVE:
                    pdpResponseDetails = handleActiveState(pdpStateChangeMsg, pdpStatusContext, pdpMessageHandler);
                    break;
                default:
                    break;
            }
            final var pdpStatus = pdpMessageHandler.createPdpStatusFromContext();
            pdpStatus.setResponse(pdpResponseDetails);
            pdpStatus.setDescription("Pdp status response message for PdpStateChange");
            pdpStatusPublisher.send(pdpStatus);
        }
    }

    /**
     * Method to handle when the new state from pap is active.
     *
     * @param pdpStateChangeMsg pdp state change message
     * @param pdpStatusContext pdp status object in memory
     * @param pdpMessageHandler the pdp message handler
     * @return pdpResponseDetails pdp response
     */
    private PdpResponseDetails handleActiveState(final PdpStateChange pdpStateChangeMsg,
            final PdpStatus pdpStatusContext, final PdpMessageHandler pdpMessageHandler) {
        PdpResponseDetails pdpResponseDetails = null;
        if (pdpStatusContext.getState().equals(PdpState.ACTIVE)) {
            pdpResponseDetails = pdpMessageHandler.createPdpResponseDetails(pdpStateChangeMsg.getRequestId(),
                    PdpResponseStatus.SUCCESS, "Pdp already in active state");
        } else {
            final List<ToscaPolicy> policies = Registry.get(PdpSimulatorConstants.REG_PDP_TOSCA_POLICY_LIST);
            if (policies.isEmpty()) {
                pdpStatusContext.setState(PdpState.ACTIVE);
                pdpResponseDetails = pdpMessageHandler.createPdpResponseDetails(pdpStateChangeMsg.getRequestId(),
                        PdpResponseStatus.SUCCESS, "State changed to active. No policies found.");
            } else {
                pdpResponseDetails = pdpMessageHandler.createPdpResponseDetails(pdpStateChangeMsg.getRequestId(),
                        PdpResponseStatus.SUCCESS, "Pdp started. State changed to active.");
                pdpStatusContext.setState(PdpState.ACTIVE);
            }
        }
        return pdpResponseDetails;
    }

    /**
     * Method to handle when the new state from pap is passive.
     *
     * @param pdpStateChangeMsg pdp state change message
     * @param pdpStatusContext pdp status object in memory
     * @param pdpMessageHandler the pdp message handler
     * @return pdpResponseDetails pdp response
     */
    private PdpResponseDetails handlePassiveState(final PdpStateChange pdpStateChangeMsg,
            final PdpStatus pdpStatusContext, final PdpMessageHandler pdpMessageHandler) {
        PdpResponseDetails pdpResponseDetails = null;
        if (pdpStatusContext.getState().equals(PdpState.PASSIVE)) {
            pdpResponseDetails = pdpMessageHandler.createPdpResponseDetails(pdpStateChangeMsg.getRequestId(),
                    PdpResponseStatus.SUCCESS, "Pdp already in passive state");
        } else {
            pdpResponseDetails = pdpMessageHandler.createPdpResponseDetails(pdpStateChangeMsg.getRequestId(),
                    PdpResponseStatus.SUCCESS, "Pdp state changed from Active to Passive.");
            pdpStatusContext.setState(PdpState.PASSIVE);
        }
        return pdpResponseDetails;
    }
}
