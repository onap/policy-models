/*-
 * ============LICENSE_START=======================================================
 * appclcm
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019, 2024 Nordix Foundation.
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

package org.onap.policy.appclcm.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.Test;

class SerializationTest {

    @Test
    void test() {
        String nameString = "Dorothy";
        String jsonName = Serialization.gsonPretty.toJson(nameString, String.class);
        assertEquals("\"Dorothy\"", jsonName);
        String jsonInOutName = Serialization.gsonPretty.fromJson(jsonName, String.class);
        assertEquals("Dorothy", jsonInOutName);

        Instant instant0 = Instant.ofEpochMilli(1516127215000L);
        String instantString0 = Serialization.gsonPretty.toJson(instant0, Instant.class);
        assertEquals("\"2018-01-16T18:26:55Z\"", instantString0);
        Instant outInstant0 = Serialization.gsonPretty.fromJson(instantString0, Instant.class);
        assertEquals(instant0, outInstant0);

        Instant instant1 = Instant.ofEpochMilli(1516127215000L);
        String instantString1 = Serialization.gsonJunit.toJson(instant1, Instant.class);
        assertEquals("1516127215000", instantString1);
        Instant outInstant1 = Serialization.gsonJunit.fromJson(instantString1, Instant.class);
        assertEquals(instant1, outInstant1);

        ZonedDateTime zdt = ZonedDateTime.ofInstant(instant0, ZoneId.of("UTC"));
        String zdtString = Serialization.gsonPretty.toJson(zdt, ZonedDateTime.class);
        assertEquals("{\n  \"dateTime\": {\n    \"date\":", zdtString.substring(0, 29));
    }
}
