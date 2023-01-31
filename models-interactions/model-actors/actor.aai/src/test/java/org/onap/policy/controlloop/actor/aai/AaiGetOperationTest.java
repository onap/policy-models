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

package org.onap.policy.controlloop.actor.aai;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.policy.aai.AaiConstants;

@RunWith(MockitoJUnitRunner.class)
public class AaiGetOperationTest extends BasicAaiOperation {
    private static final String MY_NAME = "my-operation-name";
    private static final String PARAM_NAME = "my-param";
    private static final String PARAM_VALUE = "my-value";

    private AaiGetOperation oper;

    public AaiGetOperationTest() {
        super(AaiConstants.ACTOR_NAME, MY_NAME);
    }

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        initBeforeClass();
    }

    @AfterClass
    public static void tearDownAfterClass() {
        destroyAfterClass();
    }

    /**
     * Sets up.
     */
    @Before
    public void setUp() throws Exception {
        super.setUpBasic();
        oper = new AaiGetOperation(params, config);
    }

    @Test
    public void testConstructor() {
        assertEquals(AaiConstants.ACTOR_NAME, oper.getActorName());
        assertEquals(MY_NAME, oper.getName());
    }

    @Test
    public void testGenerateSubRequestId() {
        oper.generateSubRequestId(3);
        assertEquals("3", oper.getSubRequestId());
    }

    @Test
    public void testAddQuery() {
        WebTarget web = mock(WebTarget.class);
        when(web.queryParam(any(), any())).thenReturn(web);

        StringBuilder bldr = new StringBuilder();

        assertSame(web, oper.addQuery(web, bldr, ",", PARAM_NAME, PARAM_VALUE));
        assertEquals(",my-param=my-value", bldr.toString());
    }

    @Test
    public void testAddHeaders() {
        Builder bldr = mock(Builder.class);
        oper.addHeaders(bldr, Map.of("hdrA", "valA", "hdrB", "valB"));

        verify(bldr, times(2)).header(any(), any());
        verify(bldr).header("hdrA", "valA");
        verify(bldr).header("hdrB", "valB");
    }

    @Test
    public void testGetRetry() {
        // use default if null retry
        assertEquals(AaiGetOperation.DEFAULT_RETRY, oper.getRetry(null));

        // otherwise, use specified value
        assertEquals(0, oper.getRetry(0));
        assertEquals(10, oper.getRetry(10));
    }

    @Test
    public void testMakeHeaders() {
        verifyHeaders(oper.makeHeaders());
    }
}
