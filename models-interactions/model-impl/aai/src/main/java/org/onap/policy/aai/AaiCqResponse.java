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

import com.google.gson.annotations.SerializedName;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import org.eclipse.persistence.jaxb.JAXBContextFactory;
import org.eclipse.persistence.jaxb.JAXBContextProperties;
import org.json.JSONArray;
import org.json.JSONObject;
import org.onap.aai.domain.yang.v15.CloudRegion;
import org.onap.aai.domain.yang.v15.GenericVnf;
import org.onap.aai.domain.yang.v15.Relationship;
import org.onap.aai.domain.yang.v15.RelationshipData;
import org.onap.aai.domain.yang.v15.ServiceInstance;
import org.onap.aai.domain.yang.v15.Tenant;
import org.onap.aai.domain.yang.v15.VfModule;
import org.onap.aai.domain.yang.v15.Vserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AaiCqResponse {

    private static final String GENERIC_VNF = "generic-vnf";
    private static final String VF_MODULE = "vf-module";
    private static final Logger LOGGER = LoggerFactory.getLogger(AaiCqResponse.class);
    private static JAXBContext jaxbContext;
    private static Unmarshaller unmarshaller;


    // JABX initial stuff
    static {
        Map<String, Object> properties = new HashMap<>();
        properties.put(JAXBContextProperties.MEDIA_TYPE, "application/json");
        properties.put(JAXBContextProperties.JSON_INCLUDE_ROOT, false);
        // Define JAXB context
        try {
            jaxbContext = JAXBContextFactory.createContext(new Class[] {Vserver.class, GenericVnf.class, VfModule.class,
                CloudRegion.class, ServiceInstance.class, Tenant.class}, properties);
            unmarshaller = jaxbContext.createUnmarshaller();
        } catch (JAXBException e) {
            LOGGER.error("Could not initialize JAXBContext", e);
            LOGGER.info("Problem initiatlizing JAXBContext", e);
        }
    }

    @SerializedName("results")
    private List<Object> inventoryResponseItems = new LinkedList<>();


    /**
     * Constructor creates a custom query response from a valid json string.
     *
     * @param jsonString A&AI Custom Query response JSON string
     */
    public AaiCqResponse(String jsonString) {

        // Read JSON String and add all AaiObjects
        JSONObject responseObj = new JSONObject(jsonString);
        JSONArray resultsArray = new JSONArray();
        if (responseObj.has("results")) {
            resultsArray = (JSONArray) responseObj.get("results");
        }
        for (int i = 0; i < resultsArray.length(); i++) {
            // Object is a vserver
            if (resultsArray.getJSONObject(i).has("vserver")) {

                // Create the StreamSource by creating StringReader using the
                // JSON input
                StreamSource json = new StreamSource(
                        new StringReader(resultsArray.getJSONObject(i).getJSONObject("vserver").toString()));

                // Getting the vserver pojo again from the json
                Vserver vserver = this.getAaiObject(json, Vserver.class);
                this.inventoryResponseItems.add(vserver);
            }

            // Object is a Generic VNF
            if (resultsArray.getJSONObject(i).has(GENERIC_VNF)) {
                // Create the StreamSource by creating StringReader using the
                // JSON input
                StreamSource json = new StreamSource(
                        new StringReader(resultsArray.getJSONObject(i).getJSONObject(GENERIC_VNF).toString()));

                // Getting the generic vnf pojo again from the json
                GenericVnf genericVnf = this.getAaiObject(json, GenericVnf.class);

                this.inventoryResponseItems.add(genericVnf);
            }

            // Object is a Service Instance
            if (resultsArray.getJSONObject(i).has("service-instance")) {

                // Create the StreamSource by creating StringReader using the
                // JSON input
                StreamSource json = new StreamSource(
                        new StringReader(resultsArray.getJSONObject(i).getJSONObject("service-instance").toString()));

                // Getting the employee pojo again from the json
                ServiceInstance serviceInstance = this.getAaiObject(json, ServiceInstance.class);

                this.inventoryResponseItems.add(serviceInstance);
            }

            // Object is a VF Module
            if (resultsArray.getJSONObject(i).has(VF_MODULE)) {
                // Create the StreamSource by creating StringReader using the
                // JSON input
                StreamSource json = new StreamSource(
                        new StringReader(resultsArray.getJSONObject(i).getJSONObject(VF_MODULE).toString()));

                // Getting the vf module pojo again from the json
                VfModule vfModule = this.getAaiObject(json, VfModule.class);

                this.inventoryResponseItems.add(vfModule);
            }

            // Object is a CloudRegion
            if (resultsArray.getJSONObject(i).has("cloud-region")) {
                // Create the StreamSource by creating StringReader using the
                // JSON input
                StreamSource json = new StreamSource(
                        new StringReader(resultsArray.getJSONObject(i).getJSONObject("cloud-region").toString()));

                // Getting the cloud region pojo again from the json
                CloudRegion cloudRegion = this.getAaiObject(json, CloudRegion.class);

                this.inventoryResponseItems.add(cloudRegion);
            }

            // Object is a Tenant
            if (resultsArray.getJSONObject(i).has("tenant")) {
                // Create the StreamSource by creating StringReader using the
                // JSON input
                StreamSource json = new StreamSource(
                        new StringReader(resultsArray.getJSONObject(i).getJSONObject("tenant").toString()));

                // Getting the tenant pojo again from the json
                Tenant tenant = this.getAaiObject(json, Tenant.class);

                this.inventoryResponseItems.add(tenant);
            }

        }



    }



    private <T> T getAaiObject(StreamSource json, final Class<T> classOfResponse) {
        try {
            return unmarshaller.unmarshal(json, classOfResponse).getValue();
        } catch (JAXBException e) {
            LOGGER.error("JAXBCOntext error", e);
            return null;
        }
    }

    public List<Object> getInventoryResponseItems() {
        return inventoryResponseItems;
    }

    public void setInventoryResponseItems(List<Object> inventoryResponseItems) {
        this.inventoryResponseItems = inventoryResponseItems;
    }

    /**
     * Get list of A&AI objects in the custom query.
     *
     * @param classOfResponse Class of the type of A&AI objects to be returned
     * @return List A&AI objects matching the class
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> getItemListByType(Class<T> classOfResponse) {
        List<T> returnItemList = new ArrayList<>();
        for (Object i : this.inventoryResponseItems) {
            if (i.getClass() == classOfResponse) {
                returnItemList.add((T) i);
            }
        }
        return returnItemList;

    }

    /**
     * Get Service Instance.
     *
     * @return Service Instance
     */
    public ServiceInstance getServiceInstance() {
        ServiceInstance serviceInstance = null;
        for (Object i : this.inventoryResponseItems) {
            if (i.getClass() == ServiceInstance.class) {
                serviceInstance = (ServiceInstance) i;
            }
        }
        return serviceInstance;

    }

    /**
     * Get Tenant.
     *
     * @return Tenant
     */
    public Tenant getDefaultTenant() {
        Tenant tenant = null;
        for (Object i : this.inventoryResponseItems) {
            if (i.getClass() == Tenant.class) {
                tenant = (Tenant) i;
            }
        }
        return tenant;

    }

    /**
     * Get Cloud Region.
     *
     * @return Cloud Region
     */
    public CloudRegion getDefaultCloudRegion() {
        CloudRegion cloudRegion = null;
        for (Object i : this.inventoryResponseItems) {
            if (i.getClass() == CloudRegion.class) {
                cloudRegion = (CloudRegion) i;
            }
        }
        return cloudRegion;

    }

    /**
     * Get Generic Vnfs in the custom query.
     *
     * @return List of generic Vnf
     */
    public List<GenericVnf> getGenericVnfs() {
        List<GenericVnf> genericVnfList = new ArrayList<>();
        for (Object i : this.inventoryResponseItems) {
            if (i.getClass() == GenericVnf.class) {
                genericVnfList.add((GenericVnf) i);
            }
        }
        return genericVnfList;

    }


    /**
     * Returns a generic Vnf matching vnf name.
     *
     * @param vnfName Name of the vnf to match
     * @return generic Vnf
     */
    public GenericVnf getGenericVnfByVnfName(String vnfName) {
        List<GenericVnf> genericVnfList = new ArrayList<>();
        GenericVnf genericVnf = null;
        for (Object i : this.inventoryResponseItems) {
            if (i.getClass() == GenericVnf.class) {
                genericVnfList.add((GenericVnf) i);
            }
        }

        for (GenericVnf genVnf : genericVnfList) {
            if (vnfName.equals(genVnf.getVnfName())) {
                genericVnf = genVnf;
            }

        }
        return genericVnf;

    }

    /**
     * Returns a generic Vnf matching model invariant ID.
     *
     * @param modelInvariantId Name of the vnf to match
     * @return generic Vnf
     */
    public GenericVnf getGenericVnfByModelInvariantId(String modelInvariantId) {
        List<GenericVnf> genericVnfList = new ArrayList<>();
        GenericVnf genericVnf = null;
        for (Object i : this.inventoryResponseItems) {
            if (i.getClass() == GenericVnf.class) {
                genericVnfList.add((GenericVnf) i);
            }
        }

        for (GenericVnf genVnf : genericVnfList) {
            if (modelInvariantId.equals(genVnf.getModelInvariantId())) {
                genericVnf = genVnf;
            }

        }
        return genericVnf;

    }



    /**
     * Get the generic vnf associated with the vserver in the custom query.
     *
     * @return Generic VNF
     */
    public GenericVnf getDefaultGenericVnf() {
        GenericVnf genericVnf = null;

        // Get the vserver associated with the query
        Vserver vserver = this.getVserver();

        // Get the relationships of the vserver
        List<Relationship> relations = vserver.getRelationshipList().getRelationship();

        // Find the relationship of the genericVNF
        String genericVnfId = "";
        List<RelationshipData> relationshipData = null;

        // Iterate through the list of relationships and get generic vnf relationship data
        for (Relationship r : relations) {
            // Get the name of generic-vnf related to this server
            if (GENERIC_VNF.equals(r.getRelatedTo())) {
                relationshipData = r.getRelationshipData();
            }
        }

        // Iterate through relationship data, and get vnf-id
        for (RelationshipData rd : relationshipData) {
            // Get the id of the generic-vnf
            if ("generic-vnf.vnf-id".equals(rd.getRelationshipKey())) {
                genericVnfId = rd.getRelationshipValue();
            }
        }

        // Get the list of generic vnfs
        List<GenericVnf> genericVnfList = this.getGenericVnfs();

        for (GenericVnf genVnf : genericVnfList) {
            if (genericVnfId.equals(genVnf.getVnfId())) {
                genericVnf = genVnf;
            }
        }

        return genericVnf;
    }


    /**
     * Get Vf Module associated with the vserver in the custom query.
     *
     * @return Vf Module
     */
    public VfModule getDefaultVfModule() {
        GenericVnf genericVnf = null;
        VfModule vfModule = null;

        // Get the vserver associated with the query
        Vserver vserver = this.getVserver();

        // Get the relationships of the vserver
        List<Relationship> relations = vserver.getRelationshipList().getRelationship();

        // Find the relationship of VfModule
        String vfModuleId = "";
        List<RelationshipData> relationshipData = null;

        // Iterate through the list of relationships and get vf module relationship data
        for (Relationship r : relations) {
            // Get relationship data of vfmodule related to this server
            if (VF_MODULE.equals(r.getRelatedTo())) {
                relationshipData = r.getRelationshipData();
            }
        }

        // Iterate through relationship data, and get vf-module-id
        for (RelationshipData rd : relationshipData) {
            // Get the id of the vf-module
            if ("vf-module.vf-module-id".equals(rd.getRelationshipKey())) {
                vfModuleId = rd.getRelationshipValue();
            }
        }

        // Get the generic VNF associated with this vserver query
        genericVnf = this.getDefaultGenericVnf();

        // Get the list of VFmodules associated with this generic Vnf
        List<VfModule> vfModuleList = genericVnf.getVfModules().getVfModule();

        for (VfModule vfMod : vfModuleList) {
            if (vfModuleId.equals(vfMod.getVfModuleId())) {
                vfModule = vfMod;
            }
        }

        return vfModule;
    }


    /**
     * Get vf modules in the custom query.
     *
     * @return List of VfModule
     */
    public List<VfModule> getAllVfModules() {
        List<VfModule> vfModuleList = new ArrayList<>();

        for (GenericVnf genVnf : this.getGenericVnfs()) {
            vfModuleList.addAll(genVnf.getVfModules().getVfModule());
        }
        return vfModuleList;

    }

    /**
     * Get Vf Module matching a specific VF module name.
     *
     * @return VfModule
     */
    public VfModule getVfModuleByVfModuleName(String vfModuleName) {
        VfModule vfModule = null;

        for (VfModule vfMod : this.getAllVfModules()) {
            if (vfModuleName.equals(vfMod.getVfModuleName())) {
                vfModule = vfMod;
            }

        }
        return vfModule;
    }

    /**
     * Get verver in the custom query.
     *
     * @return Vserver
     */
    public Vserver getVserver() {
        Vserver vserver = null;
        int index = 0;
        while (this.inventoryResponseItems.get(index).getClass() != Vserver.class) {
            index = index + 1;
        }
        vserver = (Vserver) this.inventoryResponseItems.get(index);
        return vserver;

    }

}
