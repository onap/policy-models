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

package org.onap.policy.controlloop.actor.appclcm;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AppcLcmConstants {

    // Strings for OPERATIONs
    public static final String OPERATION_RESTART = "Restart";
    public static final String OPERATION_REBUILD = "Rebuild";
    public static final String OPERATION_MIGRATE = "Migrate";
    public static final String OPERATION_CONFIG_MODIFY = "ConfigModify";

    public static final Set<String> OPERATION_NAMES =
                    Set.of(OPERATION_RESTART, OPERATION_REBUILD, OPERATION_MIGRATE, OPERATION_CONFIG_MODIFY);

    // operations from legacy APPC
    public static final String LEGACY_MODIFY_CONFIG = "ModifyConfig";

    public static final Set<String> LEGACY_NAMES =
                    Set.of(LEGACY_MODIFY_CONFIG);

    public static final Set<String> COMBINED_OPERATION_NAMES;

    static {
        Set<String> set = new HashSet<>(OPERATION_NAMES);
        set.addAll(LEGACY_NAMES);
        COMBINED_OPERATION_NAMES = Collections.unmodifiableSet(set);
    }

    protected static final Set<String> SUPPORTS_PAYLOAD =
                    Set.of(OPERATION_CONFIG_MODIFY).stream().map(String::toLowerCase).collect(Collectors.toSet());
}
