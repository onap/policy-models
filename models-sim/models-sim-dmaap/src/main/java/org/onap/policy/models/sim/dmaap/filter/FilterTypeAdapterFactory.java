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

package org.onap.policy.models.sim.dmaap.filter;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Factory to create an adapter for Filter classes.
 */
public class FilterTypeAdapterFactory implements TypeAdapterFactory {
    public static final String CLASS_NAME = "class";
    private static final List<Class<? extends Filter>> TYPES =
                    List.of(Equals.class, EndsWith.class, StartsWith.class, And.class);

    @Override
    @SuppressWarnings("unchecked")
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        if (!Filter.class.isAssignableFrom(type.getRawType())) {
            return null;
        }

        return (TypeAdapter<T>) new FilterTypeAdapter(gson);
    }


    /**
     * Type adapter for a single Filter subclass.
     */
    private class FilterTypeAdapter extends TypeAdapter<Filter> {

        /**
         * Adapters/delegates that convert a JsonElement to a given Filter subclass.
         */
        private final Map<String, TypeAdapter<? extends Filter>> class2adapter = new HashMap<>();

        /**
         * Adapter used to serialize/deserialize a JsonElement.
         */
        private final TypeAdapter<JsonElement> elementAdapter;


        /**
         * Constructs the object.
         *
         * @param gson the associated gson object
         */
        public FilterTypeAdapter(Gson gson) {
            this.elementAdapter = gson.getAdapter(JsonElement.class);

            for (Class<? extends Filter> clazz : TYPES) {
                class2adapter.put(clazz.getSimpleName(),
                                gson.getDelegateAdapter(FilterTypeAdapterFactory.this, TypeToken.get(clazz)));
            }
        }


        @Override
        public void write(JsonWriter out, Filter value) throws IOException {
            String clazz = value.getClass().getSimpleName();

            @SuppressWarnings("rawtypes")
            TypeAdapter adapter = class2adapter.get(clazz);
            if (adapter == null) {
                throw new JsonParseException("Unknown 'filter' class: " + clazz);
            }

            @SuppressWarnings("unchecked")
            JsonElement tree = adapter.toJsonTree(value);

            tree.getAsJsonObject().add(CLASS_NAME, new JsonPrimitive(value.getClass().getSimpleName()));

            elementAdapter.write(out, tree);
        }

        @Override
        public Filter read(JsonReader in) throws IOException {
            JsonElement tree = elementAdapter.read(in);

            if (!tree.isJsonObject()) {
                throw new JsonParseException("Expecting a Filter object");
            }

            String clazz = tree.getAsJsonObject().get(CLASS_NAME).getAsString();

            TypeAdapter<? extends Filter> adapter = class2adapter.get(clazz);
            if (adapter == null) {
                throw new JsonParseException("Unknown 'filter' class: " + clazz);
            }

            return adapter.fromJsonTree(tree);
        }
    }
}
