/*-
 * ============LICENSE_START=======================================================
 * appc
 * ================================================================================
 * Copyright (C) 2017-2019, 2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019 Nordix Foundation.
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

package org.onap.policy.appc.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import org.onap.policy.common.gson.InstantAsMillisTypeAdapter;
import org.onap.policy.common.gson.ZonedDateTimeTypeAdapter;

public final class Serialization {
    public static final DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSxxx");

    public static final Gson gsonPretty = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting()
            .registerTypeAdapter(ZonedDateTime.class, new ZonedDateTimeTypeAdapter(format))
            .registerTypeAdapter(Instant.class, new InstantAsMillisTypeAdapter())
            // .registerTypeAdapter(CommonHeader1607.class, new gsonCommonHeaderInstance())
            // .registerTypeAdapter(ResponseStatus1607.class, new gsonResponseStatus())
            .create();

    private Serialization() {
        // Private constructor to prevent subclassing
    }
}
