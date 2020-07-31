/*-
 * ============LICENSE_START=======================================================
 * policy-yaml
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019-2020 Nordix Foundation.
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

package org.onap.policy.controlloop.policy.builder.impl;

import com.google.common.base.Strings;
import java.util.LinkedList;
import java.util.UUID;
import org.onap.aai.domain.yang.Pnf;
import org.onap.policy.controlloop.compiler.CompilerException;
import org.onap.policy.controlloop.compiler.ControlLoopCompiler;
import org.onap.policy.controlloop.compiler.ControlLoopCompilerCallback;
import org.onap.policy.controlloop.policy.ControlLoop;
import org.onap.policy.controlloop.policy.ControlLoopPolicy;
import org.onap.policy.controlloop.policy.FinalResult;
import org.onap.policy.controlloop.policy.OperationsAccumulateParams;
import org.onap.policy.controlloop.policy.Policy;
import org.onap.policy.controlloop.policy.PolicyParam;
import org.onap.policy.controlloop.policy.PolicyResult;
import org.onap.policy.controlloop.policy.builder.BuilderException;
import org.onap.policy.controlloop.policy.builder.ControlLoopPolicyBuilder;
import org.onap.policy.controlloop.policy.builder.MessageLevel;
import org.onap.policy.controlloop.policy.builder.Results;
import org.onap.policy.sdc.Resource;
import org.onap.policy.sdc.Service;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.Yaml;

public class ControlLoopPolicyBuilderImpl implements ControlLoopPolicyBuilder {
    private static final String UNKNOWN_POLICY = "Unknown policy ";
    private ControlLoopPolicy controlLoopPolicy;

    /**
     * Constructor.
     *
     * @param controlLoopName control loop id
     * @param timeout timeout value
     */
    public ControlLoopPolicyBuilderImpl(String controlLoopName, Integer timeout) {
        controlLoopPolicy = new ControlLoopPolicy();
        ControlLoop controlLoop = new ControlLoop();
        controlLoop.setControlLoopName(controlLoopName);
        controlLoop.setTimeout(timeout);
        controlLoopPolicy.setControlLoop(controlLoop);
    }

    /**
     * Constructor.
     *
     * @param controlLoopName control loop id
     * @param timeout timeout value
     * @param resource resource
     * @param services services
     * @throws BuilderException builder exception
     */
    public ControlLoopPolicyBuilderImpl(String controlLoopName, Integer timeout, Resource resource,
        Service... services) throws BuilderException {
        this(controlLoopName, timeout);
        this.addResource(resource);
        this.addService(services);
    }

    public ControlLoopPolicyBuilderImpl(String controlLoopName, Integer timeout, Pnf pnf) throws BuilderException {
        this(controlLoopName, timeout);
        this.setPnf(pnf);
    }

    /**
     * Constructor.
     *
     * @param controlLoopName control loop id
     * @param timeout timeout
     * @param service service
     * @param resources resources
     * @throws BuilderException builder exception
     */
    public ControlLoopPolicyBuilderImpl(String controlLoopName, Integer timeout, Service service,
        Resource[] resources) throws BuilderException {
        this(controlLoopName, timeout);
        this.addService(service);
        this.addResource(resources);
    }

    @Override
    public ControlLoopPolicyBuilder removePnf() throws BuilderException {
        controlLoopPolicy.getControlLoop().setPnf(null);
        return this;
    }

    @Override
    public ControlLoopPolicyBuilder addService(Service... services) throws BuilderException {
        for (Service service : services) {
            if (service == null) {
                throw new BuilderException("Service must not be null");
            }
            if (service.getServiceUUID() == null && Strings.isNullOrEmpty(service.getServiceName())) {
                throw new BuilderException("Invalid service - need either a serviceUUID or serviceName");
            }
            if (controlLoopPolicy.getControlLoop().getServices() == null) {
                controlLoopPolicy.getControlLoop().setServices(new LinkedList<>());
            }
            controlLoopPolicy.getControlLoop().getServices().add(service);
        }
        return this;
    }

    @Override
    public ControlLoopPolicyBuilder removeService(Service... services) throws BuilderException {
        if (controlLoopPolicy.getControlLoop().getServices() == null) {
            throw new BuilderException("No existing services to remove");
        }
        for (Service service : services) {
            if (service == null) {
                throw new BuilderException("Service must not be null");
            }
            if (service.getServiceUUID() == null && Strings.isNullOrEmpty(service.getServiceName())) {
                throw new BuilderException("Invalid service - need either a serviceUUID or serviceName");
            }
            boolean removed = controlLoopPolicy.getControlLoop().getServices().remove(service);
            if (!removed) {
                throw new BuilderException("Unknown service " + service.getServiceName());
            }
        }
        return this;
    }

    @Override
    public ControlLoopPolicyBuilder removeAllServices() throws BuilderException {
        controlLoopPolicy.getControlLoop().getServices().clear();
        return this;
    }

    @Override
    public ControlLoopPolicyBuilder addResource(Resource... resources) throws BuilderException {
        for (Resource resource : resources) {
            if (resource == null) {
                throw new BuilderException("Resource must not be null");
            }
            if (resource.getResourceUuid() == null && Strings.isNullOrEmpty(resource.getResourceName())) {
                throw new BuilderException("Invalid resource - need either resourceUUID or resourceName");
            }
            if (controlLoopPolicy.getControlLoop().getResources() == null) {
                controlLoopPolicy.getControlLoop().setResources(new LinkedList<>());
            }
            controlLoopPolicy.getControlLoop().getResources().add(resource);
        }
        return this;
    }

    @Override
    public ControlLoopPolicyBuilder setPnf(Pnf pnf) throws BuilderException {
        if (pnf == null) {
            throw new BuilderException("PNF must not be null");
        }
        if (pnf.getPnfName() == null && pnf.getEquipType() == null) {
            throw new BuilderException("Invalid PNF - need either pnfName or pnfType");
        }
        controlLoopPolicy.getControlLoop().setPnf(pnf);
        return this;
    }

    @Override
    public ControlLoopPolicyBuilder setAbatement(Boolean abatement) throws BuilderException {
        if (abatement == null) {
            throw new BuilderException("abatement must not be null");
        }
        controlLoopPolicy.getControlLoop().setAbatement(abatement);
        return this;
    }

    @Override
    public ControlLoopPolicyBuilder setTimeout(Integer timeout) {
        controlLoopPolicy.getControlLoop().setTimeout(timeout);
        return this;
    }

    @Override
    public Policy setTriggerPolicy(PolicyParam policyParam) throws BuilderException {

        Policy trigger = new Policy(policyParam);

        controlLoopPolicy.getControlLoop().setTriggerPolicy(trigger.getId());

        this.addNewPolicy(trigger);
        //
        // Return a copy of the policy
        //
        return new Policy(trigger);
    }

    @Override
    public ControlLoop setExistingTriggerPolicy(String id) throws BuilderException {
        if (id == null) {
            throw new BuilderException("Id must not be null");
        }
        Policy trigger = this.findPolicy(id);
        if (trigger == null) {
            throw new BuilderException(UNKNOWN_POLICY + id);
        } else {
            this.controlLoopPolicy.getControlLoop().setTriggerPolicy(id);
        }
        return new ControlLoop(this.controlLoopPolicy.getControlLoop());
    }

    @Override
    public Policy setPolicyForPolicyResult(PolicyParam policyParam, PolicyResult... results) throws BuilderException {
        //
        // Find the existing policy
        //
        Policy existingPolicy = this.findPolicy(policyParam.getId());
        if (existingPolicy == null) {
            throw new BuilderException(UNKNOWN_POLICY + policyParam.getId());
        }
        //
        // Create the new Policy
        //
        // @formatter:off
        Policy newPolicy = new Policy(PolicyParam.builder()
            .id(UUID.randomUUID().toString())
            .name(policyParam.getName())
            .description(policyParam.getDescription())
            .actor(policyParam.getActor())
            .payload(policyParam.getPayload())
            .target(policyParam.getTarget())
            .recipe(policyParam.getRecipe())
            .retries(policyParam.getRetries())
            .timeout(policyParam.getTimeout())
            .build());
        // @formatter:on
        //
        // Connect the results
        //
        for (PolicyResult result : results) {
            switch (result) {
                case FAILURE:
                    existingPolicy.setFailure(newPolicy.getId());
                    break;
                case FAILURE_EXCEPTION:
                    existingPolicy.setFailureException(newPolicy.getId());
                    break;
                case FAILURE_RETRIES:
                    existingPolicy.setFailureRetries(newPolicy.getId());
                    break;
                case FAILURE_TIMEOUT:
                    existingPolicy.setFailureTimeout(newPolicy.getId());
                    break;
                case FAILURE_GUARD:
                    existingPolicy.setFailureGuard(newPolicy.getId());
                    break;
                case SUCCESS:
                    existingPolicy.setSuccess(newPolicy.getId());
                    break;
                default:
                    throw new BuilderException("Invalid PolicyResult " + result);
            }
        }
        //
        // Add it to our list
        //
        this.controlLoopPolicy.getPolicies().add(newPolicy);
        //
        // Return a policy to them
        //
        return new Policy(newPolicy);
    }

    @Override
    public Policy setPolicyForPolicyResult(String policyResultId, String policyId, PolicyResult... results)
        throws BuilderException {
        //
        // Find the existing policy
        //
        Policy existingPolicy = this.findPolicy(policyId);
        if (existingPolicy == null) {
            throw new BuilderException(policyId + " does not exist");
        }
        if (this.findPolicy(policyResultId) == null) {
            throw new BuilderException("Operational policy " + policyResultId + " does not exist");
        }
        //
        // Connect the results
        //
        for (PolicyResult result : results) {
            switch (result) {
                case FAILURE:
                    existingPolicy.setFailure(policyResultId);
                    break;
                case FAILURE_EXCEPTION:
                    existingPolicy.setFailureException(policyResultId);
                    break;
                case FAILURE_RETRIES:
                    existingPolicy.setFailureRetries(policyResultId);
                    break;
                case FAILURE_TIMEOUT:
                    existingPolicy.setFailureTimeout(policyResultId);
                    break;
                case FAILURE_GUARD:
                    existingPolicy.setFailureGuard(policyResultId);
                    break;
                case SUCCESS:
                    existingPolicy.setSuccess(policyResultId);
                    break;
                default:
                    throw new BuilderException("Invalid PolicyResult " + result);
            }
        }
        return new Policy(this.findPolicy(policyResultId));
    }

    private class BuilderCompilerCallback implements ControlLoopCompilerCallback {

        private ResultsImpl results = new ResultsImpl();

        @Override
        public boolean onWarning(String message) {
            results.addMessage(new MessageImpl(message, MessageLevel.WARNING));
            return false;
        }

        @Override
        public boolean onError(String message) {
            results.addMessage(new MessageImpl(message, MessageLevel.ERROR));
            return false;
        }
    }

    @Override
    public Results buildSpecification() {
        //
        // Dump the specification
        //
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        Yaml yaml = new Yaml(options);
        String dumpedYaml = yaml.dump(controlLoopPolicy);
        //
        // This is our callback class for our compiler
        //
        BuilderCompilerCallback callback = new BuilderCompilerCallback();
        //
        // Compile it
        //
        try {
            ControlLoopCompiler.compile(controlLoopPolicy, callback);
        } catch (CompilerException e) {
            callback.results.addMessage(new MessageImpl(e.getMessage(), MessageLevel.EXCEPTION));
        }
        //
        // Save the spec
        //
        callback.results.setSpecification(dumpedYaml);
        return callback.results;
    }

    private void addNewPolicy(Policy policy) {
        if (this.controlLoopPolicy.getPolicies() == null) {
            this.controlLoopPolicy.setPolicies(new LinkedList<>());
        }
        this.controlLoopPolicy.getPolicies().add(policy);
    }

    private Policy findPolicy(String id) {
        if (this.controlLoopPolicy.getPolicies() != null) {
            for (Policy policy : this.controlLoopPolicy.getPolicies()) {
                if (policy.getId().equals(id)) {
                    return policy;
                }
            }
        }
        return null;
    }

    @Override
    public ControlLoopPolicyBuilder removeResource(Resource... resources) throws BuilderException {
        if (controlLoopPolicy.getControlLoop().getResources() == null) {
            throw new BuilderException("No existing resources to remove");
        }
        for (Resource resource : resources) {
            if (resource == null) {
                throw new BuilderException("Resource must not be null");
            }
            if (resource.getResourceUuid() == null && Strings.isNullOrEmpty(resource.getResourceName())) {
                throw new BuilderException("Invalid resource - need either a resourceUUID or resourceName");
            }
            boolean removed = controlLoopPolicy.getControlLoop().getResources().remove(resource);
            if (!removed) {
                throw new BuilderException("Unknown resource " + resource.getResourceName());
            }
        }
        return this;
    }

    @Override
    public ControlLoopPolicyBuilder removeAllResources() throws BuilderException {
        controlLoopPolicy.getControlLoop().getResources().clear();
        return this;
    }

    @Override
    public Integer calculateTimeout() {
        int sum = 0;
        for (Policy policy : this.controlLoopPolicy.getPolicies()) {
            sum += policy.getTimeout().intValue();
        }
        return Integer.valueOf(sum);
    }

    @Override
    public boolean isOpenLoop() {
        return this.controlLoopPolicy.getControlLoop().getTriggerPolicy()
            .equals(FinalResult.FINAL_OPENLOOP.toString());
    }

    @Override
    public Policy getTriggerPolicy() throws BuilderException {
        if (this.controlLoopPolicy.getControlLoop().getTriggerPolicy().equals(FinalResult.FINAL_OPENLOOP.toString())) {
            return null;
        } else {
            return new Policy(this.findPolicy(this.controlLoopPolicy.getControlLoop().getTriggerPolicy()));
        }
    }

    @Override
    public ControlLoop getControlLoop() {
        return new ControlLoop(this.controlLoopPolicy.getControlLoop());
    }

    @Override
    public boolean removePolicy(String policyId) throws BuilderException {
        Policy existingPolicy = this.findPolicy(policyId);
        if (existingPolicy == null) {
            throw new BuilderException(UNKNOWN_POLICY + policyId);
        }
        //
        // Check if the policy to remove is trigger_policy
        //
        if (this.controlLoopPolicy.getControlLoop().getTriggerPolicy().equals(policyId)) {
            this.controlLoopPolicy.getControlLoop().setTriggerPolicy(FinalResult.FINAL_OPENLOOP.toString());
        } else {
            updateChainedPoliciesForPolicyRemoval(policyId);
        }
        //
        // remove the policy
        //
        return this.controlLoopPolicy.getPolicies().remove(existingPolicy);
    }

    private void updateChainedPoliciesForPolicyRemoval(String idOfPolicyBeingRemoved) {
        for (Policy policy : this.controlLoopPolicy.getPolicies()) {
            final int index = this.controlLoopPolicy.getPolicies().indexOf(policy);
            if (policy.getSuccess().equals(idOfPolicyBeingRemoved)) {
                policy.setSuccess(FinalResult.FINAL_SUCCESS.toString());
            }
            if (policy.getFailure().equals(idOfPolicyBeingRemoved)) {
                policy.setFailure(FinalResult.FINAL_FAILURE.toString());
            }
            if (policy.getFailureRetries().equals(idOfPolicyBeingRemoved)) {
                policy.setFailureRetries(FinalResult.FINAL_FAILURE_RETRIES.toString());
            }
            if (policy.getFailureTimeout().equals(idOfPolicyBeingRemoved)) {
                policy.setFailureTimeout(FinalResult.FINAL_FAILURE_TIMEOUT.toString());
            }
            if (policy.getFailureException().equals(idOfPolicyBeingRemoved)) {
                policy.setFailureException(FinalResult.FINAL_FAILURE_EXCEPTION.toString());
            }
            if (policy.getFailureGuard().equals(idOfPolicyBeingRemoved)) {
                policy.setFailureGuard(FinalResult.FINAL_FAILURE_GUARD.toString());
            }
            this.controlLoopPolicy.getPolicies().set(index, policy);
        }
    }

    @Override
    public Policy resetPolicyResults(String policyId) throws BuilderException {
        Policy existingPolicy = this.findPolicy(policyId);
        if (existingPolicy == null) {
            throw new BuilderException(UNKNOWN_POLICY + policyId);
        }
        //
        // reset policy results
        //
        existingPolicy.setSuccess(FinalResult.FINAL_SUCCESS.toString());
        existingPolicy.setFailure(FinalResult.FINAL_FAILURE.toString());
        existingPolicy.setFailureRetries(FinalResult.FINAL_FAILURE_RETRIES.toString());
        existingPolicy.setFailureTimeout(FinalResult.FINAL_FAILURE_TIMEOUT.toString());
        existingPolicy.setFailureException(FinalResult.FINAL_FAILURE_EXCEPTION.toString());
        existingPolicy.setFailureGuard(FinalResult.FINAL_FAILURE_GUARD.toString());
        return new Policy(existingPolicy);
    }

    @Override
    public ControlLoopPolicyBuilder removeAllPolicies() {
        //
        // Remove all existing operational policies
        //
        this.controlLoopPolicy.getPolicies().clear();
        //
        // Revert controlLoop back to an open loop
        //
        this.controlLoopPolicy.getControlLoop().setTriggerPolicy(FinalResult.FINAL_OPENLOOP.toString());
        return this;
    }

    @Override
    public Policy addOperationsAccumulateParams(String policyId, OperationsAccumulateParams operationsAccumulateParams)
        throws BuilderException {
        Policy existingPolicy = this.findPolicy(policyId);
        if (existingPolicy == null) {
            throw new BuilderException(UNKNOWN_POLICY + policyId);
        }
        //
        // Add operationsAccumulateParams to existingPolicy
        //
        existingPolicy.setOperationsAccumulateParams(operationsAccumulateParams);
        return new Policy(existingPolicy);
    }

}
