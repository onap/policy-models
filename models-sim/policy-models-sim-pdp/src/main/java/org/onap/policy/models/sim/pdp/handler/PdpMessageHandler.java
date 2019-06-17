/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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

package org.onap.policy.models.sim.pdp.handler;

import java.util.ArrayList;
import java.util.List;
import org.onap.policy.common.utils.services.Registry;
import org.onap.policy.models.pdp.concepts.PdpResponseDetails;
import org.onap.policy.models.pdp.concepts.PdpStatus;
import org.onap.policy.models.pdp.enums.PdpHealthStatus;
import org.onap.policy.models.pdp.enums.PdpResponseStatus;
import org.onap.policy.models.pdp.enums.PdpState;
import org.onap.policy.models.sim.pdp.PdpSimulatorConstants;
import org.onap.policy.models.sim.pdp.parameters.PdpStatusParameters;
import org.onap.policy.models.sim.pdp.parameters.ToscaPolicyTypeIdentifierParameters;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicy;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyIdentifier;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyTypeIdentifier;

/**
 * This class supports the handling of pdp messages.
 *
 * @author Ajith Sreekumar (ajith.sreekumar@est.tech)
 */
public class PdpMessageHandler {

    /**
     * Method to create PdpStatus message from the parameters which will be saved to the context.
     *
     * @param instanceId instance id of the pdp
     * @param pdpStatusParameters pdp status parameters read from the configuration file
     *
     * @return pdpStatus the pdp status message
     */
    public PdpStatus createPdpStatusFromParameters(final String instanceId,
            final PdpStatusParameters pdpStatusParameters) {
        final PdpStatus pdpStatus = new PdpStatus();
        pdpStatus.setPdpType(pdpStatusParameters.getPdpType());
        pdpStatus.setState(PdpState.PASSIVE);
        pdpStatus.setHealthy(PdpHealthStatus.HEALTHY);
        pdpStatus.setDescription(pdpStatusParameters.getDescription());
        pdpStatus.setName(instanceId);
        pdpStatus.setSupportedPolicyTypes(getSupportedPolicyTypesFromParameters(pdpStatusParameters));
        return pdpStatus;
    }

    /**
     * Method to get supported policy types from the parameters.
     *
     * @param pdpStatusParameters pdp status parameters
     * @return supportedPolicyTypes list of PolicyTypeIdent
     */
    private List<ToscaPolicyTypeIdentifier> getSupportedPolicyTypesFromParameters(
            final PdpStatusParameters pdpStatusParameters) {
        final List<ToscaPolicyTypeIdentifier> supportedPolicyTypes =
                new ArrayList<>(pdpStatusParameters.getSupportedPolicyTypes().size());
        for (final ToscaPolicyTypeIdentifierParameters policyTypeIdentParameters : pdpStatusParameters
                .getSupportedPolicyTypes()) {
            supportedPolicyTypes.add(new ToscaPolicyTypeIdentifier(policyTypeIdentParameters.getName(),
                    policyTypeIdentParameters.getVersion()));
        }
        return supportedPolicyTypes;
    }

    /**
     * Method to create PdpStatus message from the context, which is to be sent by pdp simulator to pap.
     *
     * @return PdpStatus the pdp status message
     */
    public PdpStatus createPdpStatusFromContext() {
        final PdpStatus pdpStatusContext = Registry.get(PdpSimulatorConstants.REG_PDP_STATUS_OBJECT, PdpStatus.class);
        final PdpStatus pdpStatus = new PdpStatus();
        pdpStatus.setName(pdpStatusContext.getName());
        pdpStatus.setPdpType(pdpStatusContext.getPdpType());
        pdpStatus.setState(pdpStatusContext.getState());
        pdpStatus.setHealthy(pdpStatusContext.getHealthy());
        pdpStatus.setDescription(pdpStatusContext.getDescription());
        pdpStatus.setSupportedPolicyTypes(pdpStatusContext.getSupportedPolicyTypes());
        pdpStatus.setPolicies(pdpStatusContext.getPolicies());
        pdpStatus.setPdpGroup(pdpStatusContext.getPdpGroup());
        pdpStatus.setPdpSubgroup(pdpStatusContext.getPdpSubgroup());
        return pdpStatus;
    }

    /**
     * Method to get a final pdp status when the simulator is shut down.
     *
     * @return PdpStatus the pdp status message
     */
    public PdpStatus getTerminatedPdpStatus() {
        final PdpStatus pdpStatusInContext = Registry.get(PdpSimulatorConstants.REG_PDP_STATUS_OBJECT, PdpStatus.class);
        pdpStatusInContext.setState(PdpState.TERMINATED);
        pdpStatusInContext.setDescription("Pdp Simulator shutting down.");
        return createPdpStatusFromContext();
    }

    /**
     * Method create PdpResponseDetails which will be sent as part of pdp status to PAP.
     *
     * @param requestId request id of the PdpUpdate message from pap
     * @param status response status to be sent back
     * @param responseMessage response message to be sent back
     *
     * @return PdpResponseDetails
     */
    public PdpResponseDetails createPdpResonseDetails(final String requestId, final PdpResponseStatus status,
            final String responseMessage) {
        final PdpResponseDetails pdpResponseDetails = new PdpResponseDetails();
        pdpResponseDetails.setResponseTo(requestId);
        pdpResponseDetails.setResponseStatus(status);
        pdpResponseDetails.setResponseMessage(responseMessage);
        return pdpResponseDetails;
    }

    /**
     * Method to retrieve list of ToscaPolicyIdentifier from the list of ToscaPolicy.
     *
     * @param policies list of ToscaPolicy
     *
     * @return policyTypeIdentifiers
     */
    public List<ToscaPolicyIdentifier> getToscaPolicyIdentifiers(final List<ToscaPolicy> policies) {
        final List<ToscaPolicyIdentifier> policyIdentifiers = new ArrayList<>(policies.size());
        for (final ToscaPolicy policy : policies) {
            if (null != policy.getName() && null != policy.getVersion()) {
                policyIdentifiers.add(new ToscaPolicyIdentifier(policy.getName(), policy.getVersion()));
            }
        }
        return policyIdentifiers;
    }
}
