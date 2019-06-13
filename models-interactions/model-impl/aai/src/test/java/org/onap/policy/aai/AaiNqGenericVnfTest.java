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

import org.junit.Test;

public class AaiNqGenericVnfTest {

    private static final String VERSION_ID = "98f410f6-4c63-447b-97d2-42508437cec0";
    private static final String INVARIANT_ID = "653d2caa-7e47-4614-95b3-26c8d82755b8";

    @Test
    public void test() {
        AaiNqGenericVnf aaiNqGenericVnf = new AaiNqGenericVnf();
        aaiNqGenericVnf.setEncrypedAccessFlag(true);
        aaiNqGenericVnf.setInMaint(false);
        aaiNqGenericVnf.setIpv4Loopback0Address("aa");
        aaiNqGenericVnf.setIpv4OamAddress("oamAddress");
        aaiNqGenericVnf.setIsClosedLoopDisabled(false);
        aaiNqGenericVnf.setModelInvariantId(INVARIANT_ID);
        aaiNqGenericVnf.setModelVersionId(VERSION_ID);
        aaiNqGenericVnf.setModelCustomizationId("SomeCustomizationId");
        aaiNqGenericVnf.setOperationalState("active");
        aaiNqGenericVnf.setPersonaModelId(INVARIANT_ID);
        aaiNqGenericVnf.setPersonaModelVersion(VERSION_ID);
        aaiNqGenericVnf.setProvStatus("complete");
        aaiNqGenericVnf.setResourceVersion("1505056714553");
        aaiNqGenericVnf.setServiceId("e8cb8968-5411-478b-906a-f28747de72cd");
        aaiNqGenericVnf.setVnfId("ed8b2bce-6b27-4089-992c-4a2c66024bcd");
        aaiNqGenericVnf.setVnfName("vCPEInfraVNF14a");
        aaiNqGenericVnf.setVnfName2("malumabb12");
        aaiNqGenericVnf.setVnfType("vCPEInfraService10/vCPEInfraService10 0");
        assertNotNull(aaiNqGenericVnf);

        assertEquals(true, aaiNqGenericVnf.getEncrypedAccessFlag());
        assertEquals(false, aaiNqGenericVnf.getInMaint());
        assertEquals("aa", aaiNqGenericVnf.getIpv4Loopback0Address());
        assertEquals("oamAddress", aaiNqGenericVnf.getIpv4OamAddress());
        assertEquals(false, aaiNqGenericVnf.getIsClosedLoopDisabled());
        assertEquals(INVARIANT_ID, aaiNqGenericVnf.getModelInvariantId());
        assertEquals(VERSION_ID, aaiNqGenericVnf.getModelVersionId());
        assertEquals("SomeCustomizationId", aaiNqGenericVnf.getModelCustomizationId());
        assertEquals("active", aaiNqGenericVnf.getOperationalState());
        assertEquals(INVARIANT_ID, aaiNqGenericVnf.getPersonaModelId());
        assertEquals(VERSION_ID, aaiNqGenericVnf.getPersonaModelVersion());
        assertEquals("complete", aaiNqGenericVnf.getProvStatus());
        assertEquals("1505056714553", aaiNqGenericVnf.getResourceVersion());
        assertEquals("e8cb8968-5411-478b-906a-f28747de72cd", aaiNqGenericVnf.getServiceId());
        assertEquals("ed8b2bce-6b27-4089-992c-4a2c66024bcd", aaiNqGenericVnf.getVnfId());
        assertEquals("vCPEInfraVNF14a", aaiNqGenericVnf.getVnfName());
        assertEquals("malumabb12", aaiNqGenericVnf.getVnfName2());
        assertEquals("vCPEInfraService10/vCPEInfraService10 0", aaiNqGenericVnf.getVnfType());
    }

}
