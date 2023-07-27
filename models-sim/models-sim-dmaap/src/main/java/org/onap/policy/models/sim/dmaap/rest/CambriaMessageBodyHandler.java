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
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.io.IOUtils;

/**
 * Provider that decodes "application/cambria" messages.
 */
@Provider
@Consumes(CambriaMessageBodyHandler.MEDIA_TYPE_APPLICATION_CAMBRIA)
public class CambriaMessageBodyHandler implements MessageBodyReader<Object> {
    public static final String MEDIA_TYPE_APPLICATION_CAMBRIA = "application/cambria";

    /**
     * Maximum length of a message or partition.
     */
    private static final int MAX_LEN = 10000000;

    /**
     * Maximum digits in a length field.
     */
    private static final int MAX_DIGITS = 10;

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return (mediaType != null && MEDIA_TYPE_APPLICATION_CAMBRIA.equals(mediaType.toString()));
    }

    @Override
    public List<Object> readFrom(Class<Object> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                    MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException {

        try (var bufferedReader = new BufferedReader(new InputStreamReader(entityStream, StandardCharsets.UTF_8))) {
            List<Object> messages = new LinkedList<>();
            String msg;
            while ((msg = readMessage(bufferedReader)) != null) {
                messages.add(msg);
            }

            return messages;
        }
    }

    /**
     * Reads a message.
     *
     * @param reader source from which to read
     * @return the message that was read, or {@code null} if there are no more messages
     * @throws IOException if an error occurs
     */
    private String readMessage(Reader reader) throws IOException {
        if (!skipWhitespace(reader)) {
            return null;
        }

        int partlen = readLength(reader);
        if (partlen > MAX_LEN) {
            throw new IOException("invalid partition length");
        }

        int msglen = readLength(reader);
        if (msglen > MAX_LEN) {
            throw new IOException("invalid message length");
        }

        // skip over the partition
        reader.skip(partlen);

        return readString(reader, msglen);
    }

    /**
     * Skips whitespace.
     *
     * @param reader source from which to read
     * @return {@code true} if there is another character after the whitespace,
     *         {@code false} if the end of the stream has been reached
     * @throws IOException if an error occurs
     */
    private boolean skipWhitespace(Reader reader) throws IOException {
        int chr;

        do {
            reader.mark(1);
            if ((chr = reader.read()) < 0) {
                return false;
            }
        } while (Character.isWhitespace(chr));

        // push the last character back onto the reader
        reader.reset();

        return true;
    }

    /**
     * Reads a length field, which is a number followed by ".".
     *
     * @param reader source from which to read
     * @return the length, or -1 if EOF has been reached
     * @throws IOException if an error occurs
     */
    private int readLength(Reader reader) throws IOException {
        var bldr = new StringBuilder(MAX_DIGITS);

        int chr;
        for (var x = 0; x < MAX_DIGITS; ++x) {
            if ((chr = reader.read()) < 0) {
                throw new EOFException("missing '.' in 'length' field");
            }

            if (chr == '.') {
                String text = bldr.toString().trim();
                return (text.isEmpty() ? 0 : Integer.parseInt(text));
            }

            if (!Character.isDigit(chr)) {
                throw new IOException("invalid character in 'length' field");
            }

            bldr.append((char) chr);
        }

        throw new IOException("too many digits in 'length' field");
    }

    /**
     * Reads a string.
     *
     * @param reader source from which to read
     * @param len length of the string (i.e., number of characters to read)
     * @return the string that was read
     * @throws IOException if an error occurs
     */
    private String readString(Reader reader, int len) throws IOException {
        var buf = new char[len];
        IOUtils.readFully(reader, buf);

        return new String(buf);
    }
}
