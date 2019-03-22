/*
 * ============LICENSE_START=======================================================
 * ONAP Policy Models
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.models.base.keys;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.onap.policy.models.base.PfKey;
import org.onap.policy.models.base.PfValidationResult;
import org.onap.policy.models.base.Validated;

/**
 * Policy identifier with an optional version; only the "name" is required.
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PolicyIdentOptVersion {

    @NonNull
    private String name;

    private String version;


    public PolicyIdentOptVersion(PolicyIdentOptVersion source) {
        this.name = source.name;
        this.version = source.version;
    }

    /**
     * Validates the object.
     *
     * @param result where to place any errors
     * @return a validation result
     */
    public PfValidationResult validate(@NonNull final PfValidationResult result) {
        Validated validator = new Validated();

        validator.validateNotNull(this, "name", name, result);
        validator.validateText(this, "name", name, PfKey.NAME_REGEXP, result);

        validator.validateText(this, "version", version, PfKey.VERSION_REGEXP, result);

        return result;
    }
}
