/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019,2021 Nordix Foundation.
 *  Modifications Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 *  Modifications Copyright (C) 2021 Bell Canada. All rights reserved.
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

package org.onap.policy.models.sim.dmaap.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import org.onap.policy.models.sim.dmaap.provider.DmaapSimProvider;

/**
 * Class to provide REST endpoints for DMaaP simulator component statistics.
 */
@Produces(DmaapSimRestControllerV1.MEDIA_TYPE_APPLICATION_JSON)
public class DmaapSimRestControllerV1 extends BaseRestControllerV1 {
    public static final String MEDIA_TYPE_APPLICATION_JSON = "application/json";

    /**
     * Get a DMaaP message.
     *
     * @param topicName topic to get message from
     * @param consumerGroup consumer group that is getting the message
     * @param consumerId consumer ID that is getting the message
     * @param timeoutMs timeout for the message
     * @return the message
     */
    @GET
    @Path("/events/{topicName}/{consumerGroup}/{consumerId}")
    public Response getDmaapMessage(@PathParam("topicName") final String topicName,
                    @PathParam("consumerGroup") final String consumerGroup,
                    @PathParam("consumerId") final String consumerId,
                    @QueryParam("limit") @DefaultValue("1") final int limit,
                    @QueryParam("timeout") @DefaultValue("15000") final long timeoutMs) {

        return DmaapSimProvider.getInstance().processDmaapMessageGet(topicName, consumerGroup, consumerId, limit,
                        timeoutMs);
    }

    /**
     * Post a DMaaP message.
     *
     * @param topicName topic to get message from
     * @return the response to the post
     */
    @POST
    @Path("/events/{topicName}")
    @Consumes(value = {CambriaMessageBodyHandler.MEDIA_TYPE_APPLICATION_CAMBRIA,
        TextMessageBodyHandler.MEDIA_TYPE_TEXT_PLAIN, MEDIA_TYPE_APPLICATION_JSON})
    public Response postDmaapMessage(@PathParam("topicName") final String topicName, final Object dmaapMessage) {

        return DmaapSimProvider.getInstance().processDmaapMessagePut(topicName, dmaapMessage);
    }

    /**
     * Get the list of topics configured.
     *
     * @return the message
     */
    @GET
    @Path("/topics")
    public Response getDmaapTopics() {

        return DmaapSimProvider.getInstance().processDmaapTopicsGet();
    }
}
