/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2019 Bell Canada. All rights reserved.
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

package org.onap.policy.controlloop.actor.cds.beans;

import com.google.gson.annotations.SerializedName;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.onap.policy.common.utils.coder.Coder;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
@Setter
public class CdsActionRequest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CdsActionRequest.class);
    private static final Coder CODER = new StandardCoder();

    @SerializedName("policy-payload")
    private Map<String, String> configDeployProperties;

    @SerializedName("aai-properties")
    private Map<String, String> aaiProperties;

    @SerializedName("resolution-key")
    private String resolutionKey;

    private transient String actionName;

    @Override
    public String toString() {
        try {
            return "{" + "\"" + actionName + "-request\":" + CODER.encode(this) + '}';
        } catch (CoderException e) {
            LOGGER.error("Failure serializing CdsActionRequest object: ", e);
            return "";
        }
    }
}
