/*
 * ============LICENSE_START======================================================= ONAP
 * ================================================================================ Copyright (C) 2019 AT&T Intellectual
 * Property. All rights reserved. ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.policy.models.sim.dmaap.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provider that serializes and de-serializes JSON via gson.
 */
@Provider
@Consumes(CambriaMessageBodyHandler.MEDIA_TYPE_APPLICATION_CAMBRIA)
@Produces(CambriaMessageBodyHandler.MEDIA_TYPE_APPLICATION_CAMBRIA)
public class CambriaMessageBodyHandler implements MessageBodyReader<Object> {
    // Media type for Cambria
    public static final String MEDIA_TYPE_APPLICATION_CAMBRIA = "application/cambria";

    public static final Logger logger = LoggerFactory.getLogger(CambriaMessageBodyHandler.class);

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return MEDIA_TYPE_APPLICATION_CAMBRIA.equals(mediaType.toString());
    }

    @Override
    public String readFrom(Class<Object> type, Type genericType, Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
            throws IOException {

        String cambriaString = "";
        try (BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(entityStream))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                cambriaString += line;
            }

            return cambriaString.substring(cambriaString.indexOf('{'), cambriaString.length());
        }
    }
}
