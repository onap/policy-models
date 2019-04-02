/*-
 * ============LICENSE_START=======================================================
 * so
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

package org.onap.policy.so;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class SoRequestInfo implements Serializable {

    private static final long serialVersionUID = -3283942659786236032L;

    @SerializedName("instanceName")
    private String instanceName;

    @SerializedName("source")
    private String source;

    @SerializedName("productFamilyId")
    private String productFamilyId;

    @SerializedName("suppressRollback")
    private boolean suppressRollback;

    @SerializedName("billingAccountNumber")
    private String billingAccountNumber;

    @SerializedName("callbackUrl")
    private String callbackUrl;

    @SerializedName("correlator")
    private String correlator;

    @SerializedName("orderNumber")
    private String orderNumber;

    @SerializedName("orderVersion")
    private Integer orderVersion;

    @SerializedName("requestorId")
    private String requestorId;

    public SoRequestInfo() {
        // required by author
    }

    public String getBillingAccountNumber() {
        return billingAccountNumber;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public String getCorrelator() {
        return correlator;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public Integer getOrderVersion() {
        return orderVersion;
    }

    public String getProductFamilyId() {
        return productFamilyId;
    }

    public String getRequestorId() {
        return requestorId;
    }

    public String getSource() {
        return source;
    }

    public boolean isSuppressRollback() {
        return suppressRollback;
    }

    public void setBillingAccountNumber(String billingAccountNumber) {
        this.billingAccountNumber = billingAccountNumber;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public void setCorrelator(String correlator) {
        this.correlator = correlator;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public void setOrderVersion(Integer orderVersion) {
        this.orderVersion = orderVersion;
    }

    public void setProductFamilyId(String productFamilyId) {
        this.productFamilyId = productFamilyId;
    }

    public void setRequestorId(String requestorId) {
        this.requestorId = requestorId;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setSuppressRollback(boolean suppressRollback) {
        this.suppressRollback = suppressRollback;
    }

}
