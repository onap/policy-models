/*-
 * ============LICENSE_START=======================================================
 * policy-yaml
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.controlloop.compiler;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.lang3.StringUtils;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.ClassBasedEdgeFactory;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedMultigraph;
import org.onap.policy.controlloop.policy.ControlLoop;
import org.onap.policy.controlloop.policy.ControlLoopPolicy;
import org.onap.policy.controlloop.policy.FinalResult;
import org.onap.policy.controlloop.policy.Policy;
import org.onap.policy.controlloop.policy.PolicyResult;
import org.onap.policy.controlloop.policy.TargetType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;


public class ControlLoopCompiler implements Serializable {
    private static final String OPERATION_POLICY = "Operation Policy ";
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(ControlLoopCompiler.class.getName());

    /**
     * Compiles the policy from an object.
     */
    public static ControlLoopPolicy compile(ControlLoopPolicy policy,
                    ControlLoopCompilerCallback callback) throws CompilerException {
        //
        // Ensure the control loop is sane
        //
        validateControlLoop(policy.getControlLoop(), callback);
        //
        // Validate the policies
        //
        validatePolicies(policy, callback);

        return policy;
    }

    /**
     * Compiles the policy from an input stream.
     *
     * @param yamlSpecification the yaml input stream
     * @param callback method to callback during compilation
     * @return Control Loop object
     * @throws CompilerException throws any compile exception found
     */
    public static ControlLoopPolicy compile(InputStream yamlSpecification,
                    ControlLoopCompilerCallback callback) throws CompilerException {
        Yaml yaml = new Yaml(new Constructor(ControlLoopPolicy.class));
        Object obj = yaml.load(yamlSpecification);
        if (obj == null) {
            throw new CompilerException("Could not parse yaml specification.");
        }
        if (! (obj instanceof ControlLoopPolicy)) {
            throw new CompilerException("Yaml could not parse specification into required ControlLoopPolicy object");
        }
        return ControlLoopCompiler.compile((ControlLoopPolicy) obj, callback);
    }

    private static void validateControlLoop(ControlLoop controlLoop,
                    ControlLoopCompilerCallback callback) throws CompilerException {
        if (controlLoop == null && callback != null) {
            callback.onError("controlLoop cannot be null");
        }
        if (controlLoop != null) {
            if (StringUtils.isEmpty(controlLoop.getControlLoopName()) && callback != null) {
                callback.onError("Missing controlLoopName");
            }
            if ((!controlLoop.getVersion().contentEquals(ControlLoop.getCompilerVersion())) && callback != null) {
                callback.onError("Unsupported version for this compiler");
            }
            if (StringUtils.isEmpty(controlLoop.getTrigger_policy())) {
                throw new CompilerException("trigger_policy is not valid");
            }
        }
    }

    private static void validatePolicies(ControlLoopPolicy policy,
                    ControlLoopCompilerCallback callback) throws CompilerException {
        if (policy == null) {
            throw new CompilerException("policy cannot be null");
        }
        if (policy.getPolicies() == null) {
            callback.onWarning("controlLoop is an open loop.");
        } else {
            //
            // For this version we can use a directed multigraph, in the future we may not be able to
            //
            DirectedGraph<NodeWrapper, LabeledEdge> graph =
                            new DirectedMultigraph<>(new ClassBasedEdgeFactory<NodeWrapper,
                                            LabeledEdge>(LabeledEdge.class));
            //
            // Check to see if the trigger Event is for OpenLoop, we do so by
            // attempting to create a FinalResult object from it. If its a policy id, this should
            // return null.
            //
            FinalResult triggerResult = FinalResult.toResult(policy.getControlLoop().getTrigger_policy());
            TriggerNodeWrapper triggerNode;
            //
            // Did this turn into a FinalResult object?
            //
            if (triggerResult != null) {
                validateOpenLoopPolicy(policy, triggerResult, callback);
                return;
                //
            } else {
                validatePoliciesContainTriggerPolicyAndCombinedTimeoutIsOk(policy, callback);
                triggerNode = new TriggerNodeWrapper(policy.getControlLoop().getControlLoopName());
            }
            //
            // Add in the trigger node
            //
            graph.addVertex(triggerNode);
            //
            // Add in our Final Result nodes. All paths should end to these nodes.
            //
            FinalResultNodeWrapper finalSuccess = new FinalResultNodeWrapper(FinalResult.FINAL_SUCCESS);
            FinalResultNodeWrapper finalFailure = new FinalResultNodeWrapper(FinalResult.FINAL_FAILURE);
            FinalResultNodeWrapper finalFailureTimeout = new FinalResultNodeWrapper(FinalResult.FINAL_FAILURE_TIMEOUT);
            FinalResultNodeWrapper finalFailureRetries = new FinalResultNodeWrapper(FinalResult.FINAL_FAILURE_RETRIES);
            FinalResultNodeWrapper finalFailureException =
                            new FinalResultNodeWrapper(FinalResult.FINAL_FAILURE_EXCEPTION);
            FinalResultNodeWrapper finalFailureGuard = new FinalResultNodeWrapper(FinalResult.FINAL_FAILURE_GUARD);
            graph.addVertex(finalSuccess);
            graph.addVertex(finalFailure);
            graph.addVertex(finalFailureTimeout);
            graph.addVertex(finalFailureRetries);
            graph.addVertex(finalFailureException);
            graph.addVertex(finalFailureGuard);
            //
            // Work through the policies and add them in as nodes.
            //
            Map<Policy, PolicyNodeWrapper> mapNodes = addPoliciesAsNodes(policy, graph, triggerNode, callback);
            //
            // last sweep to connect remaining edges for policy results
            //
            for (Policy operPolicy : policy.getPolicies()) {
                PolicyNodeWrapper node = mapNodes.get(operPolicy);
                //
                // Just ensure this has something
                //
                if (node == null) {
                    continue;
                }
                addEdge(graph, mapNodes, operPolicy.getId(), operPolicy.getSuccess(), finalSuccess,
                                PolicyResult.SUCCESS, node);
                addEdge(graph, mapNodes, operPolicy.getId(), operPolicy.getFailure(), finalFailure,
                                PolicyResult.FAILURE, node);
                addEdge(graph, mapNodes, operPolicy.getId(), operPolicy.getFailure_timeout(), finalFailureTimeout,
                                PolicyResult.FAILURE_TIMEOUT, node);
                addEdge(graph, mapNodes, operPolicy.getId(), operPolicy.getFailure_retries(), finalFailureRetries,
                                PolicyResult.FAILURE_RETRIES, node);
                addEdge(graph, mapNodes, operPolicy.getId(), operPolicy.getFailure_exception(), finalFailureException,
                                PolicyResult.FAILURE_EXCEPTION, node);
                addEdge(graph, mapNodes, operPolicy.getId(), operPolicy.getFailure_guard(), finalFailureGuard,
                                PolicyResult.FAILURE_GUARD, node);
            }
            validateNodesAndEdges(graph, callback);
        }
    }

    private static void validateOpenLoopPolicy(ControlLoopPolicy policy, FinalResult triggerResult,
                    ControlLoopCompilerCallback callback) throws CompilerException {
        //
        // Ensure they didn't use some other FinalResult code
        //
        if (triggerResult != FinalResult.FINAL_OPENLOOP) {
            throw new CompilerException("Unexpected Final Result for trigger_policy, should only be "
        + FinalResult.FINAL_OPENLOOP.toString() + " or a valid Policy ID");
        }
        //
        // They really shouldn't have any policies attached.
        //
        if ((policy.getPolicies() != null || policy.getPolicies().isEmpty()) && callback != null ) {
            callback.onWarning("Open Loop policy contains policies. The policies will never be invoked.");
        }
    }

    private static void validatePoliciesContainTriggerPolicyAndCombinedTimeoutIsOk(ControlLoopPolicy policy,
                    ControlLoopCompilerCallback callback) throws CompilerException {
        int sum = 0;
        boolean triggerPolicyFound = false;
        for (Policy operPolicy : policy.getPolicies()) {
            sum += operPolicy.getTimeout().intValue();
            if (policy.getControlLoop().getTrigger_policy().equals(operPolicy.getId())) {
                triggerPolicyFound = true;
            }
        }
        if (policy.getControlLoop().getTimeout().intValue() < sum && callback != null) {
            callback.onError("controlLoop overall timeout is less than the sum of operational policy timeouts.");
        }

        if (!triggerPolicyFound) {
            throw new CompilerException("Unexpected value for trigger_policy, should only be "
        + FinalResult.FINAL_OPENLOOP.toString() + " or a valid Policy ID");
        }
    }

    private static Map<Policy, PolicyNodeWrapper> addPoliciesAsNodes(ControlLoopPolicy policy,
            DirectedGraph<NodeWrapper, LabeledEdge> graph, TriggerNodeWrapper triggerNode,
            ControlLoopCompilerCallback callback) {
        Map<Policy, PolicyNodeWrapper> mapNodes = new HashMap<>();
        for (Policy operPolicy : policy.getPolicies()) {
            //
            // Is it still ok to add?
            //
            if (!okToAdd(operPolicy, callback)) {
                //
                // Do not add it in
                //
                continue;
            }
            //
            // Create wrapper policy node and save it into our map so we can
            // easily retrieve it.
            //
            PolicyNodeWrapper node = new PolicyNodeWrapper(operPolicy);
            mapNodes.put(operPolicy, node);
            graph.addVertex(node);
            //
            // Is this the trigger policy?
            //
            if (operPolicy.getId().equals(policy.getControlLoop().getTrigger_policy())) {
                //
                // Yes add an edge from our trigger event node to this policy
                //
                graph.addEdge(triggerNode, node, new LabeledEdge(triggerNode, node, new TriggerEdgeWrapper("ONSET")));
            }
        }
        return mapNodes;
    }

    private static void addEdge(DirectedGraph<NodeWrapper, LabeledEdge> graph, Map<Policy, PolicyNodeWrapper> mapNodes,
                    String policyId, String connectedPolicy,
                    FinalResultNodeWrapper finalResultNodeWrapper,
                    PolicyResult policyResult, NodeWrapper node) throws CompilerException {
        FinalResult finalResult = FinalResult.toResult(finalResultNodeWrapper.getId());
        if (FinalResult.isResult(connectedPolicy, finalResult)) {
            graph.addEdge(node, finalResultNodeWrapper, new LabeledEdge(node, finalResultNodeWrapper,
                            new FinalResultEdgeWrapper(finalResult)));
        } else {
            PolicyNodeWrapper toNode = findPolicyNode(mapNodes, connectedPolicy);
            if (toNode == null) {
                throw new CompilerException(OPERATION_POLICY + policyId + " is connected to unknown policy "
            + connectedPolicy);
            } else {
                graph.addEdge(node, toNode, new LabeledEdge(node, toNode, new PolicyResultEdgeWrapper(policyResult)));
            }
        }
    }

    private static void validateNodesAndEdges(DirectedGraph<NodeWrapper, LabeledEdge> graph,
                    ControlLoopCompilerCallback callback) throws CompilerException {
        for (NodeWrapper node : graph.vertexSet()) {
            if (node instanceof TriggerNodeWrapper) {
                validateTriggerNodeWrapper(graph, node);
            } else if (node instanceof FinalResultNodeWrapper) {
                validateFinalResultNodeWrapper(graph, node);
            } else if (node instanceof PolicyNodeWrapper) {
                validatePolicyNodeWrapper(graph, node, callback);
            }
            for (LabeledEdge edge : graph.outgoingEdgesOf(node)) {
                LOGGER.info("{} invokes {} upon {}", edge.from.getId(), edge.to.getId(), edge.edge.getId());
            }
        }
    }

    private static void validateTriggerNodeWrapper(DirectedGraph<NodeWrapper, LabeledEdge> graph,
                    NodeWrapper node) throws CompilerException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.info("Trigger Node {}", node);
        }
        if (graph.inDegreeOf(node) > 0 ) {
            //
            // Really should NEVER get here unless someone messed up the code above.
            //
            throw new CompilerException("No inputs to event trigger");
        }
        //
        // Should always be 1, except in the future we may support multiple events
        //
        if (graph.outDegreeOf(node) > 1) {
            throw new CompilerException("The event trigger should only go to ONE node");
        }
    }

    private static void validateFinalResultNodeWrapper(DirectedGraph<NodeWrapper, LabeledEdge> graph,
                    NodeWrapper node) throws CompilerException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.info("FinalResult Node {}", node);
        }
        //
        // FinalResult nodes should NEVER have an out edge
        //
        if (graph.outDegreeOf(node) > 0) {
            throw new CompilerException("FinalResult nodes should never have any out edges.");
        }
    }

    private static void validatePolicyNodeWrapper(DirectedGraph<NodeWrapper, LabeledEdge> graph,
                    NodeWrapper node, ControlLoopCompilerCallback callback) throws CompilerException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.info("Policy Node {}", node);
        }
        //
        // All Policy Nodes should have the 5 out degrees defined.
        //
        if (graph.outDegreeOf(node) != 6) {
            throw new CompilerException("Policy node should ALWAYS have 6 out degrees.");
        }
        //
        // All Policy Nodes should have at least 1 in degrees
        //
        if (graph.inDegreeOf(node) == 0 && callback != null) {
            callback.onWarning("Policy " + node.getId() + " is not reachable.");
        }
    }

    private static boolean okToAdd(Policy operPolicy, ControlLoopCompilerCallback callback) {
        boolean isOk = isPolicyIdOk(operPolicy, callback);
        if (! isActorOk(operPolicy, callback)) {
            isOk = false;
        }
        if (! isRecipeOk(operPolicy, callback)) {
            isOk = false;
        }
        if (! isTargetOk(operPolicy, callback) ) {
            isOk = false;
        }
        if (! arePolicyResultsOk(operPolicy, callback) ) {
            isOk = false;
        }
        return isOk;
    }

    private static boolean isPolicyIdOk(Policy operPolicy, ControlLoopCompilerCallback callback) {
        boolean isOk = true;
        if (operPolicy.getId() == null || operPolicy.getId().length() < 1) {
            if (callback != null) {
                callback.onError("Operational Policy has an bad ID");
            }
            isOk = false;
        } else {
            //
            // Check if they decided to make the ID a result object
            //
            if (PolicyResult.toResult(operPolicy.getId()) != null) {
                if (callback != null) {
                    callback.onError("Policy id is set to a PolicyResult " + operPolicy.getId());
                }
                isOk = false;
            }
            if (FinalResult.toResult(operPolicy.getId()) != null) {
                if (callback != null) {
                    callback.onError("Policy id is set to a FinalResult " + operPolicy.getId());
                }
                isOk = false;
            }
        }
        return isOk;
    }

    private static boolean isActorOk(Policy operPolicy, ControlLoopCompilerCallback callback) {
        boolean isOk = true;
        if (operPolicy.getActor() == null) {
            if (callback != null) {
                callback.onError("Policy actor is null");
            }
            isOk = false;
        }
        //
        // Construct a list for all valid actors
        //
        ImmutableList<String> actors = ImmutableList.of("APPC", "SDNC", "SDNR", "SO", "VFC");
        //
        if (operPolicy.getActor() != null && (!actors.contains(operPolicy.getActor())) ) {
            if (callback != null) {
                callback.onError("Policy actor is invalid");
            }
            isOk = false;
        }
        return isOk;
    }

    private static boolean isRecipeOk(Policy operPolicy, ControlLoopCompilerCallback callback) {
        boolean isOk = true;
        if (operPolicy.getRecipe() == null) {
            if (callback != null) {
                callback.onError("Policy recipe is null");
            }
            isOk = false;
        }
        //
        // NOTE: We need a way to find the acceptable recipe values (either Enum or a database that has these)
        //
        ImmutableMap<String, List<String>> recipes = new ImmutableMap.Builder<String, List<String>>()
                .put("APPC", ImmutableList.of("Restart", "Rebuild", "Migrate", "ModifyConfig"))
                .put("SDNC", ImmutableList.of("Reroute"))
                .put("SDNR", ImmutableList.of("ModifyConfig"))
                .put("SO", ImmutableList.of("VF Module Create", "VF Module Delete"))
                .put("VFC", ImmutableList.of("Restart"))
                .build();
        //
        if (operPolicy.getRecipe() != null
                        && (!recipes.getOrDefault(operPolicy.getActor(),
                                        Collections.emptyList()).contains(operPolicy.getRecipe()))) {
            if (callback != null) {
                callback.onError("Policy recipe is invalid");
            }
            isOk = false;
        }
        return isOk;
    }

    private static boolean isTargetOk(Policy operPolicy, ControlLoopCompilerCallback callback) {
        boolean isOk = true;
        if (operPolicy.getTarget() == null) {
            if (callback != null) {
                callback.onError("Policy target is null");
            }
            isOk = false;
        }
        if (operPolicy.getTarget() != null
                        && operPolicy.getTarget().getType() != TargetType.VM
                        && operPolicy.getTarget().getType() != TargetType.VFC
                        && operPolicy.getTarget().getType() != TargetType.PNF) {
            if (callback != null) {
                callback.onError("Policy target is invalid");
            }
            isOk = false;
        }
        return isOk;
    }

    private static boolean arePolicyResultsOk(Policy operPolicy, ControlLoopCompilerCallback callback) {
        //
        // Check that policy results are connected to either default final * or another policy
        //
        boolean isOk = isSuccessPolicyResultOk(operPolicy, callback);
        if (! isFailurePolicyResultOk(operPolicy, callback) ) {
            isOk = false;
        }
        if (! isFailureRetriesPolicyResultOk(operPolicy, callback) ) {
            isOk = false;
        }
        if (! isFailureTimeoutPolicyResultOk(operPolicy, callback) ) {
            isOk = false;
        }
        if (! isFailureExceptionPolicyResultOk(operPolicy, callback) ) {
            isOk = false;
        }
        if (! isFailureGuardPolicyResultOk(operPolicy, callback) ) {
            isOk = false;
        }
        return isOk;
    }

    private static boolean isSuccessPolicyResultOk(Policy operPolicy, ControlLoopCompilerCallback callback) {
        boolean isOk = true;
        if (FinalResult.toResult(operPolicy.getSuccess()) != null
                        && !operPolicy.getSuccess().equals(FinalResult.FINAL_SUCCESS.toString())) {
            if (callback != null) {
                callback.onError("Policy success is neither another policy nor FINAL_SUCCESS");
            }
            isOk = false;
        }
        return isOk;
    }

    private static boolean isFailurePolicyResultOk(Policy operPolicy, ControlLoopCompilerCallback callback) {
        boolean isOk = true;
        if (FinalResult.toResult(operPolicy.getFailure()) != null
                        && !operPolicy.getFailure().equals(FinalResult.FINAL_FAILURE.toString())) {
            if (callback != null) {
                callback.onError("Policy failure is neither another policy nor FINAL_FAILURE");
            }
            isOk = false;
        }
        return isOk;
    }

    private static boolean isFailureRetriesPolicyResultOk(Policy operPolicy, ControlLoopCompilerCallback callback) {
        boolean isOk = true;
        if (FinalResult.toResult(operPolicy.getFailure_retries()) != null
                        && !operPolicy.getFailure_retries().equals(FinalResult.FINAL_FAILURE_RETRIES.toString())) {
            if (callback != null) {
                callback.onError("Policy failure retries is neither another policy nor FINAL_FAILURE_RETRIES");
            }
            isOk = false;
        }
        return isOk;
    }

    private static boolean isFailureTimeoutPolicyResultOk(Policy operPolicy, ControlLoopCompilerCallback callback) {
        boolean isOk = true;
        if (FinalResult.toResult(operPolicy.getFailure_timeout()) != null
                        && !operPolicy.getFailure_timeout().equals(FinalResult.FINAL_FAILURE_TIMEOUT.toString())) {
            if (callback != null) {
                callback.onError("Policy failure timeout is neither another policy nor FINAL_FAILURE_TIMEOUT");
            }
            isOk = false;
        }
        return isOk;
    }

    private static boolean isFailureExceptionPolicyResultOk(Policy operPolicy, ControlLoopCompilerCallback callback) {
        boolean isOk = true;
        if (FinalResult.toResult(operPolicy.getFailure_exception()) != null
                        && !operPolicy.getFailure_exception().equals(FinalResult.FINAL_FAILURE_EXCEPTION.toString())) {
            if (callback != null) {
                callback.onError("Policy failure exception is neither another policy nor FINAL_FAILURE_EXCEPTION");
            }
            isOk = false;
        }
        return isOk;
    }

    private static boolean isFailureGuardPolicyResultOk(Policy operPolicy, ControlLoopCompilerCallback callback) {
        boolean isOk = true;
        if (FinalResult.toResult(operPolicy.getFailure_guard()) != null
                        && !operPolicy.getFailure_guard().equals(FinalResult.FINAL_FAILURE_GUARD.toString())) {
            if (callback != null) {
                callback.onError("Policy failure guard is neither another policy nor FINAL_FAILURE_GUARD");
            }
            isOk = false;
        }
        return isOk;
    }

    private static PolicyNodeWrapper findPolicyNode(Map<Policy, PolicyNodeWrapper> mapNodes, String id) {
        for (Entry<Policy, PolicyNodeWrapper> entry : mapNodes.entrySet()) {
            if (entry.getKey().getId().equals(id)) {
                return entry.getValue();
            }
        }
        return null;
    }

    @FunctionalInterface
    private interface NodeWrapper extends Serializable {
        public String   getId();
    }

    private static class TriggerNodeWrapper implements NodeWrapper {
        private static final long serialVersionUID = -187644087811478349L;
        private String closedLoopControlName;

        public TriggerNodeWrapper(String closedLoopControlName) {
            this.closedLoopControlName = closedLoopControlName;
        }

        @Override
        public String toString() {
            return "TriggerNodeWrapper [closedLoopControlName=" + closedLoopControlName + "]";
        }

        @Override
        public String getId() {
            return closedLoopControlName;
        }

    }

    private static class FinalResultNodeWrapper implements NodeWrapper {
        private static final long serialVersionUID = 8540008796302474613L;
        private FinalResult result;

        public FinalResultNodeWrapper(FinalResult result) {
            this.result = result;
        }

        @Override
        public String toString() {
            return "FinalResultNodeWrapper [result=" + result + "]";
        }

        @Override
        public String getId() {
            return result.toString();
        }
    }

    private static class PolicyNodeWrapper implements NodeWrapper {
        private static final long serialVersionUID = 8170162175653823082L;
        private transient Policy policy;

        public PolicyNodeWrapper(Policy operPolicy) {
            this.policy = operPolicy;
        }

        @Override
        public String toString() {
            return "PolicyNodeWrapper [policy=" + policy + "]";
        }

        @Override
        public String getId() {
            return policy.getId();
        }
    }

    @FunctionalInterface
    private interface EdgeWrapper extends Serializable {
        public String getId();

    }

    private static class TriggerEdgeWrapper implements EdgeWrapper {
        private static final long serialVersionUID = 2678151552623278863L;
        private String trigger;

        public TriggerEdgeWrapper(String trigger) {
            this.trigger = trigger;
        }

        @Override
        public String getId() {
            return trigger;
        }

        @Override
        public String toString() {
            return "TriggerEdgeWrapper [trigger=" + trigger + "]";
        }

    }

    private static class PolicyResultEdgeWrapper implements EdgeWrapper {
        private static final long serialVersionUID = 6078569477021558310L;
        private PolicyResult policyResult;

        public PolicyResultEdgeWrapper(PolicyResult policyResult) {
            super();
            this.policyResult = policyResult;
        }

        @Override
        public String toString() {
            return "PolicyResultEdgeWrapper [policyResult=" + policyResult + "]";
        }

        @Override
        public String getId() {
            return policyResult.toString();
        }


    }

    private static class FinalResultEdgeWrapper implements EdgeWrapper {
        private static final long serialVersionUID = -1486381946896779840L;
        private FinalResult finalResult;

        public FinalResultEdgeWrapper(FinalResult result) {
            this.finalResult = result;
        }

        @Override
        public String toString() {
            return "FinalResultEdgeWrapper [finalResult=" + finalResult + "]";
        }

        @Override
        public String getId() {
            return finalResult.toString();
        }
    }


    private static class LabeledEdge extends DefaultEdge {
        private static final long serialVersionUID = 579384429573385524L;

        private NodeWrapper from;
        private NodeWrapper to;
        private EdgeWrapper edge;

        public LabeledEdge(NodeWrapper from, NodeWrapper to, EdgeWrapper edge) {
            this.from = from;
            this.to = to;
            this.edge = edge;
        }

        @SuppressWarnings("unused")
        public NodeWrapper from() {
            return from;
        }

        @SuppressWarnings("unused")
        public NodeWrapper to() {
            return to;
        }

        @SuppressWarnings("unused")
        public EdgeWrapper edge() {
            return edge;
        }

        @Override
        public String toString() {
            return "LabeledEdge [from=" + from + ", to=" + to + ", edge=" + edge + "]";
        }
    }

}
