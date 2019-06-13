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

import static org.junit.Assert.assertNotNull;

import java.util.LinkedList;
import org.junit.Test;
import org.onap.policy.aai.util.Serialization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AaiNqInventoryResponseItemTest {
    private static final String WIDGET = "widget";
    private static final String SERVICE_INSTANCE = "service-instance";
    private static final String MODEL_VERSION_KEY = "model.model-version";
    private static final String MODEL_TYPE_KEY = "model.model-type";
    private static final String MODEL_NAME_KEY = "model.model-name";
    private static final String MODEL_ID_KEY = "model.model-id";
    private static final String PERSONA_MODEL_ID2 = "ef86f9c5-2165-44f3-8fc3-96018b609ea5";
    private static final String PERSONA_MODEL_ID = "82194af1-3c2c-485a-8f44-420e22a9eaa4";
    private static final String RESOURCE_VERSION = "1485366450";
    private static final Logger logger = LoggerFactory.getLogger(AaiNqInventoryResponseItemTest.class);

    @Test
    public void test() {
        AaiNqInventoryResponseItem aaiNqInventoryResponseItem = new AaiNqInventoryResponseItem();
        aaiNqInventoryResponseItem.setModelName(SERVICE_INSTANCE);
        AaiNqCloudRegion aaiNqCloudRegion = new AaiNqCloudRegion();
        aaiNqCloudRegion.setCloudOwner("OWNER");
        aaiNqCloudRegion.setCloudRegionId("REGIONID");
        aaiNqCloudRegion.setCloudRegionVersion("2.5");
        aaiNqCloudRegion.setComplexName("COMPLEXNAME");
        aaiNqCloudRegion.setResourceVersion("1485365988");
        aaiNqInventoryResponseItem.setCloudRegion(aaiNqCloudRegion);
        AaiNqExtraProperties aaiNqExtraProperties = new AaiNqExtraProperties();
        aaiNqExtraProperties.setExtraProperty(new LinkedList<>());
        aaiNqExtraProperties.getExtraProperty().add(new AaiNqExtraProperty(MODEL_NAME_KEY, "generic-vnf"));
        aaiNqExtraProperties.getExtraProperty().add(new AaiNqExtraProperty(MODEL_TYPE_KEY, WIDGET));
        aaiNqExtraProperties.getExtraProperty().add(new AaiNqExtraProperty(MODEL_VERSION_KEY, "1.0"));
        aaiNqExtraProperties.getExtraProperty()
                .add(new AaiNqExtraProperty(MODEL_ID_KEY, "acc6edd8-a8d4-4b93-afaa-0994068be14c"));
        aaiNqExtraProperties.getExtraProperty()
                .add(new AaiNqExtraProperty("model.model-name-version-id", "93a6166f-b3d5-4f06-b4ba-aed48d009ad9"));
        aaiNqInventoryResponseItem.setExtraProperties(aaiNqExtraProperties);
        AaiNqGenericVnf aaiNqGenericVnf = new AaiNqGenericVnf();
        aaiNqGenericVnf.setVnfId("dhv-test-gvnf");
        aaiNqGenericVnf.setVnfName("dhv-test-gvnf-name");
        aaiNqGenericVnf.setVnfName2("dhv-test-gvnf-name2");
        aaiNqGenericVnf.setVnfType("SW");
        aaiNqGenericVnf.setServiceId("d7bb0a21-66f2-4e6d-87d9-9ef3ced63ae4");
        aaiNqGenericVnf.setProvStatus("PREPROV");
        aaiNqGenericVnf.setOperationalState("dhv-test-operational-state");
        aaiNqGenericVnf.setIpv4OamAddress("dhv-test-gvnf-ipv4-oam-address");
        aaiNqGenericVnf.setIpv4Loopback0Address("dhv-test-gvnfipv4-loopback0-address");
        aaiNqGenericVnf.setInMaint(false);
        aaiNqGenericVnf.setIsClosedLoopDisabled(false);
        aaiNqGenericVnf.setResourceVersion(RESOURCE_VERSION);
        aaiNqGenericVnf.setEncrypedAccessFlag(true);
        aaiNqGenericVnf.setPersonaModelId("acc6edd8-a8d4-4b93-afaa-0994068be14c");
        aaiNqGenericVnf.setPersonaModelVersion("1.0");
        aaiNqInventoryResponseItem.setGenericVnf(aaiNqGenericVnf);
        AaiNqInventoryResponseItem serviceItem = new AaiNqInventoryResponseItem();
        serviceItem.setModelName(SERVICE_INSTANCE);
        serviceItem.setServiceInstance(new AaiNqServiceInstance());
        serviceItem.getServiceInstance().setServiceInstanceId("dhv-test-vhnfportal-service-instance-id");
        serviceItem.getServiceInstance().setServiceInstanceName("dhv-test-service-instance-name1");
        serviceItem.getServiceInstance().setPersonaModelId(PERSONA_MODEL_ID);
        serviceItem.getServiceInstance().setPersonaModelVersion("1.0");
        serviceItem.getServiceInstance().setServiceInstanceLocationId("dhv-test-service-instance-location-id1");
        serviceItem.getServiceInstance().setResourceVersion("1485366092");
        serviceItem.setExtraProperties(new AaiNqExtraProperties());
        serviceItem.getExtraProperties().getExtraProperty()
                .add(new AaiNqExtraProperty(MODEL_NAME_KEY, SERVICE_INSTANCE));
        serviceItem.getExtraProperties().getExtraProperty().add(new AaiNqExtraProperty(MODEL_TYPE_KEY, WIDGET));
        serviceItem.getExtraProperties().getExtraProperty().add(new AaiNqExtraProperty(MODEL_VERSION_KEY, "1.0"));
        serviceItem.getExtraProperties().getExtraProperty()
                .add(new AaiNqExtraProperty(MODEL_ID_KEY, PERSONA_MODEL_ID));
        serviceItem.getExtraProperties().getExtraProperty()
                .add(new AaiNqExtraProperty(MODEL_NAME_KEY, "46b92144-923a-4d20-b85a-3cbd847668a9"));

        AaiNqInventoryResponseItem vfModuleItem = new AaiNqInventoryResponseItem();
        vfModuleItem.setModelName("vf-module");
        vfModuleItem.setVfModule(new AaiNqVfModule());
        vfModuleItem.getVfModule().setVfModuleId("example-vf-module-id-val-49261");
        vfModuleItem.getVfModule().setVfModuleName("example-vf-module-name-val-73074");
        vfModuleItem.getVfModule().setHeatStackId("example-heat-stack-id-val-86300");
        vfModuleItem.getVfModule().setOrchestrationStatus("example-orchestration-status-val-56523");
        vfModuleItem.getVfModule().setIsBaseVfModule(true);
        vfModuleItem.getVfModule().setResourceVersion(RESOURCE_VERSION);
        vfModuleItem.getVfModule().setPersonaModelId(PERSONA_MODEL_ID2);
        vfModuleItem.getVfModule().setPersonaModelVersion("1.0");
        vfModuleItem.getVfModule().setWidgetModelId("example-widget-model-id-val-92571");
        vfModuleItem.getVfModule().setWidgetModelVersion("example-widget-model-version-val-83317");
        vfModuleItem.getVfModule().setContrailServiceInstanceFqdn("example-contrail-service-instance-fqdn-val-86796");
        vfModuleItem.setExtraProperties(new AaiNqExtraProperties());
        vfModuleItem.getExtraProperties().getExtraProperty()
                .add(new AaiNqExtraProperty(MODEL_NAME_KEY, "vf-module"));
        vfModuleItem.getExtraProperties().getExtraProperty().add(new AaiNqExtraProperty(MODEL_TYPE_KEY, WIDGET));
        vfModuleItem.getExtraProperties().getExtraProperty().add(new AaiNqExtraProperty(MODEL_VERSION_KEY, "1.0"));
        vfModuleItem.getExtraProperties().getExtraProperty()
                .add(new AaiNqExtraProperty(MODEL_ID_KEY, PERSONA_MODEL_ID2));
        vfModuleItem.getExtraProperties().getExtraProperty()
                .add(new AaiNqExtraProperty(MODEL_NAME_KEY, "c00563ae-812b-4e62-8330-7c4d0f47088a"));

        AaiNqInventoryResponseItems aaiNqInventoryResponseItems = new AaiNqInventoryResponseItems();
        aaiNqInventoryResponseItems.getInventoryResponseItems().add(serviceItem);
        aaiNqInventoryResponseItems.getInventoryResponseItems().add(vfModuleItem);
        aaiNqInventoryResponseItem.setItems(aaiNqInventoryResponseItems);
        aaiNqInventoryResponseItem.setModelName("model-name");
        AaiNqServiceInstance serviceInstance = new AaiNqServiceInstance();
        serviceInstance.setServiceInstanceId("dhv-test-vhnfportal-service-instance-id");
        serviceInstance.setServiceInstanceName("dhv-test-service-instance-name1");
        serviceInstance.setPersonaModelId(PERSONA_MODEL_ID);
        serviceInstance.setPersonaModelVersion("1.0");
        serviceInstance.setServiceInstanceLocationId("dhv-test-service-instance-location-id1");
        serviceInstance.setResourceVersion("1485366092");
        aaiNqInventoryResponseItem.setServiceInstance(serviceInstance);
        AaiNqTenant aaiNqTenant = new AaiNqTenant();
        aaiNqTenant.setTenantId("dhv-test-tenant");
        aaiNqTenant.setTenantName("dhv-test-tenant-name");
        aaiNqTenant.setResourceVersion("1485366334");
        aaiNqInventoryResponseItem.setTenant(aaiNqTenant);
        AaiNqVfModule aaiNqVfModule = new AaiNqVfModule();
        aaiNqVfModule.setVfModuleId("example-vf-module-id-val-49261");
        aaiNqVfModule.setVfModuleName("example-vf-module-name-val-73074");
        aaiNqVfModule.setHeatStackId("example-heat-stack-id-val-86300");
        aaiNqVfModule.setOrchestrationStatus("example-orchestration-status-val-56523");
        aaiNqVfModule.setIsBaseVfModule(true);
        aaiNqVfModule.setResourceVersion(RESOURCE_VERSION);
        aaiNqVfModule.setPersonaModelId(PERSONA_MODEL_ID2);
        aaiNqVfModule.setPersonaModelVersion("1.0");
        aaiNqVfModule.setWidgetModelId("example-widget-model-id-val-92571");
        aaiNqVfModule.setWidgetModelVersion("example-widget-model-version-val-83317");
        aaiNqVfModule.setContrailServiceInstanceFqdn("example-contrail-service-instance-fqdn-val-86796");
        aaiNqInventoryResponseItem.setVfModule(aaiNqVfModule);
        AaiNqVServer aaiNqVServer = new AaiNqVServer();
        aaiNqVServer.setVserverId("dhv-test-vserver");
        aaiNqVServer.setVserverName("dhv-test-vserver-name");
        aaiNqVServer.setVserverName2("dhv-test-vserver-name2");
        aaiNqVServer.setProvStatus("PREPROV");
        aaiNqVServer.setVserverSelflink("dhv-test-vserver-selflink");
        aaiNqVServer.setInMaint(false);
        aaiNqVServer.setIsClosedLoopDisabled(false);
        aaiNqVServer.setResourceVersion("1485366417");
        aaiNqInventoryResponseItem.setVserver(aaiNqVServer);
        assertNotNull(aaiNqInventoryResponseItem);

        logger.info(Serialization.gsonPretty.toJson(aaiNqInventoryResponseItem));
    }

}
