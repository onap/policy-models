/*-
 * ============LICENSE_START=======================================================
 * policy-yaml
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019 Nordix Foundation.
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

package org.onap.policy.controlloop.policy.builder;

import org.onap.policy.aai.Pnf;
import org.onap.policy.controlloop.policy.ControlLoop;
import org.onap.policy.controlloop.policy.OperationsAccumulateParams;
import org.onap.policy.controlloop.policy.Policy;
import org.onap.policy.controlloop.policy.PolicyParam;
import org.onap.policy.controlloop.policy.PolicyResult;
import org.onap.policy.controlloop.policy.builder.impl.ControlLoopPolicyBuilderImpl;
import org.onap.policy.sdc.Resource;
import org.onap.policy.sdc.Service;

public interface ControlLoopPolicyBuilder {

    /**
     * Adds one or more services to the ControlLoop.
     * 
     * @param services service to add
     * @return builder object
     * @throws BuilderException builder exception
     */
    public ControlLoopPolicyBuilder addService(Service... services) throws BuilderException;

    /**
     * Remove service.
     * 
     * @param services to remove
     * @return builder object
     * @throws BuilderException builder exception
     */
    public ControlLoopPolicyBuilder removeService(Service... services) throws BuilderException;

    /**
     * Remove all the services.
     * 
     * @return builder object
     * @throws BuilderException builder exception
     */
    public ControlLoopPolicyBuilder removeAllServices() throws BuilderException;

    /**
     * Adds one or more resources to the ControlLoop.
     * 
     * @return builder object
     * @throws BuilderException builder exception
     */
    public ControlLoopPolicyBuilder addResource(Resource... resources) throws BuilderException;

    /**
     * Remove the resources.
     * 
     * @param resources resources to be removed
     * @return object
     * @throws BuilderException builder exception
     */
    public ControlLoopPolicyBuilder removeResource(Resource... resources) throws BuilderException;

    /**
     * Remove all resources.
     * 
     * @return object
     * @throws BuilderException builder exception
     */
    public ControlLoopPolicyBuilder removeAllResources() throws BuilderException;

    /**
     * Set the PNF.
     * 
     * @param pnf input pnf
     * @return builder object
     * @throws BuilderException builder exception
     */
    public ControlLoopPolicyBuilder setPNF(Pnf pnf) throws BuilderException;

    /**
     * Remove PNF.
     * 
     * @return the object
     * @throws BuilderException builder exception
     */
    public ControlLoopPolicyBuilder removePNF() throws BuilderException;

    /**
     * Set the abatement.
     * 
     * @param abatement whether abatement is possible
     * @return object
     * @throws BuilderException builder exception
     */
    public ControlLoopPolicyBuilder setAbatement(Boolean abatement) throws BuilderException;


    /**
     * Sets the overall timeout value for the Control Loop. If any operational policies have retries
     * and timeouts, then this overall timeout value should exceed all those values.
     * 
     * @param timeout timeout value
     * @return control loop policy builder
     * @throws BuilderException builder exception
     */
    public ControlLoopPolicyBuilder setTimeout(Integer timeout) throws BuilderException;

    /**
     * Scans the operational policies and calculate an minimum overall timeout for the Control Loop.
     * 
     * 
     * @return Integer
     */
    public Integer calculateTimeout();

    /**
     * Sets the initial trigger policy when a DCAE Closed Loop Event arrives in the ONAP Policy
     * Platform.
     * 
     * 
     * @param policy Policy parameters object
     * @return Policy object
     * @throws BuilderException builder exception
     */
    public Policy setTriggerPolicy(PolicyParam policy) throws BuilderException;

    /**
     * Changes the trigger policy to point to another existing Policy.
     * 
     * @param id the id
     * @return ControlLoop object
     * @throws BuilderException build exception
     */
    public ControlLoop setExistingTriggerPolicy(String id) throws BuilderException;

    /**
     * Is an open loop.
     * 
     * @return true or false
     */
    public boolean isOpenLoop();

    /**
     * Get the trigger policy.
     * 
     * @return the policy object
     * @throws BuilderException if there is a builder exception
     */
    public Policy getTriggerPolicy() throws BuilderException;

    /**
     * Simply returns a copy of the ControlLoop information.
     * 
     * 
     * @return ControlLoop
     */
    public ControlLoop getControlLoop();

    /**
     * Creates a policy that is chained to the result of another Policy.
     * 
     * @param policyParam policy parameters object
     * @param results results
     * @return Policy that was set
     * @throws BuilderException builder exception
     */
    public Policy setPolicyForPolicyResult(PolicyParam policyParam, PolicyResult... results)
            throws BuilderException;


    /**
     * Sets the policy result(s) to an existing Operational Policy.
     * 
     * @param policyResultId result ID
     * @param policyId id
     * @param results results
     * @return Policy that was set
     * @throws BuilderException builder exception
     */
    public Policy setPolicyForPolicyResult(String policyResultId, String policyId, PolicyResult... results)
            throws BuilderException;

    /**
     * Removes an Operational Policy. Be mindful that if any other Operational Policies have results
     * that point to this policy, any policies that have results pointing to this policy will have
     * their result reset to the appropriate default FINAL_* result.
     * 
     * 
     * @param policyID id for the policy
     * @return true if removed else false
     * @throws BuilderException builder exception
     */
    public boolean removePolicy(String policyID) throws BuilderException;

    /**
     * Resets a policy's results to defualt FINAL_* codes.
     * 
     * @return Policy object
     * @throws BuilderException - Policy does not exist
     */
    public Policy resetPolicyResults(String policyID) throws BuilderException;

    /**
     * Removes all existing Operational Policies and reverts back to an Open Loop.
     * 
     * @return Policy builder object
     */
    public ControlLoopPolicyBuilder removeAllPolicies();

    /**
     * Adds an operationsAccumulateParams to an existing operational policy.
     * 
     * @return Policy
     * @throws BuilderException - Policy does not exist
     */
    public Policy addOperationsAccumulateParams(String policyID, OperationsAccumulateParams operationsAccumulateParams)
            throws BuilderException;

    /**
     * This will compile and build the YAML specification for the Control Loop Policy. Please
     * iterate the Results object for details. The Results object will contains warnings and errors.
     * If the specification compiled successfully, you will be able to retrieve the YAML.
     * 
     * @return Results
     */
    public Results buildSpecification();

    /**
     * The Factory is used to build a ControlLoopPolicyBuilder implementation.
     * 
     * @author pameladragosh
     *
     */
    public static class Factory {
        private Factory() {
            // Private Constructor.
        }

        /**
         * Builds a basic Control Loop with an overall timeout. Use this method if you wish to
         * create an OpenLoop, or if you want to interactively build a Closed Loop.
         * 
         * @param controlLoopName - Per Closed Loop AID v1.0, unique string for the closed loop.
         * @param timeout - Overall timeout for the Closed Loop to execute.
         * @return ControlLoopPolicyBuilder object
         */
        public static ControlLoopPolicyBuilder buildControlLoop(String controlLoopName, Integer timeout) {
            return new ControlLoopPolicyBuilderImpl(controlLoopName, timeout);
        }

        /**
         * Build a Control Loop for a resource and services associated with the resource.
         * 
         * @param controlLoopName - Per Closed Loop AID v1.0, unique string for the closed loop.
         * @param timeout - Overall timeout for the Closed Loop to execute.
         * @param resource - Resource this closed loop is for. Should come from ASDC, but if not
         *        available use resourceName to distinguish.
         * @param services - Zero or more services associated with this resource. Should come from
         *        ASDC, but if not available use serviceName to distinguish.
         * @return ControlLoopPolicyBuilder object
         * @throws BuilderException builder exception
         */
        public static ControlLoopPolicyBuilder buildControlLoop(String controlLoopName, Integer timeout,
                Resource resource, Service... services) throws BuilderException {
            return new ControlLoopPolicyBuilderImpl(controlLoopName, timeout, resource, services);
        }

        /**
         * Build the control loop.
         * 
         * @param controlLoopName control loop id
         * @param timeout timeout
         * @param service service
         * @param resources resources
         * @return builder object
         * @throws BuilderException builder exception
         */
        public static ControlLoopPolicyBuilder buildControlLoop(String controlLoopName, Integer timeout,
                Service service, Resource... resources) throws BuilderException {
            return new ControlLoopPolicyBuilderImpl(controlLoopName, timeout, service, resources);
        }

        /**
         * Build control loop.
         * 
         * @param controlLoopName - Per Closed Loop AID v1.0, unique string for the closed loop.
         * @param timeout - Overall timeout for the Closed Loop to execute.
         * @param pnf - Physical Network Function. Should come from AIC, but if not available use
         *        well-known name to distinguish. Eg. eNodeB
         * @return ControlLoopPolicyBuilder object
         * @throws BuilderException builder exception
         */
        public static ControlLoopPolicyBuilder buildControlLoop(String controlLoopName, Integer timeout, Pnf pnf)
                throws BuilderException {
            return new ControlLoopPolicyBuilderImpl(controlLoopName, timeout, pnf);
        }
    }

}
