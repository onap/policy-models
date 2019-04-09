/*-
 * ============LICENSE_START=======================================================
 *
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.onap.aai.domain.yang.v15.CloudRegion;
import org.onap.aai.domain.yang.v15.GenericVnf;
import org.onap.aai.domain.yang.v15.ServiceInstance;
import org.onap.aai.domain.yang.v15.Tenant;
import org.onap.aai.domain.yang.v15.VfModule;
import org.onap.aai.domain.yang.v15.Vserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AaiCqResponseTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(AaiCqResponseTest.class);
    private static final String CQ_RESPONSE_SAMPLE = "src/test/resources/org/onap/policy/aai/AaiCqResponse.json";


    @Test
    public void testConstructor() throws Exception {
        /*
         * Read JSON String and add all AaiObjects
         */

        String responseString = "";
        responseString = new String(Files.readAllBytes(new File(CQ_RESPONSE_SAMPLE).toPath()));

        AaiCqResponse aaiCqResponse;
        aaiCqResponse = new AaiCqResponse(responseString);
        assertNotNull(aaiCqResponse);
        assertNotNull(aaiCqResponse.getInventoryResponseItems());
    }

    @Test
    public void testAaiMalformedCqResponse() throws Exception {
        /*
         * Read JSON String and add all AaiObjects
         */

        String responseString = "";
        responseString = new String(Files
                .readAllBytes(new File("src/test/resources/org/onap/policy/aai/AaiMalformedCqResponse.json").toPath()));

        AaiCqResponse aaiCqResponse;
        aaiCqResponse = new AaiCqResponse(responseString);
        for (Object aaiObj : aaiCqResponse.getInventoryResponseItems()) {
            assertNull(aaiObj);
        }


    }

    @Test
    public void testGetItemByList() throws Exception {
        /*
         * Read JSON String and add all AaiObjects
         */

        String responseString = "";
        responseString = new String(Files.readAllBytes(new File(CQ_RESPONSE_SAMPLE).toPath()));

        AaiCqResponse aaiCqResponse;
        aaiCqResponse = new AaiCqResponse(responseString);
        ArrayList<Vserver> vs = (ArrayList<Vserver>) aaiCqResponse.getItemListByType(Vserver.class);
        assertNotNull(vs);
        assertEquals("e7f1db09-ff78-44fc-b256-69095c5556fb", vs.get(0).getVserverId());
        LOGGER.info(vs.get(0).getVserverId());

    }

    @Test
    public void testGetServiceInstance() throws Exception {

        String responseString = "";
        responseString = new String(Files.readAllBytes(new File(CQ_RESPONSE_SAMPLE).toPath()));

        AaiCqResponse aaiCqResponse;
        aaiCqResponse = new AaiCqResponse(responseString);
        ServiceInstance si = aaiCqResponse.getServiceInstance();
        assertNotNull(si);
        assertEquals("vLoadBalancerMS-0211-1", si.getServiceInstanceName());
        LOGGER.info(si.getServiceInstanceName());
    }

    @Test
    public void testGetDefaultCloudRegion() throws Exception {

        String responseString = "";
        responseString = new String(Files.readAllBytes(new File(CQ_RESPONSE_SAMPLE).toPath()));

        AaiCqResponse aaiCqResponse;
        aaiCqResponse = new AaiCqResponse(responseString);
        CloudRegion cloudRegion = aaiCqResponse.getDefaultCloudRegion();
        assertNotNull(cloudRegion);
        assertEquals("cr-16197-01-as988q", cloudRegion.getCloudRegionId());
        LOGGER.info(cloudRegion.getCloudRegionId());
    }

    @Test
    public void testGetDefaultTenant() throws Exception {

        String responseString = "";
        responseString = new String(Files.readAllBytes(new File(CQ_RESPONSE_SAMPLE).toPath()));

        AaiCqResponse aaiCqResponse;
        aaiCqResponse = new AaiCqResponse(responseString);
        Tenant tenant = aaiCqResponse.getDefaultTenant();
        assertNotNull(tenant);
        assertEquals("tenant1-16197-as988q", tenant.getTenantId());
        LOGGER.info(tenant.getTenantId());
    }



    @Test
    public void testGetGenericVnfs() throws Exception {

        String responseString = "";
        responseString = new String(Files.readAllBytes(new File(CQ_RESPONSE_SAMPLE).toPath()));

        AaiCqResponse aaiCqResponse;
        aaiCqResponse = new AaiCqResponse(responseString);
        List<GenericVnf> genericVnfList = aaiCqResponse.getGenericVnfs();
        assertNotNull(genericVnfList);
        for (GenericVnf genVnf : genericVnfList) {
            LOGGER.info(genVnf.getVnfName());
        }

    }



    @Test
    public void testGetDefaultGenericVnf() throws Exception {

        String responseString = "";
        responseString = new String(Files.readAllBytes(new File(CQ_RESPONSE_SAMPLE).toPath()));

        AaiCqResponse aaiCqResponse;
        aaiCqResponse = new AaiCqResponse(responseString);
        GenericVnf genVnf = aaiCqResponse.getDefaultGenericVnf();
        assertNotNull(genVnf);
        assertEquals("TestVM-Vnf-0201-1", genVnf.getVnfName());
        LOGGER.info(genVnf.getVnfName());

    }

    @Test
    public void testGetGenericVnfByName() throws Exception {

        String responseString = "";
        responseString = new String(Files.readAllBytes(new File(CQ_RESPONSE_SAMPLE).toPath()));

        AaiCqResponse aaiCqResponse;
        aaiCqResponse = new AaiCqResponse(responseString);
        GenericVnf genVnf = aaiCqResponse.getGenericVnfByVnfName("TestVM-Vnf-0201-1");
        assertNotNull(genVnf);
        assertEquals("17044ef4-e7f3-46a1-af03-e2aa562f23ac", genVnf.getVnfId());
        LOGGER.info(genVnf.getVnfId());
    }


    @Test
    public void testGetGenericVnfByModelInvariantId() throws Exception {

        String responseString = "";
        responseString = new String(Files.readAllBytes(new File(CQ_RESPONSE_SAMPLE).toPath()));

        AaiCqResponse aaiCqResponse;
        aaiCqResponse = new AaiCqResponse(responseString);
        GenericVnf genVnf = aaiCqResponse.getGenericVnfByModelInvariantId("724ab1cf-6120-49e8-b909-849963bed1d6");
        assertNotNull(genVnf);
        assertEquals("724ab1cf-6120-49e8-b909-849963bed1d6", genVnf.getModelInvariantId());
        LOGGER.info(genVnf.getModelInvariantId());
    }

    @Test
    public void testGetAllVfModules() throws Exception {

        String responseString = "";
        responseString = new String(Files.readAllBytes(new File(CQ_RESPONSE_SAMPLE).toPath()));

        AaiCqResponse aaiCqResponse;
        aaiCqResponse = new AaiCqResponse(responseString);
        List<VfModule> vfModuleList = aaiCqResponse.getAllVfModules();
        assertNotNull(vfModuleList);
        for (VfModule vfMod : vfModuleList) {
            LOGGER.info(vfMod.getVfModuleName());
        }

    }


    @Test
    public void testGetVfModuleByVfModuleName() throws Exception {

        String responseString = "";
        responseString = new String(Files.readAllBytes(new File(CQ_RESPONSE_SAMPLE).toPath()));

        AaiCqResponse aaiCqResponse;
        aaiCqResponse = new AaiCqResponse(responseString);
        VfModule vfModule = aaiCqResponse.getVfModuleByVfModuleName("vLoadBalancerMS-0211-1");
        assertNotNull(vfModule);
        assertEquals("vLoadBalancerMS-0211-1", vfModule.getVfModuleName());
        LOGGER.info(vfModule.getVfModuleName());


    }

    @Test
    public void testGetDefaultVfModule() throws Exception {

        String responseString = "";
        responseString = new String(Files.readAllBytes(new File(CQ_RESPONSE_SAMPLE).toPath()));

        AaiCqResponse aaiCqResponse;
        aaiCqResponse = new AaiCqResponse(responseString);
        VfModule vfModule = aaiCqResponse.getDefaultVfModule();
        assertNotNull(vfModule);
        assertEquals("TestVM-0201-2", vfModule.getVfModuleName());
        LOGGER.info(vfModule.getVfModuleName());
    }

    @Test
    public void testGetVserver() throws Exception {

        String responseString = "";
        responseString = new String(Files.readAllBytes(new File(CQ_RESPONSE_SAMPLE).toPath()));

        AaiCqResponse aaiCqResponse;
        aaiCqResponse = new AaiCqResponse(responseString);
        Vserver vserver = aaiCqResponse.getVserver();
        assertNotNull(vserver);
        assertEquals("vfw-vm-0201-2", vserver.getVserverName());
        LOGGER.info(vserver.getVserverName());

    }

    /**
     * Aai Cq sample response.
     * @return String return response
     * @throws Exception file read exception
     */
    public String getAaiCqResponse() throws Exception {
        String responseString = "";
        responseString = new String(Files.readAllBytes(new File(CQ_RESPONSE_SAMPLE).toPath()));
        return responseString;
    }

}
