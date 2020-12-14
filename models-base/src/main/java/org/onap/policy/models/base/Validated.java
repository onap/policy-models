/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.models.base;

import lombok.NonNull;
import org.onap.policy.common.parameters.ValidationResult;

public interface Validated {

    /**
     * Validate that this value is structurally correct.
     *
     * @param fieldName name of the field containing this concept
     * @return the validation result or {@code null} (if it's clean)
     */
    ValidationResult validate(@NonNull final String fieldName);
}
