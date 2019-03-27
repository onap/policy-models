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

public class SoRequestError implements Serializable {

    private static final long serialVersionUID = -3283942659786236032L;

    @SerializedName("policyException")
    private SoPolicyExceptionHolder policyException;

    @SerializedName("serviceException")
    private SoServiceExceptionHolder serviceException;

    public SoRequestError() {
        // required by author
    }

    public SoPolicyExceptionHolder getPolicyException() {
        return policyException;
    }

    public SoServiceExceptionHolder getServiceException() {
        return serviceException;
    }

    public void setPolicyException(SoPolicyExceptionHolder policyException) {
        this.policyException = policyException;
    }

    public void setServiceException(SoServiceExceptionHolder serviceException) {
        this.serviceException = serviceException;
    }

}
