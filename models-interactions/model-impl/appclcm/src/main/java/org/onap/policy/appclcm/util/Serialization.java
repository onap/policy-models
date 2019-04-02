/*-
 * ============LICENSE_START=======================================================
 * appc
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

package org.onap.policy.appclcm.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.time.Instant;

import org.onap.policy.appclcm.LcmRequest;
import org.onap.policy.appclcm.LcmResponse;

public final class Serialization {
    public static final Gson gsonPretty = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting()
            .registerTypeAdapter(Instant.class, new InstantAdapter()).create();

    public static final Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting()
            .registerTypeAdapter(LcmRequest.class, new RequestAdapter())
            .registerTypeAdapter(LcmResponse.class, new ResponseAdapter()).create();

    public static final Gson gsonJunit = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting()
            .registerTypeAdapter(Instant.class, new InstantJunitAdapter()).create();

    private Serialization() {}

    public static class RequestAdapter implements JsonSerializer<LcmRequest>, JsonDeserializer<LcmRequest> {

        @Override
        public JsonElement serialize(LcmRequest src, Type typeOfSrc, JsonSerializationContext context) {
            JsonElement requestJson = gsonPretty.toJsonTree(src, LcmRequest.class);
            JsonObject input = new JsonObject();
            input.add("input", requestJson);

            return input;
        }

        @Override
        public LcmRequest deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            return gsonPretty.fromJson(json.getAsJsonObject().get("input"), LcmRequest.class);
        }
    }

    public static class ResponseAdapter implements JsonSerializer<LcmResponse>, JsonDeserializer<LcmResponse> {

        @Override
        public JsonElement serialize(LcmResponse src, Type typeOfSrc, JsonSerializationContext context) {
            JsonElement responseJson = gsonPretty.toJsonTree(src, LcmResponse.class);
            JsonObject output = new JsonObject();
            output.add("output", responseJson);
            return output;
        }

        @Override
        public LcmResponse deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            return gsonPretty.fromJson(json.getAsJsonObject().get("output"), LcmResponse.class);
        }
    }

    public static class InstantAdapter implements JsonSerializer<Instant>, JsonDeserializer<Instant> {

        @Override
        public Instant deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            return Instant.parse(json.getAsString());
        }

        @Override
        public JsonElement serialize(Instant src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.toString());
        }

    }

    public static class InstantJunitAdapter implements JsonSerializer<Instant>, JsonDeserializer<Instant> {

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
