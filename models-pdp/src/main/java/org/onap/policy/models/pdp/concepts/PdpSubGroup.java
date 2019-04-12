/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
 *  Modifications Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.models.pdp.concepts;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.NonNull;
import org.onap.policy.common.parameters.BeanValidationResult;
import org.onap.policy.common.parameters.ObjectValidationResult;
import org.onap.policy.common.parameters.ValidationResult;
import org.onap.policy.common.parameters.ValidationStatus;
import org.onap.policy.models.base.PfUtils;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyIdentifier;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyTypeIdentifier;

/**
 * Class to represent a group of all PDP's of the same pdp type running for a particular
 * domain.
 *
 * @author Ram Krishna Verma (ram.krishna.verma@est.tech)
 */
@Data
public class PdpSubGroup {
    private String pdpType;
    private List<ToscaPolicyTypeIdentifier> supportedPolicyTypes;
    private List<ToscaPolicyIdentifier> policies;
    private int currentInstanceCount;
    private int desiredInstanceCount;
    private Map<String, String> properties;
    private List<Pdp> pdpInstances;

    /**
     * Constructs the object.
     */
    public PdpSubGroup() {
        super();
    }

    /**
     * Constructs the object, making a deep copy from the source.
     *
     * @param source source from which to copy fields
     */
    public PdpSubGroup(@NonNull final PdpSubGroup source) {
        this.pdpType = source.pdpType;
        this.supportedPolicyTypes = PfUtils.mapList(source.supportedPolicyTypes, ToscaPolicyTypeIdentifier::new);
        this.policies = PfUtils.mapList(source.policies, ToscaPolicyIdentifier::new);
        this.currentInstanceCount = source.currentInstanceCount;
        this.desiredInstanceCount = source.desiredInstanceCount;
        this.properties = (source.properties == null ? null : new LinkedHashMap<>(source.properties));
        this.pdpInstances = PfUtils.mapList(source.pdpInstances, Pdp::new);
    }

    /**
     * Validates that appropriate fields are populated for an incoming call to the PAP
     * REST API.
     *
     * @return a validation result, if there is an error, {@code null} otherwise
     */
    public ValidationResult validatePapRest() {
        BeanValidationResult result = new BeanValidationResult("group", this);

        result.validateNotNull("pdpType", pdpType);
        result.validateNotNullList("supportedPolicyTypes", supportedPolicyTypes,
                        ToscaPolicyTypeIdentifier::validatePapRest);

        if (supportedPolicyTypes != null && supportedPolicyTypes.isEmpty()) {
            result.addResult(new ObjectValidationResult("supportedPolicyTypes", supportedPolicyTypes,
                            ValidationStatus.INVALID, "empty list"));
        }

        if (desiredInstanceCount <= 0) {
            result.addResult(new ObjectValidationResult("desiredInstanceCount", desiredInstanceCount,
                            ValidationStatus.INVALID, "non-positive"));
        }

        return (result.isValid() ? null : result);
    }
}
