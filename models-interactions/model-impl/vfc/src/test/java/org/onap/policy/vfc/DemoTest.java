/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2017-2019 Intel Corp. All rights reserved.
 * Modifications Copyright (C) 2019, 2024 Nordix Foundation.
 * Modifications Copyright (C) 2018-2020 AT&T Corporation. All rights reserved.
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

package org.onap.policy.vfc;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.LinkedList;
import org.junit.jupiter.api.Test;
import org.onap.policy.vfc.util.Serialization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DemoTest {
    private static final Logger logger = LoggerFactory.getLogger(DemoTest.class);

    @Test
    public void test() {
        VfcRequest request = new VfcRequest();

        request.setNsInstanceId("100");
        request.setHealRequest(new VfcHealRequest());
        request.getHealRequest().setVnfInstanceId("1");
        request.getHealRequest().setCause("vm is down");

        request.getHealRequest().setAdditionalParams(new VfcHealAdditionalParams());
        request.getHealRequest().getAdditionalParams().setAction("restartvm");

        request.getHealRequest().getAdditionalParams().setActionInfo(new VfcHealActionVmInfo());
        request.getHealRequest().getAdditionalParams().getActionInfo().setVmid("33");
        request.getHealRequest().getAdditionalParams().getActionInfo().setVmname("xgw-smp11");

        String body = Serialization.gsonPretty.toJson(request);
        logger.info("{}", body);

        VfcResponse response = new VfcResponse();
        response.setJobId("1");

        body = Serialization.gsonPretty.toJson(response);
        logger.info("{}", body);

        response.setResponseDescriptor(new VfcResponseDescriptor());
        response.getResponseDescriptor().setProgress("40");
        response.getResponseDescriptor().setStatus("processing");
        response.getResponseDescriptor().setStatusDescription("OMC VMs are decommissioned in VIM");
        response.getResponseDescriptor().setErrorCode(null);
        response.getResponseDescriptor().setResponseId("42");
        body = Serialization.gsonPretty.toJson(response);
        logger.info("{}", body);

        VfcResponseDescriptor responseDescriptor = new VfcResponseDescriptor();
        responseDescriptor.setProgress("20");
        responseDescriptor.setStatus("processing");
        responseDescriptor.setStatusDescription("OMC VMs are decommissioned in VIM");
        responseDescriptor.setErrorCode(null);
        responseDescriptor.setResponseId("11");

        response.getResponseDescriptor().setResponseHistoryList(new LinkedList<>());
        response.getResponseDescriptor().getResponseHistoryList().add(responseDescriptor);

        body = Serialization.gsonPretty.toJson(response);
        logger.info("{}", body);

        response = Serialization.gsonPretty.fromJson(body, VfcResponse.class);
        body = Serialization.gsonPretty.toJson(response);
        logger.info("{}", body);

        assertNotNull(body);
    }
}
