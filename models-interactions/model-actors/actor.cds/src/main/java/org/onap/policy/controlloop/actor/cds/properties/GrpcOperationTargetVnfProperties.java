/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2022 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.controlloop.actor.cds.properties;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.onap.policy.controlloop.actor.cds.GrpcOperation;
import org.onap.policy.controlloop.actorserviceprovider.OperationProperties;

/**
 * GRPC Operation with Target VNF properties.
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class GrpcOperationTargetVnfProperties extends GrpcOperationProperties {

    // @formatter:off
    protected static final List<String> VNF_PROPERTY_NAMES = List.of(
                            OperationProperties.AAI_TARGET_ENTITY,
                            OperationProperties.EVENT_ADDITIONAL_PARAMS,
                            OperationProperties.OPT_CDS_GRPC_AAI_PROPERTIES);
    // @formatter:on

    @Override
    public List<String> getPropertyNames() {
        return VNF_PROPERTY_NAMES;
    }

    @Override
    public Map<String, String> convertToAaiProperties(GrpcOperation operation) {
        Map<String, String> result = operation.getProperty(OperationProperties.OPT_CDS_GRPC_AAI_PROPERTIES);
        if (result != null) {
            return result;
        }

        result = new LinkedHashMap<>();
        result.put(AAI_VNF_ID_KEY,
            operation.getRequiredProperty(OperationProperties.AAI_TARGET_ENTITY, "vnf id"));
        return result;
    }
}
