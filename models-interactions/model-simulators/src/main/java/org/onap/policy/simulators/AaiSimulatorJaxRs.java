/*-
 * ============LICENSE_START=======================================================
 * simulators
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.simulators;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.UUID;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.apache.commons.io.IOUtils;
import org.onap.policy.aai.AaiNqRequest;
import org.onap.policy.aai.util.Serialization;

@Path("/aai")
public class AaiSimulatorJaxRs {

    private static final String VSERVER = "vserver";
    private static final String DISABLE_CLOSEDLOOP = "disableClosedLoop";
    private static final String ERROR = "error";
    private static final String GETFAIL = "getFail";

    /**
     * A&AI get query.
     *
     * @param vnfId the VNF Id
     * @return the result
     */
    @GET
    @Path("/v8/network/generic-vnfs/generic-vnf/{vnfId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces("application/json")
    public String aaiGetQuery(@PathParam("vnfID") final String vnfId) {
        return "{\"relationship-list\": {\"relationship\":[{\"related-to-property\": [{\"property-key\": "
                + "\"service-instance.service-instance-name\"}]},{\"related-to-property\": [ {\"property-key\": "
                + "\"vserver.vserver-name\",\"property-value\": \"USUCP0PCOIL0110UJZZ01-vsrx\" }]} ]}}";
    }

    /**
     * A&AI get query.
     *
     * @return the result
     */
    @GET
    @Path("/v16/search/nodes-query")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces("application/json")
    public String aaiGetVserverQuery() {
        return "{\"result-data\":[{\"resource-type\": \"vserver\",\"resource-link\":\"/aai/v15/"
                + "cloud-infrastructure/cloud-regions/cloud-region/CloudOwner/RegionOne/tenants"
                + "/tenant/3f2aaef74ecb4b19b35e26d0849fe9a2/vservers/vserver/"
                + "6c3b3714-e36c-45af-9f16-7d3a73d99497\"}]}}";
    }


    /**
     * A&AI put query.
     *
     * @param req the request
     * @return the response
     * @throws IOException if a response file cannot be read
     */
    @PUT
    @Path("/v16/query")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces("application/json")
    public String aaiPutQuery(final String req) throws IOException {
        return IOUtils.toString(getClass().getResource("aai/AaiCqResponse.json"), StandardCharsets.UTF_8);
    }

    /**
     * A&AI post query.
     *
     * @param req the request
     * @return the response
     * @throws IOException if a response file cannot be read
     */
    @POST
    @Path("/search/named-query")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces("application/json")
    public String aaiPostQuery(final String req) throws IOException {
        final AaiNqRequest request = Serialization.gsonPretty.fromJson(req, AaiNqRequest.class);

        if (request.getInstanceFilters().getInstanceFilter().get(0).containsKey(VSERVER)) {
            final String vserverName =
                    request.getInstanceFilters().getInstanceFilter().get(0).get(VSERVER).get("vserver-name");
            if (ERROR.equals(vserverName)) {
                Map<String, String> params = new TreeMap<>();
                params.put("type", VSERVER);
                return load("aai/AaiNqResponse-Error.json", params);
            } else {
                // vll format - new
                // new aai response from Brian 11/13/2017
                return load("aai/AaiNqResponse-Vserver.json", new TreeMap<>());
            }
        } else {
            final String vnfId =
                    request.getInstanceFilters().getInstanceFilter().get(0).get("generic-vnf").get("vnf-id");
            if (ERROR.equals(vnfId)) {
                Map<String, String> params = new TreeMap<>();
                params.put("type", "generic-vnf");
                return load("aai/AaiNqResponse-Error.json", params);
            } else {
                Map<String, String> params = new TreeMap<>();
                params.put("vnfId", "" + vnfId);
                params.put("vnfName", getUuidValue(vnfId, "ZRDM2MMEX39"));
                params.put("pnfVndName", "pnf-test-" + vnfId);
                params.put("pnfVnfId", getUuidValue(params.get("pnfVndName"), "jimmy-test"));

                params.put("serviceInstanceVnfName", "service-instance-test-" + vnfId);
                params.put("serviceInstanceVnfId",
                        getUuidValue(params.get("serviceInstanceVnfName"), "jimmy-test-vnf2"));

                return load("aai/AaiNqResponse-GenericVnf.json", params);
            }
        }
    }

    /**
     * Get by VNF name.
     *
     * @param vnfName the VNF name
     * @return the response
     */
    @GET
    @Path("/v11/network/generic-vnfs/generic-vnf")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces("application/json")
    public String getByVnfName(@QueryParam("vnf-name") final String vnfName) {
        if (GETFAIL.equals(vnfName)) {
            return "{\"requestError\":{\"serviceException\":{\"messageId\":\"SVC3001\",\"text\":\"Resource not found"
                    + " for %1 using id %2 (msg=%3) (ec=%4)\",\"variables\":[\"GET\",\"network/generic-vnfs/"
                    + "generic-vnf\",\"Node Not Found:No Node of type generic-vnf found at network/generic-vnfs"
                    + "/generic-vnf\",\"ERR.5.4.6114\"]}}}";
        }
        final boolean isDisabled = DISABLE_CLOSEDLOOP.equals(vnfName);
        if (ERROR.equals(vnfName)) {
            return "{ \"vnf-id\": \"error\", \"vnf-name\": \"" + vnfName
                    + "\", \"vnf-type\": \"RT\", \"service-id\": \"d7bb0a21-66f2-4e6d-87d9-9ef3ced63ae4\", \""
                    + "equipment-role\": \"UCPE\", \"orchestration-status\": \"created\", \"management-option\": \""
                    + "ATT\", \"ipv4-oam-address\": \"32.40.68.35\", \"ipv4-loopback0-address\": \"32.40.64.57\", \""
                    + "nm-lan-v6-address\": \"2001:1890:e00e:fffe::1345\", \"management-v6-address\": \""
                    + "2001:1890:e00e:fffd::36\", \"in-maint\": false, \"prov-status\":\"ACTIVE\", "
                    + "\"is-closed-loop-disabled\": " + isDisabled
                    + ", \"resource-version\": \"1493389458092\", \"relationship-list\": {\"relationship\":[{ \""
                    + "related-to\": \"service-instance\", \"related-link\": \"/aai/v11/business/customers/customer/"
                    + "1610_Func_Global_20160817084727/service-subscriptions/service-subscription/uCPE-VMS/"
                    + "service-instances/service-instance/USUCP0PCOIL0110UJZZ01\", \"relationship-data\":[{ \""
                    + "relationship-key\": \"customer.global-customer-id\", \"relationship-value\": \""
                    + "1610_Func_Global_20160817084727\"},{ \"relationship-key\": \"service-subscription.service-type"
                    + "\", \"relationship-value\": \"uCPE-VMS\"},{ \"relationship-key\": \""
                    + "service-instance.service-instance-id\", \"relationship-value\": \"USUCP0PCOIL0110UJZZ01"
                    + "\"} ], \"related-to-property\": [{\"property-key\": \"service-instance.service-instance-name"
                    + "\"}]},{ \"related-to\": \"vserver\", \"related-link\": \"/aai/v11/cloud-infrastructure/"
                    + "cloud-regions/cloud-region/att-aic/AAIAIC25/tenants/tenant/"
                    + "USUCP0PCOIL0110UJZZ01%3A%3AuCPE-VMS/vservers/vserver/3b2558f4-39d8-40e7-bfc7-30660fb52c45"
                    + "\", \"relationship-data\":[{ \"relationship-key\": \"cloud-region.cloud-owner\", \""
                    + "relationship-value\": \"att-aic\"},{ \"relationship-key\": \"cloud-region.cloud-region-id"
                    + "\", \"relationship-value\": \"AAIAIC25\"},{ \"relationship-key\": \"tenant.tenant-id"
                    + "\", \"relationship-value\": \"USUCP0PCOIL0110UJZZ01::uCPE-VMS\"},{ \"relationship-key\": \""
                    + "vserver.vserver-id\", \"relationship-value\": \"3b2558f4-39d8-40e7-bfc7-30660fb52c45\"} ], \""
                    + "related-to-property\": [ {\"property-key\": \"vserver.vserver-name\",\"property-value\": \""
                    + "USUCP0PCOIL0110UJZZ01-vsrx\" }]} ]}}";

        }
        final String vnfId = getUuidValue(vnfName, "5e49ca06-2972-4532-9ed4-6d071588d792");
        return "{ \"vnf-id\": \"" + vnfId + "\", \"vnf-name\": \"" + vnfName
                + "\", \"vnf-type\": \"RT\", \"service-id\": \"d7bb0a21-66f2-4e6d-87d9-9ef3ced63ae4\", \""
                + "equipment-role\": \"UCPE\", \"orchestration-status\": \"created\", \"management-option\": \"ATT"
                + "\", \"ipv4-oam-address\": \"32.40.68.35\", \"ipv4-loopback0-address\": \"32.40.64.57\", \""
                + "nm-lan-v6-address\": \"2001:1890:e00e:fffe::1345\", \"management-v6-address\": \""
                + "2001:1890:e00e:fffd::36\", \"in-maint\": false, \"prov-status\":\"ACTIVE\", "
                + "\"is-closed-loop-disabled\": " + isDisabled
                + ", \"resource-version\": \"1493389458092\", \"relationship-list\": {\"relationship\":[{ \""
                + "related-to\": \"service-instance\", \"related-link\": \"/aai/v11/business/customers/customer"
                + "/1610_Func_Global_20160817084727/service-subscriptions/service-subscription/uCPE-VMS/"
                + "service-instances/service-instance/USUCP0PCOIL0110UJZZ01\", \"relationship-data\":[{ \""
                + "relationship-key\": \"customer.global-customer-id\", \"relationship-value\": \""
                + "1610_Func_Global_20160817084727\"},{ \"relationship-key\": \"service-subscription.service-type"
                + "\", \"relationship-value\": \"uCPE-VMS\"},{ \"relationship-key\": \""
                + "service-instance.service-instance-id\", \"relationship-value\": \"USUCP0PCOIL0110UJZZ01\"} ], \""
                + "related-to-property\": [{\"property-key\": \"service-instance.service-instance-name\"}]},{ \""
                + "related-to\": \"vserver\", \"related-link\": \"/aai/v11/cloud-infrastructure/cloud-regions/"
                + "cloud-region/att-aic/AAIAIC25/tenants/tenant/USUCP0PCOIL0110UJZZ01%3A%3AuCPE-VMS/vservers/vserver"
                + "/3b2558f4-39d8-40e7-bfc7-30660fb52c45\", \"relationship-data\":[{ \"relationship-key\": \""
                + "cloud-region.cloud-owner\", \"relationship-value\": \"att-aic\"},{ \"relationship-key\": \""
                + "cloud-region.cloud-region-id\", \"relationship-value\": \"AAIAIC25\"},{ \"relationship-key\": \""
                + "tenant.tenant-id\", \"relationship-value\": \"USUCP0PCOIL0110UJZZ01::uCPE-VMS\"},{ \""
                + "relationship-key\": \"vserver.vserver-id\", \"relationship-value\": \""
                + "3b2558f4-39d8-40e7-bfc7-30660fb52c45\"} ], \"related-to-property\": [ {\"property-key\": \""
                + "vserver.vserver-name\",\"property-value\": \"USUCP0PCOIL0110UJZZ01-vsrx\" }]} ]}}";
    }

    /**
     * Get by VNF Id.
     *
     * @param vnfId the VNF Id
     * @return the response
     */
    @GET
    @Path("/v11/network/generic-vnfs/generic-vnf/{vnfId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces("application/json")
    public String getByVnfId(@PathParam("vnfId") final String vnfId) {
        if (GETFAIL.equals(vnfId)) {
            return "{\"requestError\":{\"serviceException\":{\"messageId\":\"SVC3001\",\"text\":\"Resource not found"
                    + " for %1 using id %2 (msg=%3) (ec=%4)\",\"variables\":[\"GET\",\"network/generic-vnfs/"
                    + "generic-vnf/getFail\",\"Node Not Found:No Node of type generic-vnf found at network/"
                    + "generic-vnfs/generic-vnf/getFail\",\"ERR.5.4.6114\"]}}}";
        }
        final boolean isDisabled = DISABLE_CLOSEDLOOP.equals(vnfId);
        final String vnfName = getUuidValue(vnfId, "USUCP0PCOIL0110UJRT01");
        return "{ \"vnf-id\": \"" + vnfId + "\", \"vnf-name\": \"" + vnfName
                + "\", \"vnf-type\": \"RT\", \"service-id\": \""
                + "d7bb0a21-66f2-4e6d-87d9-9ef3ced63ae4\", \"equipment-role\": \"UCPE\", \"orchestration-status"
                + "\": \"created\", \"management-option\": \"ATT\", \"ipv4-oam-address\": \"32.40.68.35\", \""
                + "ipv4-loopback0-address\": \"32.40.64.57\", \"nm-lan-v6-address\": \"2001:1890:e00e:fffe::1345"
                + "\", \"management-v6-address\": \"2001:1890:e00e:fffd::36\", \"in-maint\": false, "
                + "\"prov-status\":\"ACTIVE\", \"" + "" + "is-closed-loop-disabled\": " + isDisabled
                + ", \"resource-version\": \"1493389458092\", \""
                + "relationship-list\": {\"relationship\":[{ \"related-to\": \"service-instance\", \"related-link"
                + "\": \"/aai/v11/business/customers/customer/1610_Func_Global_20160817084727/service-subscriptions"
                + "/service-subscription/uCPE-VMS/service-instances/service-instance/USUCP0PCOIL0110UJZZ01\", \""
                + "relationship-data\":[{ \"relationship-key\": \"customer.global-customer-id\", \""
                + "relationship-value\": \"1610_Func_Global_20160817084727\"},{ \"relationship-key\": \""
                + "service-subscription.service-type\", \"relationship-value\": \"uCPE-VMS\"},{ \"relationship-key"
                + "\": \"service-instance.service-instance-id\", \"relationship-value\": \"USUCP0PCOIL0110UJZZ01\"} "
                + "], \"related-to-property\": [{\"property-key\": \"service-instance.service-instance-name\"}]},"
                + "{ \"related-to\": \"vserver\", \"related-link\": \"/aai/v11/cloud-infrastructure/cloud-regions/"
                + "cloud-region/att-aic/AAIAIC25/tenants/tenant/USUCP0PCOIL0110UJZZ01%3A%3AuCPE-VMS/vservers/vserver"
                + "/3b2558f4-39d8-40e7-bfc7-30660fb52c45\", \"relationship-data\":[{ \"relationship-key\": \""
                + "cloud-region.cloud-owner\", \"relationship-value\": \"att-aic\"},{ \"relationship-key\": \""
                + "cloud-region.cloud-region-id\", \"relationship-value\": \"AAIAIC25\"},{ \"relationship-key\": \""
                + "tenant.tenant-id\", \"relationship-value\": \"USUCP0PCOIL0110UJZZ01::uCPE-VMS\"},{ \""
                + "relationship-key\": \"vserver.vserver-id\", \"relationship-value\": \""
                + "3b2558f4-39d8-40e7-bfc7-30660fb52c45\"} ], \"related-to-property\": [ {\"property-key\": \""
                + "vserver.vserver-name\",\"property-value\": \"USUCP0PCOIL0110UJZZ01-vsrx\" }]} ]}}";
    }

    /**
     * Get by VServer name.
     *
     * @param vserverName the VServer name
     * @return the response
     */
    @GET
    @Path("/v11/nodes/vservers")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces("application/json")
    public String getByVserverName(@QueryParam("vserver-name") final String vserverName) {
        if (GETFAIL.equals(vserverName)) {
            return "{\"requestError\":{\"serviceException\":{\"messageId\":\"SVC3001\",\"text\":\"Resource not found"
                    + " for %1 using id %2 (msg=%3) (ec=%4)\",\"variables\":[\"GET\",\"nodes/vservers\",\"Node Not"
                    + " Found:No Node of type generic-vnf found at nodes/vservers\",\"ERR.5.4.6114\"]}}}";
        }
        final boolean isDisabled = DISABLE_CLOSEDLOOP.equals(vserverName);
        final String vserverId = getUuidValue(vserverName, "d0668d4f-c25e-4a1b-87c4-83845c01efd8");
        return "{\"vserver\": [{ \"vserver-id\": \"" + vserverId + "\", \"vserver-name\": \"" + vserverName
                + "\", \"vserver-name2\": \"vjunos0\", \"vserver-selflink\": \"https://aai-ext1.test.att.com:8443/aai/v7/cloud-infrastructure/cloud-regions/cloud-region/att-aic/AAIAIC25/tenants/tenant/USMSO1SX7NJ0103UJZZ01%3A%3AuCPE-VMS/vservers/vserver/d0668d4f-c25e-4a1b-87c4-83845c01efd8\", \"in-maint\": false, \"is-closed-loop-disabled\": "
                + isDisabled + ", \"prov-status\":\"ACTIVE\", \"resource-version\": \"1494001931513\", "
                + "\"relationship-list\": {\"relationship\":[{ \"related-to"
                + "\": \"generic-vnf\", \"related-link\": \"/aai/v11/network/generic-vnfs/generic-vnf/"
                + "e1a41e99-4ede-409a-8f9d-b5e12984203a\", \"relationship-data\": [ {\"relationship-key\": \""
                + "generic-vnf.vnf-id\",\"relationship-value\": \"e1a41e99-4ede-409a-8f9d-b5e12984203a\" }], \""
                + "related-to-property\": [ {\"property-key\": \"generic-vnf.vnf-name\",\"property-value\": \""
                + "USMSO1SX7NJ0103UJSW01\" }]},{ \"related-to\": \"pserver\", \"related-link\": \"/aai/v11/"
                + "cloud-infrastructure/pservers/pserver/USMSO1SX7NJ0103UJZZ01\", \"relationship-data\": [ {\""
                + "relationship-key\": \"pserver.hostname\",\"relationship-value\": \"USMSO1SX7NJ0103UJZZ01\" }], \""
                + "related-to-property\": [{\"property-key\": \"pserver.pserver-name2\"}]} ]}}]}";
    }

    private String getUuidValue(final String value, final String defaultValue) {
        return value != null ? UUID.nameUUIDFromBytes(value.getBytes()).toString() : defaultValue;
    }

    /**
     * Loads a JSON response from a file and then replaces parameters of the form, ${xxx}, with values.
     *
     * @param fileName name of the file containing the JSON
     * @param params parameters to be substituted
     * @return the JSON response, after parameter substitution
     * @throws IOException if the file cannot be read
     */
    private String load(String fileName, Map<String, String> params) throws IOException {
        String json = IOUtils.toString(getClass().getResource(fileName), StandardCharsets.UTF_8);

        // perform parameter substitution
        for (Entry<String, String> ent : params.entrySet()) {
            String name = "${" + ent.getKey() + "}";
            String value = ent.getValue();
            json = json.replace(name, value);
        }

        return json;
    }
}
