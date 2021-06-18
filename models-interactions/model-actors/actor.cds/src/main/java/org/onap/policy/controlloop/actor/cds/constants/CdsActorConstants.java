/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2019 Bell Canada. All rights reserved.
 * Modifications Copyright (C) 2021 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.controlloop.actor.cds.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CdsActorConstants {
    public static final String CDS_ACTOR = "CDS";

    // CDS Status
    public static final String SUCCESS = "Success";
    public static final String FAILED = "Failed";
    public static final String PROCESSING = "Processing";
    public static final String TIMED_OUT = "Timed out";
    public static final String INTERRUPTED = "Thread interrupted";
    public static final String ERROR = "Error";

    // CDS blueprint archive parameters
    public static final String KEY_CBA_NAME = "artifact_name";
    public static final String KEY_CBA_VERSION = "artifact_version";
    public static final String KEY_POLICY_PAYLOAD_DATA = "data";
    public static final String KEY_RESOLUTION_KEY = "resolution-key";
    public static final String CDS_REQUEST_SUFFIX = "-request";
    public static final String CDS_REQUEST_PROPERTIES_SUFFIX = "-properties";
    public static final String ORIGINATOR_ID = "POLICY";
    // Temporarily set to synchronous mode to support current rules, since callbacks aren't supported yet
    public static final String CDS_MODE = "sync";
}
