/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2019 Bell Canada. All rights reserved.
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

package org.onap.policy.controlloop.actor.cds.request;

import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.junit.Test;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.controlloop.actor.cds.constants.CdsActorConstants;

public class CdsActionRequestTest {

    private static final String TEST_ACTION_NAME = "vfw-modify-config";

    @Test
    public void testGenerateCdsPayload() throws CoderException {
        // Setup the CdsActionRequest object
        CdsActionRequest req = new CdsActionRequest();
        req.setActionName(TEST_ACTION_NAME);
        req.setResolutionKey("1234567890");
        Map<String, String> payloadProps = ImmutableMap.of("data", "{\"active-streams\":\"5\"}");
        req.setPolicyPayload(payloadProps);

        Map<String, String> aaiParams =
                ImmutableMap.of("service-instance.service-instance-id", "1234", "generic-vnf.vnf-id", "5678");
        req.setAaiProperties(aaiParams);

        // Act
        String result = req.generateCdsPayload();

        // Assert
        assertTrue(result.contains(TEST_ACTION_NAME + CdsActorConstants.CDS_REQUEST_PROPERTIES_SUFFIX));
        assertTrue(result.contains(TEST_ACTION_NAME + CdsActorConstants.CDS_REQUEST_SUFFIX));
        assertTrue(result.contains(CdsActorConstants.KEY_RESOLUTION_KEY));
    }
}
