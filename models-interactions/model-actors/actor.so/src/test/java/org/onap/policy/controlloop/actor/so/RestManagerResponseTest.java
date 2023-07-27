/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.controlloop.actor.so;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import jakarta.ws.rs.core.GenericType;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.utils.coder.Coder;
import org.onap.policy.common.utils.coder.StandardCoder;

public class RestManagerResponseTest {
    private static final Coder coder = new StandardCoder();

    private static final int MY_STATUS = 200;
    private static final String MY_TEXT = "{'text': 'hello'}".replace('\'', '"');

    private RestManagerResponse resp;

    @Before
    public void setUp() {
        resp = new RestManagerResponse(MY_STATUS, MY_TEXT, coder);
    }

    @Test
    public void testGetStatus() {
        assertEquals(MY_STATUS, resp.getStatus());
    }

    @Test
    public void testClose() {
        assertThatCode(() -> resp.close()).doesNotThrowAnyException();
    }

    @Test
    public void testReadEntityClassOfT() {
        // try with JSON
        MyObject obj = resp.readEntity(MyObject.class);
        assertNotNull(obj);
        assertEquals("hello", obj.text);

        // try plain string
        resp = new RestManagerResponse(MY_STATUS, "some text", coder);
        assertEquals("some text", resp.readEntity(String.class));

        // coder throws an exception
        resp = new RestManagerResponse(MY_STATUS, "{invalid-json", coder);
        assertThatIllegalArgumentException().isThrownBy(() -> resp.readEntity(MyObject.class))
                        .withMessage("cannot decode response");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testUnsupported() {
        GenericType<String> generic = GenericType.forInstance(String.class);

        assertThatThrownBy(() -> resp.hasEntity()).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> resp.bufferEntity()).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> resp.getLength()).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> resp.readEntity(generic)).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> resp.readEntity(String.class, null)).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> resp.readEntity(generic, null)).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> resp.getStatusInfo()).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> resp.getEntity()).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> resp.getMediaType()).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> resp.getLanguage()).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> resp.getAllowedMethods()).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> resp.getCookies()).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> resp.getEntityTag()).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> resp.getDate()).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> resp.getLanguage()).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> resp.getLastModified()).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> resp.getLocation()).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> resp.getLinks()).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> resp.hasLink(null)).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> resp.getLink(null)).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> resp.getLinkBuilder(null)).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> resp.getMetadata()).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> resp.getStringHeaders()).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> resp.getHeaderString(null)).isInstanceOf(UnsupportedOperationException.class);
    }


    private static class MyObject {
        private String text;
    }
}
