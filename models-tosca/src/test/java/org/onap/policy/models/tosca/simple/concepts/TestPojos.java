/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy Model
 * ================================================================================
 * Copyright (C) 2019, 2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019, 2024 Nordix Foundation.
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

package org.onap.policy.models.tosca.simple.concepts;

import com.openpojo.reflection.filters.FilterPackageInfo;
import com.openpojo.validation.Validator;
import com.openpojo.validation.ValidatorBuilder;
import com.openpojo.validation.rule.impl.GetterMustExistRule;
import com.openpojo.validation.test.impl.GetterTester;
import com.openpojo.validation.test.impl.SetterTester;
import org.junit.jupiter.api.Test;
import org.onap.policy.common.utils.test.ToStringTester;

/**
 * Class to perform unit tests of all pojos.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 *
 */
class TestPojos {
    private static final String POJO_PACKAGE = "org.onap.policy.models.tosca.simple.concepts";

    @Test
    void testPojos() {
        // @formatter:off
        final Validator validator = ValidatorBuilder
                .create()
                .with(new ToStringTester())
                .with(new GetterMustExistRule())
                .with(new SetterTester())
                .with(new GetterTester())
                .build();
        // @formatter:on

        validator.validate(POJO_PACKAGE,
            new FilterPackageInfo(),
            pc -> !pc.getName().startsWith("Test"),
            pc -> !pc.getName().endsWith("Test"));
    }
}
