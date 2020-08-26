/*-
 * ============LICENSE_START=======================================================
 * so
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019 Nordix Foundation.
 * Modifications Copyright (C) 2019 Tech Mahindra
 * Modifications Copyright (C) 2020 Wipro Limited.
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
import lombok.Getter;
import lombok.Setter;

public enum TargetType {
    VM("VM"),
    PNF("PNF"),
    VFC("VFC"),
    VNF("VNF"),
    VFMODULE("VFMODULE"),
    SERVICEINSTANCE("SERVICEINSTANCE")
    ;

@Getter
@Setter
public class SoCloudConfiguration implements Serializable {

    private static final long serialVersionUID = -3283942659786236032L;

    private String lcpCloudRegionId;
    private String tenantId;

    public SoCloudConfiguration() {
        //required by author
    }
}
