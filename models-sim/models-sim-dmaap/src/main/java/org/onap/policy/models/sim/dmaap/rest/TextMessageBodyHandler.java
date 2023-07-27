/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy Models
 * ================================================================================
 * Copyright (C) 2019, 2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2023 Nordix Foundation.
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

package org.onap.policy.models.sim.dmaap.rest;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.Provider;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

/**
 * Provider that decodes "text/plain" messages.
 */
@Provider
@Consumes(TextMessageBodyHandler.MEDIA_TYPE_TEXT_PLAIN)
public class TextMessageBodyHandler implements MessageBodyReader<Object> {
    public static final String MEDIA_TYPE_TEXT_PLAIN = "text/plain";

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return (mediaType != null && MEDIA_TYPE_TEXT_PLAIN.equals(mediaType.toString()));
    }

    @Override
    public List<Object> readFrom(Class<Object> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                    MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException {

        try (var bufferedReader = new BufferedReader(new InputStreamReader(entityStream, StandardCharsets.UTF_8))) {
            List<Object> messages = new LinkedList<>();
            String msg;
            while ((msg = bufferedReader.readLine()) != null) {
                messages.add(msg);
            }

            return messages;
        }
    }
}
