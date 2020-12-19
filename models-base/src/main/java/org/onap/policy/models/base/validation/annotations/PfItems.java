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
import org.intellij.lang.annotations.Pattern;
import org.onap.policy.common.parameters.annotations.Max;
import org.onap.policy.common.parameters.annotations.Min;
import org.onap.policy.common.parameters.annotations.NotBlank;
import org.onap.policy.common.parameters.annotations.NotNull;
import org.onap.policy.common.parameters.annotations.Valid;

/**
 * Validations on individual items, typically within a collection.
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface PfItems {

    /**
     * Validates the item is not {@code null}.
     */
    NotNull[] notNull() default {};

    /**
     * Validates the item is not blank.
     */
    NotBlank[] notBlank() default {};

    /**
     * Validates the item matches a regular expression.
     */
    Pattern[] pattern() default {};

    /**
     * Validates the item is not greater than a certain value.
     */
    Max[] max() default {};

    /**
     * Validates the item is not less than a certain value.
     */
    Min[] min() default {};

    /**
     * Validates the item is not less than a certain value.
     */
    PfMin[] pfMin() default {};

    /**
     * Validates the item is valid, using a {@link BeanValidator}.
     */
    Valid[] valid() default {};

    /**
     * Validates a key.
     */
    VerifyKey[] key() default {};

}
