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

package org.onap.policy.models.sim.dmaap.rest;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.onap.policy.models.sim.dmaap.provider.DmaapSimProvider;

/**
 * Class to provide REST endpoints for DMaaP simulator component statistics.
 */
@Path("/events")
public class DmaapSimRestControllerV1 extends BaseRestControllerV1 {

    /**
     * Get a DMaaP message.
     *
     * @param topicName topic to get message from
     * @param consumerGroup consumer group that is getting the message
     * @param consumerId consumer ID that is getting the message
     * @param timeout timeout for the message
     * @return the message
     */
    @GET
    @Path("{topicName}/{consumerGroup}/{consumerId}")
    // @formatter:off
    @ApiOperation(
            value = "Get a DMaaP event on a topic",
            notes = "Returns an event on a DMaaP topic",
            response = Object.class,
            authorizations =
                @Authorization(value = AUTHORIZATION_TYPE)
        )
    @ApiResponses(
            value = {
                @ApiResponse(
                        code = AUTHENTICATION_ERROR_CODE,
                        message = AUTHENTICATION_ERROR_MESSAGE),
                @ApiResponse(
                        code = AUTHORIZATION_ERROR_CODE,
                        message = AUTHORIZATION_ERROR_MESSAGE),
                @ApiResponse(
                        code = SERVER_ERROR_CODE,
                        message = SERVER_ERROR_MESSAGE)
            }
        )
    // @formatter:on
    public Response getDmaaapMessage(@PathParam("topicName") final String topicName,
            @PathParam("consumerGroup") final String consumerGroup, @PathParam("consumerId") final String consumerId,
            @QueryParam("timeout") final int timeout) {

        return new DmaapSimProvider().processDmaapMessageGet(topicName, consumerGroup, consumerId, timeout);
    }

    /**
     * Post a DMaaP message.
     *
     * @param topicName topic to get message from415
     * @return the response to the post
     */
    @POST
    @Path("{topicName}")
    // @formatter:off
    @ApiOperation(
            value = "Post a DMaaP event on a topic",
            notes = "Returns an event on a DMaaP topic",
            response = Response.class,
            authorizations =
                @Authorization(value = AUTHORIZATION_TYPE)
        )
    @ApiResponses(
            value = {
                @ApiResponse(
                        code = AUTHENTICATION_ERROR_CODE,
                        message = AUTHENTICATION_ERROR_MESSAGE),
                @ApiResponse(
                        code = AUTHORIZATION_ERROR_CODE,
                        message = AUTHORIZATION_ERROR_MESSAGE),
                @ApiResponse(
                        code = SERVER_ERROR_CODE,
                        message = SERVER_ERROR_MESSAGE)
            }
        )
    // @formatter:on
    public Response postDmaaapMessage(@PathParam("topicName") final String topicName, final Object dmaapMessage) {

        return new DmaapSimProvider().processDmaapMessagePut(topicName, dmaapMessage);
    }
}
