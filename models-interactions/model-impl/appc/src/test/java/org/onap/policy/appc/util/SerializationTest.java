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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParseException;
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

        Instant instant = Instant.ofEpochMilli(1516127215000L);
        String instantString = Serialization.gsonPretty.toJson(instant, Instant.class);
        assertEquals("1516127215000", instantString);
        Instant outInstant = Serialization.gsonPretty.fromJson(instantString, Instant.class);
        assertEquals(instant, outInstant);

        ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, ZoneId.of("UTC"));
        String zdtString = Serialization.gsonPretty.toJson(zdt, ZonedDateTime.class);
        assertEquals("\"2018-01-16 18:26:55.000000+00:00\"", zdtString);
        ZonedDateTime outZdt = Serialization.gsonPretty.fromJson(zdtString, ZonedDateTime.class);
        assertEquals(zdt.getDayOfWeek(), outZdt.getDayOfWeek());

        assertThatThrownBy(() -> Serialization.gsonPretty.fromJson("oz time is weird", ZonedDateTime.class))
                        .isInstanceOf(JsonParseException.class);
    }
}
