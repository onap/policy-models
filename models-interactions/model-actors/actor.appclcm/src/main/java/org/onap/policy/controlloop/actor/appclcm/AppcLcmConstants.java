/*-
 * ============LICENSE_START=======================================================
 * AppcLcmActorServiceProvider
 * ================================================================================
 * Copyright (C) 2017-2020 AT&T Intellectual Property. All rights reserved.
 * Modifications copyright (c) 2018 Nokia
 * Modifications Copyright (C) 2019 Nordix Foundation.
 * Modifications Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.controlloop.actor.appclcm;

import java.util.Set;
import java.util.stream.Collectors;

public class AppcLcmConstants {

    // Strings for OPERATIONs
    public static final String OPERATION_RESTART = "Restart";
    public static final String OPERATION_REBUILD = "Rebuild";
    public static final String OPERATION_MIGRATE = "Migrate";
    public static final String OPERATION_CONFIG_MODIFY = "ConfigModify";

    public static final Set<String> OPERATION_NAMES =
                    Set.of(OPERATION_RESTART, OPERATION_REBUILD, OPERATION_MIGRATE, OPERATION_CONFIG_MODIFY);

    public static final Set<String> SUPPORTS_PAYLOAD =
                    Set.of(OPERATION_CONFIG_MODIFY).stream().map(String::toLowerCase).collect(Collectors.toSet());

    private AppcLcmConstants() {
        // do nothing
    }
}
