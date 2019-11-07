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

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SoRequestDetails implements Serializable {

    private static final long serialVersionUID = -3283942659786236032L;

    private SoModelInfo modelInfo;
    private SoCloudConfiguration cloudConfiguration;
    private SoRequestInfo requestInfo;
    private SoSubscriberInfo subscriberInfo;
    private List<SoRelatedInstanceListElement> relatedInstanceList = new LinkedList<>();
    private SoRequestParameters requestParameters;
    private List<Map<String, String>> configurationParameters = new LinkedList<>();

    public SoRequestDetails() {
    }

    /**
     * Constructor.
     *
     * @param soRequestDetails copy object
     */
    public SoRequestDetails(SoRequestDetails soRequestDetails) {
        this.modelInfo = soRequestDetails.modelInfo;
        this.cloudConfiguration = soRequestDetails.cloudConfiguration;
        this.requestInfo = soRequestDetails.requestInfo;
        this.relatedInstanceList = soRequestDetails.relatedInstanceList;
        this.requestParameters = soRequestDetails.requestParameters;
        this.subscriberInfo = soRequestDetails.subscriberInfo;
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
        SoRequestDetails other = (SoRequestDetails) obj;
        if (cloudConfiguration == null) {
            if (other.cloudConfiguration != null) {
                return false;
            }
        } else if (!cloudConfiguration.equals(other.cloudConfiguration)) {
            return false;
        }
        if (configurationParameters == null) {
            if (other.configurationParameters != null) {
                return false;
            }
        } else if (!configurationParameters.equals(other.configurationParameters)) {
            return false;
        }
        if (modelInfo == null) {
            if (other.modelInfo != null) {
                return false;
            }
        } else if (!modelInfo.equals(other.modelInfo)) {
            return false;
        }
        if (relatedInstanceList == null) {
            if (other.relatedInstanceList != null) {
                return false;
            }
        } else if (!relatedInstanceList.equals(other.relatedInstanceList)) {
            return false;
        }
        if (requestInfo == null) {
            if (other.requestInfo != null) {
                return false;
            }
        } else if (!requestInfo.equals(other.requestInfo)) {
            return false;
        }
        if (requestParameters == null) {
            if (other.requestParameters != null) {
                return false;
            }
        } else if (!requestParameters.equals(other.requestParameters)) {
            return false;
        }
        if (subscriberInfo == null) {
            return other.subscriberInfo == null;
        } else {
            return subscriberInfo.equals(other.subscriberInfo);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((cloudConfiguration == null) ? 0 : cloudConfiguration.hashCode());
        result = prime * result + ((configurationParameters == null) ? 0 : configurationParameters.hashCode());
        result = prime * result + ((modelInfo == null) ? 0 : modelInfo.hashCode());
        result = prime * result + ((relatedInstanceList == null) ? 0 : relatedInstanceList.hashCode());
        result = prime * result + ((requestInfo == null) ? 0 : requestInfo.hashCode());
        result = prime * result + ((requestParameters == null) ? 0 : requestParameters.hashCode());
        result = prime * result + ((subscriberInfo == null) ? 0 : subscriberInfo.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "SORequestDetails [modelInfo=" + modelInfo + ", cloudConfiguration=" + cloudConfiguration
            + ", requestInfo=" + requestInfo + ", subscriberInfo=" + subscriberInfo
            + ", relatedInstanceList=" + relatedInstanceList + ", requestParameters=" + requestParameters
            + ", configurationParameters=" + configurationParameters + "]";
    }

}
