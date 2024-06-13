/*-
 * ============LICENSE_START=======================================================
 *
 * ================================================================================
 * Copyright (C) 2019-2020 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2024 Nordix Foundation
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.onap.aai.domain.yang.CloudRegion;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.aai.domain.yang.ModelVer;
import org.onap.aai.domain.yang.ServiceInstance;
import org.onap.aai.domain.yang.Tenant;
import org.onap.aai.domain.yang.VfModule;
import org.onap.aai.domain.yang.Vserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AaiCqResponseTest {
    private static final String ETE_VFMODULE = "Vfmodule_Ete_vFWCLvFWSNK_7ba1fbde_0";
    private static final String ETE_VNF = "Ete_vFWCLvFWSNK_7ba1fbde_0";
    private static final Logger LOGGER = LoggerFactory.getLogger(AaiCqResponseTest.class);
    private static final String CQ_RESPONSE_SAMPLE =
        "src/test/resources/org/onap/policy/aai/AaiCqResponseFull.json";

    @Test
    public void testConstructor() throws Exception {
        String responseString = getAaiCqResponse();

        AaiCqResponse aaiCqResponse;
        aaiCqResponse = new AaiCqResponse(responseString);
        assertNotNull(aaiCqResponse);
        assertNotNull(aaiCqResponse.getInventoryResponseItems());
    }

    @Test
    public void testMultiThreaded() throws Exception {
        final AtomicInteger success = new AtomicInteger(0);
        final String json = getAaiCqResponse();

        Thread[] threads = new Thread[5];
        for (int x = 0; x < threads.length; ++x) {
            threads[x] = new Thread(() -> runIt(json, success));
        }

        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        assertEquals(threads.length, success.get());
    }

    @Test
    public void testAaiMalformedCqResponse() throws Exception {
        String responseString = Files.readString(
                        new File("src/test/resources/org/onap/policy/aai/AaiMalformedCqResponse.json").toPath(),
                        StandardCharsets.UTF_8);

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
        assertEquals("f953c499-4b1e-426b-8c6d-e9e9f1fc730f", vs.get(0).getVserverId());
        LOGGER.info(vs.get(0).getVserverId());

    }

    @Test
    public void testGetServiceInstance() throws Exception {
        String responseString = getAaiCqResponse();

        AaiCqResponse aaiCqResponse;
        aaiCqResponse = new AaiCqResponse(responseString);
        ServiceInstance si = aaiCqResponse.getServiceInstance();
        assertNotNull(si);
        assertEquals("Service_Ete_Name7ba1fbde-6187-464a-a62d-d9dd25bdf4e8",
            si.getServiceInstanceName());
        LOGGER.info(si.getServiceInstanceName());
    }

    @Test
    public void testGetDefaultCloudRegion() throws Exception {
        String responseString = getAaiCqResponse();

        AaiCqResponse aaiCqResponse;
        aaiCqResponse = new AaiCqResponse(responseString);
        CloudRegion cloudRegion = aaiCqResponse.getDefaultCloudRegion();
        assertNotNull(cloudRegion);
        assertEquals("RegionOne", cloudRegion.getCloudRegionId());
        LOGGER.info(cloudRegion.getCloudRegionId());
    }

    @Test
    public void testGetDefaultTenant() throws Exception {
        String responseString = getAaiCqResponse();

        AaiCqResponse aaiCqResponse;
        aaiCqResponse = new AaiCqResponse(responseString);
        Tenant tenant = aaiCqResponse.getDefaultTenant();
        assertNotNull(tenant);
        assertEquals("41d6d38489bd40b09ea8a6b6b852dcbd", tenant.getTenantId());
        LOGGER.info(tenant.getTenantId());
    }

    @Test
    public void testGetGenericVnfs() throws Exception {
        String responseString = getAaiCqResponse();

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
        String responseString = getAaiCqResponse();

        AaiCqResponse aaiCqResponse;
        aaiCqResponse = new AaiCqResponse(responseString);
        GenericVnf genVnf = aaiCqResponse.getDefaultGenericVnf();
        assertNotNull(genVnf);
        assertEquals(ETE_VNF, genVnf.getVnfName());
        LOGGER.info(genVnf.getVnfName());

    }

    @Test
    public void testGetGenericVnfByName() throws Exception {
        String responseString = getAaiCqResponse();

        AaiCqResponse aaiCqResponse;
        aaiCqResponse = new AaiCqResponse(responseString);
        GenericVnf genVnf = aaiCqResponse.getGenericVnfByVnfName(ETE_VNF);
        assertNotNull(genVnf);
        assertEquals("f17face5-69cb-4c88-9e0b-7426db7edddd", genVnf.getVnfId());
        LOGGER.info(genVnf.getVnfId());
    }

    @Test
    public void testGetGenericVnfByModelInvariantId() throws Exception {
        String responseString = getAaiCqResponse();

        AaiCqResponse aaiCqResponse;
        aaiCqResponse = new AaiCqResponse(responseString);
        GenericVnf genVnf =
            aaiCqResponse.getGenericVnfByModelInvariantId("9a243c47-fd5f-43d1-bd2a-f17bd12a61f2");
        assertNotNull(genVnf);
        assertEquals("9a243c47-fd5f-43d1-bd2a-f17bd12a61f2", genVnf.getModelInvariantId());
        LOGGER.info(genVnf.getModelInvariantId());
    }

    @Test
    public void testGetGenericVnfByVfModuleModelInvariantId() throws Exception {
        String responseString = getAaiCqResponse();

        AaiCqResponse aaiCqResponse;
        aaiCqResponse = new AaiCqResponse(responseString);
        GenericVnf genVnf = aaiCqResponse
            .getGenericVnfByVfModuleModelInvariantId("e6130d03-56f1-4b0a-9a1d-e1b2ebc30e0e");
        assertNotNull(genVnf);
        assertEquals(ETE_VNF, genVnf.getVnfName());
        LOGGER.info(genVnf.getVnfName());
    }

    @Test
    public void testGetAllVfModules() throws Exception {
        String responseString = getAaiCqResponse();

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
        String responseString = getAaiCqResponse();

        AaiCqResponse aaiCqResponse;
        aaiCqResponse = new AaiCqResponse(responseString);
        VfModule vfModule = aaiCqResponse.getVfModuleByVfModuleName(ETE_VFMODULE);
        assertNotNull(vfModule);
        assertEquals(ETE_VFMODULE, vfModule.getVfModuleName());
        LOGGER.info(vfModule.getVfModuleName());

    }

    @Test
    public void testGetVfModuleByVfModelInvariantId() throws Exception {
        String responseString = getAaiCqResponse();

        AaiCqResponse aaiCqResponse;
        aaiCqResponse = new AaiCqResponse(responseString);
        VfModule vfModule =
            aaiCqResponse.getVfModuleByVfModelInvariantId("e6130d03-56f1-4b0a-9a1d-e1b2ebc30e0e");
        assertNotNull(vfModule);
        assertEquals(ETE_VFMODULE, vfModule.getVfModuleName());
        LOGGER.info(vfModule.getVfModuleName());

    }

    @Test
    public void testGetDefaultVfModule() throws Exception {
        String responseString = getAaiCqResponse();

        AaiCqResponse aaiCqResponse;
        aaiCqResponse = new AaiCqResponse(responseString);
        VfModule vfModule = aaiCqResponse.getDefaultVfModule();
        assertNotNull(vfModule);
        assertEquals(ETE_VFMODULE, vfModule.getVfModuleName());
        LOGGER.info(vfModule.getVfModuleName());
    }

    @Test
    public void testGetVserver() throws Exception {
        String responseString = getAaiCqResponse();

        AaiCqResponse aaiCqResponse;
        aaiCqResponse = new AaiCqResponse(responseString);
        Vserver vserver = aaiCqResponse.getVserver();
        assertNotNull(vserver);
        assertEquals(ETE_VNF, vserver.getVserverName());
        LOGGER.info(vserver.getVserverName());

    }

    @Test
    public void testGetAllModelVer() throws Exception {
        String responseString = getAaiCqResponse();

        AaiCqResponse aaiCqResponse;
        aaiCqResponse = new AaiCqResponse(responseString);
        List<ModelVer> modelVerList = aaiCqResponse.getAllModelVer();
        assertNotNull(modelVerList);
        for (ModelVer modV : modelVerList) {
            LOGGER.info(modV.getModelName());
        }

    }

    @Test
    public void testGetModelVerByVersionId() throws Exception {
        String responseString = getAaiCqResponse();

        AaiCqResponse aaiCqResponse;
        aaiCqResponse = new AaiCqResponse(responseString);
        ModelVer modelVer =
            aaiCqResponse.getModelVerByVersionId("189a5070-3bd5-45ac-8a1d-c84ca40b277b");
        assertNotNull(modelVer);
        assertEquals("vFWCL_vFWSNK bbefb8ce-2bde", modelVer.getModelName());
        LOGGER.info(modelVer.getModelName());

    }

    @Test
    public void testGetVfModuleCount() throws Exception {
        String responseString = getAaiCqResponse();
        AaiCqResponse aaiCqResponse = new AaiCqResponse(responseString);
        int count = aaiCqResponse.getVfModuleCount("47958575-138f-452a-8c8d-d89b595f8164",
            "e6130d03-56f1-4b0a-9a1d-e1b2ebc30e0e", "94b18b1d-cc91-4f43-911a-e6348665f292");
        assertEquals(1, count);
    }

    /**
     * Provides sample CQ response.
     *
     * @return a CQ response
     * @throws Exception file read exception
     */
    public String getAaiCqResponse() throws Exception {
        return Files.readString(new File(CQ_RESPONSE_SAMPLE).toPath(), StandardCharsets.UTF_8);
    }

    /**
     * Creates a response object from the given json.
     * @param json response
     * @param success incremented if the test succeeds
     */
    private void runIt(String json, AtomicInteger success) {
        AaiCqResponse cq = new AaiCqResponse(json);
        List<VfModule> list = cq.getAllVfModules();
        assertFalse(list.isEmpty());
        success.incrementAndGet();
    }

}
