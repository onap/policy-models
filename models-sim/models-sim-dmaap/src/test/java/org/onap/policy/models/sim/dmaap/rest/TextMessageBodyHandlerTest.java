/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import jakarta.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class TextMessageBodyHandlerTest {
    private TextMessageBodyHandler hdlr;

    @Before
    public void setUp() {
        hdlr = new TextMessageBodyHandler();
    }

    @Test
    public void testIsReadable() {
        assertTrue(hdlr.isReadable(null, null, null, MediaType.valueOf("text/plain")));

        assertFalse(hdlr.isReadable(null, null, null, null));
        assertFalse(hdlr.isReadable(null, null, null, MediaType.valueOf("text/other")));
        assertFalse(hdlr.isReadable(null, null, null, MediaType.valueOf("other/plain")));
    }

    @Test
    public void testReadFrom() throws IOException {
        List<Object> lst = readStream("hello", "world");
        assertEquals("[hello, world]", lst.toString());

        // empty stream
        lst = readStream();
        assertEquals("[]", lst.toString());
    }

    /**
     * Reads a stream via the handler.
     *
     * @param text lines of text to be read
     * @return the list of objects that were decoded from the stream
     * @throws IOException if an error occurs
     */
    private List<Object> readStream(String... text) throws IOException {
        return hdlr.readFrom(null, null, null, null, null, makeStream(text));
    }

    /**
     * Creates an input stream from lines of text.
     *
     * @param text lines of text
     * @return an input stream
     */
    private InputStream makeStream(String... text) {
        return new ByteArrayInputStream(String.join("\n", text).getBytes(StandardCharsets.UTF_8));
    }
}
