/*
 * ============LICENSE_START=======================================================
 * aai
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

package org.onap.policy.aai;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;
import org.junit.Test;
import org.onap.policy.aai.util.Serialization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AaiGetVserverResponseTest {
    private static final Logger logger = LoggerFactory.getLogger(AaiGetVserverResponseTest.class);

    @Test
    public void test() throws Exception {
        // deserialize json and verify fields are populated properly
        String json = new String(Files.readAllBytes(
                        new File("src/test/resources/org/onap/policy/aai/AaiGetVserverResponse.json").toPath()));

        AaiGetVserverResponse resp = Serialization.gsonPretty.fromJson(json, AaiGetVserverResponse.class);

        // don't need to verify this in depth, as it has its own tests that do that
        List<AaiNqVServer> lst = resp.getVserver();
        assertEquals(1, lst.size());

        AaiNqVServer svr = lst.get(0);
        assertNotNull(svr);
        assertEquals("1c94da3f-16f1-4fc7-9ed1-e018dfa62774", svr.getVserverId());

        logger.info(Serialization.gsonPretty.toJson(resp));

        // verify that setXxx methods work
        lst = new LinkedList<>();
        lst.add(new AaiNqVServer());
        lst.add(new AaiNqVServer());

        resp.setVserver(lst);

        assertEquals(lst, resp.getVserver());


        // test error case
        json = new String(Files.readAllBytes(
                        new File("src/test/resources/org/onap/policy/aai/AaiGetResponseError.json").toPath()));
        resp = Serialization.gsonPretty.fromJson(json, AaiGetVserverResponse.class);

        // don't need to verify this in depth, as it has its own tests that do that
        assertNotNull(resp.getRequestError());
        assertNotNull(resp.getRequestError().getServiceExcept());
        assertEquals("SVC3001", resp.getRequestError().getServiceExcept().getMessageId());
    }

}
