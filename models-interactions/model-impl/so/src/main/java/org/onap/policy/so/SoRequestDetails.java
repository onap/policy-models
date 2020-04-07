/*-
 * ============LICENSE_START=======================================================
 * so
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019 Nordix Foundation.
 * Modifications Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
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
    public String toString() {
        return "SORequestDetails [modelInfo=" + modelInfo + ", cloudConfiguration=" + cloudConfiguration
            + ", requestInfo=" + requestInfo + ", subscriberInfo=" + subscriberInfo
            + ", relatedInstanceList=" + relatedInstanceList + ", requestParameters=" + requestParameters
            + ", configurationParameters=" + configurationParameters + "]";
    }

}
