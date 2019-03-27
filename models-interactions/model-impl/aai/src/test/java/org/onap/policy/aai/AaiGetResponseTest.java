/*-
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
import static org.junit.Assert.assertNull;

import java.io.File;
import java.nio.file.Files;
import org.junit.Test;
import org.onap.policy.aai.util.Serialization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AaiGetResponseTest {
    private static final Logger logger = LoggerFactory.getLogger(AaiGetResponseTest.class);

    @Test
    public void test() throws Exception {
        // deserialize json and verify fields are populated properly
        String json = new String(Files.readAllBytes(
                        new File("src/test/resources/org/onap/policy/aai/AaiGetResponseError.json").toPath()));

        AaiGetResponse resp = Serialization.gsonPretty.fromJson(json, AaiGetResponse.class);

        // don't need to verify this in depth, as it has its own tests that do that
        assertNotNull(resp.getRequestError());
        assertNotNull(resp.getRequestError().getServiceExcept());
        assertEquals("SVC3001", resp.getRequestError().getServiceExcept().getMessageId());

        logger.info(Serialization.gsonPretty.toJson(resp));

        // verify that setXxx methods work
        resp.setRequestError(null);
        assertNull(resp.getRequestError());

        AaiNqRequestError err = new AaiNqRequestError();
        resp.setRequestError(err);
        assertEquals(err, resp.getRequestError());
    }

}
