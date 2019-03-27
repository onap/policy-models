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

public class SoResponse implements Serializable {

    private static final long serialVersionUID = -3283942659786236032L;

    @SerializedName("requestReferences")
    private SoRequestReferences requestReferences;

    @SerializedName("requestError")
    private SoRequestError requestError;

    @SerializedName("request")
    private SoRequest request;

    private int httpResponseCode;

    public SoResponse() {
        // required by author
    }

    public int getHttpResponseCode() {
        return httpResponseCode;
    }

    public SoRequest getRequest() {
        return request;
    }

    public SoRequestError getRequestError() {
        return requestError;
    }

    public SoRequestReferences getRequestReferences() {
        return requestReferences;
    }

    public void setHttpResponseCode(int httpResponseCode) {
        this.httpResponseCode = httpResponseCode;
    }

    public void setRequest(SoRequest request) {
        this.request = request;
    }

    public void setRequestError(SoRequestError requestError) {
        this.requestError = requestError;
    }

    public void setRequestReferences(SoRequestReferences requestReferences) {
        this.requestReferences = requestReferences;
    }

}
