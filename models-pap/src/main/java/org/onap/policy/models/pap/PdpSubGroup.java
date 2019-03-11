/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.policy.models.pap;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.onap.policy.pdp.common.models.Policy;

/**
 * Class to represent a group of all PDP's of the same pdp type running for a particular domain.
 *
 * @author Ram Krishna Verma (ram.krishna.verma@est.tech)
 */
@Getter
@Setter
@ToString
public class PdpSubGroup {

    private String pdpType;
    private List<String> supportedPolicyTypes;
    private List<Policy> policies;
    private int currentInstanceCount;
    private int minInstanceCount;
    private String properties;
    private String deploymentInfo;
    private List<PdpInstanceDetails> pdpInstances;

}
