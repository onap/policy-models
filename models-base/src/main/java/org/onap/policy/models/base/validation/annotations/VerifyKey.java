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

package org.onap.policy.models.base.validation.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Validates a key.
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface VerifyKey {

    /**
     * Validates that key.isNullKey() is {@code false}.
     */
    boolean keyNotNull() default true;

    /**
     * Validates that key.isNullName() is {@code false}.
     */
    boolean nameNotNull() default true;

    /**
     * Validates that key.isNullVersion() is {@code false}.
     */
    boolean versionNotNull() default false;

    /**
     * Invokes key.validate(), avoiding the need to include the "Valid" annotation. Note:
     * if this is {@code true}, then the "Valid" annotation should not be specified, as
     * that would result in duplicate validation checks.
     */
    boolean valid() default true;
}
