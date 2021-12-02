/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy Model
 * ================================================================================
 * Copyright (C) 2022 Nordix Foundation. All rights reserved.
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

package org.onap.policy.models.tosca.authorative.concepts;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

/**
 *  Class to represent TOSCA policy type impl matching input/output from/to client.
 */
@Data
@NoArgsConstructor
@ToString
public class ToscaPolicyTypeImpl implements Serializable {

    private ToscaConceptIdentifier policyTypeImplRef;

    private ToscaConceptIdentifier policyTypeRef;

    private String pdpType;

    private Map<String, Object> policyModel;


    /**
     * Copy constructor.
     *
     * @param copyObject object to copy
     */
    public ToscaPolicyTypeImpl(@NonNull ToscaPolicyTypeImpl copyObject) {

        this.policyTypeImplRef = copyObject.getPolicyTypeImplRef();
        this.policyTypeRef = copyObject.getPolicyTypeRef();
        this.pdpType = copyObject.getPdpType();
        if (copyObject.policyModel != null) {
            policyModel = new LinkedHashMap<>(copyObject.policyModel);
        }
    }

    /**
     * Gets the identifier for this policy impl.
     *
     * @return this policy's identifier
     */
    public ToscaConceptIdentifier getIdentifier() {
        return getPolicyTypeImplRef();
    }

}
