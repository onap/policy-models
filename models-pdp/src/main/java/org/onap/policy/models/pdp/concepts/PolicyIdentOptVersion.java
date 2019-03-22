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

package org.onap.policy.models.pdp.concepts;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfKey;
import org.onap.policy.models.base.PfValidationResult;
import org.onap.policy.models.base.Validated;

/**
 * Policy identifier with an optional version; only the "name" is required.
 */
@NonNull
@NoArgsConstructor
public class PolicyIdentOptVersion extends PfConceptKey {
    private static final long serialVersionUID = 1L;
    private static final Validated validator = new Validated();


    public PolicyIdentOptVersion(PolicyIdentOptVersion source) {
        super(source);
    }

    /**
     * Validates the object.
     *
     * @param resultIn where to place any errors
     * @return a validation result
     */
    public PfValidationResult validate(@NonNull final PfValidationResult resultIn) {
        PfValidationResult result = resultIn;

        String name = getName();
        if (PfConceptKey.NULL_KEY_NAME.equals(name)) {
            validator.addError(this, "name", result, "null");
        }
        result = validator.validateText(this, "name", name, PfKey.NAME_REGEXP, result);

        return validator.validateText(this, "version", getVersion(), PfKey.VERSION_REGEXP, result);
    }
}
