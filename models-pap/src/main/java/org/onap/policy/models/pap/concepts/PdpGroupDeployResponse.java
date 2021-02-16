/*
 * ============LICENSE_START=======================================================
 * ONAP Policy Models
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2021 Bell Canada. All rights reserved.
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

package org.onap.policy.models.pap.concepts;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Response to PDP Group DEPLOY REST API.
 */
@Getter
@Setter
@ToString(callSuper = true)
public class PdpGroupDeployResponse extends SimpleResponse {

    /**
     * Response message for deployment.
     */
    private String message;

    /**
     * URI to fetch the deployment status.
     */
    private String uri;

    /**
     * Constructs the object.
     *
     * @param message the message
     * @param uri the uri to get actual deployment status
     */
    public PdpGroupDeployResponse(String message, String uri) {
        this.message = message;
        this.uri = uri;
    }
}
