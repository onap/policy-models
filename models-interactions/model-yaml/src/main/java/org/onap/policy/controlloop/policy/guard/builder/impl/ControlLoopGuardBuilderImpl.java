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

package org.onap.policy.controlloop.policy.guard.builder.impl;

import java.util.LinkedList;

import org.onap.policy.controlloop.compiler.CompilerException;
import org.onap.policy.controlloop.compiler.ControlLoopCompilerCallback;
import org.onap.policy.controlloop.guard.compiler.ControlLoopGuardCompiler;
import org.onap.policy.controlloop.policy.builder.BuilderException;
import org.onap.policy.controlloop.policy.builder.MessageLevel;
import org.onap.policy.controlloop.policy.builder.Results;
import org.onap.policy.controlloop.policy.builder.impl.MessageImpl;
import org.onap.policy.controlloop.policy.builder.impl.ResultsImpl;
import org.onap.policy.controlloop.policy.guard.Constraint;
import org.onap.policy.controlloop.policy.guard.ControlLoopGuard;
import org.onap.policy.controlloop.policy.guard.Guard;
import org.onap.policy.controlloop.policy.guard.GuardPolicy;
import org.onap.policy.controlloop.policy.guard.builder.ControlLoopGuardBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.Yaml;

public class ControlLoopGuardBuilderImpl implements ControlLoopGuardBuilder {
    private static final String NO_EXISTING_GUARD_POLICY_MATCHING_THE_ID = "No existing guard policy matching the id: ";
    private static final String THE_ID_OF_TARGET_GUARD_POLICY_MUST_NOT_BE_NULL = 
                    "The id of target guard policy must not be null";
    private static Logger logger = LoggerFactory.getLogger(ControlLoopGuardBuilderImpl.class.getName());
    private ControlLoopGuard clGuard;
    
    public ControlLoopGuardBuilderImpl(Guard guard) {
        clGuard = new ControlLoopGuard();
        clGuard.setGuard(guard);
    }
    
    @Override
    public ControlLoopGuardBuilder addGuardPolicy(GuardPolicy... policies) throws BuilderException {
        if (policies == null) {
            throw new BuilderException("GuardPolicy must not be null");
        }
        for (GuardPolicy policy : policies) {
            if (!policy.isValid()) {
                throw new BuilderException("Invalid guard policy - some required fields are missing");
            }
            if (clGuard.getGuards() == null) {
                clGuard.setGuards(new LinkedList<>());
            }
            clGuard.getGuards().add(policy);
        }
        return this;
    }

    @Override
    public ControlLoopGuardBuilder removeGuardPolicy(GuardPolicy... policies) throws BuilderException {
        if (policies == null) {
            throw new BuilderException("GuardPolicy must not be null");
        }
        if (clGuard.getGuards() == null) {
            throw new BuilderException("No existing guard policies to remove");
        }
        for (GuardPolicy policy : policies) {
            if (!policy.isValid()) {
                throw new BuilderException("Invalid guard policy - some required fields are missing");
            }
            boolean removed = clGuard.getGuards().remove(policy);
            if (!removed) {
                throw new BuilderException("Unknown guard policy: " + policy.getName());
            }
        }
        return this;
    }

    @Override
    public ControlLoopGuardBuilder removeAllGuardPolicies() throws BuilderException {
        clGuard.getGuards().clear();
        return this;
    }

    @Override
    public ControlLoopGuardBuilder addLimitConstraint(String id, Constraint... constraints) throws BuilderException {
        if (id == null) {
            throw new BuilderException(THE_ID_OF_TARGET_GUARD_POLICY_MUST_NOT_BE_NULL);
        }
        if (constraints == null) {
            throw new BuilderException("Constraint much not be null");
        }
        if (!addLimitConstraints(id,constraints)) {
            throw new BuilderException(NO_EXISTING_GUARD_POLICY_MATCHING_THE_ID + id);
        }
        return this;
    }

    private boolean addLimitConstraints(String id, Constraint... constraints) throws BuilderException {
        boolean exist = false;
        for (GuardPolicy policy: clGuard.getGuards()) {
            //
            // We could have only one guard policy matching the id
            //
            if (policy.getId().equals(id)) {
                exist = true;
                for (Constraint cons: constraints) {
                    if (!cons.isValid()) {
                        throw new BuilderException("Invalid guard constraint - some required fields are missing");
                    }
                    if (policy.getLimit_constraints() == null) {
                        policy.setLimit_constraints(new LinkedList<>());
                    }
                    policy.getLimit_constraints().add(cons);
                }
                break;
            }
        }
        return exist;
    }

    @Override
    public ControlLoopGuardBuilder removeLimitConstraint(String id, Constraint... constraints) throws BuilderException {
        if (id == null) {
            throw new BuilderException(THE_ID_OF_TARGET_GUARD_POLICY_MUST_NOT_BE_NULL);
        }
        if (constraints == null) {
            throw new BuilderException("Constraint much not be null");
        }
        if (!removeConstraints(id, constraints)) {
            throw new BuilderException(NO_EXISTING_GUARD_POLICY_MATCHING_THE_ID + id);
        }
        return this;
    }

    private boolean removeConstraints(String id, Constraint... constraints) throws BuilderException {
        boolean exist = false;
        for (GuardPolicy policy: clGuard.getGuards()) {
            //
            // We could have only one guard policy matching the id
            //
            if (policy.getId().equals(id)) {
                exist = true;
                for (Constraint cons: constraints) {
                    if (!cons.isValid()) {
                        throw new BuilderException("Invalid guard constraint - some required fields are missing");
                    }
                    boolean removed = policy.getLimit_constraints().remove(cons);
                    if (!removed) {
                        throw new BuilderException("Unknown guard constraint: " + cons);
                    }
                }
                break;
            }
        }
        return exist;
    }

    @Override
    public ControlLoopGuardBuilder removeAllLimitConstraints(String id) throws BuilderException {
        if (clGuard.getGuards() == null || clGuard.getGuards().isEmpty()) {
            throw new BuilderException("No guard policies exist");
        } 
        if (id == null) {
            throw new BuilderException(THE_ID_OF_TARGET_GUARD_POLICY_MUST_NOT_BE_NULL);
        }
        boolean exist = false;
        for (GuardPolicy policy: clGuard.getGuards()) {
            if (policy.getId().equals(id)) {
                exist = true;
                policy.getLimit_constraints().clear();
            }
        }
        if (!exist) {
            throw new BuilderException(NO_EXISTING_GUARD_POLICY_MATCHING_THE_ID + id);
        }
        return this;
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
    public ControlLoopGuard getControlLoopGuard() {
        return new ControlLoopGuard(this.clGuard);
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
        String dumpedYaml = yaml.dump(clGuard);
        //
        // This is our callback class for our compiler
        //
        BuilderCompilerCallback callback = new BuilderCompilerCallback();
        //
        // Compile it
        //
        try {
            ControlLoopGuardCompiler.compile(clGuard, callback);
        } catch (CompilerException e) {
            logger.error("Build specification threw ", e);
            callback.results.addMessage(new MessageImpl(e.getMessage(), MessageLevel.EXCEPTION));
        }
        //
        // Save the spec
        //
        callback.results.setSpecification(dumpedYaml);
        return callback.results;
    }

}
