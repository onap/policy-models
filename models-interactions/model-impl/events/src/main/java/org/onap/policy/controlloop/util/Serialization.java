/*-
 * ============LICENSE_START=======================================================
 * controlloop
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.controlloop.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.onap.policy.controlloop.ControlLoopNotificationType;
import org.onap.policy.controlloop.ControlLoopTargetType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Serialization {
    public static final Gson gson =
            new GsonBuilder().disableHtmlEscaping().registerTypeAdapter(ZonedDateTime.class, new GsonUtcAdapter())
                    .registerTypeAdapter(Instant.class, new GsonInstantAdapter())
                    .registerTypeAdapter(ControlLoopNotificationType.class, new NotificationTypeAdapter())
                    .registerTypeAdapter(ControlLoopTargetType.class, new TargetTypeAdapter()).create();


    public static final Gson gsonPretty = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting()
            .registerTypeAdapter(ZonedDateTime.class, new GsonUtcAdapter())
            .registerTypeAdapter(Instant.class, new GsonInstantAdapter())
            .registerTypeAdapter(ControlLoopNotificationType.class, new NotificationTypeAdapter())
            .registerTypeAdapter(ControlLoopTargetType.class, new TargetTypeAdapter()).create();

    public static final Gson gsonJunit = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting()
            .registerTypeAdapter(ZonedDateTime.class, new GsonUtcAdapter())
            .registerTypeAdapter(Instant.class, new GsonInstantAdapter())
            .registerTypeAdapter(ControlLoopTargetType.class, new TargetTypeAdapter()).create();

    private Serialization() {}

    public static class NotificationTypeAdapter
            implements JsonSerializer<ControlLoopNotificationType>, JsonDeserializer<ControlLoopNotificationType> {
        @Override
        public JsonElement serialize(ControlLoopNotificationType src, Type typeOfSrc,
                JsonSerializationContext context) {
            return new JsonPrimitive(src.toString());
        }

        @Override
        public ControlLoopNotificationType deserialize(JsonElement json, Type typeOfT,
                JsonDeserializationContext context) {
            return ControlLoopNotificationType.toType(json.getAsString());
        }
    }

    public static class TargetTypeAdapter
            implements JsonSerializer<ControlLoopTargetType>, JsonDeserializer<ControlLoopTargetType> {
        @Override
        public JsonElement serialize(ControlLoopTargetType src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.toString());
        }

        @Override
        public ControlLoopTargetType deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            return ControlLoopTargetType.toType(json.getAsString());
        }
    }

    public static class GsonUtcAdapter implements JsonSerializer<ZonedDateTime>, JsonDeserializer<ZonedDateTime> {
        private static final Logger logger = LoggerFactory.getLogger(GsonUtcAdapter.class);
        public static final DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSxxx");

        @Override
        public ZonedDateTime deserialize(JsonElement element, Type type, JsonDeserializationContext context) {
            try {
                return ZonedDateTime.parse(element.getAsString(), format);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            return null;
        }

        @Override
        public JsonElement serialize(ZonedDateTime datetime, Type type, JsonSerializationContext context) {
            return new JsonPrimitive(datetime.format(format));
        }
    }

    public static class GsonInstantAdapter implements JsonSerializer<Instant>, JsonDeserializer<Instant> {

        @Override
        public Instant deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            return Instant.ofEpochMilli(json.getAsLong());
        }

        @Override
        public JsonElement serialize(Instant src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.toEpochMilli());
        }

    }

}
