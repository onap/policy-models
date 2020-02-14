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

package org.onap.policy.controlloop.actorserviceprovider.topic;

/**
 * Manages bidirectional topics.
 */
@FunctionalInterface
public interface BidirectionalTopicManager {

    /**
     * Gets the topic handler for the given parameters, creating one if it does not exist.
     *
     * @param sinkTopic sink topic name
     * @param sourceTopic source topic name
     * @return the topic handler associated with the given sink and source topic names
     */
    BidirectionalTopicHandler getTopicHandler(String sinkTopic, String sourceTopic);
}
