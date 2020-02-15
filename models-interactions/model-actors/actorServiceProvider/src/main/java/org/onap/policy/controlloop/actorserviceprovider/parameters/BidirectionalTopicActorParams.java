/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.controlloop.actorserviceprovider.parameters;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.onap.policy.common.parameters.annotations.Min;

/**
 * Parameters used by Actors whose Operators use bidirectional topic.
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class BidirectionalTopicActorParams extends CommonActorParams {

    /*
     * Optional, default values that are used if missing from the operation-specific
     * parameters.
     */

    /**
     * Sink topic name to which requests should be published.
     */
    private String sinkTopic;

    /**
     * Source topic name, from which to read responses.
     */
    private String sourceTopic;

    /**
     * Amount of time, in seconds, to wait for the HTTP request to complete. The default
     * is 90 seconds.
     */
    @Min(1)
    private int timeoutSec = 90;
}
