/*
 * ============LICENSE_START=======================================================
 * ONAP PAP
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;

public class BaseRestControllerV1Test {

    private BaseRestControllerV1 ctlr;
    private ResponseBuilder bldr;

    @Before
    public void setUp() {
        ctlr = new BaseRestControllerV1();
        bldr = Response.status(Response.Status.OK);
    }

    @Test
    public void testAddVersionControlHeaders() {
        Response resp = ctlr.addVersionControlHeaders(bldr).build();
        assertEquals("0", resp.getHeaderString(BaseRestControllerV1.VERSION_MINOR_NAME));
        assertEquals("0", resp.getHeaderString(BaseRestControllerV1.VERSION_PATCH_NAME));
        assertEquals("1.0.0", resp.getHeaderString(BaseRestControllerV1.VERSION_LATEST_NAME));
    }

    @Test
    public void testAddLoggingHeaders_Null() {
        Response resp = ctlr.addLoggingHeaders(bldr, null).build();
        assertNotNull(resp.getHeaderString(BaseRestControllerV1.REQUEST_ID_NAME));
    }

    @Test
    public void testAddLoggingHeaders_NonNull() {
        UUID uuid = UUID.randomUUID();
        Response resp = ctlr.addLoggingHeaders(bldr, uuid).build();
        assertEquals(uuid.toString(), resp.getHeaderString(BaseRestControllerV1.REQUEST_ID_NAME));
    }
}
