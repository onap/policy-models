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

import java.util.List;
import java.util.Map;
import lombok.Data;
import org.onap.policy.controlloop.actor.cds.GrpcOperation;
import org.onap.policy.controlloop.actorserviceprovider.TargetType;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

/**
 * GRPC Operation Base Class.
 */
@Data
public abstract class GrpcOperationProperties {
    protected static final String AAI_PNF_PREFIX = "pnf.";

    /**
     * AAI VNF Identifier.
     */
    public static final String AAI_VNF_ID_KEY = "generic-vnf.vnf-id";

    /**
     * AAI Service Instance Identifier.
     */
    public static final String AAI_SERVICE_INSTANCE_ID_KEY = "service-instance.service-instance-id";

    private static final Logger logger = LoggerFactory.getLogger(GrpcOperationProperties.class);

    /**
     * Build the appropriate GrpcOperation object depending on the target type.
     */
    public static GrpcOperationProperties build(ControlLoopOperationParams params) {
        if (TargetType.PNF.equals(params.getTargetType())) {
            return new GrpcOperationPnfProperties();
        }

        // assume VNF processing for backwards compatibility with istanbul

        if (!TargetType.VNF.equals(params.getTargetType())) {
            logger.warn("Unexpected target type, build VNF-like operation properties");
        }

        return CollectionUtils.isEmpty(params.getTargetEntityIds())
                ? new GrpcOperationTargetVnfProperties()
                : new GrcpOperationResourceVnfProperties();
    }

    /**
     * Get the property names.
     */
    public abstract List<String> getPropertyNames();

    /**
     * Convert to the AAI properties.
     */
    public abstract Map<String, String> convertToAaiProperties(GrpcOperation operation);
}
