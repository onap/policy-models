/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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

package org.onap.policy.models.sim.pdp.parameters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.onap.policy.common.parameters.GroupValidationResult;

/**
 * Class to perform unit test of {@link PdpStatusParameters}.
 *
 * @author Ajith Sreekumar (ajith.sreekumar@est.tech)
 */
public class TestPdpStatusParameters {
    private static CommonTestData testData = new CommonTestData();

    @Test
    public void test() throws Exception {
        final PdpStatusParameters pdpStatusParameters =
                testData.toObject(testData.getPdpStatusParametersMap(false), PdpStatusParameters.class);
        final GroupValidationResult validationResult = pdpStatusParameters.validate();
        assertTrue(validationResult.isValid());
        assertEquals(CommonTestData.TIME_INTERVAL, pdpStatusParameters.getTimeIntervalMs());
        assertEquals(CommonTestData.PDP_TYPE, pdpStatusParameters.getPdpType());
        assertEquals(CommonTestData.DESCRIPTION, pdpStatusParameters.getDescription());
        assertEquals(CommonTestData.SUPPORTED_POLICY_TYPES, pdpStatusParameters.getSupportedPolicyTypes());
    }

    @Test
    public void testValidate() throws Exception {
        final PdpStatusParameters pdpStatusParameters =
                testData.toObject(testData.getPdpStatusParametersMap(false), PdpStatusParameters.class);
        final GroupValidationResult result = pdpStatusParameters.validate();
        assertNull(result.getResult());
        assertTrue(result.isValid());
    }
}
