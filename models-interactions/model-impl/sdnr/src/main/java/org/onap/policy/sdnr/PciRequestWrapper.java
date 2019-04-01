/*-
 * ============LICENSE_START=======================================================
 * sdnr
 * ================================================================================
 * Copyright (C) 2018 Wipro Limited Intellectual Property. All rights reserved.
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

package org.onap.policy.sdnr;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class PciRequestWrapper extends PciWrapper implements Serializable {

    private static final long serialVersionUID = 879766924715980798L;

    @SerializedName(value = "body")
    private PciRequest body;

    public PciRequestWrapper() {
        super();
    }

    public PciRequestWrapper(PciRequest request) {
        body = request;
    }

    /**
     * Get the body.
     *
     * @return the body
     */
    public PciRequest getBody() {
        return body;
    }

    /**
     * Set the body.
     *
     * @param body
     *            the body to set
     */
    public void setBody(PciRequest body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "RequestWrapper [body=" + body + ", toString()=" + super.toString() + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((body == null) ? 0 : body.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        PciRequestWrapper other = (PciRequestWrapper) obj;
        if (body == null) {
            if (other.body != null) {
                return false;
            }
        } else if (!body.equals(other.body)) {
            return false;
        }
        return true;
    }

}
