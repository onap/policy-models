/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2024 Nordix Foundation.
 *  Modifications Copyright (C) 2019 AT&T Intellectual Property.
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

import com.openpojo.reflection.filters.FilterClassName;
import com.openpojo.reflection.filters.FilterPackageInfo;
import com.openpojo.validation.Validator;
import com.openpojo.validation.ValidatorBuilder;
import com.openpojo.validation.test.impl.GetterTester;
import com.openpojo.validation.test.impl.SetterTester;
import org.junit.jupiter.api.Test;
import org.onap.policy.common.utils.test.ToStringTester;

/**
 * Class to perform unit testing of models.
 *
 * @author Ram Krishna Verma (ram.krishna.verma@est.tech)
 */
class ModelsTest {
    private static final String POJO_PACKAGE = "org.onap.policy.models.pdp.concepts";

    @Test
    void testPdpModels() {
        final Validator validator = ValidatorBuilder.create().with(new ToStringTester()).with(new SetterTester())
                .with(new GetterTester()).build();

        // exclude Test classes and PdpMessage
        validator.validate(POJO_PACKAGE, new FilterPackageInfo(), new FilterClassName("^((?!Test$).)*$"),
                new FilterClassName("^((?!" + PdpMessage.class.getName() + ").)*$"));
    }
}
