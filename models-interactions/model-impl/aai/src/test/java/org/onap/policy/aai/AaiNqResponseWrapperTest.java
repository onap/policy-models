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
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.onap.policy.aai.util.Serialization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AaiNqResponseWrapperTest {
    private static final Logger logger = LoggerFactory.getLogger(AaiNqResponseWrapperTest.class);

    @Test
    public void test() {
        AaiNqInventoryResponseItem serviceItem = new AaiNqInventoryResponseItem();
        serviceItem.setModelName("service-instance");
        serviceItem.setServiceInstance(new AaiNqServiceInstance());
        serviceItem.getServiceInstance().setServiceInstanceId("dhv-test-vhnfportal-service-instance-id");
        serviceItem.getServiceInstance().setServiceInstanceName("dhv-test-service-instance-name1");
        serviceItem.getServiceInstance().setPersonaModelId("82194af1-3c2c-485a-8f44-420e22a9eaa4");
        serviceItem.getServiceInstance().setPersonaModelVersion("1.0");
        serviceItem.getServiceInstance().setServiceInstanceLocationId("dhv-test-service-instance-location-id1");
        serviceItem.getServiceInstance().setResourceVersion("1485366092");
        serviceItem.setExtraProperties(new AaiNqExtraProperties());
        serviceItem.getExtraProperties().getExtraProperty()
                .add(new AaiNqExtraProperty("model.model-name", "service-instance"));
        serviceItem.getExtraProperties().getExtraProperty().add(new AaiNqExtraProperty("model.model-type", "widget"));
        serviceItem.getExtraProperties().getExtraProperty().add(new AaiNqExtraProperty("model.model-version", "1.0"));
        serviceItem.getExtraProperties().getExtraProperty()
                .add(new AaiNqExtraProperty("model.model-id", "82194af1-3c2c-485a-8f44-420e22a9eaa4"));
        serviceItem.getExtraProperties().getExtraProperty()
                .add(new AaiNqExtraProperty("model.model-name", "46b92144-923a-4d20-b85a-3cbd847668a9"));

        AaiNqInventoryResponseItem vfModuleItem = new AaiNqInventoryResponseItem();
        vfModuleItem.setModelName("vf-module");
        vfModuleItem.setVfModule(new AaiNqVfModule());
        vfModuleItem.getVfModule().setVfModuleId("example-vf-module-id-val-49261");
        vfModuleItem.getVfModule().setVfModuleName("example-vf-module-name-val-73074");
        vfModuleItem.getVfModule().setHeatStackId("example-heat-stack-id-val-86300");
        vfModuleItem.getVfModule().setOrchestrationStatus("example-orchestration-status-val-56523");
        vfModuleItem.getVfModule().setIsBaseVfModule(true);
        vfModuleItem.getVfModule().setResourceVersion("1485366450");
        vfModuleItem.getVfModule().setPersonaModelId("ef86f9c5-2165-44f3-8fc3-96018b609ea5");
        vfModuleItem.getVfModule().setPersonaModelVersion("1.0");
        vfModuleItem.getVfModule().setWidgetModelId("example-widget-model-id-val-92571");
        vfModuleItem.getVfModule().setWidgetModelVersion("example-widget-model-version-val-83317");
        vfModuleItem.getVfModule().setContrailServiceInstanceFqdn("example-contrail-service-instance-fqdn-val-86796");
        vfModuleItem.setExtraProperties(new AaiNqExtraProperties());
        vfModuleItem.getExtraProperties().getExtraProperty()
                .add(new AaiNqExtraProperty("model.model-name", "vf-module"));
        vfModuleItem.getExtraProperties().getExtraProperty().add(new AaiNqExtraProperty("model.model-type", "widget"));
        vfModuleItem.getExtraProperties().getExtraProperty().add(new AaiNqExtraProperty("model.model-version", "1.0"));
        vfModuleItem.getExtraProperties().getExtraProperty()
                .add(new AaiNqExtraProperty("model.model-id", "ef86f9c5-2165-44f3-8fc3-96018b609ea5"));
        vfModuleItem.getExtraProperties().getExtraProperty()
                .add(new AaiNqExtraProperty("model.model-name", "c00563ae-812b-4e62-8330-7c4d0f47088a"));

        AaiNqInventoryResponseItem genericVnfItem = new AaiNqInventoryResponseItem();
        genericVnfItem.setModelName("generic-vnf");
        genericVnfItem.setGenericVnf(new AaiNqGenericVnf());
        genericVnfItem.getGenericVnf().setVnfId("dhv-test-gvnf");
        genericVnfItem.getGenericVnf().setVnfName("dhv-test-gvnf-name");
        genericVnfItem.getGenericVnf().setVnfName2("dhv-test-gvnf-name2");
        genericVnfItem.getGenericVnf().setVnfType("SW");
        genericVnfItem.getGenericVnf().setServiceId("d7bb0a21-66f2-4e6d-87d9-9ef3ced63ae4");
        genericVnfItem.getGenericVnf().setProvStatus("PREPROV");
        genericVnfItem.getGenericVnf().setOperationalState("dhv-test-operational-state");
        genericVnfItem.getGenericVnf().setIpv4OamAddress("dhv-test-gvnf-ipv4-oam-address");
        genericVnfItem.getGenericVnf().setIpv4Loopback0Address("dhv-test-gvnfipv4-loopback0-address");
        genericVnfItem.getGenericVnf().setInMaint(false);
        genericVnfItem.getGenericVnf().setIsClosedLoopDisabled(false);
        genericVnfItem.getGenericVnf().setResourceVersion("1485366450");
        genericVnfItem.getGenericVnf().setEncrypedAccessFlag(true);
        genericVnfItem.getGenericVnf().setPersonaModelId("acc6edd8-a8d4-4b93-afaa-0994068be14c");
        genericVnfItem.getGenericVnf().setPersonaModelVersion("1.0");
        genericVnfItem.setExtraProperties(new AaiNqExtraProperties());
        genericVnfItem.getExtraProperties().setExtraProperty(new LinkedList<>());
        genericVnfItem.getExtraProperties().getExtraProperty()
                .add(new AaiNqExtraProperty("model.model-name", "generic-vnf"));
        genericVnfItem.getExtraProperties().getExtraProperty()
                .add(new AaiNqExtraProperty("model.model-type", "widget"));
        genericVnfItem.getExtraProperties().getExtraProperty()
                .add(new AaiNqExtraProperty("model.model-version", "1.0"));
        genericVnfItem.getExtraProperties().getExtraProperty()
                .add(new AaiNqExtraProperty("model.model-id", "acc6edd8-a8d4-4b93-afaa-0994068be14c"));
        genericVnfItem.getExtraProperties().getExtraProperty()
                .add(new AaiNqExtraProperty("model.model-name-version-id", "93a6166f-b3d5-4f06-b4ba-aed48d009ad9"));
        genericVnfItem.setItems(new AaiNqInventoryResponseItems());
        genericVnfItem.getItems().setInventoryResponseItems(new LinkedList<>());
        genericVnfItem.getItems().getInventoryResponseItems().add(serviceItem);
        genericVnfItem.getItems().getInventoryResponseItems().add(vfModuleItem);

        AaiNqInventoryResponseItem cloudItem = new AaiNqInventoryResponseItem();
        cloudItem.setCloudRegion(new AaiNqCloudRegion());
        cloudItem.getCloudRegion().setCloudOwner("OWNER");
        cloudItem.getCloudRegion().setCloudRegionId("REGIONID");
        cloudItem.getCloudRegion().setCloudRegionVersion("2.5");
        cloudItem.getCloudRegion().setComplexName("COMPLEXNAME");
        cloudItem.getCloudRegion().setResourceVersion("1485365988");

        AaiNqInventoryResponseItem tenantItem = new AaiNqInventoryResponseItem();
        tenantItem.setTenant(new AaiNqTenant());
        tenantItem.getTenant().setTenantId("dhv-test-tenant");
        tenantItem.getTenant().setTenantName("dhv-test-tenant-name");
        tenantItem.getTenant().setResourceVersion("1485366334");
        tenantItem.setItems(new AaiNqInventoryResponseItems());
        tenantItem.getItems().setInventoryResponseItems(new LinkedList<>());
        tenantItem.getItems().getInventoryResponseItems().add(cloudItem);

        AaiNqInventoryResponseItem vserverItem = new AaiNqInventoryResponseItem();
        vserverItem.setVserver(new AaiNqVServer());
        vserverItem.getVserver().setVserverId("dhv-test-vserver");
        vserverItem.getVserver().setVserverName("dhv-test-vserver-name");
        vserverItem.getVserver().setVserverName2("dhv-test-vserver-name2");
        vserverItem.getVserver().setProvStatus("PREPROV");
        vserverItem.getVserver().setVserverSelflink("dhv-test-vserver-selflink");
        vserverItem.getVserver().setInMaint(false);
        vserverItem.getVserver().setIsClosedLoopDisabled(false);
        vserverItem.getVserver().setResourceVersion("1485366417");
        vserverItem.setItems(new AaiNqInventoryResponseItems());
        vserverItem.getItems().setInventoryResponseItems(new LinkedList<>());
        vserverItem.getItems().getInventoryResponseItems().add(genericVnfItem);
        vserverItem.getItems().getInventoryResponseItems().add(tenantItem);

        AaiNqResponse aaiResponse = new AaiNqResponse();
        aaiResponse.getInventoryResponseItems().add(vserverItem);
        AaiNqResponseWrapper aaiNqResponseWarapper = new AaiNqResponseWrapper();
        aaiNqResponseWarapper.setAaiNqResponse(aaiResponse);
        aaiNqResponseWarapper.setRequestId(UUID.randomUUID());
        assertNotNull(aaiNqResponseWarapper);
        logger.info(Serialization.gsonPretty.toJson(aaiNqResponseWarapper));

        AaiNqResponse aaiResponse2 = new AaiNqResponse();
        aaiResponse2.getInventoryResponseItems().add(vserverItem);
        AaiNqResponseWrapper aaiNqResponseWarapper2 = new AaiNqResponseWrapper(UUID.randomUUID(), aaiResponse);
        assertNotNull(aaiNqResponseWarapper2);
        assertNotNull(aaiNqResponseWarapper2.getRequestId());
        assertNotNull(aaiNqResponseWarapper2.getAaiNqResponse());
        logger.info(Serialization.gsonPretty.toJson(aaiNqResponseWarapper2));
    }

    @Test
    public void testCountVfModules() throws Exception {
        AaiNqResponseWrapper resp;

        // null item
        resp = new AaiNqResponseWrapper();
        assertEquals(0, resp.countVfModules());
        
        // no names
        resp.setAaiNqResponse(load("AaiNqResponseWrapper-NoNames.json"));
        assertEquals(0, resp.countVfModules());

        // has VF modules
        resp.setAaiNqResponse(load("AaiNqResponseWrapper-Vserver.json"));
        assertEquals(3, resp.countVfModules());
    }

    @Test
    public void testGenVfModuleName() throws Exception {
        AaiNqResponseWrapper resp;

        // null item
        resp = new AaiNqResponseWrapper();
        assertEquals(null, resp.genVfModuleName());
        
        // no names
        resp.setAaiNqResponse(load("AaiNqResponseWrapper-NoNames.json"));
        assertEquals(null, resp.genVfModuleName());

        // has VF modules
        resp.setAaiNqResponse(load("AaiNqResponseWrapper-Vserver.json"));
        assertEquals("my-module-abc_124", resp.genVfModuleName());
    }

    @Test
    public void testGetVfModules() throws Exception {
        AaiNqResponseWrapper resp;

        // null item
        resp = new AaiNqResponseWrapper();
        assertTrue(resp.getVfModuleItems(true).isEmpty());

        // missing item
        resp = new AaiNqResponseWrapper();
        resp.setAaiNqResponse(new AaiNqResponse());
        assertTrue(resp.getVfModuleItems(false).isEmpty());

        // null item list
        resp.setAaiNqResponse(load("AaiNqResponseWrapper-NoItems.json"));
        resp.getAaiNqResponse().getInventoryResponseItems().get(0).getItems().getInventoryResponseItems().get(0)
                        .getItems().setInventoryResponseItems(null);
        assertTrue(resp.getVfModuleItems(false).isEmpty());
        
        // no modules
        resp.setAaiNqResponse(load("AaiNqResponseWrapper-NoModules.json"));
        assertTrue(resp.getVfModuleItems(false).isEmpty());
        
        // no names
        resp.setAaiNqResponse(load("AaiNqResponseWrapper-NoNames.json"));
        List<AaiNqInventoryResponseItem> lst;
        lst = resp.getVfModuleItems(false);
        assertEquals(0, lst.size());

        // base VF modules
        resp.setAaiNqResponse(load("AaiNqResponseWrapper-Vserver.json"));
        lst = resp.getVfModuleItems(true);
        assertEquals(1, lst.size());
        assertEquals("Vfmodule_vLBMS-0809-1", lst.get(0).getVfModule().getVfModuleName());
        
        // non base VF modules
        resp.setAaiNqResponse(load("AaiNqResponseWrapper-Vserver.json"));
        lst = resp.getVfModuleItems(false);
        assertEquals(3, lst.size());
        int index;
        index = 0;
        assertEquals("my-module-abc_1", lst.get(index++).getVfModule().getVfModuleName());
        assertEquals("my-module-abc_123", lst.get(index++).getVfModule().getVfModuleName());
        assertEquals("my-module-abc_34", lst.get(index++).getVfModule().getVfModuleName());
    }

    /**
     * Loads a response from a JSON file.
     * 
     * @param fileName name of the file containing the JSON response
     * @return the response
     * @throws IOException if the file cannot be read
     */
    private AaiNqResponse load(String fileName) throws IOException {
        String json = IOUtils.toString(getClass().getResource(fileName), StandardCharsets.UTF_8);
        return Serialization.gsonPretty.fromJson(json, AaiNqResponse.class);
    }
}
