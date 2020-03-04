/*-
 * ============LICENSE_START=======================================================
 * APPCActorServiceProvider
 * ================================================================================
 * Copyright (C) 2017-2020 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.controlloop.actor.appc;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.onap.policy.appc.CommonHeader;
import org.onap.policy.appc.Request;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.controlloop.ControlLoopOperation;
import org.onap.policy.controlloop.VirtualControlLoopEvent;
import org.onap.policy.controlloop.actorserviceprovider.impl.BidirectionalTopicActor;
import org.onap.policy.controlloop.actorserviceprovider.impl.BidirectionalTopicOperator;
import org.onap.policy.controlloop.actorserviceprovider.parameters.BidirectionalTopicActorParams;
import org.onap.policy.controlloop.policy.Policy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AppcActorServiceProvider extends BidirectionalTopicActor<BidirectionalTopicActorParams> {
    public static final String NAME = "APPC";

    private static final Logger logger = LoggerFactory.getLogger(AppcActorServiceProvider.class);

    // Strings for recipes
    private static final String OPERATION_RESTART = "Restart";
    private static final String OPERATION_REBUILD = "Rebuild";
    private static final String OPERATION_MIGRATE = "Migrate";
    private static final String OPERATION_MODIFY = "ModifyConfig";

    protected static final ImmutableList<String> OPERATION_NAMES =
                    ImmutableList.of(OPERATION_RESTART, OPERATION_REBUILD, OPERATION_MIGRATE, OPERATION_MODIFY);

    // TODO old code: remove lines down to **HERE**

    private static final StandardCoder coder = new StandardCoder();

    // Strings for targets
    private static final String TARGET_VM = "VM";
    private static final String TARGET_VNF = "VNF";
    private static final ImmutableMap<String, List<String>> targets =
                    new ImmutableMap.Builder<String, List<String>>().put(OPERATION_RESTART, ImmutableList.of(TARGET_VM))
                                    .put(OPERATION_REBUILD, ImmutableList.of(TARGET_VM))
                                    .put(OPERATION_MIGRATE, ImmutableList.of(TARGET_VM))
                                    .put(OPERATION_MODIFY, ImmutableList.of(TARGET_VNF)).build();
    private static final ImmutableMap<String, List<String>> payloads = new ImmutableMap.Builder<String, List<String>>()
                    .put(OPERATION_MODIFY, ImmutableList.of("generic-vnf.vnf-id")).build();

    // **HERE**

    /**
     * Constructs the object.
     */
    public AppcActorServiceProvider() {
        super(NAME, BidirectionalTopicActorParams.class);

        for (String opname : OPERATION_NAMES) {
            addOperator(new BidirectionalTopicOperator(NAME, opname, this, AppcOperation.SELECTOR_KEYS,
                            AppcOperation::new));
        }
    }

    /**
     * This actor should take precedence over APPC-LCM.
     */
    @Override
    public int getSequenceNumber() {
        return -1;
    }


    // TODO old code: remove lines down to **HERE**

    @Override
    public String actor() {
        return NAME;
    }

    @Override
    public List<String> recipes() {
        return ImmutableList.copyOf(OPERATION_NAMES);
    }

    @Override
    public List<String> recipeTargets(String recipe) {
        return ImmutableList.copyOf(targets.getOrDefault(recipe, Collections.emptyList()));
    }

    @Override
    public List<String> recipePayloads(String recipe) {
        return ImmutableList.copyOf(payloads.getOrDefault(recipe, Collections.emptyList()));
    }

    /**
     * Constructs an APPC request conforming to the legacy API. The legacy API will be
     * deprecated in future releases as all legacy functionality is moved into the LCM
     * API.
     *
     * @param onset the event that is reporting the alert for policy to perform an action
     * @param operation the control loop operation specifying the actor, operation,
     *        target, etc.
     * @param policy the policy the was specified from the yaml generated by CLAMP or
     *        through the Policy GUI/API
     * @return an APPC request conforming to the legacy API
     */
    public static Request constructRequest(VirtualControlLoopEvent onset, ControlLoopOperation operation, Policy policy,
                    String targetVnf) {
        /*
         * Construct an APPC request
         */
        Request request = new Request();
        request.setCommonHeader(new CommonHeader());
        request.getCommonHeader().setRequestId(onset.getRequestId());
        request.getCommonHeader().setSubRequestId(operation.getSubRequestId());
        request.setAction(policy.getRecipe().substring(0, 1).toUpperCase() + policy.getRecipe().substring(1));

        // convert policy payload strings to objects
        if (policy.getPayload() == null) {
            logger.info("no APPC payload specified for policy {}", policy.getName());
        } else {
            convertPayload(policy.getPayload(), request.getPayload());
        }

        // add/replace specific values
        request.getPayload().put("generic-vnf.vnf-id", targetVnf);

        /*
         * Return the request
         */

        return request;
    }

    /**
     * Converts a payload. The original value is assumed to be a JSON string, which is
     * decoded into an object.
     *
     * @param source source from which to get the values
     * @param target where to place the decoded values
     */
    private static void convertPayload(Map<String, String> source, Map<String, Object> target) {
        for (Entry<String, String> ent : source.entrySet()) {
            try {
                target.put(ent.getKey(), coder.decode(ent.getValue(), Object.class));

            } catch (CoderException e) {
                logger.warn("cannot decode JSON value {}: {}", ent.getKey(), ent.getValue(), e);
            }
        }
    }

    // **HERE**
}
