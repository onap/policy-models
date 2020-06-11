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

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import org.onap.policy.common.parameters.annotations.Min;
import org.onap.policy.common.parameters.annotations.NotBlank;
import org.onap.policy.common.parameters.annotations.NotNull;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpParams;

/**
 * Parameters used by Operators that, after issuing an HTTP request, must poll the target
 * server to determine the request completion status.
 */
@NotNull
@NotBlank
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public class HttpPollingParams extends HttpParams {

    /**
     * Path to use when polling for request completion.
     */
    private String pollPath;

    /**
     * Maximum number of times to poll to retrieve the response.
     */
    @Min(0)
    private int maxPolls;

    /**
     * Time, in seconds, to wait between polling.
     */
    @Min(1)
    private int pollWaitSec;
}
