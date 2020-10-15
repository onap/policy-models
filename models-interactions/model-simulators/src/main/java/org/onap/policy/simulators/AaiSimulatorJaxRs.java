/*-
 * ============LICENSE_START=======================================================
 * simulators
 * ================================================================================
 * Copyright (C) 2017-2018, 2020 AT&T Intellectual Property. All rights reserved.
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
import java.util.UUID;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.apache.commons.io.IOUtils;

@Path("/aai")
public class AaiSimulatorJaxRs {

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
    public String aaiGetQuery(@PathParam("vnfId") final String vnfId) {
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
    public String aaiGetVserverQuery(@QueryParam("filter") final String filter) {
        if (filter.equals("vserver-name:EQUALS:f953c499-4b1e-426b-8c6d-e9e9f1fc730f")
            || filter.equals("vserver-name:EQUALS:Ete_vFWCLvFWSNK_7ba1fbde_0")
            || filter.equals("vserver-name:EQUALS:OzVServer")
            || filter.equals("vserver-name:EQUALS:testVserverName")
            || filter.startsWith("vserver-name:EQUALS:vserver-")) {
            return "{\"result-data\":[{\"resource-type\": \"vserver\",\"resource-link\":\"/aai/v15/"
                + "cloud-infrastructure/cloud-regions/cloud-region/CloudOwner/RegionOne/tenants"
                + "/tenant/3f2aaef74ecb4b19b35e26d0849fe9a2/vservers/vserver/"
                + "6c3b3714-e36c-45af-9f16-7d3a73d99497\"}]}";
        } else {
            return null;
        }
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
        return IOUtils.toString(getClass().getResource("aai/AaiCqResponse.json"),
            StandardCharsets.UTF_8);
    }

    /**
     * A&AI get PNF query.
     *
     * @return the result
     * @throws IOException if a response file cannot be read
     */
    @GET
    @Path("/v16/network/pnfs/pnf/{pnfName}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces("application/json")
    public String aaiGetPnfQuery(@PathParam("pnfName") final String pnfName) throws IOException {
        if (GETFAIL.equals(pnfName)) {
            throw new IllegalArgumentException("query failed, as requested");
        }

        return IOUtils.toString(getClass().getResource("aai/AaiGetPnfResponse.json"),
                        StandardCharsets.UTF_8);
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
            return "{\"requestError\":{\"serviceException\":{\"messageId\":\"SVC3001\",\"text\":\"Resource not"
                + " found for %1 using id %2 (msg=%3) (ec=%4)\",\"variables\":[\"GET\",\"network/generic-vnfs/"
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
        return "{\"vserver\": [{ \"vserver-id\": \"" + vserverId + "\", \"vserver-name\": \""
            + vserverName
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

}
