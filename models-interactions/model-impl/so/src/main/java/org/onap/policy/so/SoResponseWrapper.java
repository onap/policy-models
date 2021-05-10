/*-
 * ============LICENSE_START=======================================================
 * so
 * ================================================================================
 * Copyright (C) 2017-2019, 2021 AT&T Intellectual Property. All rights reserved.
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
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SoResponseWrapper implements Serializable {

    private static final long serialVersionUID = 7673023687132889069L;

    @SerializedName("SoResponse")
    private SoResponse soResponse;

    private transient String requestId;

    public SoResponseWrapper(SoResponse response, String reqId) {
        this.soResponse = response;
        this.requestId = reqId;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SoResponseWrapper other = (SoResponseWrapper) obj;
        if (soResponse == null) {
            if (other.soResponse != null) {
                return false;
            }
        } else if (!soResponse.equals(other.soResponse)) {
            return false;
        }
        if (requestId == null) {
            return other.requestId == null;
        } else {
            return requestId.equals(other.requestId);
        }
    }

    @Override
    public int hashCode() {
        final var prime = 31;
        int result = super.hashCode();
        result = prime * result + ((soResponse == null) ? 0 : soResponse.hashCode());
        result = prime * result + ((requestId == null) ? 0 : requestId.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "SOResponseWrapper [SOResponse=" + soResponse + ", RequestId=" + requestId + "]";
    }

}
