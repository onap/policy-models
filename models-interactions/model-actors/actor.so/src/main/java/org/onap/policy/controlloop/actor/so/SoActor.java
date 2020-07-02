/*
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2017-2020 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.controlloop.actor.so;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.onap.aai.domain.yang.CloudRegion;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.aai.domain.yang.ServiceInstance;
import org.onap.aai.domain.yang.Tenant;
import org.onap.policy.aai.AaiCqResponse;
import org.onap.policy.controlloop.ControlLoopOperation;
import org.onap.policy.controlloop.VirtualControlLoopEvent;
import org.onap.policy.controlloop.actorserviceprovider.impl.HttpActor;
import org.onap.policy.controlloop.actorserviceprovider.impl.HttpPollingOperator;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpPollingActorParams;
import org.onap.policy.controlloop.policy.Policy;
import org.onap.policy.so.SoCloudConfiguration;
import org.onap.policy.so.SoManager;
import org.onap.policy.so.SoModelInfo;
import org.onap.policy.so.SoOperationType;
import org.onap.policy.so.SoRelatedInstance;
import org.onap.policy.so.SoRelatedInstanceListElement;
import org.onap.policy.so.SoRequest;
import org.onap.policy.so.SoRequestDetails;
import org.onap.policy.so.SoRequestInfo;
import org.onap.policy.so.SoRequestParameters;
import org.onap.policy.so.util.Serialization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SoActor extends HttpActor<HttpPollingActorParams> {
    private static final Logger logger = LoggerFactory.getLogger(SoActor.class);

    public static final String NAME = "SO";

    // TODO old code: remove lines down to **HERE**

    private static final String TENANT_NOT_FOUND = "Tenant Item not found in AAI response {}";
    private static final String CONSTRUCTED_SO_MSG = "Constructed SO request: {}";

    // Strings for targets
    private static final String TARGET_VFC = "VFC";

    // Strings for recipes
    private static final String RECIPE_VF_MODULE_CREATE = "VF Module Create";
    private static final String RECIPE_VF_MODULE_DELETE = "VF Module Delete";

    private static final ImmutableList<String> recipes =
            ImmutableList.of(RECIPE_VF_MODULE_CREATE, RECIPE_VF_MODULE_DELETE);
    private static final ImmutableMap<String, List<String>> targets =
            new ImmutableMap.Builder<String, List<String>>().put(RECIPE_VF_MODULE_CREATE, ImmutableList.of(TARGET_VFC))
                    .put(RECIPE_VF_MODULE_DELETE, ImmutableList.of(TARGET_VFC)).build();

    // name of request parameters within policy payload
    public static final String REQ_PARAM_NM = "requestParameters";

    // name of configuration parameters within policy payload
    public static final String CONFIG_PARAM_NM = "configurationParameters";

    // used to decode configuration parameters via gson
    private static final Type CONFIG_TYPE = new TypeToken<List<Map<String, String>>>() {}.getType();

    // Static variables required to hold the IDs of the last service item, VNF item and VF Module.
    // Note that in
    // a multithreaded deployment this WILL break
    private static String lastVNFItemVnfId;
    private static String lastServiceItemServiceInstanceId;
    private static String lastVfModuleItemVfModuleInstanceId;

    // **HERE**

    /**
     * Constructs the object.
     */
    public SoActor() {
        super(NAME, HttpPollingActorParams.class);

        addOperator(new HttpPollingOperator(NAME, VfModuleCreate.NAME, VfModuleCreate::new));
        addOperator(new HttpPollingOperator(NAME, VfModuleDelete.NAME, VfModuleDelete::new));
    }

    // TODO old code: remove lines down to **HERE**

    @Override
    public String actor() {
        return NAME;
    }

    @Override
    public List<String> recipes() {
        return ImmutableList.copyOf(recipes);
    }

    @Override
    public List<String> recipeTargets(String recipe) {
        return ImmutableList.copyOf(targets.getOrDefault(recipe, Collections.emptyList()));
    }

    @Override
    public List<String> recipePayloads(String recipe) {
        return Collections.emptyList();
    }

    private SoModelInfo prepareSoModelInfo(Policy policy) {

        if (policy.getTarget() == null || policy.getTarget().getModelCustomizationId() == null
                        || policy.getTarget().getModelInvariantId() == null) {
            return null;
        }

        if (policy.getTarget().getModelName() == null || policy.getTarget().getModelVersion() == null
                        || policy.getTarget().getModelVersionId() == null) {
            return null;
        }

        SoModelInfo soModelInfo = new SoModelInfo();
        soModelInfo.setModelCustomizationId(policy.getTarget().getModelCustomizationId());
        soModelInfo.setModelInvariantId(policy.getTarget().getModelInvariantId());
        soModelInfo.setModelName(policy.getTarget().getModelName());
        soModelInfo.setModelVersion(policy.getTarget().getModelVersion());
        soModelInfo.setModelVersionId(policy.getTarget().getModelVersionId());
        soModelInfo.setModelType("vfModule");
        return soModelInfo;
    }

    /**
     * Construct requestInfo for the SO requestDetails.
     *
     * @return SO request information
     */
    private SoRequestInfo constructRequestInfo() {
        SoRequestInfo soRequestInfo = new SoRequestInfo();
        soRequestInfo.setSource("POLICY");
        soRequestInfo.setSuppressRollback(false);
        soRequestInfo.setRequestorId("policy");
        return soRequestInfo;
    }

    /**
     * This method is needed to get the serviceInstanceId and vnfInstanceId which is used in the asyncSORestCall.
     *
     * @param requestId the request Id
     * @param callback callback method
     * @param request the request
     * @param url SO REST URL
     * @param user username
     * @param password password
     */
    public static void sendRequest(String requestId, SoManager.SoCallback callback, Object request, String url,
            String user, String password) {
        SoManager soManager = new SoManager(url, user, password);
        soManager.asyncSoRestCall(requestId, callback, lastServiceItemServiceInstanceId, lastVNFItemVnfId,
                lastVfModuleItemVfModuleInstanceId, (SoRequest) request);
    }


    /**
     * Builds the request parameters from the policy payload.
     *
     * @param policy the policy
     * @param request request into which to stick the request parameters
     */
    private void buildRequestParameters(Policy policy, SoRequestDetails request) {
        // assume null until proven otherwise
        request.setRequestParameters(null);

        if (policy.getPayload() == null) {
            return;
        }

        String json = policy.getPayload().get(REQ_PARAM_NM);
        if (json == null) {
            return;
        }

        request.setRequestParameters(Serialization.gsonPretty.fromJson(json, SoRequestParameters.class));
    }

    /**
     * Builds the configuration parameters from the policy payload.
     *
     * @param policy the policy
     * @param request request into which to stick the configuration parameters
     */
    private void buildConfigurationParameters(Policy policy, SoRequestDetails request) {
        // assume null until proven otherwise
        request.setConfigurationParameters(null);

        if (policy.getPayload() == null) {
            return;
        }

        String json = policy.getPayload().get(CONFIG_PARAM_NM);
        if (json == null) {
            return;
        }

        request.setConfigurationParameters(Serialization.gsonPretty.fromJson(json, CONFIG_TYPE));
    }

    /**
     * This method is called to remember the last service instance ID, VNF Item VNF ID and vf module ID. Note these
     * fields are static, beware for multithreaded deployments
     *
     * @param vnfInstanceId update the last VNF instance ID to this value
     * @param serviceInstanceId update the last service instance ID to this value
     * @param vfModuleId update the vfModule instance ID to this value
     */
    private static void preserveInstanceIds(final String vnfInstanceId, final String serviceInstanceId,
            final String vfModuleId) {
        lastVNFItemVnfId = vnfInstanceId;
        lastServiceItemServiceInstanceId = serviceInstanceId;
        lastVfModuleItemVfModuleInstanceId = vfModuleId;
    }

    /**
     * Constructs a SO request conforming to the lcm API. The actual request is constructed and then placed in a wrapper
     * object used to send through DMAAP.
     *
     * @param onset the event that is reporting the alert for policy to perform an action
     * @param operation the control loop operation specifying the actor, operation, target, etc.
     * @param policy the policy the was specified from the yaml generated by CLAMP or through the Policy GUI/API
     * @param aaiCqResponse response from A&AI custom query
     * @return a SO request conforming to the lcm API using the DMAAP wrapper
     */
    public SoRequest constructRequestCq(VirtualControlLoopEvent onset, ControlLoopOperation operation, Policy policy,
            AaiCqResponse aaiCqResponse) {
        if (!NAME.equals(policy.getActor()) || !recipes().contains(policy.getRecipe())) {
            return null;
        }

        // A&AI named query should have been performed by now. If not, return null
        if (aaiCqResponse == null) {
            return null;
        }

        SoModelInfo soModelInfo = prepareSoModelInfo(policy);

        // Report the error vf module is not found
        if (soModelInfo == null) {
            logger.error("vf module is not found.");
            return null;
        }

        GenericVnf vnfItem;
        ServiceInstance vnfServiceItem;
        Tenant tenantItem;
        CloudRegion cloudRegionItem;

        // Extract the items we're interested in from the response
        try {
            vnfItem = aaiCqResponse.getGenericVnfByVfModuleModelInvariantId(soModelInfo.getModelInvariantId());
            //Report VNF not found
            if (vnfItem == null) {
                logger.error("Generic Vnf is not found.");
                return null;
            }
        } catch (Exception e) {
            logger.error("VNF Item not found in AAI response {}", Serialization.gsonPretty.toJson(aaiCqResponse), e);
            return null;
        }

        try {
            vnfServiceItem = aaiCqResponse.getServiceInstance();
        } catch (Exception e) {
            logger.error("VNF Service Item not found in AAI response {}",
                    Serialization.gsonPretty.toJson(aaiCqResponse), e);
            return null;
        }

        try {
            tenantItem = aaiCqResponse.getDefaultTenant();
        } catch (Exception e) {
            logger.error(TENANT_NOT_FOUND, Serialization.gsonPretty.toJson(aaiCqResponse), e);
            return null;
        }

        try {
            cloudRegionItem = aaiCqResponse.getDefaultCloudRegion();
        } catch (Exception e) {
            logger.error(TENANT_NOT_FOUND, Serialization.gsonPretty.toJson(aaiCqResponse), e);
            return null;
        }



        // Construct SO Request for a policy's recipe
        if (RECIPE_VF_MODULE_CREATE.equals(policy.getRecipe())) {
            return constructCreateRequestCq(aaiCqResponse, policy, tenantItem, vnfItem, vnfServiceItem, soModelInfo,
                    cloudRegionItem);
        } else if (RECIPE_VF_MODULE_DELETE.equals(policy.getRecipe())) {
            return constructDeleteRequestCq(tenantItem, vnfItem, vnfServiceItem, policy, cloudRegionItem);
        } else {
            return null;
        }
    }

    /**
     * Construct the So request, based on Custom Query response from A&AI.
     *
     * @param aaiCqResponse Custom query response from A&AI
     * @param policy policy information
     * @param tenantItem Tenant from CQ response
     * @param vnfItem Generic VNF from CQ response
     * @param vnfServiceItem Service Instance from CQ response
     * @param vfModuleItem VF Module from CustomQuery response
     * @param cloudRegionItem Cloud Region from Custom query response
     * @return SoRequest well formed So Request
     */
    private SoRequest constructCreateRequestCq(AaiCqResponse aaiCqResponse, Policy policy, Tenant tenantItem,
            GenericVnf vnfItem, ServiceInstance vnfServiceItem, SoModelInfo vfModuleItem, CloudRegion cloudRegionItem) {
        SoRequest request = new SoRequest();
        request.setOperationType(SoOperationType.SCALE_OUT);
        //
        //
        // Do NOT send So the requestId, they do not support this field
        //
        request.setRequestDetails(new SoRequestDetails());
        request.getRequestDetails().setRequestParameters(new SoRequestParameters());
        request.getRequestDetails().getRequestParameters().setUserParams(null);

        // cloudConfiguration
        request.getRequestDetails().setCloudConfiguration(constructCloudConfigurationCq(tenantItem, cloudRegionItem));
        // modelInfo
        request.getRequestDetails().setModelInfo(vfModuleItem);


        // requestInfo
        request.getRequestDetails().setRequestInfo(constructRequestInfo());
        request.getRequestDetails().getRequestInfo().setInstanceName("vfModuleName");

        // relatedInstanceList
        SoRelatedInstanceListElement relatedInstanceListElement1 = new SoRelatedInstanceListElement();
        SoRelatedInstanceListElement relatedInstanceListElement2 = new SoRelatedInstanceListElement();
        relatedInstanceListElement1.setRelatedInstance(new SoRelatedInstance());
        relatedInstanceListElement2.setRelatedInstance(new SoRelatedInstance());

        // Service Item
        relatedInstanceListElement1.getRelatedInstance().setInstanceId(vnfServiceItem.getServiceInstanceId());
        relatedInstanceListElement1.getRelatedInstance().setModelInfo(new SoModelInfo());
        relatedInstanceListElement1.getRelatedInstance().getModelInfo().setModelType("service");
        relatedInstanceListElement1.getRelatedInstance().getModelInfo()
                .setModelInvariantId(vnfServiceItem.getModelInvariantId());
        relatedInstanceListElement1.getRelatedInstance().getModelInfo()
                .setModelVersionId(vnfServiceItem.getModelVersionId());
        relatedInstanceListElement1.getRelatedInstance().getModelInfo()
                .setModelName(aaiCqResponse.getModelVerByVersionId(vnfServiceItem.getModelVersionId()).getModelName());
        relatedInstanceListElement1.getRelatedInstance().getModelInfo().setModelVersion(
                aaiCqResponse.getModelVerByVersionId(vnfServiceItem.getModelVersionId()).getModelVersion());


        // VNF Item
        relatedInstanceListElement2.getRelatedInstance().setInstanceId(vnfItem.getVnfId());
        relatedInstanceListElement2.getRelatedInstance().setModelInfo(new SoModelInfo());
        relatedInstanceListElement2.getRelatedInstance().getModelInfo().setModelType("vnf");
        relatedInstanceListElement2.getRelatedInstance().getModelInfo()
                .setModelInvariantId(vnfItem.getModelInvariantId());
        relatedInstanceListElement2.getRelatedInstance().getModelInfo().setModelVersionId(vnfItem.getModelVersionId());

        relatedInstanceListElement2.getRelatedInstance().getModelInfo()
                .setModelName(aaiCqResponse.getModelVerByVersionId(vnfItem.getModelVersionId()).getModelName());
        relatedInstanceListElement2.getRelatedInstance().getModelInfo().setModelVersion(
                aaiCqResponse.getModelVerByVersionId(vnfItem.getModelVersionId()).getModelVersion());


        relatedInstanceListElement2.getRelatedInstance().getModelInfo()
                .setModelCustomizationId(vnfItem.getModelCustomizationId());


        // Insert the Service Item and VNF Item
        request.getRequestDetails().getRelatedInstanceList().add(relatedInstanceListElement1);
        request.getRequestDetails().getRelatedInstanceList().add(relatedInstanceListElement2);

        // Request Parameters
        buildRequestParameters(policy, request.getRequestDetails());

        // Configuration Parameters
        buildConfigurationParameters(policy, request.getRequestDetails());
        // Save the instance IDs for the VNF and service to static fields
        // vfModuleId is not required for the create vf-module
        preserveInstanceIds(vnfItem.getVnfId(), vnfServiceItem.getServiceInstanceId(), null);
        if (logger.isDebugEnabled()) {
            logger.debug(CONSTRUCTED_SO_MSG, Serialization.gsonPretty.toJson(request));
        }
        return request;
    }

    /**
     * constructs delete request for So.
     *
     * @param tenantItem Tenant from A&AI CQ request
     * @param vnfItem Generic VNF from A&AI CQ request
     * @param vnfServiceItem ServiceInstance from A&AI CQ request
     * @param policy policy information
     * @param cloudRegionItem CloudRegion from A&AI CQ request
     * @return SoRequest deleted
     */
    private SoRequest constructDeleteRequestCq(Tenant tenantItem, GenericVnf vnfItem, ServiceInstance vnfServiceItem,
            Policy policy, CloudRegion cloudRegionItem) {
        SoRequest request = new SoRequest();
        request.setOperationType(SoOperationType.DELETE_VF_MODULE);
        request.setRequestDetails(new SoRequestDetails());
        request.getRequestDetails().setRelatedInstanceList(null);
        request.getRequestDetails().setConfigurationParameters(null);

        // cloudConfiguration
        request.getRequestDetails().setCloudConfiguration(constructCloudConfigurationCq(tenantItem, cloudRegionItem));
        // modelInfo
        request.getRequestDetails().setModelInfo(prepareSoModelInfo(policy));
        // requestInfo
        request.getRequestDetails().setRequestInfo(constructRequestInfo());
        // Save the instance IDs for the VNF, service and vfModule to static fields
        preserveInstanceIds(vnfItem.getVnfId(), vnfServiceItem.getServiceInstanceId(), null);

        if (logger.isDebugEnabled()) {
            logger.debug(CONSTRUCTED_SO_MSG, Serialization.gsonPretty.toJson(request));
        }
        return request;
    }


    /**
     * Construct cloudConfiguration for the SO requestDetails. Overridden for custom query.
     *
     * @param tenantItem tenant item from A&AI named-query response
     * @return SO cloud configuration
     */
    private SoCloudConfiguration constructCloudConfigurationCq(Tenant tenantItem, CloudRegion cloudRegionItem) {
        SoCloudConfiguration cloudConfiguration = new SoCloudConfiguration();
        cloudConfiguration.setTenantId(tenantItem.getTenantId());
        cloudConfiguration.setLcpCloudRegionId(cloudRegionItem.getCloudRegionId());
        return cloudConfiguration;
    }

    // **HERE**

}
