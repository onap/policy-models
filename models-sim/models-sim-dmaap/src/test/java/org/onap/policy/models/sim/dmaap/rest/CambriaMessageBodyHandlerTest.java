/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy Models
 * ================================================================================
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import jakarta.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class CambriaMessageBodyHandlerTest {
    private static final String STD_INPUT = "1.3.XAbc";
    private static final String EXPECTED_OUTPUT = "[Abc]";

    private CambriaMessageBodyHandler hdlr;

    @Before
    public void setUp() {
        hdlr = new CambriaMessageBodyHandler();
    }

    @Test
    public void testIsReadable() {
        assertTrue(hdlr.isReadable(null, null, null, MediaType.valueOf("application/cambria")));

        assertFalse(hdlr.isReadable(null, null, null, null));
        assertFalse(hdlr.isReadable(null, null, null, MediaType.valueOf("application/other")));
        assertFalse(hdlr.isReadable(null, null, null, MediaType.valueOf("other/cambria")));
    }

    @Test
    public void testReadFrom() throws IOException {
        List<Object> lst = readStream("1.11.AMessageBody", "3.3.123Foo3.3.123Bar", "0.16.You can do that..8.Or that.");
        assertEquals("[MessageBody, Foo, Bar, You can do that., Or that.]", lst.toString());

        // empty stream
        lst = readStream();
        assertEquals("[]", lst.toString());
    }

    @Test
    public void testReadMessage_InvalidPartitionLength() {
        assertThatThrownBy(() -> readStream("100000000.3.")).isInstanceOf(IOException.class)
                        .hasMessage("invalid partition length");
    }

    @Test
    public void testReadMessage_InvalidMessageLength() {
        assertThatThrownBy(() -> readStream("3.100000000.ABC")).isInstanceOf(IOException.class)
                        .hasMessage("invalid message length");
    }

    @Test
    public void testSkipWhitespace() throws IOException {
        // no white space
        assertEquals(EXPECTED_OUTPUT, readStream(STD_INPUT).toString());

        // single white space
        assertEquals(EXPECTED_OUTPUT, readStream(" " + STD_INPUT).toString());

        // multiple white spaces
        assertEquals(EXPECTED_OUTPUT, readStream("\n\n\t" + STD_INPUT).toString());
    }

    @Test
    public void testReadLength_NoDigits() throws IOException {
        assertEquals("[]", readStream("..").toString());
    }

    @Test
    public void testReadLength_NoDot() {
        assertThatThrownBy(() -> readStream("3.2")).isInstanceOf(EOFException.class)
                        .hasMessage("missing '.' in 'length' field");
    }

    @Test
    public void testReadLength_NonDigit() {
        assertThatThrownBy(() -> readStream("3.2x.ABCde")).isInstanceOf(IOException.class)
                        .hasMessage("invalid character in 'length' field");
    }

    @Test
    public void testReadLength_TooManyDigits() {
        assertThatThrownBy(() -> readStream("3.12345678901234567890.ABCde")).isInstanceOf(IOException.class)
                        .hasMessage("too many digits in 'length' field");
    }

    @Test
    public void testReadString_ZeroLength() throws IOException {
        assertEquals("[]", readStream("1..X").toString());
    }

    @Test
    public void testReadString_TooShort() {
        assertThatThrownBy(() -> readStream(".5.me")).isInstanceOf(EOFException.class).hasMessageContaining("actual");
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
