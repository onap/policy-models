/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020-2021 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.controlloop.actor.xacml;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpActorParams;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class XacmlActorParams extends HttpActorParams {
    public static final String DEFAULT_ACTION = "guard";

    /*
     * Optional, default values that are used if missing from the operation-specific
     * parameters.
     */

    private String onapName;
    private String onapComponent;
    private String onapInstance;
    private String action = DEFAULT_ACTION;

    /**
     * {@code True} if xacml operations are disabled.
     */
    private boolean disabled = false;
}
